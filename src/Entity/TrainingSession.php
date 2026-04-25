<?php

namespace App\Entity;

use App\Repository\TrainingSessionRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: TrainingSessionRepository::class)]
#[ORM\Table(name: 'training_session')]
#[ORM\HasLifecycleCallbacks]
class TrainingSession
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false, onDelete: 'CASCADE')]
    private User $user;

    #[ORM\Column(type: Types::DATE_IMMUTABLE)]
    private \DateTimeImmutable $sessionDate;

    #[ORM\Column(type: Types::SMALLINT)]
    private int $weekNumber;

    #[ORM\Column(type: Types::SMALLINT)]
    private int $year;

    #[ORM\Column(length: 120, nullable: true)]
    private ?string $label = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $notes = null;

    #[ORM\Column(type: Types::SMALLINT, nullable: true)]
    private ?int $durationMin = null;

    #[ORM\Column(type: Types::DATETIME_IMMUTABLE)]
    private \DateTimeImmutable $createdAt;

    /** @var Collection<int, PerformanceLog> */
    #[ORM\OneToMany(mappedBy: 'session', targetEntity: PerformanceLog::class, cascade: ['persist', 'remove'])]
    #[ORM\OrderBy(['exerciseOrder' => 'ASC'])]
    private Collection $performanceLogs;

    public function __construct()
    {
        $this->createdAt = new \DateTimeImmutable();
        $this->performanceLogs = new ArrayCollection();
        $date = new \DateTimeImmutable();
        $this->sessionDate = $date;
        $this->weekNumber  = (int) $date->format('W');
        $this->year        = (int) $date->format('Y');
    }

    public function getId(): ?int { return $this->id; }
    public function getUser(): User { return $this->user; }
    public function setUser(User $user): static { $this->user = $user; return $this; }
    public function getSessionDate(): \DateTimeImmutable { return $this->sessionDate; }
    public function setSessionDate(\DateTimeImmutable $d): static {
        $this->sessionDate = $d;
        $this->weekNumber  = (int) $d->format('W');
        $this->year        = (int) $d->format('Y');
        return $this;
    }
    public function getWeekNumber(): int { return $this->weekNumber; }
    public function getYear(): int { return $this->year; }
    public function getLabel(): ?string { return $this->label; }
    public function setLabel(?string $label): static { $this->label = $label; return $this; }
    public function getNotes(): ?string { return $this->notes; }
    public function setNotes(?string $notes): static { $this->notes = $notes; return $this; }
    public function getDurationMin(): ?int { return $this->durationMin; }
    public function setDurationMin(?int $d): static { $this->durationMin = $d; return $this; }
    public function getCreatedAt(): \DateTimeImmutable { return $this->createdAt; }
    public function getPerformanceLogs(): Collection { return $this->performanceLogs; }
    public function addPerformanceLog(PerformanceLog $log): static {
        if (!$this->performanceLogs->contains($log)) {
            $this->performanceLogs->add($log);
            $log->setSession($this);
        }
        return $this;
    }
}
