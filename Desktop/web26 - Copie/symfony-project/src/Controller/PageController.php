<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Doctrine\ORM\EntityManagerInterface;
use App\Entity\BlogPost;
use App\Entity\ContentInteraction;
use App\Entity\DmConversation;
use App\Entity\DmMessage;
use App\Entity\DmParticipant;
use App\Entity\User;
use App\Form\BlogPostType;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\String\Slugger\SluggerInterface;
use Symfony\Component\Routing\Attribute\Route;

class PageController extends AbstractController
{
    #[Route('/', name: 'home')]
    public function home(): Response
    {
        return $this->render('pages/index.html.twig');
    }

    #[Route('/about', name: 'about')]
    public function about(): Response
    {
        return $this->render('pages/about.html.twig');
    }

    #[Route('/services', name: 'services')]
    public function services(): Response
    {
        return $this->render('pages/services.html.twig');
    }

    #[Route('/pricing', name: 'pricing')]
    public function pricing(): Response
    {
        return $this->render('pages/pricing.html.twig');
    }

    #[Route('/blog', name: 'blog')]
    public function blog(): Response
    {
        return $this->render('pages/blog.html.twig');
    }

    #[Route('/contact', name: 'contact')]
    public function contact(): Response
    {
        return $this->render('pages/contact.html.twig');
    }

    #[Route('/explore', name: 'explore_forums_blogs')]
    public function explore(
        Request $request,
        EntityManagerInterface $em,
        SluggerInterface $slugger
    ): Response
    {
        $user = $this->getUser();

        $post = new BlogPost();
        $form = $this->createForm(BlogPostType::class, $post);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            if (!$user) {
                $this->addFlash('error', 'Please login to publish a post.');
                return $this->redirectToRoute('app_login');
            }

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

            $this->addFlash('success', 'Your post has been published.');
            return $this->redirectToRoute('explore_forums_blogs');
        }

        $posts = $em->getRepository(BlogPost::class)->findBy(
            ['status' => 'published'],
            ['publishedAt' => 'DESC']
        );

        $postIds = array_map(static fn (BlogPost $post) => $post->getId(), $posts);
        $interactionMap = [];
        if (!empty($postIds)) {
            $interactions = $em->getRepository(ContentInteraction::class)
                ->createQueryBuilder('c')
                ->where('c.targetType = :type')
                ->andWhere('c.targetId IN (:ids)')
                ->setParameter('type', 'blog_post')
                ->setParameter('ids', $postIds)
                ->getQuery()
                ->getResult();

            foreach ($interactions as $interaction) {
                $targetId = $interaction->getTargetId();
                if (!isset($interactionMap[$targetId])) {
                    $interactionMap[$targetId] = [
                        'likes' => [],
                        'reposts' => [],
                        'comments' => [],
                    ];
                }
                $email = $interaction->getUser()?->getEmail() ?? 'Anonymous';
                $type = $interaction->getInteractionType();
                if ($type === 'like') {
                    $interactionMap[$targetId]['likes'][] = $email;
                } elseif ($type === 'repost') {
                    $interactionMap[$targetId]['reposts'][] = $email;
                } elseif ($type === 'comment') {
                    $interactionMap[$targetId]['comments'][] = [
                        'user' => $email,
                        'text' => $interaction->getCommentText() ?? '',
                        'date' => $interaction->getCreatedAt()->format('Y-m-d H:i'),
                    ];
                }
            }
        }

        $currentEmail = $user?->getEmail();
        $items = [];
        foreach ($posts as $p) {
            $roles = $p->getAuthor()?->getRoles() ?? [];
            $role = $roles[0] ?? 'ROLE_USER';
            $stats = $interactionMap[$p->getId()] ?? ['likes' => [], 'reposts' => [], 'comments' => []];
            $likedByMe = $currentEmail ? in_array($currentEmail, $stats['likes'], true) : false;
            $repostedByMe = $currentEmail ? in_array($currentEmail, $stats['reposts'], true) : false;
            $items[] = [
                'id' => $p->getId(),
                'title' => $p->getTitle(),
                'excerpt' => $p->getExcerpt() ?? '',
                'email' => $p->getAuthor()?->getEmail() ?? '',
                'role' => $role,
                'category' => $p->getCategory(),
                'image' => $p->getFeaturedImage(),
                'date' => $p->getPublishedAt() ? $p->getPublishedAt()->format('Y-m-d') : $p->getCreatedAt()->format('Y-m-d'),
                'likes' => [
                    'count' => count($stats['likes']),
                    'users' => $stats['likes'],
                ],
                'reposts' => [
                    'count' => count($stats['reposts']),
                    'users' => $stats['reposts'],
                ],
                'comments' => [
                    'count' => count($stats['comments']),
                    'items' => $stats['comments'],
                ],
                'likedByMe' => $likedByMe,
                'repostedByMe' => $repostedByMe,
            ];
        }

