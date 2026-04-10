<?php

namespace App\Controller;

use App\Entity\Order;
use App\Entity\OrderItem;
use App\Entity\Supplement;
use App\Repository\SupplementRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/shop')]
class ShopController extends AbstractController
{
    public function __construct(
        private SupplementRepository $supplementRepository,
        private EntityManagerInterface $entityManager,
    ) {
    }

    #[Route('', name: 'app_shop_index', methods: ['GET'])]
    public function index(Request $request): Response
    {
        // Get all supplements
        $supplements = $this->supplementRepository->findAll();
        
        // Get unique categories and brands
        $categories = array_unique(array_map(fn($s) => $s->getCategory(), $supplements));
        $brands = array_unique(array_map(fn($s) => $s->getBrand(), $supplements));
        
        sort($categories);
        sort($brands);
        
        return $this->render('shop/index.html.twig', [
            'supplements' => $supplements,
            'categories' => $categories,
            'brands' => $brands,
        ]);
    }

    #[Route('/cart', name: 'app_shop_cart', methods: ['GET'])]
    public function cart(SessionInterface $session): Response
    {
        // Ensure session is started
        if (!$session->isStarted()) {
            $session->start();
        }

        $cart = $session->get('cart', []);
        $cartItems = [];
        $subtotal = 0;

        // Debug: Log what's in the session
        error_log("=== CART PAGE DEBUG ===");
        error_log("Session ID: " . $session->getId());
        error_log("Cart from session: " . json_encode($cart));
        error_log("Cart count: " . count($cart));

        foreach ($cart as $item) {
            $supplement = $this->supplementRepository->find($item['id']);
            if ($supplement) {
                $itemTotal = $supplement->getPrice() * $item['quantity'];
                $cartItems[] = [
                    'supplement' => $supplement,
                    'quantity' => $item['quantity'],
                    'total' => $itemTotal,
                ];
                $subtotal += $itemTotal;
            }
        }

        error_log("Cart items count: " . count($cartItems));
        error_log("======================");

        return $this->render('shop/cart.html.twig', [
            'cartItems' => $cartItems,
            'subtotal' => $subtotal,
        ]);
    }

    #[Route('/cart/add', name: 'app_shop_cart_add', methods: ['POST'])]
    public function addToCart(Request $request, SessionInterface $session): JsonResponse
    {
        // Ensure session is started
        if (!$session->isStarted()) {
            $session->start();
        }

        error_log("=== ADD TO CART CALLED ===");
        error_log("Session ID: " . $session->getId());

        $data = json_decode($request->getContent(), true);
        $supplementId = $data['supplementId'] ?? null;
        $quantity = $data['quantity'] ?? 1;

        error_log("Supplement ID: " . $supplementId);
        error_log("Quantity: " . $quantity);

        if (!$supplementId) {
            return new JsonResponse(['success' => false, 'message' => 'Invalid supplement ID'], 400);
        }

        $supplement = $this->supplementRepository->find($supplementId);
        if (!$supplement) {
            return new JsonResponse(['success' => false, 'message' => 'Supplement not found'], 404);
        }

        $cart = $session->get('cart', []);
        
        // Check if item already in cart
        $found = false;
        foreach ($cart as &$item) {
            if ($item['id'] == $supplementId) {
                $item['quantity'] += $quantity;
                $found = true;
                break;
            }
        }
        
        if (!$found) {
            $cart[] = [
                'id' => $supplementId,
                'quantity' => $quantity,
            ];
        }
        
        $session->set('cart', $cart);

        // Debug: verify session was set
        $verifyCart = $session->get('cart', []);

        return new JsonResponse([
            'success' => true,
            'message' => 'Product added to cart',
            'cartCount' => count($cart),
            'debug' => [
                'cartBeforeSave' => $cart,
                'cartAfterSave' => $verifyCart,
                'sessionId' => $session->getId(),
            ],
        ]);
    }

    #[Route('/cart/update', name: 'app_shop_cart_update', methods: ['POST'])]
    public function updateCart(Request $request, SessionInterface $session): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $supplementId = $data['supplementId'] ?? null;
        $quantity = $data['quantity'] ?? 1;
        
        if (!$supplementId) {
            return new JsonResponse(['success' => false, 'message' => 'Invalid supplement ID'], 400);
        }
        
        $cart = $session->get('cart', []);
        
        foreach ($cart as &$item) {
            if ($item['id'] == $supplementId) {
                $item['quantity'] = max(1, $quantity);
                break;
            }
        }
        
        $session->set('cart', $cart);

