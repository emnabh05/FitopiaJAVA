package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.OrderStatusNotification;
import tn.esprit.Pidev3A49.Models.OrderedCustomer;
import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.Models.SupplementOrderItem;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServiceSupplementOrder {

    private static final String DEFAULT_STATUS = "ON_PROGRESS";
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "DELIVERED",
            "ON_THE_WAY",
            "CANCELED",
            "ON_PROGRESS",
            "PLACED"
    );

    private final Connection cnx;
    private final OrderEmailService orderEmailService;

    public ServiceSupplementOrder() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new IllegalStateException("Impossible de se connecter a MySQL.");
        }
        orderEmailService = new OrderEmailService();
    }

    public int placeOrder(SupplementOrder order) {
        validate(order);

        String insertOrderQuery = """
                INSERT INTO %s (
                    first_name, last_name, email, phone, address, city, postal_code,
                    notes, payment_method, discount_code, subtotal, shipping_cost,
                    discount_amount, total_amount, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

        String insertOrderItemQuery = """
                INSERT INTO %s (
                    order_id, supplement_id, supplement_name, unit_price, quantity, line_total
                ) VALUES (?, ?, ?, ?, ?, ?)
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_ITEM_TABLE);

        String updateStockQuery = """
                UPDATE %s
                SET stock = stock - ?
                WHERE id = ? AND stock >= ?
                """.formatted(SchemaInitializer.SUPPLEMENT_TABLE);

        try {
            boolean initialAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            try (PreparedStatement insertOrderStatement = cnx.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertOrderItemStatement = cnx.prepareStatement(insertOrderItemQuery);
                 PreparedStatement updateStockStatement = cnx.prepareStatement(updateStockQuery)) {

                fillOrderStatement(insertOrderStatement, order);
                insertOrderStatement.executeUpdate();

                try (ResultSet generatedKeys = insertOrderStatement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new IllegalStateException("Impossible de creer la commande.");
                    }
                    order.setId(generatedKeys.getInt(1));
                }

                for (SupplementOrderItem item : order.getItems()) {
                    updateStockStatement.setInt(1, item.getQuantity());
                    updateStockStatement.setInt(2, item.getSupplementId());
                    updateStockStatement.setInt(3, item.getQuantity());

                    if (updateStockStatement.executeUpdate() != 1) {
                        throw new IllegalStateException("Stock insuffisant pour le supplement: " + item.getSupplementName());
                    }

                    insertOrderItemStatement.setInt(1, order.getId());
                    insertOrderItemStatement.setInt(2, item.getSupplementId());
                    insertOrderItemStatement.setString(3, item.getSupplementName());
                    insertOrderItemStatement.setBigDecimal(4, item.getUnitPrice());
                    insertOrderItemStatement.setInt(5, item.getQuantity());
                    insertOrderItemStatement.setBigDecimal(6, item.getLineTotal());
                    insertOrderItemStatement.addBatch();
                }

                insertOrderItemStatement.executeBatch();
                cnx.commit();
                cnx.setAutoCommit(initialAutoCommit);
                if (order.getCreatedAt() == null) {
                    order.setCreatedAt(LocalDateTime.now());
                }
                sendOrderConfirmationEmail(order);
                return order.getId();
            } catch (SQLException | RuntimeException exception) {
                cnx.rollback();
                cnx.setAutoCommit(initialAutoCommit);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'enregistrer la commande.", exception);
        }
    }

    public List<OrderedCustomer> getOrderedCustomers() {
        String query = """
                SELECT
                    first_name,
                    last_name,
                    email,
                    phone,
                    city,
                    COUNT(*) AS order_count,
                    SUM(total_amount) AS total_spent,
                    MAX(created_at) AS last_order_at
                FROM %s
                GROUP BY first_name, last_name, email, phone, city
                ORDER BY last_order_at DESC
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

        List<OrderedCustomer> customers = new ArrayList<>();
        try (PreparedStatement statement = cnx.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Timestamp lastOrderTimestamp = resultSet.getTimestamp("last_order_at");
                customers.add(new OrderedCustomer(
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("city"),
                        resultSet.getInt("order_count"),
                        resultSet.getBigDecimal("total_spent"),
                        lastOrderTimestamp == null ? null : lastOrderTimestamp.toLocalDateTime()
                ));
            }
            return customers;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer la liste des clients ayant commande.", exception);
        }
    }

    public List<SupplementOrder> getAllOrdersForAdmin() {
        String query = """
                SELECT
                    id,
                    first_name,
                    last_name,
                    email,
                    phone,
                    city,
                    total_amount,
                    status,
                    created_at
                FROM %s
                ORDER BY created_at DESC, id DESC
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

        List<SupplementOrder> orders = new ArrayList<>();
        try (PreparedStatement statement = cnx.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                SupplementOrder order = new SupplementOrder();
                order.setId(resultSet.getInt("id"));
                order.setFirstName(resultSet.getString("first_name"));
                order.setLastName(resultSet.getString("last_name"));
                order.setEmail(resultSet.getString("email"));
                order.setPhone(resultSet.getString("phone"));
                order.setCity(resultSet.getString("city"));
                order.setTotalAmount(resultSet.getBigDecimal("total_amount"));
                order.setStatus(normalizeStatus(resultSet.getString("status")));

                Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
                order.setCreatedAt(createdAtTimestamp == null ? null : createdAtTimestamp.toLocalDateTime());
                orders.add(order);
            }
            return orders;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les commandes.", exception);
        }
    }

    public void updateOrderStatus(int orderId, String status) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Identifiant de commande invalide.");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Le statut de commande est obligatoire.");
        }

        String normalizedStatus = normalizeStatus(status);
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Statut invalide: " + status);
        }

        OrderStatusSnapshot snapshot = fetchOrderStatusSnapshot(orderId);
        String previousStatus = normalizeStatus(snapshot.status());
        if (normalizedStatus.equals(previousStatus)) {
            return;
        }

        String updateQuery = """
                UPDATE %s
                SET status = ?
                WHERE id = ?
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

        boolean shouldNotify = shouldCreateStatusNotification(previousStatus, normalizedStatus)
                && snapshot.recipientEmail() != null
                && !snapshot.recipientEmail().isBlank();

        try {
            boolean initialAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            try (PreparedStatement updateStatement = cnx.prepareStatement(updateQuery)) {
                updateStatement.setString(1, normalizedStatus);
                updateStatement.setInt(2, orderId);

                int updatedRows = updateStatement.executeUpdate();
                if (updatedRows != 1) {
                    throw new IllegalStateException("Commande introuvable pour l'id " + orderId + ".");
                }

                if (shouldNotify) {
                    insertStatusNotification(orderId, snapshot.recipientEmail().trim(), previousStatus, normalizedStatus);
                }

                cnx.commit();
                cnx.setAutoCommit(initialAutoCommit);
            } catch (SQLException | RuntimeException exception) {
                cnx.rollback();
                cnx.setAutoCommit(initialAutoCommit);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de mettre a jour le statut de la commande.", exception);
        }
    }

    public List<SupplementOrder> getOrdersByEmail(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        String query = """
                SELECT
                    id,
                    first_name,
                    last_name,
                    email,
                    phone,
                    city,
                    total_amount,
                    status,
                    created_at
                FROM %s
                WHERE LOWER(email) = LOWER(?)
                ORDER BY created_at DESC, id DESC
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

        List<SupplementOrder> orders = new ArrayList<>();
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SupplementOrder order = new SupplementOrder();
                    order.setId(resultSet.getInt("id"));
                    order.setFirstName(resultSet.getString("first_name"));
                    order.setLastName(resultSet.getString("last_name"));
                    order.setEmail(resultSet.getString("email"));
                    order.setPhone(resultSet.getString("phone"));
                    order.setCity(resultSet.getString("city"));
                    order.setTotalAmount(resultSet.getBigDecimal("total_amount"));
                    order.setStatus(normalizeStatus(resultSet.getString("status")));

                    Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
                    order.setCreatedAt(createdAtTimestamp == null ? null : createdAtTimestamp.toLocalDateTime());
                    orders.add(order);
                }
            }
            return orders;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les commandes pour cet email.", exception);
        }
    }

    public List<OrderStatusNotification> getRecentOrderNotificationsByEmail(String email, int limit) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));
        String query = """
                SELECT
                    id,
                    order_id,
                    recipient_email,
                    previous_status,
                    new_status,
                    message,
                    created_at
                FROM %s
                WHERE LOWER(recipient_email) = LOWER(?)
                ORDER BY id DESC
                LIMIT ?
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_NOTIFICATION_TABLE);

        List<OrderStatusNotification> notifications = new ArrayList<>();
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email.trim());
            statement.setInt(2, safeLimit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapOrderNotification(resultSet));
                }
            }
            return notifications;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les notifications de commandes.", exception);
        }
    }

    public List<OrderStatusNotification> getOrderNotificationsByEmailAfterId(String email, int afterId) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        int safeAfterId = Math.max(afterId, 0);
        String query = """
                SELECT
                    id,
                    order_id,
                    recipient_email,
                    previous_status,
                    new_status,
                    message,
                    created_at
                FROM %s
                WHERE LOWER(recipient_email) = LOWER(?)
                  AND id > ?
                ORDER BY id ASC
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_NOTIFICATION_TABLE);

        List<OrderStatusNotification> notifications = new ArrayList<>();
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email.trim());
            statement.setInt(2, safeAfterId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapOrderNotification(resultSet));
                }
            }
            return notifications;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les nouvelles notifications de commandes.", exception);
        }
    }

    private void fillOrderStatement(PreparedStatement statement, SupplementOrder order) throws SQLException {
        statement.setString(1, order.getFirstName());
        statement.setString(2, order.getLastName());
        statement.setString(3, order.getEmail());
        statement.setString(4, order.getPhone());
        statement.setString(5, order.getAddress());
        statement.setString(6, order.getCity());
        statement.setString(7, order.getPostalCode());

        if (order.getNotes() == null || order.getNotes().isBlank()) {
            statement.setNull(8, Types.VARCHAR);
        } else {
            statement.setString(8, order.getNotes());
        }

        statement.setString(9, order.getPaymentMethod());

        if (order.getDiscountCode() == null || order.getDiscountCode().isBlank()) {
            statement.setNull(10, Types.VARCHAR);
        } else {
            statement.setString(10, order.getDiscountCode());
        }

        statement.setBigDecimal(11, order.getSubtotal());
        statement.setBigDecimal(12, order.getShippingCost());
        statement.setBigDecimal(13, order.getDiscountAmount());
        statement.setBigDecimal(14, order.getTotalAmount());

        String normalizedStatus = normalizeStatus(order.getStatus());
        statement.setString(15, normalizedStatus);
        order.setStatus(normalizedStatus);
    }

    private void validate(SupplementOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("La commande est obligatoire.");
        }
        requireText(order.getFirstName(), "Le prenom est obligatoire.");
        requireText(order.getLastName(), "Le nom est obligatoire.");
        requireText(order.getEmail(), "L'email est obligatoire.");
        requireText(order.getPhone(), "Le telephone est obligatoire.");
        requireText(order.getAddress(), "L'adresse est obligatoire.");
        requireText(order.getCity(), "La ville est obligatoire.");
        requireText(order.getPostalCode(), "Le code postal est obligatoire.");
        requireText(order.getPaymentMethod(), "Le mode de paiement est obligatoire.");

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Le panier est vide.");
        }

        BigDecimal computedSubtotal = order.getItems().stream()
                .map(SupplementOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (computedSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant de la commande est invalide.");
        }

        if (order.getSubtotal() == null || order.getSubtotal().compareTo(computedSubtotal) != 0) {
            throw new IllegalArgumentException("Le sous-total de la commande est invalide.");
        }

        BigDecimal shippingCost = order.getShippingCost() == null ? BigDecimal.ZERO : order.getShippingCost();
        BigDecimal discountAmount = order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount();
        BigDecimal expectedTotal = computedSubtotal.add(shippingCost).subtract(discountAmount);

        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(expectedTotal) != 0) {
            throw new IllegalArgumentException("Le total de la commande est invalide.");
        }

        for (SupplementOrderItem item : order.getItems()) {
            if (item.getSupplementId() <= 0) {
                throw new IllegalArgumentException("Un supplement du panier est invalide.");
            }
            requireText(item.getSupplementName(), "Le nom d'un supplement du panier est invalide.");
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le prix d'un supplement du panier est invalide.");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("La quantite d'un supplement du panier est invalide.");
            }
            if (item.getLineTotal() == null || item.getLineTotal().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant d'un supplement du panier est invalide.");
            }
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private OrderStatusSnapshot fetchOrderStatusSnapshot(int orderId) {
        String query = """
                SELECT email, status
                FROM %s
                WHERE id = ?
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Commande introuvable pour l'id " + orderId + ".");
                }
                return new OrderStatusSnapshot(
                        resultSet.getString("email"),
                        resultSet.getString("status")
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de lire l'etat actuel de la commande.", exception);
        }
    }

    private void insertStatusNotification(int orderId, String recipientEmail, String previousStatus, String newStatus)
            throws SQLException {
        String query = """
                INSERT INTO %s (
                    order_id, recipient_email, previous_status, new_status, message
                ) VALUES (?, ?, ?, ?, ?)
                """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_NOTIFICATION_TABLE);

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, orderId);
            statement.setString(2, recipientEmail);

            if (previousStatus == null || previousStatus.isBlank()) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, previousStatus);
            }

            statement.setString(4, newStatus);
            statement.setString(5, buildStatusNotificationMessage(orderId, previousStatus, newStatus));
            statement.executeUpdate();
        }
    }

    private OrderStatusNotification mapOrderNotification(ResultSet resultSet) throws SQLException {
        OrderStatusNotification notification = new OrderStatusNotification();
        notification.setId(resultSet.getInt("id"));
        notification.setOrderId(resultSet.getInt("order_id"));
        notification.setRecipientEmail(resultSet.getString("recipient_email"));
        notification.setPreviousStatus(normalizeStatus(resultSet.getString("previous_status")));
        notification.setNewStatus(normalizeStatus(resultSet.getString("new_status")));
        notification.setMessage(resultSet.getString("message"));
        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        notification.setCreatedAt(createdAtTimestamp == null ? null : createdAtTimestamp.toLocalDateTime());
        return notification;
    }

    private boolean shouldCreateStatusNotification(String previousStatus, String newStatus) {
        String normalizedPrevious = normalizeStatus(previousStatus);
        String normalizedNew = normalizeStatus(newStatus);
        return !normalizedPrevious.equals(normalizedNew) && !isInProgressStatus(normalizedNew);
    }

    private boolean isInProgressStatus(String status) {
        return DEFAULT_STATUS.equals(normalizeStatus(status));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_STATUS;
        }
        String normalized = status.trim().toUpperCase();
        if ("PLACED".equals(normalized)) {
            return DEFAULT_STATUS;
        }
        return normalized;
    }

    private String buildStatusNotificationMessage(int orderId, String previousStatus, String newStatus) {
        String normalizedPrevious = normalizeStatus(previousStatus);
        String normalizedNew = normalizeStatus(newStatus);
        return "Order #" + orderId + " status changed from "
                + humanizeStatus(normalizedPrevious) + " to " + humanizeStatus(normalizedNew) + ".";
    }

    private void sendOrderConfirmationEmail(SupplementOrder order) {
        try {
            orderEmailService.sendOrderPlacedEmailAsync(order);
        } catch (RuntimeException exception) {
            System.err.println("Order email skipped for order #" + order.getId() + ": " + exception.getMessage());
        }
    }

    private String humanizeStatus(String status) {
        String normalized = normalizeStatus(status);
        return normalized.replace('_', ' ');
    }

    private record OrderStatusSnapshot(String recipientEmail, String status) {
    }
}
