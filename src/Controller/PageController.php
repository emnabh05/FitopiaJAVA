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
use App\Entity\FitnessExercise;
use App\Entity\FitnessPlan;
use App\Entity\FitnessProgram;
use App\Entity\FitnessTrend;
use App\Entity\User;
use App\Entity\ExerciseCatalog;
use App\Entity\TrainingSession;
use App\Entity\PerformanceLog;
use App\Entity\PerformanceSet;
use App\Entity\RegimeAlimentaire;
use App\Entity\Repas;
use App\Form\BlogPostType;
use App\Form\RegimeAlimentaireFormType;
use App\Service\NutritionRegimeCalculator;
use App\Service\FoodRecognitionService;
use App\Service\OpenAiDietChatService;
use App\Service\DailyGoalMailerService;
use App\Service\TwilioSmsService;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\String\Slugger\SluggerInterface;
use Symfony\Component\Routing\Attribute\Route;
use Dompdf\Dompdf;
use Dompdf\Options;

class PageController extends AbstractController
{
    public function __construct(
        private readonly OpenAiDietChatService $dietChatService
    ) {
    }

    #[Route('/', name: 'home')]
    public function home(): Response
    {
        return $this->render('pages/index.html.twig');
    }

    #[Route('/dashboard', name: 'fitness_dashboard')]
    public function dashboard(EntityManagerInterface $em): Response
    {
        if (!$this->getUser() instanceof User) {
            return $this->redirectToRoute('app_login');
        }
        $user   = $this->getUser();
        $userId = $user->getId();

        // Stats fitness
        $totalSessions = $em->getRepository(TrainingSession::class)->count(['user' => $user]);
        $totalPlans    = $em->getRepository(FitnessPlan::class)->count(['user' => $user]);
        $totalPrograms = $em->getRepository(FitnessProgram::class)->count([]);

        // Dernières séances
        $recentSessions = $em->getRepository(TrainingSession::class)->findByUser($userId, 5);

        // Record poids max toutes séances
        $recordRow = $em->getConnection()->fetchAssociative('
            SELECT ec.name AS exercise_name, MAX(ps.weight_kg) AS max_weight
            FROM performance_set ps
            JOIN performance_log pl ON pl.id = ps.log_id
            JOIN training_session ts ON ts.id = pl.session_id
            JOIN exercise_catalog ec ON ec.id = pl.exercise_id
            WHERE ts.user_id = :uid AND ps.weight_kg IS NOT NULL AND ps.is_warmup = 0
            GROUP BY pl.exercise_id ORDER BY max_weight DESC LIMIT 1
        ', ['uid' => $userId]);

        // Semaines actives (semaines avec au moins 1 séance)
        $weeksActive = (int) $em->getConnection()->fetchOne('
            SELECT COUNT(DISTINCT CONCAT(year, week_number))
            FROM training_session WHERE user_id = :uid
        ', ['uid' => $userId]);

        // Exercices distincts pratiqués
        $distinctExercises = (int) $em->getConnection()->fetchOne('
            SELECT COUNT(DISTINCT pl.exercise_id)
            FROM performance_log pl
            JOIN training_session ts ON ts.id = pl.session_id
            WHERE ts.user_id = :uid
        ', ['uid' => $userId]);

        return $this->render('pages/fitness_dashboard.html.twig', [
            'user'              => $user,
            'totalSessions'     => $totalSessions,
            'totalPlans'        => $totalPlans,
            'totalPrograms'     => $totalPrograms,
            'recentSessions'    => $recentSessions,
            'recordRow'         => $recordRow ?: null,
            'weeksActive'       => $weeksActive,
            'distinctExercises' => $distinctExercises,
        ]);
    }

    #[Route('/performance/session/{id}/pdf', name: 'performance_session_pdf', methods: ['GET'])]
    public function exportSessionPdf(int $id, EntityManagerInterface $em): Response
    {
        if (!$this->getUser() instanceof User) {
            return $this->redirectToRoute('app_login');
        }

        $session = $em->getRepository(TrainingSession::class)->find($id);
        if (!$session || $session->getUser()->getId() !== $this->getUser()->getId()) {
            throw $this->createNotFoundException('Séance introuvable.');
        }

        $html = $this->renderView('emails/session_pdf.html.twig', ['session' => $session]);
        $html = @iconv('UTF-8', 'UTF-8//IGNORE', $html) ?: $html;

        $options = new Options();
        $options->set('isRemoteEnabled', false);
        $options->set('isFontSubsettingEnabled', false);
        $options->set('defaultFont', 'Helvetica');

        $dompdf = new Dompdf($options);
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'portrait');
        $dompdf->render();

        $filename = 'seance-' . $session->getSessionDate()->format('Y-m-d') . '.pdf';
        return new Response($dompdf->output(), 200, [
            'Content-Type'        => 'application/pdf',
            'Content-Disposition' => 'attachment; filename="' . $filename . '"',
        ]);
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

    #[Route('/supplements', name: 'supplements')]
    public function supplements(): Response
    {
        return $this->redirectToRoute('app_shop_index');
    }

    #[Route('/fitness-planner', name: 'fitness_planner', methods: ['GET', 'POST'])]
    public function fitnessPlanner(Request $request, EntityManagerInterface $em): Response
    {
        $tablesReady = $this->fitnessTablesExist($em);

        if ($request->isMethod('POST')) {
            if (!$this->isCsrfTokenValid('fitness_planner', (string) $request->request->get('_token'))) {
                $this->addFlash('error', 'Invalid security token.');
                return $this->redirectToRoute('fitness_planner');
            }
            if (!$tablesReady) {
                $this->addFlash('error', 'Fitness planner tables are missing. Run migrations first.');
                return $this->redirectToRoute('fitness_planner');
            }

            $this->handleFitnessPlannerPost($request, $em);
            return $this->redirectToRoute('fitness_planner', [
                'view' => (string) $request->request->get('redirect_view', 'manage'),
            ]);
        }

        $search = trim((string) $request->query->get('q', ''));
        $sort = (string) $request->query->get('sort', 'newest');
        $view = (string) $request->query->get('view', 'plans');
        $programEditId = $request->query->getInt('program_edit', 0);
        $exerciseEditId = $request->query->getInt('exercise_edit', 0);

        $programs = [];
        $exercises = [];
        $trends = [];
        $programInEdit = null;
        $exerciseInEdit = null;
        $stats = ['programs' => 0, 'exercises' => 0, 'avgDuration' => 0];
        $programImageLinks = [];
        $savedPlans = [];
        $plansPersistAvailable = false;

        if ($tablesReady) {
            $programsQb = $em->getRepository(FitnessProgram::class)->createQueryBuilder('p');
            if ($search !== '') {
                $programsQb
                    ->andWhere('LOWER(p.title) LIKE :q OR LOWER(p.category) LIKE :q OR LOWER(p.level) LIKE :q')
                    ->setParameter('q', '%'.mb_strtolower($search).'%');
            }

            match ($sort) {
                'oldest' => $programsQb->orderBy('p.createdAt', 'ASC'),
                'title_asc' => $programsQb->orderBy('p.title', 'ASC'),
                'title_desc' => $programsQb->orderBy('p.title', 'DESC'),
                'duration_asc' => $programsQb->orderBy('p.sessionDuration', 'ASC'),
                'duration_desc' => $programsQb->orderBy('p.sessionDuration', 'DESC'),
                default => $programsQb->orderBy('p.createdAt', 'DESC'),
            };

            $programs = $programsQb->getQuery()->getResult();
            $exercises = $em->getRepository(FitnessExercise::class)->findBy([], ['createdAt' => 'DESC']);

            if ($programEditId > 0) {
                $programInEdit = $em->getRepository(FitnessProgram::class)->find($programEditId);
            }
            if ($exerciseEditId > 0) {
                $exerciseInEdit = $em->getRepository(FitnessExercise::class)->find($exerciseEditId);
            }

            $stats['programs'] = (int) $em->getRepository(FitnessProgram::class)->count([]);
            $stats['exercises'] = (int) $em->getRepository(FitnessExercise::class)->count([]);

            $avgDuration = $em->getRepository(FitnessProgram::class)->createQueryBuilder('p')
                ->select('AVG(p.sessionDuration)')
                ->getQuery()
                ->getSingleScalarResult();
            $stats['avgDuration'] = (int) round((float) ($avgDuration ?? 0));

            $connection = $em->getConnection();
            $schemaManager = $connection->createSchemaManager();
            $tableNames = array_map('strtolower', $schemaManager->listTableNames());
            if (in_array('fitness_program_image_link', $tableNames, true)) {
                $rows = $connection->fetchAllAssociative('SELECT program_name, image_url FROM fitness_program_image_link');
                foreach ($rows as $row) {
                    $name = (string) ($row['program_name'] ?? '');
                    if ($name === '') {
                        continue;
                    }
                    $programImageLinks[$name] = (string) ($row['image_url'] ?? '');
                }
            }

            if (in_array('fitness_trend', $tableNames, true)) {
                $trends = $em->getRepository(FitnessTrend::class)->findBy(['isActive' => true], ['updatedAt' => 'DESC']);
            }

            $plansPersistAvailable = in_array('fitness_plan', $tableNames, true) && $this->getUser() instanceof User;
            if ($plansPersistAvailable) {
                $plans = $em->getRepository(FitnessPlan::class)->findBy(
                    ['user' => $this->getUser()],
                    ['createdAt' => 'DESC']
                );
                $savedPlans = array_map(fn (FitnessPlan $plan) => $this->serializeFitnessPlan($plan), $plans);
            }
        }

        return $this->render('pages/fitness-planner.html.twig', [
            'tablesReady' => $tablesReady,
            'programs' => $programs,
            'exercises' => $exercises,
            'programInEdit' => $programInEdit,
            'exerciseInEdit' => $exerciseInEdit,
            'trends' => $trends,
            'search' => $search,
            'sort' => $sort,
            'activeView' => in_array($view, ['plans', 'manage', 'exercises', 'explore', 'coach'], true) ? $view : 'plans',
            'stats' => $stats,
            'programImageLinks' => $programImageLinks,
            'savedPlans' => $savedPlans,
            'plansPersistAvailable' => $plansPersistAvailable,
            'exerciseImageMapJs' => $this->buildExerciseImageMap($exercises),
            'externalImageMapJs' => $programImageLinks,
        ]);
    }

    #[Route('/fitness-planner/plans', name: 'fitness_planner_plan_create', methods: ['POST'])]
    public function createFitnessPlannerPlan(Request $request, EntityManagerInterface $em): JsonResponse
    {
        if (!$this->isCsrfTokenValid('fitness_planner', (string) $request->request->get('_token'))) {
            return new JsonResponse(['ok' => false, 'error' => 'invalid_csrf'], 400);
        }

        $user = $this->getUser();
        if (!$user instanceof User) {
            return new JsonResponse(['ok' => false, 'error' => 'unauthorized'], 401);
        }

        if (!$this->fitnessPlansTableExist($em)) {
            return new JsonResponse(['ok' => false, 'error' => 'missing_table'], 400);
        }

        $title = trim((string) $request->request->get('title', ''));
        $place = trim((string) $request->request->get('place', '')) ?: null;
        $programLabel = trim((string) $request->request->get('program_label', ''));
        $estimatedMinutes = max(0, $request->request->getInt('estimated_minutes', 0)) ?: null;

        $rawExercises = json_decode((string) $request->request->get('exercises', '[]'), true);
        $normalizedExercises = [];
        if (is_array($rawExercises)) {
            foreach ($rawExercises as $exercise) {
                if (!is_array($exercise)) {
                    continue;
                }
                $name = trim((string) ($exercise['name'] ?? ''));
                if ($name === '') {
                    continue;
                }
                $normalizedExercises[] = [
                    'name' => $name,
                    'video' => trim((string) ($exercise['video'] ?? '')),
                ];
            }
        }

        if (empty($normalizedExercises)) {
            return new JsonResponse(['ok' => false, 'error' => 'missing_exercises'], 400);
        }

        $plan = new FitnessPlan();
        $plan->setTitle($title !== '' ? $title : sprintf('Ma seance (%d exercices)', count($normalizedExercises)));
        $plan->setPlace($place);
        $plan->setEstimatedMinutes($estimatedMinutes);
        $plan->setUser($user);
        $plan->setExercisesData($normalizedExercises);

        $program = $this->resolveProgramForPlan($programLabel, $em);
        if ($program) {
            $plan->setProgram($program);
        }

        $em->persist($plan);
        $em->flush();

        $payload = $this->serializeFitnessPlan($plan);
        if ($programLabel !== '' && $payload['program'] === '') {
            $payload['program'] = $programLabel;
        }

        return new JsonResponse(['ok' => true, 'plan' => $payload]);
    }

    #[Route('/fitness-planner/plans/{id}/delete', name: 'fitness_planner_plan_delete', methods: ['POST'])]
    public function deleteFitnessPlannerPlan(int $id, Request $request, EntityManagerInterface $em): JsonResponse
    {
        if (!$this->isCsrfTokenValid('fitness_planner', (string) $request->request->get('_token'))) {
            return new JsonResponse(['ok' => false, 'error' => 'invalid_csrf'], 400);
        }

        $user = $this->getUser();
        if (!$user instanceof User) {
            return new JsonResponse(['ok' => false, 'error' => 'unauthorized'], 401);
        }

        /** @var FitnessPlan|null $plan */
        $plan = $em->getRepository(FitnessPlan::class)->find($id);
        if (!$plan) {
            return new JsonResponse(['ok' => false, 'error' => 'not_found'], 404);
        }

        if ($plan->getUser()?->getId() !== $user->getId()) {
            return new JsonResponse(['ok' => false, 'error' => 'forbidden'], 403);
        }

        $em->remove($plan);
        $em->flush();

        return new JsonResponse(['ok' => true]);
    }

    #[Route('/fitness-planner/coaches/sms', name: 'fitness_planner_coach_send_sms', methods: ['POST'])]
    public function sendFitnessCoachSms(Request $request, TwilioSmsService $twilioSmsService): JsonResponse
    {
        if (!$this->isCsrfTokenValid('fitness_planner', (string) $request->request->get('_token'))) {
            return new JsonResponse(['ok' => false, 'error' => 'invalid_csrf'], 400);
        }

        if (!$twilioSmsService->isConfigured()) {
            return new JsonResponse([
                'ok' => false,
                'error' => 'sms_not_configured',
                'message' => 'Twilio is not configured. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN and TWILIO_FROM_NUMBER.',
            ], 503);
        }

        $kind = trim((string) $request->request->get('kind', 'message'));
        $coachName = trim((string) $request->request->get('coach_name', 'Coach'));
        $coachSpeciality = trim((string) $request->request->get('coach_speciality', ''));
        $coachPhone = trim((string) $request->request->get('coach_phone', ''));
        $reservationDate = trim((string) $request->request->get('reservation_date', ''));
        $reservationSlot = trim((string) $request->request->get('reservation_slot', ''));
        $clientMessage = trim((string) $request->request->get('client_message', ''));

        if ($coachPhone === '') {
            return new JsonResponse(['ok' => false, 'error' => 'missing_phone'], 400);
        }

        if ($kind === 'reservation' && ($reservationDate === '' || $reservationSlot === '')) {
            return new JsonResponse(['ok' => false, 'error' => 'missing_reservation_fields'], 400);
        }

        $sender = 'Client web';
        $user = $this->getUser();
        if ($user instanceof User) {
            $name = trim($user->getFirstName().' '.$user->getLastName());
            $sender = $name !== '' ? $name : ($user->getEmail() ?: 'Client web');
        }

        if ($kind === 'reservation') {
            $smsBody = implode("\n", [
                'Fitopia - Reservation client',
                'Coach: '.$coachName.($coachSpeciality !== '' ? ' ('.$coachSpeciality.')' : ''),
                'Client: '.$sender,
                'Date: '.$reservationDate,
                'Creneau: '.$reservationSlot,
                'Demande: '.($clientMessage !== '' ? $clientMessage : 'Je confirme ma reservation.'),
            ]);
        } else {
            $smsBody = implode("\n", [
                'Fitopia - Message client',
                'Coach: '.$coachName.($coachSpeciality !== '' ? ' ('.$coachSpeciality.')' : ''),
                'Client: '.$sender,
                'Message: '.($clientMessage !== '' ? $clientMessage : 'Bonjour, je souhaite vous contacter.'),
            ]);
        }

        try {
            $result = $twilioSmsService->sendMessage($coachPhone, $smsBody);
        } catch (\Throwable $exception) {
            return new JsonResponse([
                'ok' => false,
                'error' => 'sms_send_failed',
                'message' => $exception->getMessage(),
            ], 502);
        }

        return new JsonResponse([
            'ok' => true,
            'provider' => 'twilio',
            'sid' => $result['sid'],
            'status' => $result['status'],
        ]);
    }

    #[Route('/events', name: 'events')]
    public function events(): Response
    {
        return $this->render('pages/events.html.twig');
    }

    #[Route('/diet-eating-planner', name: 'diet_planner')]
    public function dietPlanner(Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        // RÃƒÂ©gime actif sÃƒÂ©lectionnÃƒÂ© (via query string) ou dernier rÃƒÂ©gime par dÃƒÂ©faut
        $activeRegimeId = $request->query->getInt('regime_id', 0);
        $regimeRepo = $em->getRepository(RegimeAlimentaire::class);

        $regime = null;
        if ($activeRegimeId > 0) {
            $regime = $regimeRepo->findOneBy([
                'id' => $activeRegimeId,
                'user' => $user,
            ]);
        }
        if (!$regime) {
            $regime = $regimeRepo->findOneBy(
                ['user' => $user],
                ['id' => 'DESC']
            );
        }

        $regimeForm = null;
        $editForms = [];
        $regimes = $regimeRepo->findBy(
            ['user' => $user],
            ['id' => 'DESC']
        );

        $newRegime = new RegimeAlimentaire();
        $newRegime->setUser($user);
        $createForm = $this->createForm(RegimeAlimentaireFormType::class, $newRegime);
        $createForm->handleRequest($request);
        if ($createForm->isSubmitted() && $createForm->isValid()) {
            $r = $createForm->getData();
            $r->setUser($user);
            if ($r->getTaille() && $r->getPoids()) {
                $tM = $r->getTaille() / 100;
                $bmi = $tM > 0 ? round($r->getPoids() / ($tM * $tM), 2) : null;
                $r->setBmi($bmi);
            }
            $em->persist($r);
            $em->flush();
            $this->addFlash('success', 'RÃƒÂ©gime crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s.');
            return $this->redirectToRoute('diet_planner');
        }

        foreach ($regimes as $r) {
            $editForms[$r->getId()] = $this->createForm(RegimeAlimentaireFormType::class, $r, [
                'action' => $this->generateUrl('diet_regime_edit', ['id' => $r->getId()]),
            ])->createView();
        }

        // Tous les repas de la base (catalogue global pour sÃƒÂ©lection rapide)
        $catalogMeals = $em->getRepository(Repas::class)->findBy(
            [],
            ['nomRepas' => 'ASC']
        );

        $todayStart = (new \DateTimeImmutable('today'))->setTime(0, 0);
        $todayEnd = (new \DateTimeImmutable('today'))->setTime(23, 59, 59);
        $repasQb = $em->getRepository(Repas::class)->createQueryBuilder('r')
            ->where('r.user = :user')
            ->andWhere('r.dateRepas BETWEEN :start AND :end')
            ->setParameter('user', $user)
            ->setParameter('start', $todayStart)
            ->setParameter('end', $todayEnd)
            ->orderBy('r.dateRepas', 'DESC');

        // Si un rÃƒÂ©gime est actif, on ne montre que les repas associÃƒÂ©s ÃƒÂ  ce rÃƒÂ©gime
        if ($regime) {
            $repasQb->andWhere('r.regime = :regime')
                ->setParameter('regime', $regime);
        }

        $repas = $repasQb->getQuery()->getResult();

        $totals = [
            'calories' => 0,
            'proteines' => 0,
            'glucides' => 0,
            'lipides' => 0,
        ];
        foreach ($repas as $r) {
            $totals['calories'] += (int) ($r->getCalories() ?? 0);
            $totals['proteines'] += (int) ($r->getProteines() ?? 0);
            $totals['glucides'] += (int) ($r->getGlucides() ?? 0);
            $totals['lipides'] += (int) ($r->getLipides() ?? 0);
        }

        // Plan quotidien de repas (calendrier) pour le rÃƒÂ©gime actif : 7 derniers jours
        $dailyPlan = [];
        $todayBadges = null;
        $checklist = [
            'breakfast' => false,
            'lunch' => false,
            'dinner' => false,
            'snack' => false,
            'hydration' => false,
        ];

        if ($regime) {
            $mealRepo = $em->getRepository(Repas::class);
            $today = new \DateTimeImmutable('today');
            $target = (int) ($regime->getCaloriesCibles() ?? 0);
            $lower = $target > 0 ? (int) round($target * 0.9) : 0;
            $upper = $target > 0 ? (int) round($target * 1.1) : 0;

            // On remplit de J-6 ÃƒÂ  J (ordre chronologique)
            for ($i = 6; $i >= 0; $i--) {
                $day = $today->sub(new \DateInterval('P'.$i.'D'));
                $dayStart = $day->setTime(0, 0);
                $dayEnd = $day->setTime(23, 59, 59);

                $dayMeals = $mealRepo->createQueryBuilder('r')
                    ->where('r.user = :user')
                    ->andWhere('r.regime = :regime')
                    ->andWhere('r.dateRepas BETWEEN :start AND :end')
                    ->setParameter('user', $user)
                    ->setParameter('regime', $regime)
                    ->setParameter('start', $dayStart)
                    ->setParameter('end', $dayEnd)
                    ->orderBy('r.dateRepas', 'ASC')
                    ->getQuery()
                    ->getResult();

                $dayTotals = [
                    'calories' => 0,
                    'proteines' => 0,
                    'glucides' => 0,
                    'lipides' => 0,
                ];
                foreach ($dayMeals as $m) {
                    $dayTotals['calories'] += (int) ($m->getCalories() ?? 0);
                    $dayTotals['proteines'] += (int) ($m->getProteines() ?? 0);
                    $dayTotals['glucides'] += (int) ($m->getGlucides() ?? 0);
                    $dayTotals['lipides'] += (int) ($m->getLipides() ?? 0);
                }

                $hasMeals = \count($dayMeals) > 0;
                $caloriesOk = $target > 0 && $hasMeals && $dayTotals['calories'] >= $lower && $dayTotals['calories'] <= $upper;
                // Heuristique simple hydratation: au moins 3 prises (repas/collations)
                $hydrationOk = $hasMeals && \count($dayMeals) >= 3;

                $dailyPlan[] = [
                    'date' => $day,
                    'meals' => $dayMeals,
                    'totals' => $dayTotals,
                    'badges' => [
                        'calories_ok' => $caloriesOk,
                        'hydration_ok' => $hydrationOk,
                    ],
                ];

                // Badges du jour (i === 0 => aujourd'hui)
                if ($i === 0) {
                    $todayBadges = [
                        'calories_ok' => $caloriesOk,
                        'hydration_ok' => $hydrationOk,
                        'totals' => $dayTotals,
                    ];
                }
            }
        }

        // Checklist du jour basÃƒÂ©e sur les repas du jour (quel que soit le rÃƒÂ©gime)
        $hasBreakfast = false;
        $hasLunch = false;
        $hasDinner = false;
        $hasSnack = false;
        foreach ($repas as $meal) {
            $type = strtolower($meal->getTypeRepas());
            if (str_contains($type, 'petit') || str_contains($type, 'breakfast')) {
                $hasBreakfast = true;
            } elseif (str_contains($type, 'dÃƒÂ©j') || str_contains($type, 'dej') || str_contains($type, 'lunch')) {
                $hasLunch = true;
            } elseif (str_contains($type, 'dÃƒÂ®ner') || str_contains($type, 'diner') || str_contains($type, 'dinner') || str_contains($type, 'soir')) {
                $hasDinner = true;
            } else {
                $hasSnack = true;
            }
        }
        $checklist['breakfast'] = $hasBreakfast;
        $checklist['lunch'] = $hasLunch;
        $checklist['dinner'] = $hasDinner;
        $checklist['snack'] = $hasSnack;
        $checklist['hydration'] = $todayBadges['hydration_ok'] ?? (\count($repas) >= 3);

        return $this->render('pages/diet-planner.html.twig', [
            'regime' => $regime,
            'regimes' => $regimes,
            'regimeCreateForm' => $createForm->createView(),
            'regimeEditForms' => $editForms,
            'repas' => $repas,
            'catalogMeals' => $catalogMeals,
            'totals' => $totals,
            'dailyPlan' => $dailyPlan,
            'todayBadges' => $todayBadges,
            'checklist' => $checklist,
        ]);
    }

    #[Route('/diet-eating-planner/regime/{id}/edit', name: 'diet_regime_edit', methods: ['POST'])]
    public function editRegime(RegimeAlimentaire $regime, Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user || $regime->getUser()?->getId() !== $user->getId()) {
            return $this->redirectToRoute('diet_planner');
        }

        $form = $this->createForm(RegimeAlimentaireFormType::class, $regime);
        $form->handleRequest($request);
        if ($form->isSubmitted() && $form->isValid()) {
            $r = $form->getData();
            if ($r->getTaille() && $r->getPoids()) {
                $tM = $r->getTaille() / 100;
                $bmi = $tM > 0 ? round($r->getPoids() / ($tM * $tM), 2) : null;
                $r->setBmi($bmi);
            }
            $em->flush();
            $this->addFlash('success', 'RÃƒÂ©gime mis ÃƒÂ  jour.');
            return $this->redirectToRoute('diet_planner');
        }
        return $this->redirectToRoute('diet_planner');
    }

    #[Route('/diet-eating-planner/regime/{id}/delete', name: 'diet_regime_delete', methods: ['POST'])]
    public function deleteRegime(RegimeAlimentaire $regime, Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user || $regime->getUser()?->getId() !== $user->getId()) {
            return $this->redirectToRoute('diet_planner');
        }

        if (!$this->isCsrfTokenValid('regime_delete_'.$regime->getId(), (string) $request->request->get('_token'))) {
            return $this->redirectToRoute('diet_planner');
        }

        $meals = $em->getRepository(Repas::class)->findBy(['regime' => $regime]);
        foreach ($meals as $m) {
            $m->setRegime(null);
        }
        $em->remove($regime);
        $em->flush();
        $this->addFlash('success', 'RÃƒÂ©gime supprimÃƒÂ©.');
        return $this->redirectToRoute('diet_planner');
    }

    #[Route('/diet-eating-planner/meal/add', name: 'diet_meal_add', methods: ['POST'])]
    public function addMeal(Request $request, EntityManagerInterface $em, DailyGoalMailerService $dailyGoalMailer): Response
    {
        $user = $this->getUser();
        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        if (!$this->isCsrfTokenValid('meal_add', (string) $request->request->get('_token'))) {
            return $this->redirectToRoute('diet_planner');
        }

        $requestedRegimeId = $request->request->getInt('regime_id', 0);
        $regime = null;
        if ($requestedRegimeId > 0) {
            $regime = $em->getRepository(RegimeAlimentaire::class)->findOneBy([
                'id' => $requestedRegimeId,
                'user' => $user,
            ]);
        }
        if (!$regime) {
            $regime = $em->getRepository(RegimeAlimentaire::class)->findOneBy(
                ['user' => $user],
                ['id' => 'DESC']
            );
        }

        // Si un repas existant est sÃƒÂ©lectionnÃƒÂ©, on le rÃƒÂ©utilise comme modÃƒÂ¨le
        $sourceId = $request->request->getInt('source_repas_id') ?: null;
        $calories = 0;
        if ($sourceId) {
            /** @var Repas|null $source */
            $source = $em->getRepository(Repas::class)->find($sourceId);
            if (!$source) {
                $this->addFlash('error', 'Repas sÃƒÂ©lectionnÃƒÂ© invalide.');
                return $this->redirectToRoute('diet_planner');
            }
            $calories = (int) ($source->getCalories() ?? 0);
        } else {
            $calories = $request->request->getInt('calories') ?: 0;
        }

        $currentTotal = 0;
        $wouldExceedTarget = false;

        // VÃƒÂ©rification cÃƒÂ´tÃƒÂ© serveur : ne pas dÃƒÂ©passer les calories cibles du rÃƒÂ©gime du jour
        if ($regime && $regime->getCaloriesCibles()) {
            $todayStart = (new \DateTimeImmutable('today'))->setTime(0, 0);
            $todayEnd = (new \DateTimeImmutable('today'))->setTime(23, 59, 59);

            $qb = $em->getRepository(Repas::class)->createQueryBuilder('r')
                ->select('COALESCE(SUM(r.calories), 0)')
                ->where('r.user = :user')
                ->andWhere('r.dateRepas BETWEEN :start AND :end')
                ->setParameter('user', $user)
                ->setParameter('start', $todayStart)
                ->setParameter('end', $todayEnd);

            if ($regime) {
                $qb->andWhere('r.regime = :regime')
                    ->setParameter('regime', $regime);
            }

            $currentTotal = (int) $qb->getQuery()->getSingleScalarResult();
            $wouldExceedTarget = ($currentTotal + $calories) > (int) $regime->getCaloriesCibles();
        }

        $meal = new Repas();
        $meal->setUser($user);

        if (isset($source) && $source instanceof Repas) {
            // On duplique les infos du repas existant
            $meal->setTypeRepas($source->getTypeRepas());
            $meal->setNomRepas($source->getNomRepas());
            $meal->setCalories($source->getCalories());
            $meal->setProteines($source->getProteines());
            $meal->setGlucides($source->getGlucides());
            $meal->setLipides($source->getLipides());
            $meal->setCommentaire($source->getCommentaire());
        } else {
            // Fallback: creation classique depuis le formulaire libre
            $typeRepas = trim((string) $request->request->get('type_repas'));
            $nomRepas = trim((string) $request->request->get('nom_repas'));
            if ($typeRepas === '' || $nomRepas === '') {
                $this->addFlash('error', 'Type de repas et nom du repas sont obligatoires.');
                if ($requestedRegimeId > 0) {
                    return $this->redirectToRoute('diet_planner', ['regime_id' => $requestedRegimeId]);
                }
                return $this->redirectToRoute('diet_planner');
            }
            $meal->setTypeRepas($typeRepas);
            $meal->setNomRepas($nomRepas);
            $meal->setCalories($calories ?: null);
            $meal->setProteines($request->request->getInt('proteines') ?: null);
            $meal->setGlucides($request->request->getInt('glucides') ?: null);
            $meal->setLipides($request->request->getInt('lipides') ?: null);
            $meal->setCommentaire((string) $request->request->get('commentaire') ?: null);
        }

        $meal->setRegime($regime);

        try {
            $em->persist($meal);
            $em->flush();
        } catch (\Throwable $e) {
            $this->addFlash('error', 'Echec enregistrement repas (BDD): '.$e->getMessage());
            if ($requestedRegimeId > 0) {
                return $this->redirectToRoute('diet_planner', ['regime_id' => $requestedRegimeId]);
            }
            return $this->redirectToRoute('diet_planner');
        }

        $this->addFlash('success', 'Repas ajoute avec succes (ID: '.$meal->getId().').');

        if ($regime && $regime->getCaloriesCibles()) {
            $target = (int) $regime->getCaloriesCibles();
            $newTotal = $currentTotal + (int) $calories;
            if ($currentTotal < $target && $newTotal >= $target) {
                try {
                    $dailyGoalMailer->sendGoalReachedSheet($user, $regime, $currentTotal, $newTotal);
                    $this->addFlash('success', 'Mail envoye via API a bhemna05@gmail.com : fiche nutrition professionnelle.');
                } catch (\Throwable $e) {
                    $this->addFlash('error', 'Repas ajoute, mais l\'API mail a echoue: '.$e->getMessage());
                }
            } else {
                $this->addFlash('info', 'Repas ajoute. Email non envoye car objectif journalier pas encore atteint.');
            }
        }

        if ($wouldExceedTarget) {
            $this->addFlash('warning', 'Repas ajoute, mais vous depassez la cible calorique du jour.');
        }

        if ($requestedRegimeId > 0) {
            return $this->redirectToRoute('diet_planner', ['regime_id' => $requestedRegimeId]);
        }
        return $this->redirectToRoute('diet_planner');
    }

    #[Route('/diet-eating-planner/shopping-list', name: 'diet_shopping_list', methods: ['GET'])]
    public function shoppingList(Request $request, EntityManagerInterface $em): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'unauthorized'], 401);
        }

        $todayStart = (new \DateTimeImmutable('today'))->setTime(0, 0);
        $todayEnd = (new \DateTimeImmutable('today'))->setTime(23, 59, 59);

        $activeRegimeId = $request->query->getInt('regime_id', 0);
        $regime = null;
        if ($activeRegimeId > 0) {
            $regime = $em->getRepository(RegimeAlimentaire::class)->findOneBy([
                'id' => $activeRegimeId,
                'user' => $user,
            ]);
        }

        $qb = $em->getRepository(Repas::class)->createQueryBuilder('r')
            ->where('r.user = :user')
            ->andWhere('r.dateRepas BETWEEN :start AND :end')
            ->setParameter('user', $user)
            ->setParameter('start', $todayStart)
            ->setParameter('end', $todayEnd)
            ->orderBy('r.dateRepas', 'DESC');

        if ($regime) {
            $qb->andWhere('r.regime = :regime')
               ->setParameter('regime', $regime);
        }

        /** @var Repas[] $meals */
        $meals = $qb->getQuery()->getResult();

        $ingredientsMap = [];
        $totals = [
            'calories' => 0,
            'proteines' => 0,
            'glucides' => 0,
            'lipides' => 0,
        ];

        foreach ($meals as $meal) {
            $totals['calories'] += (int) ($meal->getCalories() ?? 0);
            $totals['proteines'] += (int) ($meal->getProteines() ?? 0);
            $totals['glucides'] += (int) ($meal->getGlucides() ?? 0);
            $totals['lipides'] += (int) ($meal->getLipides() ?? 0);

            $comment = $meal->getCommentaire();
            if (!$comment) {
                continue;
            }

            $parts = preg_split('/[,;\n]+/', $comment);
            if (!is_array($parts)) {
                continue;
            }

            foreach ($parts as $raw) {
                $name = trim(mb_strtolower($raw));
                if ($name === '') {
                    continue;
                }
                $name = preg_replace('/\s+/', ' ', $name);
                $ingredientsMap[$name] = ($ingredientsMap[$name] ?? 0) + 1;
            }
        }

        $items = [];
        foreach ($ingredientsMap as $name => $count) {
            $items[] = [
                'name' => $name,
                'count' => $count,
            ];
        }

        usort($items, static function (array $a, array $b): int {
            return strcmp($a['name'], $b['name']);
        });

        return new JsonResponse([
            'items' => $items,
            'totals' => $totals,
            'regimeType' => $regime ? $regime->getTypeSante() : null,
        ]);
    }

    #[Route('/diet-eating-planner/meal/{id}/update', name: 'diet_meal_update', methods: ['POST'])]
    public function updateMeal(Repas $meal, Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user || $meal->getUser()?->getId() !== $user->getId()) {
            return $this->redirectToRoute('diet_planner');
        }

        if (!$this->isCsrfTokenValid('meal_update_'.$meal->getId(), (string) $request->request->get('_token'))) {
            return $this->redirectToRoute('diet_planner');
        }

        $meal->setTypeRepas((string) $request->request->get('type_repas'));
        $meal->setNomRepas((string) $request->request->get('nom_repas'));
        $meal->setCalories($request->request->getInt('calories') ?: null);
        $meal->setProteines($request->request->getInt('proteines') ?: null);
        $meal->setGlucides($request->request->getInt('glucides') ?: null);
        $meal->setLipides($request->request->getInt('lipides') ?: null);
        $meal->setCommentaire((string) $request->request->get('commentaire') ?: null);
        $em->flush();

        return $this->redirectToRoute('diet_planner');
    }

    #[Route('/diet-eating-planner/meal/{id}/delete', name: 'diet_meal_delete', methods: ['POST'])]
    public function deleteMeal(Repas $meal, Request $request, EntityManagerInterface $em): Response
    {
        $user = $this->getUser();
        if (!$user || $meal->getUser()?->getId() !== $user->getId()) {
            return $this->redirectToRoute('diet_planner');
        }

        if (!$this->isCsrfTokenValid('meal_delete_'.$meal->getId(), (string) $request->request->get('_token'))) {
            return $this->redirectToRoute('diet_planner');
        }

        $em->remove($meal);
        $em->flush();
        return $this->redirectToRoute('diet_planner');
    }

    #[Route('/diet-eating-planner/food-recognition', name: 'diet_food_recognition', methods: ['POST'])]
    public function foodRecognition(
        Request $request,
        FoodRecognitionService $foodRecognition
    ): JsonResponse {
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'unauthorized'], 401);
        }

        if (!$this->isCsrfTokenValid('food_recognition', (string) $request->request->get('_token'))) {
            return new JsonResponse(['error' => 'invalid_csrf'], 400);
        }

        $file = $request->files->get('image');
        if (!$file) {
            return new JsonResponse(['error' => 'missing_image'], 400);
        }

        $tmpPath = $file->getPathname();
        try {
            $result = $foodRecognition->analyzeImage($tmpPath);
            return new JsonResponse(['ok' => true] + $result);
        } catch (\Throwable $e) {
            return new JsonResponse(['error' => $e->getMessage()], 400);
        }
    }

    #[Route('/diet-eating-planner/chat', name: 'diet_ai_chat', methods: ['POST'])]
    public function dietAiChat(Request $request): JsonResponse
    {
        $user = $this->getUser();
        if (!$user) {
            return new JsonResponse(['error' => 'unauthorized'], 401);
        }

        $payload = json_decode((string) $request->getContent(), true);
        if (!is_array($payload)) {
            return new JsonResponse(['error' => 'invalid_json'], 400);
        }

        if (!$this->isCsrfTokenValid('diet_ai_chat', (string) ($payload['_token'] ?? ''))) {
            return new JsonResponse(['error' => 'invalid_csrf'], 400);
        }

        $message = trim((string) ($payload['message'] ?? ''));
        if ($message === '') {
            return new JsonResponse(['error' => 'empty_message'], 400);
        }

        try {
            $reply = $this->dietChatService->reply($message, [
                'first_name' => method_exists($user, 'getFirstName') ? $user->getFirstName() : '',
            ]);

            return new JsonResponse([
                'ok' => true,
                'reply' => $reply,
            ]);
        } catch (\RuntimeException $e) {
            return new JsonResponse(['error' => $e->getMessage()], 400);
        } catch (\Throwable) {
            return new JsonResponse(['error' => 'internal_error'], 500);
        }
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
        $dmStatusLine = null;
        $dmUnreadCounts = [];
        $dmLastMessage = [];
        $dmLastMessageTime = [];
        $dmUsers = [];
        $dmUnreadTotal = 0;
        $dmUnreadSenders = [];

        if ($user) {
            $dmParticipants = $em->getRepository(DmParticipant::class)->findBy(['user' => $user]);
            $dmConversations = [];
            $conversationById = [];
            foreach ($dmParticipants as $participant) {
                $conv = $participant->getConversation();
                if (!$conv) {
                    continue;
                }
                $cid = $conv->getId();
                if (isset($conversationById[$cid])) {
                    continue;
                }
                $conversationById[$cid] = $conv;
                $dmConversations[] = $conv;
            }
            usort($dmConversations, static function (DmConversation $a, DmConversation $b) {
                $aTime = $a->getLastMessageAt()?->getTimestamp() ?? 0;
                $bTime = $b->getLastMessageAt()?->getTimestamp() ?? 0;
                return $bTime <=> $aTime;
            });

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
                $dmUnreadTotal += $dmUnreadCounts[$conv->getId()];
                if ($other && $dmUnreadCounts[$conv->getId()] > 0) {
                    $oid = $other->getId();
                    if (!isset($dmUnreadSenders[$oid])) {
                        $name = trim($other->getFirstName().' '.$other->getLastName());
                        if ($name === '') {
                            $name = $other->getUsername() ?: $other->getEmail();
                        }
                        $dmUnreadSenders[$oid] = [
                            'id' => $oid,
                            'name' => $name,
                            'email' => $other->getEmail(),
                            'avatar' => $other->getAvatar(),
                        ];
                    }
                }

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

                $lastMessage = $em->getRepository(DmMessage::class)->findOneBy(
                    ['conversation' => $dmSelectedConversation],
                    ['createdAt' => 'DESC']
                );

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

                $otherLastReadAt = null;
                foreach ($dmSelectedConversation->getParticipants() as $p) {
                    if ($p->getUser()?->getId() === $user->getId()) {
                        $p->setLastReadAt(new \DateTimeImmutable());
                    } elseif ($p->getUser()) {
                        $otherLastReadAt = $p->getLastReadAt();
                    }
                }

                if ($lastMessage) {
                    if ($lastMessage->getSender()?->getId() === $user->getId()) {
                        if ($otherLastReadAt && $otherLastReadAt >= $lastMessage->getCreatedAt()) {
                            $dmStatusLine = 'Seen '.$this->formatAgo($otherLastReadAt);
                        } else {
                            $dmStatusLine = 'Sent '.$this->formatAgo($lastMessage->getCreatedAt());
                        }
                    } else {
                        $dmStatusLine = 'Received '.$this->formatAgo($lastMessage->getCreatedAt());
                    }
                } else {
                    $dmStatusLine = 'Active now';
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
            'dmStatusLine' => $dmStatusLine,
            'dmUnreadCounts' => $dmUnreadCounts,
            'dmLastMessage' => $dmLastMessage,
            'dmLastMessageTime' => $dmLastMessageTime ?? [],
            'dmUsers' => $dmUsers,
            'dmUnreadTotal' => $dmUnreadTotal,
            'dmUnreadSenders' => array_values($dmUnreadSenders),
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

    private function formatAgo(\DateTimeImmutable $time): string
    {
        $diff = (new \DateTimeImmutable())->getTimestamp() - $time->getTimestamp();
        if ($diff < 60) {
            return 'just now';
        }
        $mins = (int) floor($diff / 60);
        if ($mins < 60) {
            return $mins.' min ago';
        }
        $hours = (int) floor($mins / 60);
        if ($hours < 24) {
            return $hours.' h ago';
        }
        $days = (int) floor($hours / 24);
        return $days.' d ago';
    }

    private function fitnessTablesExist(EntityManagerInterface $em): bool
    {
        try {
            $tables = $em->getConnection()->createSchemaManager()->listTableNames();
            return in_array('fitness_program', $tables, true)
                && in_array('fitness_exercise', $tables, true)
                && in_array('fitness_program_exercise', $tables, true);
        } catch (\Throwable) {
            return false;
        }
    }

    private function fitnessPlansTableExist(EntityManagerInterface $em): bool
    {
        try {
            $tables = $em->getConnection()->createSchemaManager()->listTableNames();
            return in_array('fitness_plan', $tables, true);
        } catch (\Throwable) {
            return false;
        }
    }

    private function resolveProgramForPlan(string $programLabel, EntityManagerInterface $em): ?FitnessProgram
    {
        if ($programLabel === '') {
            return null;
        }

        $byTitle = $em->getRepository(FitnessProgram::class)->findOneBy(['title' => $programLabel]);
        if ($byTitle instanceof FitnessProgram) {
            return $byTitle;
        }

        return $em->getRepository(FitnessProgram::class)
            ->createQueryBuilder('p')
            ->where('p.category = :category')
            ->setParameter('category', $programLabel)
            ->orderBy('p.id', 'DESC')
            ->setMaxResults(1)
            ->getQuery()
            ->getOneOrNullResult();
    }

    private function serializeFitnessPlan(FitnessPlan $plan): array
    {
        return [
            'id' => $plan->getId(),
            'title' => $plan->getTitle() ?? '',
            'program' => $plan->getProgram()?->getTitle() ?? '',
            'place' => $plan->getPlace() ?? '',
            'estimatedMinutes' => $plan->getEstimatedMinutes() ?? 0,
            'exercises' => $plan->getExercisesData(),
        ];
    }

    private function buildExerciseImageMap(array $exercises): array
    {
        $map = [];
        foreach ($exercises as $exercise) {
            $name = $exercise->getName();
            $imageUrl = $exercise->getImageUrl();
            if ($name !== null && $imageUrl !== null && $imageUrl !== '') {
                $map[$name] = $imageUrl;
            }
        }
        return $map;
    }

    private function handleFitnessPlannerPost(Request $request, EntityManagerInterface $em): void
    {
        $action = (string) $request->request->get('action');

        if ($action === 'create_program' || $action === 'update_program') {
            $program = $action === 'update_program'
                ? $em->getRepository(FitnessProgram::class)->find($request->request->getInt('id'))
                : new FitnessProgram();

            if (!$program) {
                $this->addFlash('error', 'Program not found.');
                return;
            }

            $title = trim((string) $request->request->get('title'));
            $category = trim((string) $request->request->get('category'));
            $level = trim((string) $request->request->get('level'));
            $sessionDuration = max(1, $request->request->getInt('session_duration', 45));
            $durationWeeks = max(1, $request->request->getInt('duration_weeks', 4));
            $sessionsPerWeek = max(1, $request->request->getInt('sessions_per_week', 3));

            if ($title === '' || $category === '' || $level === '') {
                $this->addFlash('error', 'Program title, category and level are required.');
                return;
            }

            $program->setTitle($title);
            $program->setCategory($category);
            $program->setLevel($level);
            $program->setDescription(trim((string) $request->request->get('description')) ?: null);
            $program->setSessionDuration($sessionDuration);
            $program->setDurationWeeks($durationWeeks);
            $program->setSessionsPerWeek($sessionsPerWeek);
            $program->setImageUrl(trim((string) $request->request->get('image_url')) ?: null);
            $program->setVideoUrl(trim((string) $request->request->get('video_url')) ?: null);
            $program->setIsPublic($request->request->getBoolean('is_public', true));

            foreach ($program->getExercises()->toArray() as $exercise) {
                $program->removeExercise($exercise);
            }
            $selectedExercises = $request->request->all('exercise_ids');
            foreach ($selectedExercises as $exerciseId) {
                $exercise = $em->getRepository(FitnessExercise::class)->find((int) $exerciseId);
                if ($exercise) {
                    $program->addExercise($exercise);
                }
            }

            $em->persist($program);
            $em->flush();
            $this->addFlash('success', $action === 'create_program' ? 'Program created.' : 'Program updated.');
            return;
        }

        if ($action === 'delete_program') {
            $program = $em->getRepository(FitnessProgram::class)->find($request->request->getInt('id'));
            if (!$program) {
                $this->addFlash('error', 'Program not found.');
                return;
            }
            $em->remove($program);
            $em->flush();
            $this->addFlash('success', 'Program deleted.');
            return;
        }

        if ($action === 'create_exercise' || $action === 'update_exercise') {
            $exercise = $action === 'update_exercise'
                ? $em->getRepository(FitnessExercise::class)->find($request->request->getInt('id'))
                : new FitnessExercise();

            if (!$exercise) {
                $this->addFlash('error', 'Exercise not found.');
                return;
            }

            $name = trim((string) $request->request->get('name'));
            $group = trim((string) $request->request->get('muscle_group'));
            $difficulty = trim((string) $request->request->get('difficulty'));

            if ($name === '' || $group === '' || $difficulty === '') {
                $this->addFlash('error', 'Exercise name, muscle group and difficulty are required.');
                return;
            }

            $exercise->setName($name);
            $exercise->setMuscleGroup($group);
            $exercise->setDifficulty($difficulty);
            $exercise->setDescription(trim((string) $request->request->get('description')) ?: null);
            $exercise->setSets($request->request->getInt('sets') ?: null);
            $exercise->setRepetitions($request->request->getInt('repetitions') ?: null);
            $exercise->setDuration($request->request->getInt('duration') ?: null);
            $exercise->setImageUrl(trim((string) $request->request->get('image_url')) ?: null);
            $exercise->setVideoUrl(trim((string) $request->request->get('video_url')) ?: null);

            $em->persist($exercise);
            $em->flush();
            $this->addFlash('success', $action === 'create_exercise' ? 'Exercise created.' : 'Exercise updated.');
            return;
        }

        if ($action === 'delete_exercise') {
            $exercise = $em->getRepository(FitnessExercise::class)->find($request->request->getInt('id'));
            if (!$exercise) {
                $this->addFlash('error', 'Exercise not found.');
                return;
            }
            $em->remove($exercise);
            $em->flush();
            $this->addFlash('success', 'Exercise deleted.');
        }
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

    // ── Performance Tracking ─────────────────────────────────────────────────

    #[Route('/performance/exercises', name: 'performance_exercises', methods: ['GET'])]
    public function performanceExercises(EntityManagerInterface $em): JsonResponse
    {
        if (!$this->getUser() instanceof User) {
            return new JsonResponse(['error' => 'unauthenticated'], 401);
        }
        $userId = $this->getUser()->getId();
        $exercises = $em->getRepository(ExerciseCatalog::class)->findForUser($userId);

        $data = array_map(fn(ExerciseCatalog $e) => [
            'id'          => $e->getId(),
            'name'        => $e->getName(),
            'muscleGroup' => $e->getMuscleGroup(),
            'equipment'   => $e->getEquipment(),
            'isCustom'    => $e->isCustom(),
        ], $exercises);

        return new JsonResponse($data);
    }

    #[Route('/performance/log', name: 'performance_log', methods: ['POST'])]
    public function performanceLog(Request $request, EntityManagerInterface $em): JsonResponse
    {
        if (!$this->getUser() instanceof User) {
            return new JsonResponse(['error' => 'unauthenticated'], 401);
        }

        $payload = json_decode($request->getContent(), true);
        if (!$payload) {
            return new JsonResponse(['error' => 'invalid_json'], 400);
        }

        $user = $this->getUser();

        // Créer ou récupérer la session du jour
        $dateStr = $payload['date'] ?? date('Y-m-d');
        $date    = new \DateTimeImmutable($dateStr);

        $session = new TrainingSession();
        $session->setUser($user);
        $session->setSessionDate($date);
        $session->setLabel($payload['label'] ?? null);
        $session->setNotes($payload['notes'] ?? null);
        $session->setDurationMin(isset($payload['durationMin']) ? (int) $payload['durationMin'] : null);

        $order = 1;
        foreach (($payload['exercises'] ?? []) as $exData) {
            $exercise = $em->getRepository(ExerciseCatalog::class)->find((int) ($exData['exerciseId'] ?? 0));
            if (!$exercise) continue;

            $log = new PerformanceLog();
            $log->setExercise($exercise);
            $log->setExerciseOrder($order++);
            $log->setRestSeconds(isset($exData['restSeconds']) ? (int) $exData['restSeconds'] : null);
            $log->setNotes($exData['notes'] ?? null);

            foreach (($exData['sets'] ?? []) as $i => $setData) {
                $set = new PerformanceSet();
                $set->setSetNumber($i + 1);
                $set->setWeightKg(isset($setData['weightKg']) && $setData['weightKg'] !== '' ? (float) $setData['weightKg'] : null);
                $set->setRepetitions(isset($setData['reps']) && $setData['reps'] !== '' ? (int) $setData['reps'] : null);
                $set->setDurationSec(isset($setData['durationSec']) ? (int) $setData['durationSec'] : null);
                $set->setRpe(isset($setData['rpe']) ? (int) $setData['rpe'] : null);
                $set->setIsWarmup((bool) ($setData['isWarmup'] ?? false));
                $log->addSet($set);
                $em->persist($set);
            }

            $session->addPerformanceLog($log);
            $em->persist($log);
        }

        $em->persist($session);
        $em->flush();

        return new JsonResponse(['ok' => true, 'sessionId' => $session->getId()]);
    }

    #[Route('/performance/progression/{exerciseId}', name: 'performance_progression', methods: ['GET'])]
    public function performanceProgression(int $exerciseId, EntityManagerInterface $em): JsonResponse
    {
        if (!$this->getUser() instanceof User) {
            return new JsonResponse(['error' => 'unauthenticated'], 401);
        }

        $userId = $this->getUser()->getId();
        $data   = $em->getRepository(PerformanceLog::class)->getWeeklyProgression($userId, $exerciseId);

        return new JsonResponse($data);
    }

    #[Route('/performance/history', name: 'performance_history', methods: ['GET'])]
    public function performanceHistory(EntityManagerInterface $em): JsonResponse
    {
        if (!$this->getUser() instanceof User) {
            return new JsonResponse(['error' => 'unauthenticated'], 401);
        }

        $userId   = $this->getUser()->getId();
        $sessions = $em->getRepository(TrainingSession::class)->findByUser($userId, 30);

        $data = array_map(fn(TrainingSession $s) => [
            'id'          => $s->getId(),
            'date'        => $s->getSessionDate()->format('Y-m-d'),
            'week'        => 'S' . $s->getWeekNumber() . ' ' . $s->getYear(),
            'label'       => $s->getLabel() ?? $s->getSessionDate()->format('d/m/Y'),
            'durationMin' => $s->getDurationMin(),
            'exercises'   => $s->getPerformanceLogs()->map(fn(PerformanceLog $l) => [
                'name'        => $l->getExercise()->getName(),
                'muscleGroup' => $l->getExercise()->getMuscleGroup(),
                'maxWeight'   => $l->getMaxWeight(),
                'totalVolume' => $l->getTotalVolume(),
                'nbSets'      => $l->getSets()->count(),
            ])->toArray(),
        ], $sessions);

        return new JsonResponse($data);
    }
}




