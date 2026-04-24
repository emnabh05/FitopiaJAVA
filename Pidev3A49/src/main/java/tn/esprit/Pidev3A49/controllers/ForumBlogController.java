package tn.esprit.Pidev3A49.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import tn.esprit.Pidev3A49.Models.Comment;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.Models.Forum;
import tn.esprit.Pidev3A49.services.ServiceComment;
import tn.esprit.Pidev3A49.services.ServiceForum;
import tn.esprit.Pidev3A49.test.UserSession;

import java.util.List;

public class ForumBlogController {

    @FXML private VBox paneForumCreate;
    @FXML private VBox paneForumEdit;
    @FXML private VBox paneForumDelete;
    @FXML private VBox paneForumExplore;
    @FXML private VBox paneCommentCreate;
    @FXML private VBox paneCommentEdit;
    @FXML private VBox paneCommentDelete;
    @FXML private VBox paneCommentExplore;

    @FXML private Button btnActionCreerForum;
    @FXML private Button btnActionModifierForum;
    @FXML private Button btnActionSupprimerForum;
    @FXML private Button btnActionExplorerForum;
    @FXML private Button btnActionCreerComment;
    @FXML private Button btnActionModifierComment;
    @FXML private Button btnActionSupprimerComment;
    @FXML private Button btnActionExplorerComment;

    @FXML private TextField tfForumId;
    @FXML private TextField tfForumTitle;
    @FXML private TextArea taForumContent;
    @FXML private TextField tfForumTitleEdit;
    @FXML private TextArea taForumContentEdit;
    @FXML private Label lblForumSelectionDelete;
    @FXML private TableView<Forum> tableForums;
    @FXML private TableView<Forum> tableForumsEdit;
    @FXML private TableView<Forum> tableForumsDelete;
    @FXML private TableColumn<Forum, Integer> colForumId;
    @FXML private TableColumn<Forum, String> colForumAuthor;
    @FXML private TableColumn<Forum, String> colForumTitle;
    @FXML private TableColumn<Forum, String> colForumContent;
    @FXML private TableColumn<Forum, Integer> colForumIdEdit;
    @FXML private TableColumn<Forum, String> colForumAuthorEdit;
    @FXML private TableColumn<Forum, String> colForumTitleEdit;
    @FXML private TableColumn<Forum, String> colForumContentEdit;
    @FXML private TableColumn<Forum, Integer> colForumIdDelete;
    @FXML private TableColumn<Forum, String> colForumAuthorDelete;
    @FXML private TableColumn<Forum, String> colForumTitleDelete;
    @FXML private TableColumn<Forum, String> colForumContentDelete;

    @FXML private TextField tfCommentId;
    @FXML private Label lblNoForumsAvailable;
    @FXML private Label lblSelectedForum;
    @FXML private TableView<Forum> tableForumsForComment;
    @FXML private TableColumn<Forum, Integer> colCommentForumSelectId;
    @FXML private TableColumn<Forum, String> colCommentForumSelectAuthor;
    @FXML private TableColumn<Forum, String> colCommentForumSelectTitle;
    @FXML private TableColumn<Forum, String> colCommentForumSelectContent;
    @FXML private TextArea taCommentContent;
    @FXML private ComboBox<Forum> cbCommentForum;
    @FXML private TextArea taCommentContentEdit;
    @FXML private ComboBox<Forum> cbCommentForumEdit;
    @FXML private Label lblCommentSelectionDelete;
    @FXML private TableView<Comment> tableComments;
    @FXML private TableView<Comment> tableCommentsEdit;
    @FXML private TableView<Comment> tableCommentsDelete;
    @FXML private TableColumn<Comment, Integer> colCommentId;
    @FXML private TableColumn<Comment, String> colCommentAuthor;
    @FXML private TableColumn<Comment, String> colCommentContent;
    @FXML private TableColumn<Comment, String> colCommentForum;
    @FXML private TableColumn<Comment, Integer> colCommentIdEdit;
    @FXML private TableColumn<Comment, String> colCommentAuthorEdit;
    @FXML private TableColumn<Comment, String> colCommentContentEdit;
    @FXML private TableColumn<Comment, String> colCommentForumEdit;
    @FXML private TableColumn<Comment, Integer> colCommentIdDelete;
    @FXML private TableColumn<Comment, String> colCommentAuthorDelete;
    @FXML private TableColumn<Comment, String> colCommentContentDelete;
    @FXML private TableColumn<Comment, String> colCommentForumDelete;

    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceComment serviceComment = new ServiceComment();
    private FitopiaUser currentUser;

