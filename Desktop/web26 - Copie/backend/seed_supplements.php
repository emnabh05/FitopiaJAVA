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
    
    echo "🌟 Adding popular supplements with images...\n\n";

    // Clear existing data (in correct order due to foreign keys)
    echo "Clearing existing data...\n";
    $conn->executeStatement("SET FOREIGN_KEY_CHECKS = 0");
    $conn->executeStatement("TRUNCATE TABLE order_item");
    $conn->executeStatement("TRUNCATE TABLE `order`");
    $conn->executeStatement("TRUNCATE TABLE supplement");
    $conn->executeStatement("SET FOREIGN_KEY_CHECKS = 1");
    echo "✓ Cleared\n\n";
    
    $supplements = [
        // Protein Supplements - House Nutrition Products
        [
            'name' => 'Premium Whey Enhanced Impact - 1.890KG',
            'description' => 'Premium whey protein concentrate with superior quality. High protein content for muscle recovery and growth. Excellent taste and mixability.',
            'price' => 199.00,
            'stock' => 50,
            'category' => 'Protein',
            'brand' => 'Impact Sport Nutrition',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/whey-1.8-pot.webp-6987697f5c911.webp'
        ],
        [
            'name' => 'Premium Whey Enhanced Sachet Impact - 945GR',
            'description' => 'Premium whey protein in convenient sachet format. Perfect for on-the-go nutrition with high protein content and great taste.',
            'price' => 115.00,
            'stock' => 45,
            'category' => 'Protein',
            'brand' => 'Impact Sport Nutrition',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/945.webp-69879038471fb.webp'
        ],
        [
            'name' => 'Whey Gold Kevin Levrone - 2KG',
            'description' => 'Premium whey protein from legendary bodybuilder Kevin Levrone. High-quality protein for serious athletes and bodybuilders.',
            'price' => 269.00,
            'stock' => 40,
            'category' => 'Protein',
            'brand' => 'Kevin Levrone',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/snikers gold.jpeg-6952d02caf0cc.webp-697c9afe2bf6f.webp'
        ],
        [
            'name' => 'ISO Whey Zero Black BiotechUSA - 1.8KG',
            'description' => 'Premium whey protein isolate with zero sugar and zero fat. Perfect for lean muscle building and cutting phases.',
            'price' => 380.00,
            'stock' => 35,
            'category' => 'Protein',
            'brand' => 'BiotechUSA',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/WhatsApp Image 2025-12-27 at 17.31.57.webp-695012a7bdf22.webp'
        ],
        [
            'name' => 'Ultra Whey American Wolf - 2KG',
            'description' => 'High-quality whey protein concentrate with excellent amino acid profile. Great for muscle recovery and growth.',
            'price' => 170.00,
            'stock' => 55,
            'category' => 'Protein',
            'brand' => 'American Wolf',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/vanille (1).webp-690a745caffd8.webp'
        ],
        [
            'name' => 'Pure Whey American Wolf - 2KG',
            'description' => 'Pure whey protein concentrate for maximum muscle building. High protein content with great taste and mixability.',
            'price' => 190.00,
            'stock' => 60,
            'category' => 'Protein',
            'brand' => 'American Wolf',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/pure (1).webp-691ee30f58ba5.webp'
        ],
        [
            'name' => 'Isolate Whey Animal - 1.81KG',
            'description' => 'Premium whey protein isolate from Animal. Fast-absorbing protein for rapid muscle recovery and growth.',
            'price' => 330.00,
            'stock' => 38,
            'category' => 'Protein',
            'brand' => 'Animal',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/iso animal 1.webp-6912483f0dce5.webp'
        ],
        [
            'name' => '100% Pure Whey BiotechUSA - 1KG',
            'description' => '100% pure whey protein concentrate. High-quality protein for muscle building and recovery with great taste.',
            'price' => 160.00,
            'stock' => 50,
            'category' => 'Protein',
            'brand' => 'BiotechUSA',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/100 pure whey 1k chocoloat.png-68c309e22bb03.png'
        ],
        [
            'name' => 'Isolate Impact - 1.6KG',
            'description' => 'Premium whey protein isolate with high protein content and low fat. Perfect for lean muscle building.',
            'price' => 250.00,
            'stock' => 42,
            'category' => 'Protein',
            'brand' => 'Impact Sport Nutrition',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/ISO FRAISE.webp-696ba6e11f2c1.webp'
        ],
        [
            'name' => 'ISO Clear Impact - 336GR',
            'description' => 'Clear whey protein isolate with refreshing fruit flavors. Light and easy to digest, perfect for post-workout.',
            'price' => 65.00,
            'stock' => 70,
            'category' => 'Protein',
            'brand' => 'Impact Sport Nutrition',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/FRUITS.webp-696ba3f7b43bd.webp'
        ],
        [
            'name' => 'ISO HD BPI - 2.2KG',
            'description' => 'Premium hydrolyzed whey protein isolate. Fast-absorbing protein for maximum muscle recovery and growth.',
            'price' => 290.00,
            'stock' => 35,
            'category' => 'Protein',
            'brand' => 'BPI',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/iso_hd-chocolate_brownie-69serv-ts.webp-68c6ef9fe4407.webp'
        ],
        
        // Creatine
        [
            'name' => 'Optimum Nutrition Micronized Creatine Monohydrate',
            'description' => 'Pure micronized creatine monohydrate powder. 5g per serving. Supports muscle strength, power, and performance.',
            'price' => 35.00,
            'stock' => 80,
            'category' => 'Creatine',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71VqJQVQ0HL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'MuscleTech Platinum 100% Creatine',
            'description' => 'Ultra-pure micronized creatine monohydrate. Scientifically proven to increase strength and muscle size.',
            'price' => 29.99,
            'stock' => 70,
            'category' => 'Creatine',
            'brand' => 'MuscleTech',
            'image_url' => 'https://m.media-amazon.com/images/I/71kqLG8WJNL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Cellucor COR-Performance Creatine',
            'description' => 'Micronized creatine monohydrate for enhanced absorption. Unflavored and easy to mix with any beverage.',
            'price' => 32.00,
            'stock' => 55,
            'category' => 'Creatine',
            'brand' => 'Cellucor',
            'image_url' => 'https://m.media-amazon.com/images/I/71YqE3ZQJSL._AC_SL1500_.jpg'
        ],
        
        // Omega-3 Fish Oil
        [
            'name' => 'Nordic Naturals Ultimate Omega',
            'description' => 'High-potency omega-3 fish oil. 1280mg omega-3s per serving. Supports heart, brain, and immune health.',
            'price' => 55.00,
            'stock' => 65,
            'category' => 'Omega-3',
            'brand' => 'Nordic Naturals',
            'image_url' => 'https://m.media-amazon.com/images/I/71wXGJ8CKQL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Optimum Nutrition Fish Oil Softgels',
            'description' => 'Premium fish oil with 300mg omega-3 fatty acids per softgel. Supports cardiovascular and joint health.',
            'price' => 25.00,
            'stock' => 90,
            'category' => 'Omega-3',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71nQZGqW7LL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Nature Made Fish Oil 1200mg',
            'description' => 'Purified fish oil with 720mg omega-3 fatty acids. No artificial colors or flavors. Gluten-free.',
            'price' => 22.00,
            'stock' => 100,
            'category' => 'Omega-3',
            'brand' => 'Nature Made',
            'image_url' => 'https://m.media-amazon.com/images/I/71VHqJLLjyL._AC_SL1500_.jpg'
        ],
        
        // Pre-Workout
        [
            'name' => 'Cellucor C4 Original Pre-Workout',
            'description' => 'America\'s #1 selling pre-workout. Contains caffeine, beta-alanine, and creatine for explosive energy and performance.',
            'price' => 45.00,
            'stock' => 75,
            'category' => 'Pre-Workout',
            'brand' => 'Cellucor',
            'image_url' => 'https://m.media-amazon.com/images/I/71qPu3qF8LL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Optimum Nutrition Gold Standard Pre-Workout',
            'description' => 'Advanced pre-workout formula with 175mg caffeine, creatine, and beta-alanine. Enhances energy and focus.',
            'price' => 42.00,
            'stock' => 60,
            'category' => 'Pre-Workout',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71kZxqH9SQL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'JYM Supplement Science Pre JYM',
            'description' => 'Science-based pre-workout with 13 hand-picked ingredients. No proprietary blends, full transparency.',
            'price' => 52.00,
            'stock' => 40,
            'category' => 'Pre-Workout',
            'brand' => 'JYM',
            'image_url' => 'https://m.media-amazon.com/images/I/71xqvN8QJNL._AC_SL1500_.jpg'
        ],

        // BCAAs
        [
            'name' => 'Optimum Nutrition BCAA 1000 Caps',
            'description' => '1g of BCAAs per capsule in the ideal 2:1:1 ratio. Supports muscle recovery and reduces muscle breakdown.',
            'price' => 38.00,
            'stock' => 70,
            'category' => 'BCAA',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71kqLG8WJNL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Scivation Xtend BCAA Powder',
            'description' => '7g BCAAs, 2.5g glutamine, and 1g citrulline malate. Zero sugar, zero carbs. Delicious flavors.',
            'price' => 45.00,
            'stock' => 55,
            'category' => 'BCAA',
            'brand' => 'Scivation',
            'image_url' => 'https://m.media-amazon.com/images/I/71VfcNJ3PqL._AC_SL1500_.jpg'
        ],

        // Multivitamins
        [
            'name' => 'Optimum Nutrition Opti-Men Multivitamin',
            'description' => 'Complete nutrient optimization system for men. 75+ ingredients including vitamins, minerals, and amino acids.',
            'price' => 35.00,
            'stock' => 85,
            'category' => 'Vitamins',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71Z8ZxJZPyL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Optimum Nutrition Opti-Women Multivitamin',
            'description' => 'Complete nutrient optimization system for women. 40+ active ingredients tailored for female health.',
            'price' => 35.00,
            'stock' => 80,
            'category' => 'Vitamins',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71eV0F1DXNL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Animal Pak Multivitamin',
            'description' => 'The ultimate training pack for serious athletes. 11 tablets per pack with vitamins, minerals, and performance complex.',
            'price' => 48.00,
            'stock' => 50,
            'category' => 'Vitamins',
            'brand' => 'Universal Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71qhXPJQMsL._AC_SL1500_.jpg'
        ],

        // Mass Gainers
        [
            'name' => 'Optimum Nutrition Serious Mass',
            'description' => 'High-calorie weight gainer with 1250 calories and 50g protein per serving. Perfect for hard gainers.',
            'price' => 75.00,
            'stock' => 45,
            'category' => 'Mass Gainer',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71VqJQVQ0HL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'BSN True-Mass 1200',
            'description' => 'Ultra-premium lean mass gainer with 1200 calories and 50g protein. Multi-functional protein blend.',
            'price' => 82.00,
            'stock' => 35,
            'category' => 'Mass Gainer',
            'brand' => 'BSN',
            'image_url' => 'https://m.media-amazon.com/images/I/71kqLG8WJNL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Dymatize Super Mass Gainer',
            'description' => '1280 calories and 52g protein per serving. Includes 10.7g BCAAs and 17 vitamins and minerals.',
            'price' => 68.00,
            'stock' => 40,
            'category' => 'Mass Gainer',
            'brand' => 'Dymatize',
            'image_url' => 'https://m.media-amazon.com/images/I/71YqE3ZQJSL._AC_SL1500_.jpg'
        ],

        // Fat Burners
        [
            'name' => 'Cellucor Super HD',
            'description' => 'Advanced thermogenic fat burner. Supports metabolism, energy, and mental focus during weight loss.',
            'price' => 42.00,
            'stock' => 60,
            'category' => 'Fat Burner',
            'brand' => 'Cellucor',
            'image_url' => 'https://m.media-amazon.com/images/I/71wXGJ8CKQL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Hydroxycut Hardcore Elite',
            'description' => 'America\'s #1 selling weight loss supplement. Contains caffeine and scientifically researched ingredients.',
            'price' => 38.00,
            'stock' => 55,
            'category' => 'Fat Burner',
            'brand' => 'Hydroxycut',
            'image_url' => 'https://m.media-amazon.com/images/I/71nQZGqW7LL._AC_SL1500_.jpg'
        ],

        // Glutamine
        [
            'name' => 'Optimum Nutrition Glutamine Powder',
            'description' => 'Pure L-Glutamine powder. 5g per serving. Supports muscle recovery and immune system health.',
            'price' => 32.00,
            'stock' => 65,
            'category' => 'Amino Acids',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71VHqJLLjyL._AC_SL1500_.jpg'
        ],

        // Casein Protein
        [
            'name' => 'Optimum Nutrition Gold Standard 100% Casein',
            'description' => 'Slow-digesting micellar casein protein. 24g protein per serving. Perfect for overnight muscle recovery.',
            'price' => 78.00,
            'stock' => 50,
            'category' => 'Protein',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71qPu3qF8LL._AC_SL1500_.jpg'
        ],
        [
            'name' => 'Dymatize Elite Casein',
            'description' => 'Slow-absorbing casein protein with 25g protein and 5g BCAAs. Ideal for nighttime recovery.',
            'price' => 72.00,
            'stock' => 45,
            'category' => 'Protein',
            'brand' => 'Dymatize',
            'image_url' => 'https://m.media-amazon.com/images/I/71kZxqH9SQL._AC_SL1500_.jpg'
        ],

        // ZMA
        [
            'name' => 'Optimum Nutrition ZMA',
            'description' => 'Zinc, magnesium, and vitamin B6 supplement. Supports muscle recovery, sleep quality, and immune function.',
            'price' => 28.00,
            'stock' => 70,
            'category' => 'Vitamins',
            'brand' => 'Optimum Nutrition',
            'image_url' => 'https://m.media-amazon.com/images/I/71xqvN8QJNL._AC_SL1500_.jpg'
        ],

        // Ajouts House Nutrition (15)
        [
            'name' => 'BLACK BLOOD BIOTECH USA 300GR',
            'description' => 'Pré-workout stimulant pour améliorer l\'énergie, le focus et l\'intensité pendant l\'entraînement.',
            'price' => 140.00,
            'stock' => 41,
            'category' => 'Pré-workout',
            'brand' => 'BiotechUSA',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/2.jpg-6903cb2cd347f.jpg'
        ],
        [
            'name' => 'TOTAL WAR 450G',
            'description' => 'Pré-workout puissant axé sur l\'énergie, la congestion musculaire et l\'endurance.',
            'price' => 160.00,
            'stock' => 36,
            'category' => 'Pré-workout',
            'brand' => 'REDCON1',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/TW-STRAWBERRY-MARGARITA-408x418.webp-6952d51d56aa2.webp'
        ],
        [
            'name' => 'C4 ORIGINAL 282GR',
            'description' => 'Pré-workout classique pour un boost d\'énergie et de performance avant la séance.',
            'price' => 96.00,
            'stock' => 58,
            'category' => 'Pré-workout',
            'brand' => 'CELLUCOR',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/fraise.webp-6903d418b6a07.webp'
        ],
        [
            'name' => 'SHRED SHOT 60ML',
            'description' => 'Shot pré-workout pratique pour une montée d\'énergie rapide avant l\'entraînement.',
            'price' => 12.00,
            'stock' => 72,
            'category' => 'Pré-workout',
            'brand' => 'TREC',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/produit__87_-removebg-preview.webp-66700aaaec810.webp'
        ],
        [
            'name' => 'BLOOD WOLF 9000 AMERICAN WOLF 600GR',
            'description' => 'Pré-workout avec caféine et acides aminés pour soutenir l\'intensité et le pump.',
            'price' => 115.00,
            'stock' => 49,
            'category' => 'Pré-workout',
            'brand' => 'American Wolf',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/boold.webp-6903a198a08da.webp'
        ],
        [
            'name' => 'NITRO WHEY GSN 2KG',
            'description' => 'Whey protéinée pour augmenter l\'apport quotidien et améliorer la récupération musculaire.',
            'price' => 155.00,
            'stock' => 33,
            'category' => 'Whey protein',
            'brand' => 'GSN',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/produit__66_-removebg-preview.webp-65fc0f4fba2c7.webp'
        ],
        [
            'name' => 'WHEY PROTEIN WARRIOR 2KG',
            'description' => 'Whey concentrée riche en protéines pour soutenir la prise de masse maigre.',
            'price' => 210.00,
            'stock' => 27,
            'category' => 'Whey protein',
            'brand' => 'Warrior',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/produit__72_-removebg-preview.webp-6603e2da6da9e.webp'
        ],
        [
            'name' => 'CREATINE GSN 500G',
            'description' => 'Créatine monohydrate micronisée pour améliorer la force et l\'explosivité.',
            'price' => 140.00,
            'stock' => 63,
            'category' => 'Creatine',
            'brand' => 'GSN',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/creatine_500.jpg-65fc13a20f7f5.jpg'
        ],
        [
            'name' => 'CREATINE 300GR',
            'description' => 'Créatine en poudre pour soutenir la puissance et la récupération après l\'effort.',
            'price' => 135.00,
            'stock' => 52,
            'category' => 'Creatine',
            'brand' => 'TREC',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/image.png-66a09cc62862c.png'
        ],
        [
            'name' => 'MEGA OMEGA3',
            'description' => 'Oméga‑3 concentrés (EPA/DHA) pour soutenir le cœur, le cerveau et les articulations.',
            'price' => 90.00,
            'stock' => 61,
            'category' => 'Oméga-3',
            'brand' => 'BiotechUSA',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/600x600_MegaOmega3_90caps_250ml.webp-6903d557b4ad8.webp'
        ],
        [
            'name' => 'OMEGA-3 300 CAPSULES',
            'description' => 'Huile de poisson riche en oméga‑3 pour soutenir la santé cardiovasculaire.',
            'price' => 110.00,
            'stock' => 45,
            'category' => 'Oméga-3',
            'brand' => 'Sea Treasures',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/Sans.webp-690a77b7ea3ae.webp'
        ],
        [
            'name' => 'LEAN MASS GAINER 5KG',
            'description' => 'Gainer calorique pour faciliter la prise de masse et augmenter l\'apport énergétique.',
            'price' => 210.00,
            'stock' => 24,
            'category' => 'Mass gainer',
            'brand' => 'WARRIOR',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/lean%20mass%20nutrition.png-68c3d0722cd1e.png'
        ],
        [
            'name' => 'MUSCLE JUICE 2.12KG',
            'description' => 'Gainer riche en calories et protéines pour soutenir la prise de poids.',
            'price' => 170.00,
            'stock' => 38,
            'category' => 'Mass gainer',
            'brand' => 'Ultimate Sport Nutrition',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/produit_-_2024-04-05T155259.065-removebg-preview.webp-6610027ef25e5.webp'
        ],
        [
            'name' => 'PRE 300GR',
            'description' => 'Pré-workout conçu pour booster l\'énergie, la concentration et la congestion musculaire.',
            'price' => 125.00,
            'stock' => 57,
            'category' => 'Pré-workout',
            'brand' => 'YAVA LABS',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/yava-labs-pre-300gr.png-669394b9c6130.png'
        ],
        [
            'name' => 'CREATINE YAVA 300GR',
            'description' => 'Créatine en poudre pour améliorer la force, l\'explosivité et la récupération.',
            'price' => 65.00,
            'stock' => 69,
            'category' => 'Creatine',
            'brand' => 'YAVA LABS',
            'image_url' => 'https://www.housenutrition.tn/storage/uploads/products/images/creatine-yava-labs.jpg-66bc92e12cb4b.jpg'
        ],
    ];

    // Create uploads directory if it doesn't exist
    $uploadsDir = __DIR__ . '/public/uploads/supplements';
    if (!is_dir($uploadsDir)) {
        mkdir($uploadsDir, 0777, true);
        echo "✓ Created uploads directory\n\n";
    }

    // Function to download image with proper headers
    function downloadImage($url, $savePath, $index) {
        try {
            $context = stream_context_create([
                'http' => [
                    'header' => "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\r\n" .
                               "Referer: https://www.housenutrition.tn/\r\n" .
                               "Accept: image/webp,image/apng,image/*,*/*;q=0.8\r\n"
                ],
                'ssl' => [
                    'verify_peer' => false,
                    'verify_peer_name' => false,
                ]
            ]);

            $imageData = @file_get_contents($url, false, $context);
            if ($imageData === false) {
                return false;
            }
            file_put_contents($savePath, $imageData);
            return true;
        } catch (Exception $e) {
            return false;
        }
    }

    // Insert supplements
    $count = 0;
    foreach ($supplements as $supplement) {
        $count++;
        echo "[$count/" . count($supplements) . "] Adding: {$supplement['name']}\n";

        // Download image
        $imageName = null;
        if (isset($supplement['image_url'])) {
            $imageName = 'supplement_' . $count . '.jpg';
            $imagePath = $uploadsDir . '/' . $imageName;

            echo "  → Downloading image... ";
            if (downloadImage($supplement['image_url'], $imagePath, $count - 1)) {
                echo "✓\n";
            } else {
                echo "✗ (using default)\n";
                $imageName = null;
            }
        }

        // Insert into database
        $conn->insert('supplement', [
            'name' => $supplement['name'],
            'description' => $supplement['description'],
            'price' => $supplement['price'],
            'stock' => $supplement['stock'],
            'category' => $supplement['category'],
            'brand' => $supplement['brand'],
            'image' => $imageName,
            'created_at' => date('Y-m-d H:i:s'),
            'updated_at' => date('Y-m-d H:i:s'),
        ]);

        echo "  ✓ Added to database\n\n";
    }

    echo "🎉 Successfully added " . count($supplements) . " supplements!\n";
    echo "📸 Images saved to: public/uploads/supplements/\n";

} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
    exit(1);
}
