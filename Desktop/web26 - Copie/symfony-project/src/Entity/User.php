<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;

#[UniqueEntity(fields: ['email'], message: 'An account already exists with this email.')]
#[UniqueEntity(fields: ['username'], message: 'This username is already taken.')]
#[ORM\Entity]
#[ORM\Table(name: 'users')]
class User implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 180, unique: true)]
    private string $email;

    #[ORM\Column(length: 100, unique: true)]
    private string $username;

    #[ORM\Column(length: 100)]
    private string $firstName;

    #[ORM\Column(length: 100)]
    private string $lastName;

    #[ORM\Column(length: 20)]
    private string $phone;

    #[ORM\Column(type: 'date')]
    private \DateTimeInterface $birthDate;

    #[ORM\Column(length: 10)]
    private string $gender;

    #[ORM\Column(nullable: true)]
    private ?string $avatar = null;

    #[ORM\Column(type: 'json')]
    private array $roles = [];

    #[ORM\Column]
    private string $password;

    // USER
    #[ORM\Column(nullable: true)]
    private ?float $height = null;

    #[ORM\Column(nullable: true)]
    private ?float $weight = null;

    #[ORM\Column(nullable: true)]
    private ?float $targetWeight = null;

    #[ORM\Column(nullable: true)]
    private ?string $fitnessLevel = null;

    #[ORM\Column(type: 'json', nullable: true)]
    private ?array $healthConditions = [];

    #[ORM\Column(type: 'json', nullable: true)]
    private ?array $dietaryPreferences = [];

    #[ORM\Column(type: 'json', nullable: true)]
    private ?array $fitnessGoals = [];

    // PROFESSIONAL
    #[ORM\Column(nullable: true)]
    private ?string $professionalTitle = null;

    #[ORM\Column(type: 'json', nullable: true)]
    private ?array $specialization = [];

    #[ORM\Column(nullable: true)]
    private ?string $qualification = null;

    #[ORM\Column(nullable: true)]
    private ?int $yearsOfExperience = null;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $bio = null;

    #[ORM\Column(nullable: true)]
    private ?string $licenseNumber = null;

    // ---------------- SECURITY ----------------

    public function getId(): ?int { return $this->id; }

    public function getUserIdentifier(): string { return $this->email; }

    public function getEmail(): string { return $this->email; }
    public function setEmail(string $email): self { $this->email = $email; return $this; }

    public function getUsername(): string { return $this->username; }
    public function setUsername(string $username): self { $this->username = $username; return $this; }

    public function getFirstName(): string { return $this->firstName; }
    public function setFirstName(string $v): self { $this->firstName = $v; return $this; }

    public function getLastName(): string { return $this->lastName; }
    public function setLastName(string $v): self { $this->lastName = $v; return $this; }

    public function getPhone(): string { return $this->phone; }
    public function setPhone(string $v): self { $this->phone = $v; return $this; }

    public function getBirthDate(): \DateTimeInterface { return $this->birthDate; }
    public function setBirthDate(\DateTimeInterface $v): self { $this->birthDate = $v; return $this; }

    public function getGender(): string { return $this->gender; }
    public function setGender(string $v): self { $this->gender = $v; return $this; }

    public function getAvatar(): ?string { return $this->avatar; }
    public function setAvatar(?string $avatar): self { $this->avatar = $avatar; return $this; }

    public function getRoles(): array {
        return array_unique(array_merge($this->roles, ['ROLE_USER']));
    }

    public function setRoles(array $roles): self {
        $this->roles = $roles;
        return $this;
    }

    public function getPassword(): string { return $this->password; }
    public function setPassword(string $password): self { $this->password = $password; return $this; }

    public function getHeight(): ?float { return $this->height; }
    public function setHeight(?float $height): self { $this->height = $height; return $this; }

    public function getWeight(): ?float { return $this->weight; }
    public function setWeight(?float $weight): self { $this->weight = $weight; return $this; }

    public function getTargetWeight(): ?float { return $this->targetWeight; }
    public function setTargetWeight(?float $targetWeight): self { $this->targetWeight = $targetWeight; return $this; }

    public function getFitnessLevel(): ?string { return $this->fitnessLevel; }
    public function setFitnessLevel(?string $fitnessLevel): self { $this->fitnessLevel = $fitnessLevel; return $this; }

    public function getHealthConditions(): ?array { return $this->healthConditions; }
    public function setHealthConditions(?array $healthConditions): self { $this->healthConditions = $healthConditions; return $this; }

    public function getDietaryPreferences(): ?array { return $this->dietaryPreferences; }
    public function setDietaryPreferences(?array $dietaryPreferences): self { $this->dietaryPreferences = $dietaryPreferences; return $this; }

    public function getFitnessGoals(): ?array { return $this->fitnessGoals; }
    public function setFitnessGoals(?array $fitnessGoals): self { $this->fitnessGoals = $fitnessGoals; return $this; }

    public function getProfessionalTitle(): ?string { return $this->professionalTitle; }
    public function setProfessionalTitle(?string $professionalTitle): self { $this->professionalTitle = $professionalTitle; return $this; }

    public function getSpecialization(): ?array { return $this->specialization; }
    public function setSpecialization(?array $specialization): self { $this->specialization = $specialization; return $this; }

    public function getQualification(): ?string { return $this->qualification; }
    public function setQualification(?string $qualification): self { $this->qualification = $qualification; return $this; }

    public function getYearsOfExperience(): ?int { return $this->yearsOfExperience; }
    public function setYearsOfExperience(?int $yearsOfExperience): self { $this->yearsOfExperience = $yearsOfExperience; return $this; }

    public function getBio(): ?string { return $this->bio; }
    public function setBio(?string $bio): self { $this->bio = $bio; return $this; }

    public function getLicenseNumber(): ?string { return $this->licenseNumber; }
    public function setLicenseNumber(?string $licenseNumber): self { $this->licenseNumber = $licenseNumber; return $this; }

    public function eraseCredentials(): void {}
}
