<?php

namespace App\Controller;

use App\Entity\Order;
use App\Repository\OrderRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/order')]
class OrderController extends AbstractController
{
    public function __construct(
        private OrderRepository $orderRepository,
        private EntityManagerInterface $entityManager,
    ) {
    }

    #[Route('', name: 'app_order_index', methods: ['GET'])]
    public function index(Request $request): Response
    {
        $status = $request->query->get('status');
        
        if ($status) {
            $orders = $this->orderRepository->findByStatus($status);
        } else {
            $orders = $this->orderRepository->findBy([], ['createdAt' => 'DESC']);
        }
        
        return $this->render('order/index.html.twig', [
            'orders' => $orders,
            'currentStatus' => $status,
        ]);
    }

    #[Route('/{id}', name: 'app_order_show', methods: ['GET'])]
    public function show(Order $order): Response
    {
        return $this->render('order/show.html.twig', [
            'order' => $order,
        ]);
    }

    #[Route('/{id}/status', name: 'app_order_update_status', methods: ['POST'])]
    public function updateStatus(Request $request, Order $order): Response
    {
        $newStatus = $request->request->get('status');
        
        if ($newStatus) {
            $order->setStatus($newStatus);
            $this->entityManager->flush();
            
            $this->addFlash('success', 'Order status updated successfully!');
        }
        
        return $this->redirectToRoute('app_order_show', ['id' => $order->getId()]);
    }

    #[Route('/{id}/delete', name: 'app_order_delete', methods: ['POST'])]
    public function delete(Request $request, Order $order): Response
    {
        $this->entityManager->remove($order);
        $this->entityManager->flush();
        
        $this->addFlash('success', 'Order deleted successfully!');
        
        return $this->redirectToRoute('app_order_index');
    }
}

