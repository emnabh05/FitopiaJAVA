<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Mailer\Transport;
use Symfony\Component\Mailer\Mailer;
use Symfony\Component\Mime\Email;
use Symfony\Component\Mime\Address;

class CoachController extends AbstractController
{
    private const COACHES = [
        [
            'id'          => 'ahmed',
            'name'        => 'Ahmed Chebbi',
            'specialty'   => 'Musculation & Force',
            'bio'         => 'Coach certifié avec 8 ans d\'expérience en musculation et préparation physique.',
            'email'       => 'ahmedchebbi323@gmail.com',
            'phone'       => '+216 55 123 456',
            'instagram'   => '@ahmed.coach',
            'slots'       => ['Lundi 09h-12h', 'Mercredi 14h-17h', 'Vendredi 09h-11h'],
            'image'       => 'images/coaches/ahmed.jpg',
            'rating'      => 4.9,
            'clients'     => 120,
        ],
        [
            'id'          => 'ali',
            'name'        => 'Ali Ben Salah',
            'specialty'   => 'Cardio & Endurance',
            'bio'         => 'Spécialiste en entraînement cardio-vasculaire et préparation aux courses.',
            'email'       => 'ahmedchebbi323@gmail.com',
            'phone'       => '+216 55 234 567',
            'instagram'   => '@ali.run',
            'slots'       => ['Mardi 07h-10h', 'Jeudi 07h-10h', 'Samedi 08h-11h'],
            'image'       => 'images/coaches/ali.jpg',
            'rating'      => 4.8,
            'clients'     => 95,
        ],
        [
            'id'          => 'sarah',
            'name'        => 'Sarah Mansouri',
            'specialty'   => 'Yoga & Mobilité',
            'bio'         => 'Instructrice de yoga certifiée, spécialisée en flexibilité et bien-être mental.',
            'email'       => 'ahmedchebbi323@gmail.com',
            'phone'       => '+216 55 345 678',
            'instagram'   => '@sarah.yoga',
            'slots'       => ['Lundi 17h-19h', 'Mercredi 17h-19h', 'Dimanche 10h-12h'],
            'image'       => 'images/coaches/sarah.jpg',
            'rating'      => 5.0,
            'clients'     => 80,
        ],
        [
            'id'          => 'rania',
            'name'        => 'Rania Trabelsi',
            'specialty'   => 'Nutrition & Perte de poids',
            'bio'         => 'Nutritionniste sportive et coach fitness, experte en transformation corporelle.',
            'email'       => 'ahmedchebbi323@gmail.com',
            'phone'       => '+216 55 456 789',
            'instagram'   => '@rania.fit',
            'slots'       => ['Mardi 10h-13h', 'Jeudi 14h-17h', 'Samedi 14h-16h'],
            'image'       => 'images/coaches/rania.jpg',
            'rating'      => 4.7,
            'clients'     => 110,
        ],
        [
            'id'          => 'omar',
            'name'        => 'Omar Khelifi',
            'specialty'   => 'HIIT & CrossFit',
            'bio'         => 'Coach CrossFit niveau 2, passionné par les entraînements haute intensité.',
            'email'       => 'ahmedchebbi323@gmail.com',
            'phone'       => '+216 55 567 890',
            'instagram'   => '@omar.hiit',
            'slots'       => ['Lundi 06h-08h', 'Mercredi 06h-08h', 'Vendredi 06h-08h'],
            'image'       => 'images/coaches/omar.jpg',
            'rating'      => 4.8,
            'clients'     => 140,
        ],
        [
            'id'          => 'emna',
            'name'        => 'Emna Gharbi',
            'specialty'   => 'Pilates & Gainage',
            'bio'         => 'Instructrice Pilates certifiée, spécialisée en renforcement du core et posture.',
            'email'       => 'ahmedchebbi323@gmail.com',
            'phone'       => '+216 55 678 901',
            'instagram'   => '@emna.pilates',
            'slots'       => ['Mardi 17h-19h', 'Jeudi 17h-19h', 'Dimanche 14h-16h'],
            'image'       => 'images/coaches/emna.jpg',
            'rating'      => 4.9,
            'clients'     => 75,
        ],
    ];