    @FXML
    public void initialize() {
        currentUser = UserSession.getCurrentUser();
        initialiserTableForums();
        initialiserTableComments();
        initialiserComboBoxes();
        initialiserSelections();
        chargerForums();
        chargerComments();
        afficherCreationForum();
        afficherCreationComment();
    }

    @FXML
    private void rafraichirDonneesForum() {
        currentUser = UserSession.getCurrentUser();
        chargerForums();
        chargerComments();
    }

    @FXML private void afficherCreationForum() { afficherPaneForum(paneForumCreate, btnActionCreerForum); }
    @FXML private void afficherModificationForum() { afficherPaneForum(paneForumEdit, btnActionModifierForum); }
    @FXML private void afficherSuppressionForum() { afficherPaneForum(paneForumDelete, btnActionSupprimerForum); }
    @FXML private void afficherExplorationForum() { afficherPaneForum(paneForumExplore, btnActionExplorerForum); }
    @FXML private void afficherCreationComment() { afficherPaneComment(paneCommentCreate, btnActionCreerComment); }
    @FXML private void afficherModificationComment() { afficherPaneComment(paneCommentEdit, btnActionModifierComment); }
    @FXML private void afficherSuppressionComment() { afficherPaneComment(paneCommentDelete, btnActionSupprimerComment); }
    @FXML private void afficherExplorationComment() { afficherPaneComment(paneCommentExplore, btnActionExplorerComment); }