        return new JsonResponse(['success' => true]);
    }

    #[Route('/cart/remove', name: 'app_shop_cart_remove', methods: ['POST'])]
    public function removeFromCart(Request $request, SessionInterface $session): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $supplementId = $data['supplementId'] ?? null;

        if (!$supplementId) {
            return new JsonResponse(['success' => false, 'message' => 'Invalid supplement ID'], 400);
        }

        $cart = $session->get('cart', []);
        $cart = array_filter($cart, fn($item) => $item['id'] != $supplementId);
        $session->set('cart', array_values($cart));

        return new JsonResponse(['success' => true]);
    }

    #[Route('/cart/get', name: 'app_shop_cart_get', methods: ['GET'])]
    public function getCart(SessionInterface $session): JsonResponse
    {
        // Ensure session is started
        if (!$session->isStarted()) {
            $session->start();
        }

        $cart = $session->get('cart', []);
        return new JsonResponse(['cart' => $cart, 'count' => count($cart)]);
    }

    #[Route('/checkout', name: 'app_shop_checkout', methods: ['GET'])]
    public function checkout(SessionInterface $session): Response
    {
        $cart = $session->get('cart', []);

        if (empty($cart)) {
            return $this->redirectToRoute('app_shop_index');
        }

        $cartItems = [];
        $subtotal = 0;

        foreach ($cart as $item) {
            $supplement = $this->supplementRepository->find($item['id']);
            if ($supplement) {
                $itemTotal = $supplement->getPrice() * $item['quantity'];
                $cartItems[] = [
                    'supplement' => $supplement,
                    'quantity' => $item['quantity'],
                    'total' => $itemTotal,
                ];
                $subtotal += $itemTotal;
            }
        }

        // Hardcoded payment methods
        $paymentMethods = [
            ['id' => 'visa', 'name' => 'Visa', 'icon' => 'fab fa-cc-visa'],
            ['id' => 'mastercard', 'name' => 'Mastercard', 'icon' => 'fab fa-cc-mastercard'],
            ['id' => 'paypal', 'name' => 'PayPal', 'icon' => 'fab fa-cc-paypal'],
            ['id' => 'cod', 'name' => 'Cash on Delivery', 'icon' => 'fas fa-money-bill-wave'],
        ];

        return $this->render('shop/checkout.html.twig', [
            'cartItems' => $cartItems,
            'subtotal' => $subtotal,
            'paymentMethods' => $paymentMethods,
        ]);
    }

    #[Route('/payment/visa', name: 'app_shop_payment_visa', methods: ['GET'])]
    public function paymentVisa(): Response
    {
        return $this->render('shop/payment_visa.html.twig');
    }

    #[Route('/payment/paypal', name: 'app_shop_payment_paypal', methods: ['GET'])]
    public function paymentPaypal(): Response
    {
        return $this->render('shop/payment_paypal.html.twig');
    }

    #[Route('/order/create', name: 'app_shop_order_create', methods: ['POST'])]
    public function createOrder(Request $request, SessionInterface $session): JsonResponse
    {
        $data = json_decode($request->getContent(), true);
        $cart = $session->get('cart', []);

        if (empty($cart)) {
            return new JsonResponse(['success' => false, 'message' => 'Cart is empty'], 400);
        }

        // Create order
        $order = new Order();
        $order->setFirstName($data['firstName']);
        $order->setLastName($data['lastName']);
        $order->setEmail($data['email']);
        $order->setPhone($data['phone']);
        $order->setAddress($data['address']);
        $order->setCity($data['city']);
        $order->setPostalCode($data['postalCode']);
        $order->setNotes($data['notes'] ?? null);

        // Set payment method
        $order->setPaymentMethod($data['paymentMethod'] ?? 'Unknown');

        // Calculate totals
        $subtotal = 0;
        foreach ($cart as $item) {
            $supplement = $this->supplementRepository->find($item['id']);
            if ($supplement) {
                $orderItem = new OrderItem();
                $orderItem->setSupplement($supplement);
                $orderItem->setQuantity($item['quantity']);
                $orderItem->setPrice($supplement->getPrice());
                $orderItem->setTotal($supplement->getPrice() * $item['quantity']);
                $order->addOrderItem($orderItem);

                $subtotal += $orderItem->getTotal();
            }
        }

        $order->setSubtotal($subtotal);

        // Calculate shipping (7 DT, free if > 100 DT)
        $shipping = $subtotal >= 100 ? 0 : 7;
        $order->setShipping($shipping);

        // Apply discount if code provided
        $discount = 0;
        if (!empty($data['discountCode']) && strtolower($data['discountCode']) === 'ali123') {
            $discount = $subtotal * 0.10; // 10% discount
            $order->setDiscountCode($data['discountCode']);
        }
        $order->setDiscount($discount);

        // Calculate total
        $total = $subtotal + $shipping - $discount;
        $order->setTotal($total);

        // Save order
        $this->entityManager->persist($order);
        $this->entityManager->flush();

        // Clear cart
        $session->remove('cart');

        return new JsonResponse([
            'success' => true,
            'orderNumber' => $order->getOrderNumber(),
            'orderId' => $order->getId(),
        ]);
    }

    #[Route('/order/success/{orderNumber}', name: 'app_shop_order_success', methods: ['GET'])]
    public function orderSuccess(string $orderNumber, EntityManagerInterface $entityManager): Response
    {
        $order = $entityManager->getRepository(Order::class)->findOneBy(['orderNumber' => $orderNumber]);

        if (!$order) {
            return $this->redirectToRoute('app_shop_index');
        }

        return $this->render('shop/order_success.html.twig', [
            'order' => $order,
        ]);
    }
}
