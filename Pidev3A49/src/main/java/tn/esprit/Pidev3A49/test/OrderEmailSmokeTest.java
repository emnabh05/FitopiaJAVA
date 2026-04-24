package tn.esprit.Pidev3A49.test;

import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.Models.SupplementOrderItem;
import tn.esprit.Pidev3A49.services.OrderEmailService;
import tn.esprit.Pidev3A49.utils.InvoiceExporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class OrderEmailSmokeTest {

    public static void main(String[] args) throws InterruptedException, IOException {
        SupplementOrder order = new SupplementOrder();
        order.setId(99999);
        order.setFirstName("SMTP");
        order.setLastName("Test");
        order.setEmail("alitouaiti45@gmail.com");
        order.setPhone("00000000");
        order.setAddress("Test Street");
        order.setCity("Tunis");
        order.setPostalCode("1000");
        order.setPaymentMethod("Cash on Delivery");
        order.setStatus("ON_PROGRESS");
        order.setCreatedAt(LocalDateTime.now());
        order.setSubtotal(new BigDecimal("145.00"));
        order.setShippingCost(new BigDecimal("8.00"));
        order.setDiscountAmount(new BigDecimal("10.00"));
        order.setTotalAmount(new BigDecimal("143.00"));
        order.setItems(List.of(
                new SupplementOrderItem(1, "Whey Protein", new BigDecimal("85.00"), 1, new BigDecimal("85.00")),
                new SupplementOrderItem(2, "Creatine", new BigDecimal("30.00"), 2, new BigDecimal("60.00"))
        ));

        System.out.println("Sending order email test to " + order.getEmail() + "...");
        OrderEmailService emailService = new OrderEmailService();
        boolean sent = emailService.sendOrderPlacedEmail(order);
        Thread.sleep(1500);
        System.out.println("Order email send result: " + sent);

        Path invoicePath = InvoiceExporter.exportInvoiceHtml(order);
        boolean invoiceSent = emailService.sendInvoiceEmail(order, invoicePath);
        Thread.sleep(1500);
        System.out.println("Invoice email send result: " + invoiceSent);
    }
}
