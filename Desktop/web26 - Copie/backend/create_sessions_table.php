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
    
    echo "Creating sessions table...\n";
    
    $sql = "CREATE TABLE IF NOT EXISTS sessions (
        sess_id VARCHAR(128) NOT NULL PRIMARY KEY,
        sess_data BLOB NOT NULL,
        sess_lifetime INT NOT NULL,
        sess_time INT UNSIGNED NOT NULL,
        INDEX sessions_sess_lifetime_idx (sess_lifetime)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin";
    
    $conn->executeStatement($sql);
    
    echo "✅ Sessions table created successfully!\n";
    
} catch (\Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
    exit(1);
}

