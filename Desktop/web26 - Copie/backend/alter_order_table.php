<?php

require_once __DIR__ . '/vendor/autoload.php';

use Doctrine\DBAL\DriverManager;

$connectionParams = [
    'dbname' => 'fitopia_supplements',
    'user' => 'root',
    'password' => '',
    'host' => 'localhost',
    'driver' => 'pdo_mysql',
];

try {
    $conn = DriverManager::getConnection($connectionParams);

    echo "Altering order table...\n";

    echo "1. Dropping foreign key...\n";
    $conn->executeStatement("ALTER TABLE `order` DROP FOREIGN KEY order_ibfk_1");
    echo "   ✓ Foreign key dropped\n";

    echo "2. Dropping index...\n";
    $conn->executeStatement("ALTER TABLE `order` DROP INDEX IDX_order_payment");
    echo "   ✓ Index dropped\n";

    echo "3. Dropping payment_id column...\n";
    $conn->executeStatement("ALTER TABLE `order` DROP COLUMN payment_id");
    echo "   ✓ payment_id column dropped\n";

    echo "4. Adding payment_method column...\n";
    $conn->executeStatement("ALTER TABLE `order` ADD COLUMN payment_method VARCHAR(100) NOT NULL DEFAULT 'Unknown' AFTER postal_code");
    echo "   ✓ payment_method column added\n";

    echo "\n✅ Order table successfully altered!\n";

} catch (\Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
    exit(1);
}