        $userPosts = [];
        if ($user) {
            $userPosts = $em->getRepository(BlogPost::class)->findBy(
                ['author' => $user],
                ['createdAt' => 'DESC']
            );
        }

        $dmConversations = [];
        $dmConversationItems = [];
        $dmSelectedConversation = null;
        $dmMessages = [];
        $dmOtherParticipant = null;
        $dmOtherBadge = null;
        $dmOtherName = null;
        $dmUnreadCounts = [];
        $dmLastMessage = [];
        $dmLastMessageTime = [];
        $dmUsers = [];

        if ($user) {
            $dmConversations = $em->createQueryBuilder()
                ->select('c', 'p', 'u')
                ->from(DmConversation::class, 'c')
                ->join('c.participants', 'p')
                ->join('p.user', 'u')
                ->where('p.user = :user')
                ->setParameter('user', $user)
                ->orderBy('c.lastMessageAt', 'DESC')
                ->getQuery()
                ->getResult();

            $hasDmParam = $request->query->has('dm');
            $selectedId = (int) $request->query->get('dm', 0);
            if ($selectedId > 0) {
                $dmSelectedConversation = $em->getRepository(DmConversation::class)->find($selectedId);
            }

            $knownUserIds = [];
            foreach ($dmConversations as $conv) {
                $participant = null;
                $other = null;
                foreach ($conv->getParticipants() as $p) {
                    if ($p->getUser()?->getId() === $user->getId()) {
                        $participant = $p;
                    } else {
                        $other = $p->getUser();
                    }
                }

                if ($other) {
                    $knownUserIds[] = $other->getId();
                    $displayName = trim($other->getFirstName().' '.$other->getLastName());
                    if ($displayName === '') {
                        $displayName = $other->getUsername() ?: $other->getEmail();
                    }
                    $dmConversationItems[] = [
                        'id' => $conv->getId(),
                        'otherId' => $other->getId(),
                        'otherEmail' => $other->getEmail(),
                        'displayName' => $displayName,
                    ];
                }

                $dmUnreadCounts[$conv->getId()] = $this->countUnread(
                    $em,
                    $conv->getId(),
                    $user->getId(),
                    $participant?->getLastReadAt()
                );

                $last = $em->getRepository(DmMessage::class)->findOneBy(
                    ['conversation' => $conv],
                    ['createdAt' => 'DESC']
                );
                $dmLastMessage[$conv->getId()] = $last?->getBody() ?? '';
                $dmLastMessageTime[$conv->getId()] = $last?->getCreatedAt()?->format('Y-m-d H:i') ?? '';
            }

            if ($dmSelectedConversation) {
                $dmMessages = $em->getRepository(DmMessage::class)
                    ->findBy(['conversation' => $dmSelectedConversation], ['createdAt' => 'ASC']);

                $otherUser = $em->createQueryBuilder()
                    ->select('dp', 'u')
                    ->from(DmParticipant::class, 'dp')
                    ->join('dp.user', 'u')
                    ->where('dp.conversation = :conv')
                    ->andWhere('u != :me')
                    ->setParameter('conv', $dmSelectedConversation)
                    ->setParameter('me', $user)
                    ->getQuery()
                    ->getOneOrNullResult();

                if ($otherUser instanceof DmParticipant) {
                    $otherUser = $otherUser->getUser();
                }

                if ($otherUser instanceof User) {
                    $dmOtherParticipant = $otherUser;
                    $roles = $otherUser->getRoles();
                    $dmOtherBadge = $roles[0] ?? 'ROLE_USER';
                    $name = trim($otherUser->getFirstName().' '.$otherUser->getLastName());
                    $dmOtherName = $name !== '' ? $name : ($otherUser->getUsername() ?: $otherUser->getEmail());
                }

                foreach ($dmSelectedConversation->getParticipants() as $p) {
                    if ($p->getUser()?->getId() === $user->getId()) {
                        $p->setLastReadAt(new \DateTimeImmutable());
                    }
                }
                $em->flush();
            }

            $allUsers = $em->getRepository(User::class)->findBy([], ['id' => 'DESC']);
            $knownUserIds = array_unique($knownUserIds ?? []);
            $dmUsers = array_values(array_filter($allUsers, function (User $u) use ($user, $knownUserIds) {
                if ($u->getId() === $user->getId()) {
                    return false;
                }
                return !in_array($u->getId(), $knownUserIds, true);
            }));
        }

