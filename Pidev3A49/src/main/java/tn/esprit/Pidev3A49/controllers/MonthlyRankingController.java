package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.Pidev3A49.services.ServiceSupplementAdherence;
import tn.esprit.Pidev3A49.utils.AppSession;
import tn.esprit.Pidev3A49.utils.SceneNavigator;
import tn.esprit.Pidev3A49.utils.SessionRouter;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonthlyRankingController {

    private static final DateTimeFormatter MONTH_INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final List<DateTimeFormatter> FALLBACK_MONTH_FORMATTERS = buildMonthFormatters();

    @FXML private TextField monthInputField;
    @FXML private Label selectedMonthLabel;
    @FXML private Label rankingNotificationLabel;
    @FXML private VBox leaderboardRowsContainer;
    @FXML private Label leaderboardEmptyLabel;

    private final AppSession appSession = AppSession.getInstance();
    private final ServiceSupplementAdherence adherenceService = new ServiceSupplementAdherence();

    @FXML
    private void initialize() {
        if (!SessionRouter.ensureAuthenticated(monthInputField)) {
            return;
        }
        monthInputField.setText(YearMonth.now().format(MONTH_INPUT_FORMAT));
        loadMonthRanking();
    }

    public void openProgressTracker(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.RANKING_VIEW, SceneNavigator.PROGRESS_VIEW);
    }

    public void openSupplementShowcase(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.RANKING_VIEW, SceneNavigator.FRONT_END_VIEW);
    }

    @FXML
    public void openBackEnd(ActionEvent event) throws IOException {
        SessionRouter.logoutToSignIn((Node) event.getSource());
    }

    @FXML
    public void openMyOrders(ActionEvent event) throws IOException {
        SceneNavigator.navigate(event, SceneNavigator.RANKING_VIEW, SceneNavigator.FRONT_ORDERS_VIEW);
    }

    @FXML
    public void goBackOrExit(ActionEvent event) throws IOException {
        SceneNavigator.goBackOrClose(event);
    }

    @FXML
    private void loadMonthRanking() {
        YearMonth month = parseMonth(monthInputField.getText());
        if (month == null) {
            month = YearMonth.now();
            monthInputField.setText(month.format(MONTH_INPUT_FORMAT));
        }

        ServiceSupplementAdherence.LeaderboardResult result =
                adherenceService.getLeaderboard(month, appSession.getEmail());

        selectedMonthLabel.setText("Month: " + result.month().format(MONTH_DISPLAY_FORMAT));
        rankingNotificationLabel.setText(result.notifications().isEmpty()
                ? "Ranking is based on active streak, consistency %, then goal completion."
                : String.join("  ", result.notifications()));

        renderLeaderboard(result.entries());
    }

    private void renderLeaderboard(List<ServiceSupplementAdherence.LeaderboardEntry> entries) {
        leaderboardRowsContainer.getChildren().clear();
        if (entries == null || entries.isEmpty()) {
            leaderboardEmptyLabel.setText("No adherence logs were found for this month yet.");
            leaderboardEmptyLabel.setVisible(true);
            leaderboardEmptyLabel.setManaged(true);
            return;
        }

        leaderboardEmptyLabel.setVisible(false);
        leaderboardEmptyLabel.setManaged(false);

        leaderboardRowsContainer.getChildren().add(buildHeaderRow());
        for (ServiceSupplementAdherence.LeaderboardEntry entry : entries) {
            leaderboardRowsContainer.getChildren().add(buildEntryRow(entry));
        }
    }

    private HBox buildHeaderRow() {
        HBox header = new HBox(12.0);
        header.setPadding(new Insets(10.0, 14.0, 10.0, 14.0));
        header.setStyle("-fx-background-color: #F2F8FC; -fx-background-radius: 12; -fx-border-color: #D8E6EF; -fx-border-radius: 12;");

        Label rank = createColumnLabel("#", 48.0, true);
        Label user = createColumnLabel("User", 260.0, true);
        Label streak = createColumnLabel("Longest Active Streak", 190.0, true);
        Label consistency = createColumnLabel("Consistency %", 160.0, true);
        Label goal = createColumnLabel("Goal Completion %", 170.0, true);

        header.getChildren().addAll(rank, user, stretch(), streak, consistency, goal);
        return header;
    }

    private HBox buildEntryRow(ServiceSupplementAdherence.LeaderboardEntry entry) {
        HBox row = new HBox(12.0);
        row.setPadding(new Insets(12.0, 14.0, 12.0, 14.0));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #DDE8EF; -fx-border-radius: 12;");

        Label rank = createColumnLabel("#" + entry.rank(), 48.0, false);
        Label user = createColumnLabel(entry.displayName(), 260.0, false);
        Label streak = createColumnLabel(entry.longestActiveStreak() + " days", 190.0, false);
        Label consistency = createColumnLabel(formatPercent(entry.consistencyPercent()), 160.0, false);
        Label goal = createColumnLabel(formatPercent(entry.goalCompletionPercent()), 170.0, false);

        row.getChildren().addAll(rank, user, stretch(), streak, consistency, goal);
        return row;
    }

    private Label createColumnLabel(String text, double width, boolean header) {
        Label label = new Label(text == null ? "-" : text);
        label.setMinWidth(width);
        label.setPrefWidth(width);
        label.setStyle(header
                ? "-fx-text-fill: #6C7E8A; -fx-font-size: 12px; -fx-font-weight: 900;"
                : "-fx-text-fill: #173A4A; -fx-font-size: 13px; -fx-font-weight: 700;");
        return label;
    }

    private Region stretch() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value);
    }

    private YearMonth parseMonth(String rawMonth) {
        if (rawMonth == null || rawMonth.isBlank()) {
            return YearMonth.now();
        }

        String trimmedMonth = rawMonth.trim();
        try {
            return YearMonth.parse(trimmedMonth, MONTH_INPUT_FORMAT);
        } catch (DateTimeParseException ignored) {
        }

        for (DateTimeFormatter formatter : FALLBACK_MONTH_FORMATTERS) {
            try {
                return YearMonth.parse(trimmedMonth, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static List<DateTimeFormatter> buildMonthFormatters() {
        List<DateTimeFormatter> formatters = new ArrayList<>();

        formatters.add(new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM yyyy")
                .toFormatter(Locale.ENGLISH));

        formatters.add(new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM yyyy")
                .toFormatter(Locale.ENGLISH));

        formatters.add(new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM yyyy")
                .toFormatter(Locale.FRENCH));

        formatters.add(new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MM/yyyy")
                .toFormatter(Locale.ENGLISH));

        return formatters;
    }
}
