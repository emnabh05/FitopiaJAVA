<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\HttpFoundation\Request;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\String\Slugger\SluggerInterface;
use App\Form\ProfileFormType;
use App\Form\BlogPostType;
use App\Form\DietaryRestrictionType;
use App\Entity\BlogPost;
use App\Entity\DietaryRestriction;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;

#[Route('/admin', name: 'admin_')]
class AdminController extends AbstractController
{
    #[Route('', name: 'index')]
    public function index(): Response
    {
        return $this->redirectToRoute('admin_dashboard');
    }

    #[Route('/dashboard', name: 'dashboard')]
    public function dashboard(): Response
    {
        return $this->render('admin/dashboard.html.twig');
    }

    #[Route('/tables', name: 'tables')]
    public function tables(): Response
    {
        return $this->render('admin/tables.html.twig');
    }

    #[Route('/users', name: 'users')]
    public function users(): Response
    {
        return $this->render('admin/gestion-users.html.twig');
    }

    #[Route('/blog-forum', name: 'blog_forum')]
    public function blogForum(
        Request $request,
        EntityManagerInterface $em,
        SluggerInterface $slugger
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $post = new BlogPost();
        $form = $this->createForm(BlogPostType::class, $post, [
            'attr' => ['id' => 'blog-create-form'],
        ]);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $featuredImage */
            $featuredImage = $form->get('featuredImageFile')->getData();
            if ($featuredImage) {
                $originalFilename = pathinfo($featuredImage->getClientOriginalName(), PATHINFO_FILENAME);
                $safeFilename = $slugger->slug($originalFilename);
                $newFilename = $safeFilename.'-'.uniqid().'.'.$featuredImage->guessExtension();

                try {
                    $featuredImage->move(
                        $this->getParameter('app.blog_upload_dir'),
                        $newFilename
                    );
                } catch (FileException $e) {
                    $this->addFlash('error', 'Could not upload the featured image. Please try again.');
                }

                $post->setFeaturedImage($newFilename);
            }

            $post->setAuthor($user);
            $post->setStatus('published');
            $post->setViewCount(0);
            $post->setCreatedAt(new \DateTimeImmutable());
            $post->setPublishedAt(new \DateTimeImmutable());

            $baseSlug = strtolower($slugger->slug($post->getTitle())->toString());
            $post->setSlug($baseSlug.'-'.substr(uniqid(), -6));

            $em->persist($post);
            $em->flush();

            $this->addFlash('success', 'Blog post published successfully.');
            return $this->redirectToRoute('admin_blog_forum');
        }

        $posts = $em->getRepository(BlogPost::class)->findBy([], ['createdAt' => 'DESC']);
        $items = [];
        $editForms = [];
        foreach ($posts as $p) {
            $roles = $p->getAuthor()?->getRoles() ?? [];
            $role = $roles[0] ?? 'ROLE_USER';
            $items[] = [
                'id' => $p->getId(),
                'title' => $p->getTitle(),
                'date' => $p->getCreatedAt()->format('Y-m-d'),
                'email' => $p->getAuthor()?->getEmail() ?? '',
                'role' => $role,
                'excerpt' => $p->getExcerpt() ?? '',
                'image' => $p->getFeaturedImage(),
            ];

            $editForms[$p->getId()] = $this->createForm(BlogPostType::class, $p, [
                'action' => $this->generateUrl('admin_blog_forum_edit', ['id' => $p->getId()]),
                'attr' => ['class' => 'blog-edit-form', 'data-post-id' => $p->getId()],
            ])->createView();
        }