    @FXML
    private void ajouterForum() {
        try {
            ensureCurrentUser();
            Forum forum = new Forum(tfForumTitle.getText(), taForumContent.getText());
            forum.setUserId(currentUser.getId());
            serviceForum.add(forum);
            chargerForums();
            viderFormulaireForum();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Forum ajoute avec succes pour " + displayName(currentUser) + ".");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur forum", exception.getMessage());
        }
    }

    @FXML
    private void modifierForum() {
        try {
            if (tfForumId.getText().isBlank()) {
                throw new IllegalArgumentException("Selectionne un forum a modifier.");
            }
            Forum selected = tableForumsEdit.getSelectionModel().getSelectedItem();
            if (selected == null) {
                throw new IllegalArgumentException("Selectionne un forum a modifier.");
            }
            Forum forum = new Forum(
                    Integer.parseInt(tfForumId.getText()),
                    selected.getUserId(),
                    tfForumTitleEdit.getText(),
                    taForumContentEdit.getText()
            );
            serviceForum.update(forum);
            chargerForums();
            chargerComments();
            viderFormulaireForumModification();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Forum modifie avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur forum", exception.getMessage());
        }
    }

    @FXML
    private void supprimerForum() {
        try {
            Forum forum = tableForumsDelete.getSelectionModel().getSelectedItem();
            if (forum == null) {
                throw new IllegalArgumentException("Selectionne un forum a supprimer.");
            }
            serviceForum.delete(forum);
            chargerForums();
            chargerComments();
            viderFormulaireForumModification();
            lblForumSelectionDelete.setText("Aucun forum selectionne");
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Forum supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur forum", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireForum() {
        tfForumTitle.clear();
        taForumContent.clear();
    }

    @FXML
    private void viderFormulaireForumModification() {
        tfForumId.clear();
        tfForumTitleEdit.clear();
        taForumContentEdit.clear();
        tableForumsEdit.getSelectionModel().clearSelection();
    }

    @FXML
    private void ajouterComment() {
        try {
            ensureCurrentUser();
            if (cbCommentForum.getValue() == null) {
                throw new IllegalArgumentException("Selectionne d'abord un forum dans la liste.");
            }
            Comment comment = new Comment(taCommentContent.getText(), cbCommentForum.getValue());
            comment.setUserId(currentUser.getId());
            serviceComment.add(comment);
            chargerComments();
            viderFormulaireComment();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Commentaire ajoute avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    @FXML
    private void modifierComment() {
        try {
            if (tfCommentId.getText().isBlank()) {
                throw new IllegalArgumentException("Selectionne un commentaire a modifier.");
            }
            Comment selected = tableCommentsEdit.getSelectionModel().getSelectedItem();
            if (selected == null) {
                throw new IllegalArgumentException("Selectionne un commentaire a modifier.");
            }
            Comment comment = new Comment(
                    Integer.parseInt(tfCommentId.getText()),
                    selected.getUserId(),
                    taCommentContentEdit.getText(),
                    cbCommentForumEdit.getValue()
            );
            serviceComment.update(comment);
            chargerComments();
            viderFormulaireCommentModification();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Commentaire modifie avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    @FXML
    private void supprimerComment() {
        try {
            Comment comment = tableCommentsDelete.getSelectionModel().getSelectedItem();
            if (comment == null) {
                throw new IllegalArgumentException("Selectionne un commentaire a supprimer.");
            }
            serviceComment.delete(comment);
            chargerComments();
            viderFormulaireCommentModification();
            lblCommentSelectionDelete.setText("Aucun commentaire selectionne");
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Commentaire supprime avec succes.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    @FXML
    private void viderFormulaireComment() {
        taCommentContent.clear();
        cbCommentForum.getSelectionModel().clearSelection();
        tableForumsForComment.getSelectionModel().clearSelection();
        lblSelectedForum.setText("Aucun forum selectionne");
    }

    @FXML
    private void viderFormulaireCommentModification() {
        tfCommentId.clear();
        taCommentContentEdit.clear();
        cbCommentForumEdit.getSelectionModel().clearSelection();
        tableCommentsEdit.getSelectionModel().clearSelection();
    }

    private void initialiserTableForums() {
        initialiserColonnesForum(colForumId, colForumAuthor, colForumTitle, colForumContent);
        initialiserColonnesForum(colForumIdEdit, colForumAuthorEdit, colForumTitleEdit, colForumContentEdit);
        initialiserColonnesForum(colForumIdDelete, colForumAuthorDelete, colForumTitleDelete, colForumContentDelete);
    }

    private void initialiserTableComments() {
        initialiserColonnesForum(colCommentForumSelectId, colCommentForumSelectAuthor, colCommentForumSelectTitle, colCommentForumSelectContent);
        initialiserColonnesComment(colCommentId, colCommentAuthor, colCommentContent, colCommentForum);
        initialiserColonnesComment(colCommentIdEdit, colCommentAuthorEdit, colCommentContentEdit, colCommentForumEdit);
        initialiserColonnesComment(colCommentIdDelete, colCommentAuthorDelete, colCommentContentDelete, colCommentForumDelete);
    }

    private void initialiserColonnesForum(TableColumn<Forum, Integer> id,
                                          TableColumn<Forum, String> author,
                                          TableColumn<Forum, String> title,
                                          TableColumn<Forum, String> content) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        author.setCellValueFactory(new PropertyValueFactory<>("authorDisplayName"));
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        content.setCellValueFactory(new PropertyValueFactory<>("content"));
    }

    private void initialiserColonnesComment(TableColumn<Comment, Integer> id,
                                            TableColumn<Comment, String> author,
                                            TableColumn<Comment, String> content,
                                            TableColumn<Comment, String> forumTitle) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        author.setCellValueFactory(new PropertyValueFactory<>("authorDisplayName"));
        content.setCellValueFactory(new PropertyValueFactory<>("content"));
        forumTitle.setCellValueFactory(new PropertyValueFactory<>("forumTitle"));
    }

    private void initialiserComboBoxes() {
        StringConverter<Forum> converter = new StringConverter<>() {
            @Override
            public String toString(Forum forum) {
                return forum == null ? "" : forum.getId() + " - " + forum.getTitle() + " (" + forum.getAuthorDisplayName() + ")";
            }

            @Override
            public Forum fromString(String string) {
                return null;
            }
        };

        cbCommentForum.setConverter(converter);
        cbCommentForumEdit.setConverter(converter);
        rafraichirComboForums();
    }

    private void initialiserSelections() {
        tableForumsEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireForumModification(newValue);
            }
        });

        tableForumsDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblForumSelectionDelete.setText(newValue == null
                        ? "Aucun forum selectionne"
                        : "Forum selectionne: " + newValue.getTitle() + " par " + newValue.getAuthorDisplayName()));

        tableCommentsEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireCommentModification(newValue);
            }
        });

        tableForumsForComment.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            cbCommentForum.setValue(newValue);
            lblSelectedForum.setText(newValue == null
                    ? "Aucun forum selectionne"
                    : "Forum choisi: " + newValue.getTitle() + " par " + newValue.getAuthorDisplayName());
        });

        tableCommentsDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblCommentSelectionDelete.setText(newValue == null
                        ? "Aucun commentaire selectionne"
                        : "Commentaire selectionne: #" + newValue.getId() + " par " + newValue.getAuthorDisplayName()));
    }

    private void chargerForums() {
        List<Forum> forums = serviceForum.getAll();
        tableForums.setItems(FXCollections.observableArrayList(forums));
        tableForumsEdit.setItems(FXCollections.observableArrayList(forums));
        tableForumsDelete.setItems(FXCollections.observableArrayList(forums));
        tableForumsForComment.setItems(FXCollections.observableArrayList(forums));
        boolean hasForums = !forums.isEmpty();
        lblNoForumsAvailable.setVisible(!hasForums);
        lblNoForumsAvailable.setManaged(!hasForums);
        tableForumsForComment.setVisible(hasForums);
        tableForumsForComment.setManaged(hasForums);
        if (!hasForums) {
            cbCommentForum.getSelectionModel().clearSelection();
            lblSelectedForum.setText("Aucun forum selectionne");
        }
        rafraichirComboForums();
    }

    private void chargerComments() {
        List<Comment> comments = serviceComment.getAll();
        tableComments.setItems(FXCollections.observableArrayList(comments));
        tableCommentsEdit.setItems(FXCollections.observableArrayList(comments));
        tableCommentsDelete.setItems(FXCollections.observableArrayList(comments));
    }

    private void rafraichirComboForums() {
        List<Forum> forums = serviceForum.getAll();
        cbCommentForum.setItems(FXCollections.observableArrayList(forums));
        cbCommentForumEdit.setItems(FXCollections.observableArrayList(forums));
    }

    private void remplirFormulaireForumModification(Forum forum) {
        tfForumId.setText(String.valueOf(forum.getId()));
        tfForumTitleEdit.setText(forum.getTitle());
        taForumContentEdit.setText(forum.getContent());
    }

    private void remplirFormulaireCommentModification(Comment comment) {
        tfCommentId.setText(String.valueOf(comment.getId()));
        taCommentContentEdit.setText(comment.getContent());
        selectionnerForumDansCombo(cbCommentForumEdit, comment.getForumId());
    }

    private void selectionnerForumDansCombo(ComboBox<Forum> comboBox, Integer forumId) {
        if (forumId == null) {
            comboBox.getSelectionModel().clearSelection();
            return;
        }
        for (Forum forum : comboBox.getItems()) {
            if (forum.getId() == forumId) {
                comboBox.setValue(forum);
                return;
            }
        }
        comboBox.getSelectionModel().clearSelection();
    }

    private void afficherPaneForum(VBox paneActif, VBox... vues) {
        afficherVue(paneActif, vues);
    }

    private void afficherPaneForum(VBox paneActif, Button boutonActif) {
        afficherVue(paneActif, paneForumCreate, paneForumEdit, paneForumDelete, paneForumExplore);
        activerBoutonAction(boutonActif, btnActionCreerForum, btnActionModifierForum, btnActionSupprimerForum, btnActionExplorerForum);
    }

    private void afficherPaneComment(VBox paneActif, Button boutonActif) {
        afficherVue(paneActif, paneCommentCreate, paneCommentEdit, paneCommentDelete, paneCommentExplore);
        activerBoutonAction(boutonActif, btnActionCreerComment, btnActionModifierComment, btnActionSupprimerComment, btnActionExplorerComment);
    }

    private void afficherVue(VBox vueActive, VBox... vues) {
        for (VBox vue : vues) {
            boolean active = vue == vueActive;
            vue.setVisible(active);
            vue.setManaged(active);
        }
    }

    private void activerBoutonAction(Button boutonActif, Button... boutons) {
        for (Button button : boutons) {
            if (button == boutonActif) {
                button.getStyleClass().setAll("action-card", "action-card-active");
            } else {
                button.getStyleClass().setAll("action-card");
            }
        }
    }

    private void ensureCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur connecte.");
        }
    }

    private String displayName(FitopiaUser user) {
        if (user == null) {
            return "Utilisateur inconnu";
        }
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return "Utilisateur inconnu";
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
