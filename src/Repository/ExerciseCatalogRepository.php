<?php

namespace App\Repository;

use App\Entity\ExerciseCatalog;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class ExerciseCatalogRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, ExerciseCatalog::class);
    }

    /** Retourne les exercices globaux + ceux créés par l'user */
    public function findForUser(?int $userId): array
    {
        $qb = $this->createQueryBuilder('e')
            ->orderBy('e.muscleGroup', 'ASC')
            ->addOrderBy('e.name', 'ASC');

        if ($userId) {
            $qb->where('e.user IS NULL OR e.user = :uid')
               ->setParameter('uid', $userId);
        } else {
            $qb->where('e.user IS NULL');
        }

        return $qb->getQuery()->getResult();
    }
}
