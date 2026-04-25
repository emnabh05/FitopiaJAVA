<?php

namespace App\Repository;

use App\Entity\PerformanceLog;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class PerformanceLogRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, PerformanceLog::class);
    }

    /**
     * Progression hebdomadaire d'un exercice pour un user.
     * Retourne : [['week' => 'S17 2026', 'max_weight' => 15.0, 'total_volume' => 1200.0], ...]
     */
    public function getWeeklyProgression(int $userId, int $exerciseId): array
    {
        $rows = $this->getEntityManager()->getConnection()->fetchAllAssociative('
            SELECT
                ts.year,
                ts.week_number,
                MIN(ts.session_date)                        AS date_semaine,
                MAX(ps.weight_kg)                           AS max_weight,
                ROUND(SUM(ps.weight_kg * ps.repetitions),1) AS total_volume,
                SUM(ps.repetitions)                         AS total_reps,
                COUNT(ps.id)                                AS nb_sets
            FROM performance_set ps
            JOIN performance_log pl ON pl.id = ps.log_id
            JOIN training_session ts ON ts.id = pl.session_id
            WHERE ts.user_id     = :userId
              AND pl.exercise_id = :exerciseId
              AND ps.is_warmup   = 0
              AND ps.weight_kg   IS NOT NULL
            GROUP BY ts.year, ts.week_number
            ORDER BY ts.year ASC, ts.week_number ASC
        ', ['userId' => $userId, 'exerciseId' => $exerciseId]);

        return array_map(fn($r) => [
            'week'         => 'S' . $r['week_number'] . ' ' . $r['year'],
            'date'         => $r['date_semaine'],
            'max_weight'   => (float) ($r['max_weight'] ?? 0),
            'total_volume' => (float) ($r['total_volume'] ?? 0),
            'total_reps'   => (int)   ($r['total_reps'] ?? 0),
            'nb_sets'      => (int)   ($r['nb_sets'] ?? 0),
        ], $rows);
    }
}
