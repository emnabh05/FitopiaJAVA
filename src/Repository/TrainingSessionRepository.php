<?php

namespace App\Repository;

use App\Entity\TrainingSession;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

class TrainingSessionRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, TrainingSession::class);
    }

    public function findByUser(int $userId, int $limit = 20): array
    {
        return $this->createQueryBuilder('s')
            ->where('s.user = :uid')
            ->setParameter('uid', $userId)
            ->orderBy('s.sessionDate', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()->getResult();
    }
}
