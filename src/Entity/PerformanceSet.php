<?php

namespace App\Entity;

use App\Repository\PerformanceSetRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PerformanceSetRepository::class)]
#[ORM\Table(name: 'performance_set')]
class PerformanceSet
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: PerformanceLog::class, inversedBy: 'sets')]
    #[ORM\JoinColumn(nullable: false, onDelete: 'CASCADE')]
    private PerformanceLog $log;

    #[ORM\Column(type: 'smallint')]
    private int $setNumber;

    #[ORM\Column(type: 'decimal', precision: 6, scale: 2, nullable: true)]
    private ?float $weightKg = null;

    #[ORM\Column(type: 'smallint', nullable: true)]
    private ?int $repetitions = null;

    #[ORM\Column(type: 'smallint', nullable: true)]
    private ?int $durationSec = null;

    #[ORM\Column(type: 'smallint', nullable: true)]
    private ?int $rpe = null;

    #[ORM\Column(options: ['default' => false])]
    private bool $isWarmup = false;

    public function getId(): ?int { return $this->id; }
    public function getLog(): PerformanceLog { return $this->log; }
    public function setLog(PerformanceLog $log): static { $this->log = $log; return $this; }
    public function getSetNumber(): int { return $this->setNumber; }
    public function setSetNumber(int $n): static { $this->setNumber = $n; return $this; }
    public function getWeightKg(): ?float { return $this->weightKg; }
    public function setWeightKg(?float $w): static { $this->weightKg = $w; return $this; }
    public function getRepetitions(): ?int { return $this->repetitions; }
    public function setRepetitions(?int $r): static { $this->repetitions = $r; return $this; }
    public function getDurationSec(): ?int { return $this->durationSec; }
    public function setDurationSec(?int $d): static { $this->durationSec = $d; return $this; }
    public function getRpe(): ?int { return $this->rpe; }
    public function setRpe(?int $r): static { $this->rpe = $r; return $this; }
    public function isWarmup(): bool { return $this->isWarmup; }
    public function setIsWarmup(bool $w): static { $this->isWarmup = $w; return $this; }
}
