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
    
    echo "Checking sessions table...\n\n";
    
    $result = $conn->executeQuery('SELECT COUNT(*) as count FROM sessions');
    $count = $result->fetchOne();
    
    echo "Total sessions in database: " . $count . "\n\n";
    
    if ($count > 0) {
        $sessions = $conn->executeQuery('SELECT sess_id, sess_lifetime, sess_time, LENGTH(sess_data) as data_length FROM sessions ORDER BY sess_time DESC LIMIT 5');
        
        echo "Recent sessions:\n";
        echo "================\n";
        foreach ($sessions->fetchAllAssociative() as $session) {
            echo "Session ID: " . $session['sess_id'] . "\n";
            echo "Data length: " . $session['data_length'] . " bytes\n";
            echo "Lifetime: " . $session['sess_lifetime'] . " seconds\n";
            echo "Time: " . date('Y-m-d H:i:s', $session['sess_time']) . "\n";
            echo "---\n";
        }
    }
    
} catch (\Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}

