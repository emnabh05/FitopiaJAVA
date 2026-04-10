<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'dietary_restriction')]
class DietaryRestriction
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private string $userEmail;

    #[ORM\Column(length: 100)]
    private string $restrictionName;

    #[ORM\Column(columnDefinition: "ENUM('allergie','intolerance','preference','medical','religieux','ethique')")]
    private string $restrictionType;

    #[ORM\Column(columnDefinition: "ENUM('elevee','moderee','legere')")]
    private string $severity;

    #[ORM\Column(type: 'date_immutable')]
    private \DateTimeImmutable $startDate;

    #[ORM\Column]
    private bool $isTemporary = false;

    #[ORM\Column(type: 'date_immutable', nullable: true)]
    private ?\DateTimeImmutable $endDate = null;

    public function getId(): ?int { return $this->id; }

    public function getUserEmail(): string { return $this->userEmail; }
    public function setUserEmail(string $userEmail): self { $this->userEmail = $userEmail; return $this; }

    public function getRestrictionName(): string { return $this->restrictionName; }
    public function setRestrictionName(string $restrictionName): self { $this->restrictionName = $restrictionName; return $this; }

    public function getRestrictionType(): string { return $this->restrictionType; }
    public function setRestrictionType(string $restrictionType): self { $this->restrictionType = $restrictionType; return $this; }

    public function getSeverity(): string { return $this->severity; }
    public function setSeverity(string $severity): self { $this->severity = $severity; return $this; }

    public function getStartDate(): \DateTimeImmutable { return $this->startDate; }
    public function setStartDate(\DateTimeImmutable $startDate): self { $this->startDate = $startDate; return $this; }

    public function isTemporary(): bool { return $this->isTemporary; }
    public function setIsTemporary(bool $isTemporary): self { $this->isTemporary = $isTemporary; return $this; }

    public function getEndDate(): ?\DateTimeImmutable { return $this->endDate; }
    public function setEndDate(?\DateTimeImmutable $endDate): self { $this->endDate = $endDate; return $this; }
}
