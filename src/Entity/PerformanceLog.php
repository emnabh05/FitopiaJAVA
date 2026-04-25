<?php

namespace App\Entity;

use App\Repository\PerformanceLogRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PerformanceLogRepository::class)]
#[ORM\Table(name: 'performance_log')]
class PerformanceLog
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: TrainingSession::class, inversedBy: 'performanceLogs')]
    #[ORM\JoinColumn(nullable: false, onDelete: 'CASCADE')]
    private TrainingSession $session;

    #[ORM\ManyToOne(targetEntity: ExerciseCatalog::class, inversedBy: 'performanceLogs')]
    #[ORM\JoinColumn(nullable: false, onDelete: 'CASCADE')]
    private ExerciseCatalog $exercise;

    #[ORM\Column(type: 'smallint', options: ['default' => 1])]
    private int $exerciseOrder = 1;

    #[ORM\Column(type: 'smallint', nullable: true)]
    private ?int $restSeconds = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $notes = null;

    /** @var Collection<int, PerformanceSet> */
    #[ORM\OneToMany(mappedBy: 'log', targetEntity: PerformanceSet::class, cascade: ['persist', 'remove'])]
    #[ORM\OrderBy(['setNumber' => 'ASC'])]
    private Collection $sets;

    public function __construct()
    {
        $this->sets = new ArrayCollection();
    }

    public function getId(): ?int { return $this->id; }
    public function getSession(): TrainingSession { return $this->session; }
    public function setSession(TrainingSession $s): static { $this->session = $s; return $this; }
    public function getExercise(): ExerciseCatalog { return $this->exercise; }
    public function setExercise(ExerciseCatalog $e): static { $this->exercise = $e; return $this; }
    public function getExerciseOrder(): int { return $this->exerciseOrder; }
    public function setExerciseOrder(int $o): static { $this->exerciseOrder = $o; return $this; }
    public function getRestSeconds(): ?int { return $this->restSeconds; }
    public function setRestSeconds(?int $r): static { $this->restSeconds = $r; return $this; }
    public function getNotes(): ?string { return $this->notes; }
    public function setNotes(?string $n): static { $this->notes = $n; return $this; }
    public function getSets(): Collection { return $this->sets; }
    public function addSet(PerformanceSet $set): static {
        if (!$this->sets->contains($set)) {
            $this->sets->add($set);
            $set->setLog($this);
        }
        return $this;
    }

    /** Poids max de toutes les séries (hors échauffement) */
    public function getMaxWeight(): ?float {
        $max = null;
        foreach ($this->sets as $set) {
            if (!$set->isWarmup() && $set->getWeightKg() !== null) {
                $max = $max === null ? $set->getWeightKg() : max($max, $set->getWeightKg());
            }
        }
        return $max;
    }

    /** Volume total = somme(poids × reps) */
    public function getTotalVolume(): float {
        $vol = 0.0;
        foreach ($this->sets as $set) {
            if (!$set->isWarmup()) {
                $vol += ($set->getWeightKg() ?? 0) * ($set->getRepetitions() ?? 1);
            }
        }
        return $vol;
    }
}
