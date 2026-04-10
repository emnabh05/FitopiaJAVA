<?php

namespace App\Form;

use App\Entity\DietaryRestriction;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DietaryRestrictionType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('restrictionName', TextType::class, [
                'label' => 'Restriction name',
            ])
            ->add('restrictionType', ChoiceType::class, [
                'label' => 'Restriction type',
                'choices' => [
                    'Allergie' => 'allergie',
                    'Intolerance' => 'intolerance',
                    'Preference' => 'preference',
                    'Medical' => 'medical',
                    'Religieux' => 'religieux',
                    'Ethique' => 'ethique',
                ],
            ])
            ->add('severity', ChoiceType::class, [
                'label' => 'Severity',
                'choices' => [
                    'Elevee' => 'elevee',
                    'Moderee' => 'moderee',
                    'Legere' => 'legere',
                ],
            ])
            ->add('startDate', DateType::class, [
                'label' => 'Start date',
                'widget' => 'single_text',
                'input' => 'datetime_immutable',
            ])
            ->add('isTemporary', CheckboxType::class, [
                'label' => 'Temporary restriction',
                'required' => false,
            ])
            ->add('endDate', DateType::class, [
                'label' => 'End date',
                'widget' => 'single_text',
                'required' => false,
                'input' => 'datetime_immutable',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => DietaryRestriction::class,
        ]);
    }
}
