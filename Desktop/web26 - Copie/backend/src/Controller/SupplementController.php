<?php

namespace App\Controller;

use App\Entity\Supplement;
use App\Form\SupplementType;
use App\Repository\SupplementRepository;
use App\Service\FileUploader;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/supplement')]
class SupplementController extends AbstractController
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private SupplementRepository $supplementRepository,
        private FileUploader $fileUploader,
    ) {
    }

    #[Route('/', name: 'app_supplement_index', methods: ['GET'])]
    public function index(): Response
    {
        $supplements = $this->supplementRepository->findAll();

        return $this->render('supplement/index.html.twig', [
            'supplements' => $supplements,
        ]);
    }

    #[Route('/new', name: 'app_supplement_new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        $supplement = new Supplement();
        $form = $this->createForm(SupplementType::class, $supplement);
        $form->handleRequest($request);

        if ($form->isSubmitted()) {
            /** @var UploadedFile $imageFile */
            $imageFile = $form->get('imageFile')->getData();

            if ($imageFile) {
                $imageFileName = $this->fileUploader->upload($imageFile);
                $supplement->setImage($imageFileName);
            }

            $this->entityManager->persist($supplement);
            $this->entityManager->flush();

            $this->addFlash('success', 'Supplement created successfully!');

            return $this->redirectToRoute('app_supplement_index');
        }

        return $this->render('supplement/new.html.twig', [
            'supplement' => $supplement,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_supplement_show', methods: ['GET'])]
    public function show(Supplement $supplement): Response
    {
        return $this->render('supplement/show.html.twig', [
            'supplement' => $supplement,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_supplement_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Supplement $supplement): Response
    {
        $form = $this->createForm(SupplementType::class, $supplement);
        $form->handleRequest($request);

        if ($form->isSubmitted()) {
            /** @var UploadedFile $imageFile */
            $imageFile = $form->get('imageFile')->getData();

            if ($imageFile) {
                // Delete old image if exists
                if ($supplement->getImage()) {
                    $oldImagePath = $this->fileUploader->getTargetDirectory() . '/' . $supplement->getImage();
                    if (file_exists($oldImagePath)) {
                        unlink($oldImagePath);
                    }
                }

                $imageFileName = $this->fileUploader->upload($imageFile);
                $supplement->setImage($imageFileName);
            }

            $this->entityManager->flush();

            $this->addFlash('success', 'Supplement updated successfully!');

            return $this->redirectToRoute('app_supplement_index');
        }

        return $this->render('supplement/edit.html.twig', [
            'supplement' => $supplement,
            'form' => $form,
        ]);
    }

    #[Route('/{id}/delete', name: 'app_supplement_delete', methods: ['POST'])]
    public function delete(Request $request, Supplement $supplement): Response
    {
        if ($this->isCsrfTokenValid('delete'.$supplement->getId(), $request->request->get('_token'))) {
            // Delete image file if exists
            if ($supplement->getImage()) {
                $imagePath = $this->fileUploader->getTargetDirectory() . '/' . $supplement->getImage();
                if (file_exists($imagePath)) {
                    unlink($imagePath);
                }
            }

            $this->entityManager->remove($supplement);
            $this->entityManager->flush();

            $this->addFlash('success', 'Supplement deleted successfully!');
        }

        return $this->redirectToRoute('app_supplement_index');
    }

}

