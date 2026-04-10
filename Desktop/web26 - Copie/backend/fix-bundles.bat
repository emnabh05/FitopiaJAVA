@echo off
echo Fixing bundles.php...

(
echo ^<?php
echo.
echo return [
echo     Symfony\Bundle\FrameworkBundle\FrameworkBundle::class =^> ['all' =^> true],
echo     Doctrine\Bundle\DoctrineBundle\DoctrineBundle::class =^> ['all' =^> true],
echo     Doctrine\Bundle\MigrationsBundle\DoctrineMigrationsBundle::class =^> ['all' =^> true],
echo     Symfony\Bundle\MakerBundle\MakerBundle::class =^> ['dev' =^> true],
echo     Symfony\Bundle\TwigBundle\TwigBundle::class =^> ['all' =^> true],
echo     Twig\Extra\TwigExtraBundle\TwigExtraBundle::class =^> ['all' =^> true],
echo ];
) > config\bundles.php

echo Fixed!
echo.
echo Now setting up database...
if not exist "public\uploads\supplements" mkdir "public\uploads\supplements"
php bin/console doctrine:database:create --if-not-exists
php bin/console doctrine:migrations:migrate --no-interaction
php bin/console cache:clear

echo.
echo ========================================
echo DONE! Ready to start!
echo ========================================
echo.
echo Start server with:
echo   php -S localhost:8000 -t public
echo.
echo Then open: http://localhost:8000/supplement
echo.
pause

