<?php

namespace App\Entity;

use App\Repository\ExerciseCatalogRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ExerciseCatalogRepository::class)]
#[ORM\Table(name: 'exercise_catalog')]
#[ORM\HasLifecycleCallbacks]
class ExerciseCatalog
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: true, onDelete: 'SET NULL')]
    private ?User $user = null;

    #[ORM\Column(length: 150)]
    private string $name;

    #[ORM\Column(length: 80)]
    private string $muscleGroup;

    #[ORM\Column(length: 80, nullable: true)]
    private ?string $equipment = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $description = null;

    #[ORM\Column(options: ['default' => false])]
    private bool $isCustom = false;

    #[ORM\Column(type: Types::DATETIME_IMMUTABLE)]
    private \DateTimeImmutable $createdAt;

    /** @var Collection<int, PerformanceLog> */
    #[ORM\OneToMany(mappedBy: 'exercise', targetEntity: PerformanceLog::class)]
    private Collection $performanceLogs;

    public function __construct()
    {
        $this->createdAt = new \DateTimeImmutable();
        $this->performanceLogs = new ArrayCollection();
    }

    public function getId(): ?int { return $this->id; }
    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): static { $this->user = $user; return $this; }
    public function getName(): string { return $this->name; }
    public function setName(string $name): static { $this->name = $name; return $this; }
    public function getMuscleGroup(): string { return $this->muscleGroup; }
    public function setMuscleGroup(string $muscleGroup): static { $this->muscleGroup = $muscleGroup; return $this; }
    public function getEquipment(): ?string { return $this->equipment; }
    public function setEquipment(?string $equipment): static { $this->equipment = $equipment; return $this; }
    public function getDescription(): ?string { return $this->description; }
    public function setDescription(?string $description): static { $this->description = $description; return $this; }
    public function isCustom(): bool { return $this->isCustom; }
    public function setIsCustom(bool $isCustom): static { $this->isCustom = $isCustom; return $this; }
    public function getCreatedAt(): \DateTimeImmutable { return $this->createdAt; }
    public function getPerformanceLogs(): Collection { return $this->performanceLogs; }
}