    #[Route('/coaches', name: 'coaches')]
    public function index(): Response
    {
        return $this->render('pages/coaches.html.twig', [
            'coaches' => self::COACHES,
        ]);
    }

    #[Route('/coaches/{id}/book', name: 'coach_book', methods: ['GET', 'POST'])]
    public function book(string $id, Request $request): Response
    {
        $coach = $this->findCoach($id);
        if (!$coach) {
            throw $this->createNotFoundException('Coach introuvable.');
        }

        $errors  = [];
        $success = false;

        if ($request->isMethod('POST')) {
            $data = [
                'client_name'  => trim((string) $request->request->get('client_name')),
                'client_email' => trim((string) $request->request->get('client_email')),
                'client_phone' => trim((string) $request->request->get('client_phone')),
                'desired_date' => trim((string) $request->request->get('desired_date')),
                'message'      => trim((string) $request->request->get('message')),
            ];

            $errors = $this->validateBooking($data);

            if (empty($errors)) {
                $sent = $this->sendBookingEmail($coach, $data);
                if ($sent) {
                    $success = true;
                } else {
                    $errors['send'] = 'L\'email n\'a pas pu être envoyé. Veuillez réessayer.';
                }
            }
        }

        return $this->render('pages/coach_book.html.twig', [
            'coach'   => $coach,
            'coaches' => self::COACHES,
            'errors'  => $errors,
            'success' => $success,
        ]);
    }

    // -------------------------------------------------------------------------

    private function findCoach(string $id): ?array
    {
        foreach (self::COACHES as $coach) {
            if ($coach['id'] === $id) {
                return $coach;
            }
        }
        return null;
    }

    private function validateBooking(array $data): array
    {
        $errors = [];

        if ($data['client_name'] === '') {
            $errors['client_name'] = 'Le nom est obligatoire.';
        } elseif (mb_strlen($data['client_name']) < 2) {
            $errors['client_name'] = 'Le nom doit contenir au moins 2 caractères.';
        }

        if ($data['client_email'] === '') {
            $errors['client_email'] = 'L\'email est obligatoire.';
        } elseif (!filter_var($data['client_email'], FILTER_VALIDATE_EMAIL)) {
            $errors['client_email'] = 'L\'adresse email n\'est pas valide.';
        }

        if ($data['client_phone'] === '') {
            $errors['client_phone'] = 'Le téléphone est obligatoire.';
        } elseif (!preg_match('/^[\+\d\s\-\(\)]{6,20}$/', $data['client_phone'])) {
            $errors['client_phone'] = 'Le numéro de téléphone n\'est pas valide.';
        }

        if ($data['desired_date'] === '') {
            $errors['desired_date'] = 'La date souhaitée est obligatoire.';
        } else {
            $date = \DateTime::createFromFormat('Y-m-d', $data['desired_date']);
            if (!$date || $date < new \DateTime('today')) {
                $errors['desired_date'] = 'La date doit être aujourd\'hui ou dans le futur.';
            }
        }

        return $errors;
    }

    private function sendBookingEmail(array $coach, array $data): bool
    {
        try {
            $username = $_ENV['SMTP_USERNAME'] ?? '';
            $password = $_ENV['SMTP_PASSWORD'] ?? '';

            if ($username === '' || $password === '' || $password === 'change_me_with_your_gmail_app_password') {
                return false;
            }

            $dsn     = sprintf('smtp://%s:%s@smtp.gmail.com:587?encryption=tls',
                urlencode($username), urlencode($password));
            $transport = Transport::fromDsn($dsn);
            $mailer    = new Mailer($transport);

            $html = $this->renderView('emails/coach_booking.html.twig', [
                'coach' => $coach,
                'data'  => $data,
            ]);

            $email = (new Email())
                ->from(new Address($username, 'Fitopia Reservations'))
                ->to(new Address($coach['email'], $coach['name']))
                ->replyTo(new Address($data['client_email'], $data['client_name']))
                ->subject('Nouvelle réservation — ' . $data['client_name'])
                ->html($html);

            $mailer->send($email);
            return true;
        } catch (\Throwable) {
            return false;
        }
    }
}