        return $this->render('admin/gestion-blog-forum.html.twig', [
            'blogForm' => $form->createView(),
            'items' => $items,
            'editForms' => $editForms,
        ]);
    }

    #[Route('/blog-forum/{id}/edit', name: 'blog_forum_edit', methods: ['GET', 'POST'])]
    public function editBlogPost(
        BlogPost $post,
        Request $request,
        EntityManagerInterface $em,
        SluggerInterface $slugger
    ): Response {
        if ($request->isMethod('GET')) {
            return $this->redirectToRoute('admin_blog_forum');
        }
        $form = $this->createForm(BlogPostType::class, $post);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $featuredImage */
            $featuredImage = $form->get('featuredImageFile')->getData();
            if ($featuredImage) {
                $originalFilename = pathinfo($featuredImage->getClientOriginalName(), PATHINFO_FILENAME);
                $safeFilename = $slugger->slug($originalFilename);
                $newFilename = $safeFilename.'-'.uniqid().'.'.$featuredImage->guessExtension();

                try {
                    $featuredImage->move(
                        $this->getParameter('app.blog_upload_dir'),
                        $newFilename
                    );
                } catch (FileException $e) {
                    $this->addFlash('error', 'Could not upload the featured image. Please try again.');
                }

                $post->setFeaturedImage($newFilename);
            }

            $baseSlug = strtolower($slugger->slug($post->getTitle())->toString());
            $post->setSlug($baseSlug.'-'.substr(uniqid(), -6));
            $post->setUpdatedAt(new \DateTimeImmutable());

            $em->flush();
            $this->addFlash('success', 'Blog post updated successfully.');
            return $this->redirectToRoute('admin_blog_forum');
        }

        return $this->redirectToRoute('admin_blog_forum');
    }

    #[Route('/blog-forum/{id}/delete', name: 'blog_forum_delete', methods: ['POST'])]
    public function deleteBlogPost(
        BlogPost $post,
        Request $request,
        EntityManagerInterface $em
    ): Response {
        if (!$this->isCsrfTokenValid('delete_blog_post_'.$post->getId(), (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid security token.');
            return $this->redirectToRoute('admin_blog_forum');
        }

        $em->remove($post);
        $em->flush();
        $this->addFlash('success', 'Blog post deleted.');
        return $this->redirectToRoute('admin_blog_forum');
    }

    #[Route('/stocks', name: 'stocks')]
    public function stocks(): Response
    {
        return $this->render('admin/gestion-stocks.html.twig');
    }

    #[Route('/events', name: 'events')]
    public function events(): Response
    {
        return $this->render('admin/gestion-events.html.twig');
    }

    #[Route('/trainings', name: 'trainings')]
    public function trainings(): Response
    {
        return $this->render('admin/gestion-trainings.html.twig');
    }

    #[Route('/nutrition', name: 'nutrition')]
    public function nutrition(
        Request $request,
        EntityManagerInterface $em
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $restriction = new DietaryRestriction();
        $form = $this->createForm(DietaryRestrictionType::class, $restriction, [
            'attr' => ['id' => 'restriction-create-form'],
        ]);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $restriction->setUserEmail($user->getEmail());
            if (!$restriction->isTemporary()) {
                $restriction->setEndDate(null);
            }
            $em->persist($restriction);
            $em->flush();

            $this->addFlash('success', 'Restriction alimentaire ajoutée avec succès.');
            return $this->redirectToRoute('admin_nutrition');
        }

        $restrictions = $em->getRepository(DietaryRestriction::class)->findBy([], ['startDate' => 'DESC']);
        $editForms = [];
        foreach ($restrictions as $item) {
            $editForms[$item->getId()] = $this->createForm(DietaryRestrictionType::class, $item, [
                'action' => $this->generateUrl('admin_nutrition_edit', ['id' => $item->getId()]),
                'attr' => ['class' => 'restriction-edit-form', 'data-restriction-id' => $item->getId()],
            ])->createView();
        }

        return $this->render('admin/gestion-nutrition.html.twig', [
            'restrictionForm' => $form->createView(),
            'restrictions' => $restrictions,
            'editForms' => $editForms,
        ]);
    }

    #[Route('/nutrition/{id}/edit', name: 'nutrition_edit', methods: ['POST'])]
    public function editNutritionRestriction(
        DietaryRestriction $restriction,
        Request $request,
        EntityManagerInterface $em
    ): Response {
        $form = $this->createForm(DietaryRestrictionType::class, $restriction);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            if (!$restriction->isTemporary()) {
                $restriction->setEndDate(null);
            }
            $em->flush();
            $this->addFlash('success', 'Restriction alimentaire mise à jour.');
        }

        return $this->redirectToRoute('admin_nutrition');
    }

    #[Route('/nutrition/{id}/delete', name: 'nutrition_delete', methods: ['POST'])]
    public function deleteNutritionRestriction(
        DietaryRestriction $restriction,
        Request $request,
        EntityManagerInterface $em
    ): Response {
        if (!$this->isCsrfTokenValid('delete_restriction_'.$restriction->getId(), (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid security token.');
            return $this->redirectToRoute('admin_nutrition');
        }

        $em->remove($restriction);
        $em->flush();
        $this->addFlash('success', 'Restriction alimentaire supprimée.');
        return $this->redirectToRoute('admin_nutrition');
    }

    #[Route('/billing', name: 'billing')]
    public function billing(): Response
    {
        return $this->render('admin/billing.html.twig');
    }

    #[Route('/virtual-reality', name: 'virtual_reality')]
    public function virtualReality(): Response
    {
        return $this->render('admin/virtual-reality.html.twig');
    }

    #[Route('/rtl', name: 'rtl')]
    public function rtl(): Response
    {
        return $this->render('admin/rtl.html.twig');
    }

    #[Route('/profile', name: 'profile')]
    public function profile(
        Request $request,
        EntityManagerInterface $em,
        SluggerInterface $slugger
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $form = $this->createForm(ProfileFormType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $avatarFile */
            $avatarFile = $form->get('avatarFile')->getData();
            if ($avatarFile) {
                $originalFilename = pathinfo($avatarFile->getClientOriginalName(), PATHINFO_FILENAME);
                $safeFilename = $slugger->slug($originalFilename);
                $newFilename = $safeFilename.'-'.uniqid().'.'.$avatarFile->guessExtension();

                try {
                    $avatarFile->move(
                        $this->getParameter('app.avatar_upload_dir'),
                        $newFilename
                    );
                } catch (FileException $e) {
                    $this->addFlash('error', 'Could not upload avatar. Please try again.');
                }

                $user->setAvatar($newFilename);
            }

            $em->flush();
            $this->addFlash('success', 'Profile updated successfully.');

            return $this->redirectToRoute('admin_profile');
        }

        return $this->render('admin/profile.html.twig', [
            'profileForm' => $form->createView(),
        ]);
    }

    #[Route('/profile/delete', name: 'profile_delete', methods: ['POST'])]
    public function deleteProfile(
        Request $request,
        EntityManagerInterface $em,
        TokenStorageInterface $tokenStorage
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        if (!$this->isCsrfTokenValid('delete_admin_profile', (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid security token.');
            return $this->redirectToRoute('admin_profile');
        }

        $em->remove($user);
        $em->flush();

        $tokenStorage->setToken(null);
        $request->getSession()->invalidate();

        return $this->redirectToRoute('home');
    }

    #[Route('/sign-in', name: 'sign_in')]
    public function signIn(): Response
    {
        return $this->render('admin/sign-in.html.twig');
    }

    #[Route('/sign-up', name: 'sign_up')]
    public function signUp(): Response
    {
        return $this->render('admin/sign-up.html.twig');
    }
}
