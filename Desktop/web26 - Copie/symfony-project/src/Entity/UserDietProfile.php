<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'user_diet_profile')]
class UserDietProfile
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'user_id', referencedColumnName: 'id', nullable: false, onDelete: 'CASCADE')]
    private ?User $user = null;

    #[ORM\ManyToOne(targetEntity: DietaryRestriction::class)]
    #[ORM\JoinColumn(name: 'restriction_id', referencedColumnName: 'id', nullable: false, onDelete: 'CASCADE')]
    private ?DietaryRestriction $restriction = null;

    #[ORM\Column(columnDefinition: "ENUM('health_conditions','dietary_preferences','manual','coach')")]
    private string $detectedFrom;

    #[ORM\Column(type: 'date_immutable', nullable: true)]
    private ?\DateTimeImmutable $dateStart = null;

    #[ORM\Column(type: 'date_immutable', nullable: true)]
    private ?\DateTimeImmutable $dateEnd = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $notes = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $coachNotes = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'coach_id', referencedColumnName: 'id', nullable: true, onDelete: 'SET NULL')]
    private ?User $coach = null;

    #[ORM\Column(type: 'datetime_immutable')]
    private \DateTimeImmutable $createdAt;

    public function getId(): ?int { return $this->id; }
    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): self { $this->user = $user; return $this; }
    public function getRestriction(): ?DietaryRestriction { return $this->restriction; }
    public function setRestriction(?DietaryRestriction $restriction): self { $this->restriction = $restriction; return $this; }
    public function getDetectedFrom(): string { return $this->detectedFrom; }
    public function setDetectedFrom(string $detectedFrom): self { $this->detectedFrom = $detectedFrom; return $this; }
    public function getDateStart(): ?\DateTimeImmutable { return $this->dateStart; }
    public function setDateStart(?\DateTimeImmutable $dateStart): self { $this->dateStart = $dateStart; return $this; }
    public function getDateEnd(): ?\DateTimeImmutable { return $this->dateEnd; }
    public function setDateEnd(?\DateTimeImmutable $dateEnd): self { $this->dateEnd = $dateEnd; return $this; }
    public function getNotes(): ?string { return $this->notes; }
    public function setNotes(?string $notes): self { $this->notes = $notes; return $this; }
    public function getCoachNotes(): ?string { return $this->coachNotes; }
    public function setCoachNotes(?string $coachNotes): self { $this->coachNotes = $coachNotes; return $this; }
    public function getCoach(): ?User { return $this->coach; }
    public function setCoach(?User $coach): self { $this->coach = $coach; return $this; }
    public function getCreatedAt(): \DateTimeImmutable { return $this->createdAt; }
    public function setCreatedAt(\DateTimeImmutable $createdAt): self { $this->createdAt = $createdAt; return $this; }
}
