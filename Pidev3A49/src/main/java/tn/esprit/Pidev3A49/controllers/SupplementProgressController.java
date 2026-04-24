package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.Pidev3A49.services.ServiceSupplementAdherence;
import tn.esprit.Pidev3A49.utils.AppSession;
import tn.esprit.Pidev3A49.utils.SceneNavigator;
import tn.esprit.Pidev3A49.utils.SessionRouter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SupplementProgressController {

    private static final String INFO_STYLE =
            "-fx-text-fill: #0F6A58; -fx-font-size: 13px; -fx-font-weight: 700;";
    private static final String WARNING_STYLE =
            "-fx-text-fill: #D07145; -fx-font-size: 13px; -fx-font-weight: 700;";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML private Label trackedSupplementsValueLabel;
    @FXML private Label averageComplianceValueLabel;
    @FXML private Label currentMaxStreakValueLabel;
    @FXML private Label atRiskSupplementsValueLabel;
    @FXML private Label goalCompletionSummaryValueLabel;

    @FXML private Label smartNotificationsLabel;

    @FXML private Label supplementBrandLabel;
    @FXML private Label supplementNameLabel;
    @FXML private Label supplementMetaLabel;
    @FXML private Label riskStatusLabel;
    @FXML private Label complianceValueLabel;
    @FXML private Label complianceDetailLabel;
    @FXML private Label currentStreakValueLabel;
    @FXML private Label bestStreakValueLabel;
    @FXML private Label estimatedProgressValueLabel;
    @FXML private Label resultDateValueLabel;
    @FXML private Label durationValueLabel;
    @FXML private Label lastGapValueLabel;
    @FXML private Label weeklyConsistencyValueLabel;
    @FXML private Label monthlyConsistencyValueLabel;
    @FXML private Label goalCompletionValueLabel;
    @FXML private Label safetyReminderLabel;
    @FXML private Label disciplineAlertLabel;
    @FXML private Label todayLogStatusLabel;
    @FXML private Button markTakenButton;

    private final AppSession appSession = AppSession.getInstance();
    private final ServiceSupplementAdherence adherenceService = new ServiceSupplementAdherence();
    private ServiceSupplementAdherence.SupplementProgressItem currentFocusItem;

    @FXML
    private void initialize() {
        if (!SessionRouter.ensureAuthenticated(markTakenButton)) {
            return;
        }
        refreshDashboard();
    }

    public void openSupplementShowcase(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.PROGRESS_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    public void openMonthlyRanking(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.PROGRESS_VIEW, SceneNavigator.RANKING_VIEW);
    }

    @FXML
    public void openBackEnd(ActionEvent event) throws IOException {
        SessionRouter.logoutToSignIn((Node) event.getSource());
    }

    @FXML
    public void openMyOrders(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.PROGRESS_VIEW, SceneNavigator.FRONT_ORDERS_VIEW);
    }

    @FXML
    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    @FXML
    private void markAsTakenToday() {
        if (currentFocusItem == null) {
            setTodayStatus("No active supplement plan found.", true);
            return;
        }

        ServiceSupplementAdherence.LogResult result = adherenceService.markTakenToday(
                appSession.getEmail(),
                currentFocusItem.planId(),
                LocalDate.now()
        );

        setTodayStatus(result.message(), result.warning());
        refreshDashboard();
    }

    private void refreshDashboard() {
        ServiceSupplementAdherence.ProgressDashboard dashboard =
                adherenceService.getProgressDashboard(appSession.getEmail(), LocalDate.now());

        trackedSupplementsValueLabel.setText(Integer.toString(dashboard.trackedSupplements()));
        averageComplianceValueLabel.setText(formatPercent(dashboard.averageCompliancePercent()));
        currentMaxStreakValueLabel.setText(dashboard.currentMaxStreakDays() + "d");
        atRiskSupplementsValueLabel.setText(Integer.toString(dashboard.atRiskSupplements()));
        goalCompletionSummaryValueLabel.setText(formatPercent(dashboard.goalCompletionPercent()));

        List<String> notifications = dashboard.notifications();
        if (notifications.isEmpty()) {
            smartNotificationsLabel.setText("No alerts yet. Log consistently to build streak-based insights.");
        } else {
            smartNotificationsLabel.setText(String.join("\n", notifications));
        }

        if (dashboard.supplementItems().isEmpty()) {
            currentFocusItem = null;
            renderEmptyState();
            return;
        }

        currentFocusItem = dashboard.supplementItems().get(0);
        renderFocusSupplement(currentFocusItem);
    }

    private void renderFocusSupplement(ServiceSupplementAdherence.SupplementProgressItem item) {
        supplementBrandLabel.setText(item.supplementBrand());
        supplementNameLabel.setText(item.supplementName());
        supplementMetaLabel.setText(
                "Tracked since " + DATE_FORMATTER.format(item.startDate())
                        + " | Plan: " + item.plannedDays() + " days"
                        + " | Daily limit: " + item.dailyTargetUnits()
        );

        riskStatusLabel.setText(item.riskLevel());
        riskStatusLabel.setStyle(item.atRisk()
                ? "-fx-background-color: #FFF2E9; -fx-background-radius: 999; -fx-border-color: #FFD7C4; -fx-border-radius: 999; -fx-text-fill: #C86A3B; -fx-font-size: 12px; -fx-font-weight: 900; -fx-padding: 10 16 10 16;"
                : "-fx-background-color: #EAF8F3; -fx-background-radius: 999; -fx-border-color: #CBECDD; -fx-border-radius: 999; -fx-text-fill: #0F7A55; -fx-font-size: 12px; -fx-font-weight: 900; -fx-padding: 10 16 10 16;");

        complianceValueLabel.setText(formatPercent(item.monthlyConsistencyPercent()));
        complianceDetailLabel.setText("Consistency this month");
        currentStreakValueLabel.setText(item.currentStreakDays() + " days");
        bestStreakValueLabel.setText(item.bestStreakDays() + " days");
        estimatedProgressValueLabel.setText(formatPercent(item.monthlyConsistencyPercent()));
        resultDateValueLabel.setText(item.estimatedResultDate() == null ? "N/A" : DATE_FORMATTER.format(item.estimatedResultDate()));
        durationValueLabel.setText(item.plannedDays() + " days");
        lastGapValueLabel.setText(item.lastIntakeGapDays() + " days");
        weeklyConsistencyValueLabel.setText(formatPercent(item.weeklyConsistencyPercent()));
        monthlyConsistencyValueLabel.setText(formatPercent(item.monthlyConsistencyPercent()));
        goalCompletionValueLabel.setText(formatPercent(item.goalCompletionPercent()));
        safetyReminderLabel.setText(item.safetyReminder());

        if (item.loggedToday()) {
            disciplineAlertLabel.setText("Great consistency. Keep following your planned intake only.");
            todayLogStatusLabel.setText("Today logged safely.");
            todayLogStatusLabel.setStyle(INFO_STYLE);
        } else {
            disciplineAlertLabel.setText("You missed your supplement today. Resume now to protect your streak.");
            todayLogStatusLabel.setText("Not logged yet for today.");
            todayLogStatusLabel.setStyle(WARNING_STYLE);
        }

        markTakenButton.setDisable(item.loggedToday());
    }

    private void renderEmptyState() {
        supplementBrandLabel.setText("-");
        supplementNameLabel.setText("No supplement plan found");
        supplementMetaLabel.setText("Place an order first. A safe adherence plan will be generated automatically.");
        riskStatusLabel.setText("NO DATA");
        complianceValueLabel.setText("0%");
        complianceDetailLabel.setText("Consistency this month");
        currentStreakValueLabel.setText("0 days");
        bestStreakValueLabel.setText("0 days");
        estimatedProgressValueLabel.setText("0%");
        resultDateValueLabel.setText("N/A");
        durationValueLabel.setText("N/A");
        lastGapValueLabel.setText("N/A");
        weeklyConsistencyValueLabel.setText("0%");
        monthlyConsistencyValueLabel.setText("0%");
        goalCompletionValueLabel.setText("0%");
        safetyReminderLabel.setText("More is not always better. Follow your plan when it becomes available.");
        disciplineAlertLabel.setText("No adherence logs available yet.");
        todayLogStatusLabel.setText("No active plan for today.");
        todayLogStatusLabel.setStyle(WARNING_STYLE);
        markTakenButton.setDisable(true);
    }

    private void setTodayStatus(String message, boolean warning) {
        todayLogStatusLabel.setText(message);
        todayLogStatusLabel.setStyle(warning ? WARNING_STYLE : INFO_STYLE);
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }
}
