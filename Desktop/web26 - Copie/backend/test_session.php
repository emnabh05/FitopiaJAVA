<?php

require_once __DIR__ . '/vendor/autoload.php';

use Symfony\Component\HttpFoundation\Session\Session;
use Symfony\Component\HttpFoundation\Session\Storage\NativeSessionStorage;
use Symfony\Component\HttpFoundation\Session\Storage\Handler\PdoSessionHandler;
use Doctrine\DBAL\DriverManager;

try {
    // Create PDO connection
    $pdo = new PDO('mysql:host=localhost;dbname=fitopia_supplements', 'root', '');
    
    // Create session handler
    $handler = new PdoSessionHandler($pdo);
    
    // Create session storage
    $storage = new NativeSessionStorage([], $handler);
    
    // Create session
    $session = new Session($storage);
    $session->start();
    
    echo "✅ Session started successfully!\n";
    echo "Session ID: " . $session->getId() . "\n";
    
    // Test setting and getting data
    $session->set('test', 'Hello World');
    $value = $session->get('test');
    
    echo "Test value: " . $value . "\n";
    
    // Test cart
    $session->set('cart', [['id' => 1, 'quantity' => 2]]);
    $cart = $session->get('cart', []);
    
    echo "Cart: " . json_encode($cart) . "\n";
    echo "Cart count: " . count($cart) . "\n";
    
    $session->save();
    
    echo "\n✅ Session test completed successfully!\n";
    
} catch (\Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
    echo "Stack trace:\n" . $e->getTraceAsString() . "\n";
}