        return $this->render('pages/explore.html.twig', [
            'items' => $items,
            'blogForm' => $form->createView(),
            'userPosts' => $userPosts,
            'dmConversations' => $dmConversations,
            'dmConversationItems' => $dmConversationItems,
            'dmSelectedConversation' => $dmSelectedConversation,
            'dmMessages' => $dmMessages,
            'dmOtherParticipant' => $dmOtherParticipant,
            'dmOtherBadge' => $dmOtherBadge,
            'dmOtherName' => $dmOtherName,
            'dmUnreadCounts' => $dmUnreadCounts,
            'dmLastMessage' => $dmLastMessage,
            'dmLastMessageTime' => $dmLastMessageTime ?? [],
            'dmUsers' => $dmUsers,
        ]);
    }

    #[Route('/explore/messages/{id}/send', name: 'explore_dm_send', methods: ['POST'])]
    public function sendDmMessage(
        DmConversation $conversation,
        Request $request,
        EntityManagerInterface $em
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        $body = trim((string) $request->request->get('body'));
        /** @var UploadedFile|null $attachment */
        $attachment = $request->files->get('attachment');
        if ($body === '' && !$attachment) {
            return $this->redirectToRoute('explore_forums_blogs', ['dm' => $conversation->getId()]);
        }

        $message = new DmMessage();
        $message->setConversation($conversation);
        $message->setSender($user);
        $message->setBody($body !== '' ? $body : '[Attachment]');
        $message->setStatus('sent');

        if ($attachment) {
            $uploadDir = $this->getParameter('kernel.project_dir').DIRECTORY_SEPARATOR.'public'.DIRECTORY_SEPARATOR.'uploads'.DIRECTORY_SEPARATOR.'dm';
            if (!is_dir($uploadDir)) {
                @mkdir($uploadDir, 0777, true);
            }
            $originalName = pathinfo($attachment->getClientOriginalName(), PATHINFO_FILENAME);
            $safeName = preg_replace('/[^a-zA-Z0-9_-]/', '_', $originalName);
            $newName = $safeName.'-'.uniqid().'.'.$attachment->guessExtension();

            try {
                $attachment->move($uploadDir, $newName);
                $message->setAttachmentPath('uploads/dm/'.$newName);
                $message->setAttachmentMime((string) $attachment->getMimeType());
            } catch (FileException $e) {
                // fallback: no attachment saved
            }
        }

        $conversation->setLastMessageAt(new \DateTimeImmutable());

        $em->persist($message);
        $em->flush();

        return $this->redirectToRoute('explore_forums_blogs', ['dm' => $conversation->getId()]);
    }

    #[Route('/explore/messages/{id}/poll', name: 'explore_dm_poll', methods: ['GET'])]
    public function pollDmMessages(
        DmConversation $conversation,
        Request $request,
        EntityManagerInterface $em
    ): JsonResponse {
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'unauthorized'], 401);
        }

        $messages = $em->getRepository(DmMessage::class)
            ->findBy(['conversation' => $conversation], ['createdAt' => 'ASC']);

        $typing = false;
        foreach ($conversation->getParticipants() as $p) {
            if ($p->getUser()?->getId() !== $user->getId()) {
                $typingAt = $p->getTypingAt();
                if ($typingAt && (new \DateTimeImmutable())->getTimestamp() - $typingAt->getTimestamp() <= 5) {
                    $typing = true;
                }
            } else {
                $p->setLastReadAt(new \DateTimeImmutable());
            }
        }

        foreach ($messages as $m) {
            if ($m->getSender()?->getId() !== $user->getId()) {
                $m->setStatus('read');
            }
        }
        $em->flush();

        $payload = array_map(function (DmMessage $m) use ($user) {
            return [
                'id' => $m->getId(),
                'body' => $m->getBody(),
                'createdAt' => $m->getCreatedAt()->format('H:i'),
                'isMine' => $m->getSender()?->getId() === $user->getId(),
                'status' => $m->getStatus(),
                'attachment' => $m->getAttachmentPath(),
                'attachmentMime' => $m->getAttachmentMime(),
            ];
        }, $messages);

        return new JsonResponse([
            'messages' => $payload,
            'typing' => $typing,
        ]);
    }

    #[Route('/explore/messages/{id}/typing', name: 'explore_dm_typing', methods: ['POST'])]
    public function typingDm(
        DmConversation $conversation,
        EntityManagerInterface $em
    ): JsonResponse {
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'unauthorized'], 401);
        }

        foreach ($conversation->getParticipants() as $p) {
            if ($p->getUser()?->getId() === $user->getId()) {
                $p->setTypingAt(new \DateTimeImmutable());
            }
        }
        $em->flush();

        return new JsonResponse(['ok' => true]);
    }

    private function countUnread(
        EntityManagerInterface $em,
        int $conversationId,
        int $userId,
        ?\DateTimeImmutable $lastReadAt
    ): int {
        $qb = $em->createQueryBuilder()
            ->select('COUNT(m.id)')
            ->from(DmMessage::class, 'm')
            ->where('m.conversation = :conv')
            ->andWhere('m.sender != :user')
            ->setParameter('conv', $conversationId)
            ->setParameter('user', $userId);

        if ($lastReadAt) {
            $qb->andWhere('m.createdAt > :lastReadAt')
               ->setParameter('lastReadAt', $lastReadAt);
        }

        return (int) $qb->getQuery()->getSingleScalarResult();
    }

    #[Route('/explore/messages/start/{id}', name: 'explore_dm_start', methods: ['POST'])]
    public function startDm(
        User $recipient,
        EntityManagerInterface $em
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }
        if ($recipient->getId() === $user->getId()) {
            return $this->redirectToRoute('explore_forums_blogs');
        }

        $existing = $em->createQueryBuilder()
            ->select('c')
            ->from(DmConversation::class, 'c')
            ->join('c.participants', 'p1')
            ->join('c.participants', 'p2')
            ->where('p1.user = :u1')
            ->andWhere('p2.user = :u2')
            ->setParameter('u1', $user)
            ->setParameter('u2', $recipient)
            ->getQuery()
            ->getOneOrNullResult();

        if ($existing) {
            return $this->redirectToRoute('explore_forums_blogs', ['dm' => $existing->getId()]);
        }

        $conversation = new DmConversation();
        $participantA = new DmParticipant();
        $participantA->setConversation($conversation)->setUser($user);
        $participantB = new DmParticipant();
        $participantB->setConversation($conversation)->setUser($recipient);

        $em->persist($conversation);
        $em->persist($participantA);
        $em->persist($participantB);
        $em->flush();

        return $this->redirectToRoute('explore_forums_blogs', ['dm' => $conversation->getId()]);
    }

    #[Route('/explore/{id}/interact', name: 'explore_interact', methods: ['POST'])]
    public function interact(
        int $id,
        Request $request,
        EntityManagerInterface $em
    ): Response {
        $user = $this->getUser();
        if (!$user) {
            $this->addFlash('error', 'Please login first.');
            return $this->redirectToRoute('app_login');
        }

        $type = (string) $request->request->get('type');
        if (!in_array($type, ['like', 'comment', 'repost'], true)) {
            $this->addFlash('error', 'Invalid interaction.');
            return $this->redirectToRoute('explore_forums_blogs');
        }

        if (!$this->isCsrfTokenValid('interact_'.$type.'_'.$id, (string) $request->request->get('_token'))) {
            $this->addFlash('error', 'Invalid security token.');
            return $this->redirectToRoute('explore_forums_blogs');
        }

        $post = $em->getRepository(BlogPost::class)->find($id);
        if (!$post) {
            $this->addFlash('error', 'Post not found.');
            return $this->redirectToRoute('explore_forums_blogs');
        }

        if ($type === 'comment') {
            $commentText = trim((string) $request->request->get('comment_text'));
            if ($commentText === '') {
                $this->addFlash('error', 'Comment cannot be empty.');
                return $this->redirectToRoute('explore_forums_blogs');
            }

            $interaction = new ContentInteraction();
            $interaction->setUser($user);
            $interaction->setTargetType('blog_post');
            $interaction->setTargetId($post->getId());
            $interaction->setInteractionType('comment');
            $interaction->setCommentText($commentText);
            $interaction->setCreatedAt(new \DateTimeImmutable());
            $em->persist($interaction);
            $em->flush();

            $this->addFlash('success', 'Comment added.');
            return $this->redirectToRoute('explore_forums_blogs');
        }

        $existing = $em->getRepository(ContentInteraction::class)->findOneBy([
            'user' => $user,
            'targetType' => 'blog_post',
            'targetId' => $post->getId(),
            'interactionType' => $type,
        ]);

        if ($existing) {
            $em->remove($existing);
            $em->flush();
            $this->addFlash('success', ucfirst($type).' removed.');
            return $this->redirectToRoute('explore_forums_blogs');
        }

        $interaction = new ContentInteraction();
        $interaction->setUser($user);
        $interaction->setTargetType('blog_post');
        $interaction->setTargetId($post->getId());
        $interaction->setInteractionType($type);
        $interaction->setCreatedAt(new \DateTimeImmutable());
        $em->persist($interaction);
        $em->flush();

        $this->addFlash('success', ucfirst($type).' saved.');
        return $this->redirectToRoute('explore_forums_blogs');
    }
}
