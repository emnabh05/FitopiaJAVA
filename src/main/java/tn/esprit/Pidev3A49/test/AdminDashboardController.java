package tn.esprit.Pidev3A49.test;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.services.ServiceUser;

public class AdminDashboardController {
    @FXML private Label statusLabel;
    @FXML private Label adminLabel;
    @FXML private TableView<FitopiaUser> usersTable;
    @FXML private TableColumn<FitopiaUser, Number> idCol;
    @FXML private TableColumn<FitopiaUser, String> nomCol;
    @FXML private TableColumn<FitopiaUser, String> prenomCol;
    @FXML private TableColumn<FitopiaUser, String> usernameCol;
    @FXML private TableColumn<FitopiaUser, String> emailCol;
    @FXML private TableColumn<FitopiaUser, String> roleCol;
    @FXML private TableColumn<FitopiaUser, String> phoneCol;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ObservableList<FitopiaUser> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        FitopiaUser current = UserSession.getCurrentUser();
        adminLabel.setText(current == null ? "Admin" : current.getEmail());
        setupTable();
        loadUsers();
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleLogout() {
        UserSession.clear();
        SceneNavigator.goTo(statusLabel, "/SignIn.fxml", "Sign In", 1460, 860);
    }

    private void setupTable() {
        idCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        nomCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getLastName())));
        prenomCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getFirstName())));
        usernameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getUsername())));
        emailCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getEmail())));
        roleCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getRole())));
        phoneCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(safe(c.getValue().getPhone())));
        usersTable.setItems(users);
    }

    private void loadUsers() {
        if (!serviceUser.isAvailable()) {
            users.clear();
            statusLabel.setText("Connexion MySQL indisponible. Verifiez fitopiabd.");
            return;
        }
        users.setAll(serviceUser.getAll());
        statusLabel.setText("Total utilisateurs: " + users.size());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
