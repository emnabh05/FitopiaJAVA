package tn.esprit.Pidev3A49.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.Pidev3A49.Models.OrderStatusNotification;
import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.services.ServiceSupplementOrder;
import tn.esprit.Pidev3A49.utils.CartStore;
import tn.esprit.Pidev3A49.utils.SceneNavigator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FrontOrdersController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter NOTIFICATION_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final int INITIAL_NOTIFICATION_LIMIT = 10;
    private static final int MAX_VISIBLE_NOTIFICATIONS = 10;
    private static final double POLLING_SECONDS = 3.0;

    @FXML
    private TextField emailSearchField;

    @FXML
    private VBox ordersCardsContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private Button notificationBellButton;

    @FXML
    private Label notificationBadgeLabel;

    private ContextMenu notificationsMenu;
    private Label notificationsMenuHintLabel;
    private VBox notificationsMenuContainer;

    private ServiceSupplementOrder serviceSupplementOrder;
    private Timeline notificationTimeline;
    private String trackedEmail;
    private int lastNotificationId;
    private int unreadNotificationCount;
    private final List<OrderStatusNotification> visibleNotifications = new ArrayList<>();

    @FXML
    private void initialize() {
        renderOrders(new ArrayList<>());
        configureNotificationPolling();
        configureNotificationsMenu();
        hideNotificationsMenu();
        renderNotificationEmptyState("Status notifications will appear here.");
        updateNotificationBadge();

        try {
            serviceSupplementOrder = new ServiceSupplementOrder();
            String lastEmail = CartStore.getInstance().getLastCheckoutEmail();
            if (lastEmail != null && !lastEmail.isBlank()) {
                emailSearchField.setText(lastEmail);
                searchOrders();
            } else {
                setInfoMessage("Enter your email then click Check Orders.");
                setNotificationHint("Waiting for an email search.");
            }
        } catch (RuntimeException exception) {
            setErrorMessage("MySQL unavailable: " + exception.getMessage());
            setNotificationHint("Notifications unavailable.");
        }
    }

    @FXML
    public void searchOrders() {
        if (serviceSupplementOrder == null) {
            setErrorMessage("Service unavailable.");
            stopNotificationPolling();
            return;
        }

        String email = emailSearchField.getText() == null ? "" : emailSearchField.getText().trim();
        if (email.isEmpty()) {
            stopNotificationPolling();
            hideNotificationsMenu();
            clearTrackedEmailState();
            renderOrders(new ArrayList<>());
            setInfoMessage("Please enter your email.");
            setNotificationHint("Enter your email to enable live notifications.");
            return;
        }

        try {
            List<SupplementOrder> orders = serviceSupplementOrder.getOrdersByEmail(email);
            renderOrders(orders);
            trackedEmail = email;

            loadInitialNotifications(email);
            startNotificationPolling();

            if (orders.isEmpty()) {
                setInfoMessage("No orders found for " + email + ".");
            } else {
                setSuccessMessage("Found " + orders.size() + " order(s) for " + email + ".");
            }
        } catch (RuntimeException exception) {
            stopNotificationPolling();
            hideNotificationsMenu();
            clearTrackedEmailState();
            renderOrders(new ArrayList<>());
            setErrorMessage("Could not search orders: " + exception.getMessage());
            setNotificationHint("Notifications unavailable.");
        }
    }

    @FXML
    public void openStore(ActionEvent event) throws IOException {
        stopNotificationPolling();
        hideNotificationsMenu();
        SceneNavigator.navigate(event, SceneNavigator.FRONT_ORDERS_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    @FXML
    public void openCheckout(ActionEvent event) throws IOException {
        stopNotificationPolling();
        hideNotificationsMenu();
        SceneNavigator.navigate(event, SceneNavigator.FRONT_ORDERS_VIEW, SceneNavigator.CHECKOUT_VIEW);
    }

    @FXML
    public void goBackOrExit(ActionEvent event) throws IOException {
        stopNotificationPolling();
        hideNotificationsMenu();
        SceneNavigator.goBackOrClose(event);
    }

    @FXML
    private void toggleNotificationsMenu() {
        if (notificationsMenu == null || notificationBellButton == null) {
            return;
        }

        if (notificationsMenu.isShowing()) {
            notificationsMenu.hide();
            return;
        }

        refreshNotificationsMenu();
        notificationsMenu.show(notificationBellButton, Side.BOTTOM, -334.0, 8.0);
        if (notificationsMenu.isShowing()) {
            unreadNotificationCount = 0;
            updateNotificationBadge();
        }
    }

    private void configureNotificationPolling() {
        notificationTimeline = new Timeline(new KeyFrame(Duration.seconds(POLLING_SECONDS), event -> pollForNotifications()));
        notificationTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void configureNotificationsMenu() {
        notificationsMenuContainer = new VBox(8.0);

        Label titleLabel = new Label("Notifications");
        titleLabel.setStyle("-fx-text-fill: #123748; -fx-font-size: 15px; -fx-font-weight: 900;");

        notificationsMenuHintLabel = new Label("Status notifications will appear here.");
        notificationsMenuHintLabel.setStyle("-fx-text-fill: #607887; -fx-font-size: 12px; -fx-font-weight: 700;");

        ScrollPane scrollPane = new ScrollPane(notificationsMenuContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(280.0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        VBox popupRoot = new VBox(8.0, titleLabel, notificationsMenuHintLabel, scrollPane);
        popupRoot.setPrefWidth(380.0);
        popupRoot.setMinWidth(380.0);
        popupRoot.setMaxWidth(380.0);
        popupRoot.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #DCE8E2; "
                + "-fx-border-radius: 14; -fx-padding: 10 10 10 10;");

        CustomMenuItem popupContent = new CustomMenuItem(popupRoot, false);
        notificationsMenu = new ContextMenu(popupContent);
        notificationsMenu.setAutoHide(true);
    }

    private void loadInitialNotifications(String email) {
        List<OrderStatusNotification> notifications =
                serviceSupplementOrder.getRecentOrderNotificationsByEmail(email, INITIAL_NOTIFICATION_LIMIT);

        notifications.sort(Comparator.comparingInt(OrderStatusNotification::getId).reversed());
        visibleNotifications.clear();
        visibleNotifications.addAll(notifications);
        if (visibleNotifications.size() > MAX_VISIBLE_NOTIFICATIONS) {
            visibleNotifications.subList(MAX_VISIBLE_NOTIFICATIONS, visibleNotifications.size()).clear();
        }

        lastNotificationId = notifications.stream()
                .mapToInt(OrderStatusNotification::getId)
                .max()
                .orElse(0);

        unreadNotificationCount = 0;
        renderNotifications();
        updateNotificationBadge();
        setNotificationHint(
                notifications.isEmpty()
                        ? "No status change notifications yet."
                        : notifications.size() + " recent notification(s). Live updates are active."
        );
    }

    private void pollForNotifications() {
        if (serviceSupplementOrder == null || trackedEmail == null || trackedEmail.isBlank()) {
            return;
        }

        try {
            List<OrderStatusNotification> updates =
                    serviceSupplementOrder.getOrderNotificationsByEmailAfterId(trackedEmail, lastNotificationId);

            if (updates.isEmpty()) {
                return;
            }

            for (OrderStatusNotification update : updates) {
                visibleNotifications.add(0, update);
            }
            if (visibleNotifications.size() > MAX_VISIBLE_NOTIFICATIONS) {
                visibleNotifications.subList(MAX_VISIBLE_NOTIFICATIONS, visibleNotifications.size()).clear();
            }

            lastNotificationId = updates.stream()
                    .mapToInt(OrderStatusNotification::getId)
                    .max()
                    .orElse(lastNotificationId);

            if (!isNotificationsMenuOpen()) {
                unreadNotificationCount += updates.size();
            }

            renderNotifications();
            refreshOrdersForTrackedEmail();
            updateNotificationBadge();

            String latestMessage = updates.get(updates.size() - 1).getMessage();
            setSuccessMessage("New order update: " + valueOrDash(latestMessage));
            setNotificationHint("Live updates active. Last update at " + LocalDateTime.now().format(NOTIFICATION_TIME_FORMATTER) + ".");
        } catch (RuntimeException exception) {
            stopNotificationPolling();
            setErrorMessage("Live notifications stopped: " + exception.getMessage());
            setNotificationHint("Live updates paused due to a MySQL error.");
        }
    }

    private void refreshOrdersForTrackedEmail() {
        if (trackedEmail == null || trackedEmail.isBlank()) {
            return;
        }
        List<SupplementOrder> refreshedOrders = serviceSupplementOrder.getOrdersByEmail(trackedEmail);
        renderOrders(refreshedOrders);
    }

    private void renderOrders(List<SupplementOrder> orders) {
        if (ordersCardsContainer == null) {
            return;
        }

        ordersCardsContainer.getChildren().clear();
        if (orders == null || orders.isEmpty()) {
            Label emptyLabel = new Label("No orders yet for this email.");
            emptyLabel.setStyle("-fx-text-fill: #7A909B; -fx-font-size: 14px; -fx-font-weight: 700;");
            ordersCardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (SupplementOrder order : orders) {
            ordersCardsContainer.getChildren().add(buildOrderCard(order));
        }
    }

    private VBox buildOrderCard(SupplementOrder order) {
        VBox card = new VBox(8.0);
        card.setStyle("-fx-background-color: #F8FCFA; -fx-background-radius: 14; -fx-border-color: #DDEBE5; "
                + "-fx-border-radius: 14; -fx-padding: 12 14 12 14;");

        HBox header = new HBox(10.0);
        Label dateLabel = new Label("Created: " + formatOrderDate(order.getCreatedAt()));
        dateLabel.setStyle("-fx-text-fill: #153D4E; -fx-font-size: 13px; -fx-font-weight: 800;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(normalizeStatus(order.getStatus()));
        statusLabel.setStyle(resolveStatusBadgeStyle(order.getStatus()));
        header.getChildren().addAll(dateLabel, spacer, statusLabel);

        Label totalLabel = new Label("Total: " + formatPrice(order.getTotalAmount()));
        totalLabel.setStyle("-fx-text-fill: #205367; -fx-font-size: 14px; -fx-font-weight: 900;");

        Label shippingLabel = new Label("Shipping: " + valueOrDash(order.getAddress()) + ", " + valueOrDash(order.getCity()));
        shippingLabel.setWrapText(true);
        shippingLabel.setStyle("-fx-text-fill: #4E6874; -fx-font-size: 13px; -fx-font-weight: 700;");

        card.getChildren().addAll(header, totalLabel, shippingLabel);
        return card;
    }

    private void renderNotifications() {
        if (notificationsMenuContainer == null) {
            return;
        }
        notificationsMenuContainer.getChildren().clear();
        if (visibleNotifications.isEmpty()) {
            renderNotificationEmptyState("No status notifications yet.");
            return;
        }

        for (OrderStatusNotification notification : visibleNotifications) {
            notificationsMenuContainer.getChildren().add(buildNotificationCard(notification));
        }
    }

    private VBox buildNotificationCard(OrderStatusNotification notification) {
        VBox card = new VBox(6.0);
        card.setStyle("-fx-background-color: #F8FCFA; -fx-background-radius: 12; -fx-border-color: #DDEBE5; "
                + "-fx-border-radius: 12; -fx-padding: 10 12 10 12;");

        HBox header = new HBox(10.0);
        Label orderLabel = new Label("Order #" + notification.getOrderId());
        orderLabel.setStyle("-fx-text-fill: #153D4E; -fx-font-size: 13px; -fx-font-weight: 900;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(formatNotificationDate(notification.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: #7A909B; -fx-font-size: 12px; -fx-font-weight: 700;");
        header.getChildren().addAll(orderLabel, spacer, timeLabel);

        Label messageLabel = new Label(valueOrDash(notification.getMessage()));
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #4E6874; -fx-font-size: 13px; -fx-font-weight: 700;");

        Label statusChangeLabel = new Label(
                normalizeStatus(notification.getPreviousStatus()) + " -> " + normalizeStatus(notification.getNewStatus())
        );
        statusChangeLabel.setStyle("-fx-text-fill: #0F6A58; -fx-font-size: 12px; -fx-font-weight: 800;");

        card.getChildren().addAll(header, messageLabel, statusChangeLabel);
        return card;
    }

    private void renderNotificationEmptyState(String message) {
        if (notificationsMenuContainer == null) {
            return;
        }
        Label emptyLabel = new Label(message);
        emptyLabel.setWrapText(true);
        emptyLabel.setStyle("-fx-text-fill: #7A909B; -fx-font-size: 13px; -fx-font-weight: 700;");
        notificationsMenuContainer.getChildren().setAll(emptyLabel);
    }

    private void startNotificationPolling() {
        if (notificationTimeline != null) {
            notificationTimeline.play();
        }
    }

    private void stopNotificationPolling() {
        if (notificationTimeline != null) {
            notificationTimeline.stop();
        }
    }

    private void clearTrackedEmailState() {
        trackedEmail = null;
        lastNotificationId = 0;
        unreadNotificationCount = 0;
        visibleNotifications.clear();
        renderNotificationEmptyState("Status notifications will appear here.");
        updateNotificationBadge();
    }

    private void hideNotificationsMenu() {
        if (notificationsMenu != null) {
            notificationsMenu.hide();
        }
    }

    private boolean isNotificationsMenuOpen() {
        return notificationsMenu != null && notificationsMenu.isShowing();
    }

    private void updateNotificationBadge() {
        if (notificationBadgeLabel == null) {
            return;
        }

        int safeUnread = Math.max(unreadNotificationCount, 0);
        if (safeUnread == 0) {
            notificationBadgeLabel.setVisible(false);
            notificationBadgeLabel.setManaged(false);
            notificationBadgeLabel.setText("");
            return;
        }

        notificationBadgeLabel.setVisible(true);
        notificationBadgeLabel.setManaged(true);
        notificationBadgeLabel.setText(safeUnread > 99 ? "99+" : Integer.toString(safeUnread));
    }

    private void setNotificationHint(String hint) {
        if (notificationsMenuHintLabel != null) {
            notificationsMenuHintLabel.setText(hint == null ? "" : hint);
        }
    }

    private void refreshNotificationsMenu() {
        renderNotifications();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ON_PROGRESS";
        }
        String normalized = status.trim().toUpperCase();
        return "PLACED".equals(normalized) ? "ON_PROGRESS" : normalized;
    }

    private String formatPrice(BigDecimal amount) {
        if (amount == null) {
            return "0.00 DT";
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private String formatOrderDate(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "-";
        }
        return createdAt.format(DATE_FORMATTER);
    }

    private String formatNotificationDate(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "-";
        }
        return createdAt.format(NOTIFICATION_TIME_FORMATTER);
    }

    private String resolveStatusBadgeStyle(String status) {
        String normalizedStatus = normalizeStatus(status);
        if ("DELIVERED".equals(normalizedStatus)) {
            return "-fx-background-color: #E7F8F0; -fx-background-radius: 999; -fx-text-fill: #0D8E63; "
                    + "-fx-font-size: 12px; -fx-font-weight: 900; -fx-padding: 4 10 4 10;";
        }
        if ("CANCELED".equals(normalizedStatus) || "CANCELLED".equals(normalizedStatus)) {
            return "-fx-background-color: #FDEBEC; -fx-background-radius: 999; -fx-text-fill: #C5343F; "
                    + "-fx-font-size: 12px; -fx-font-weight: 900; -fx-padding: 4 10 4 10;";
        }
        return "-fx-background-color: #FFF6E8; -fx-background-radius: 999; -fx-text-fill: #B76A00; "
                + "-fx-font-size: 12px; -fx-font-weight: 900; -fx-padding: 4 10 4 10;";
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void setInfoMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #255D71; -fx-font-size: 14px; -fx-font-weight: 700;");
    }

    private void setSuccessMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #13866A; -fx-font-size: 14px; -fx-font-weight: 800;");
    }

    private void setErrorMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #D92D20; -fx-font-size: 14px; -fx-font-weight: 800;");
    }
}
