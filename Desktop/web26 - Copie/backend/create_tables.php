<?php

require_once __DIR__ . '/vendor/autoload.php';

use Doctrine\DBAL\DriverManager;

$params = [
    'dbname' => 'fitopia_supplements',
    'user' => 'root',
    'password' => '',
    'host' => 'localhost',
    'driver' => 'pdo_mysql',
];

$conn = DriverManager::getConnection($params);

// Create order table
$sql1 = "CREATE TABLE IF NOT EXISTS `order` (
    id INT AUTO_INCREMENT NOT NULL,
    payment_id INT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    subtotal NUMERIC(10, 2) NOT NULL,
    shipping NUMERIC(10, 2) NOT NULL,
    discount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total NUMERIC(10, 2) NOT NULL,
    discount_code VARCHAR(50) DEFAULT NULL,
    notes LONGTEXT DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX IDX_order_payment (payment_id),
    PRIMARY KEY(id),
    FOREIGN KEY (payment_id) REFERENCES payment(id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB";

// Create order_item table
$sql2 = "CREATE TABLE IF NOT EXISTS order_item (
    id INT AUTO_INCREMENT NOT NULL,
    order_id INT NOT NULL,
    supplement_id INT NOT NULL,
    quantity INT NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    total NUMERIC(10, 2) NOT NULL,
    INDEX IDX_order_item_order (order_id),
    INDEX IDX_order_item_supplement (supplement_id),
    PRIMARY KEY(id),
    FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE CASCADE,
    FOREIGN KEY (supplement_id) REFERENCES supplement(id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB";

try {
    $conn->executeStatement($sql1);
    echo "✅ Order table created successfully!\n";
    
    $conn->executeStatement($sql2);
    echo "✅ OrderItem table created successfully!\n";
} catch (\Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}

