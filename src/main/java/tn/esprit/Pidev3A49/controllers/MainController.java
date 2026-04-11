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
import tn.esprit.Pidev3A49.Models.Forum;
import tn.esprit.Pidev3A49.services.ServiceComment;
import tn.esprit.Pidev3A49.services.ServiceForum;

import java.util.List;

public class MainController {

    @FXML private VBox viewForums;
    @FXML private VBox viewComments;
    @FXML private VBox paneForumCreate;
    @FXML private VBox paneForumEdit;
    @FXML private VBox paneForumDelete;
    @FXML private VBox paneForumExplore;
    @FXML private VBox paneCommentCreate;
    @FXML private VBox paneCommentEdit;
    @FXML private VBox paneCommentDelete;
    @FXML private VBox paneCommentExplore;

    @FXML private Button btnModuleForumComment;
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
    @FXML private TableColumn<Forum, String> colForumTitle;
    @FXML private TableColumn<Forum, String> colForumContent;
    @FXML private TableColumn<Forum, Integer> colForumIdEdit;
    @FXML private TableColumn<Forum, String> colForumTitleEdit;
    @FXML private TableColumn<Forum, String> colForumContentEdit;
    @FXML private TableColumn<Forum, Integer> colForumIdDelete;
    @FXML private TableColumn<Forum, String> colForumTitleDelete;
    @FXML private TableColumn<Forum, String> colForumContentDelete;

    @FXML private TextField tfCommentId;
    @FXML private Label lblNoForumsAvailable;
    @FXML private Label lblSelectedForum;
    @FXML private TableView<Forum> tableForumsForComment;
    @FXML private TableColumn<Forum, Integer> colCommentForumSelectId;
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
    @FXML private TableColumn<Comment, String> colCommentContent;
    @FXML private TableColumn<Comment, String> colCommentForum;
    @FXML private TableColumn<Comment, Integer> colCommentIdEdit;
    @FXML private TableColumn<Comment, String> colCommentContentEdit;
    @FXML private TableColumn<Comment, String> colCommentForumEdit;
    @FXML private TableColumn<Comment, Integer> colCommentIdDelete;
    @FXML private TableColumn<Comment, String> colCommentContentDelete;
    @FXML private TableColumn<Comment, String> colCommentForumDelete;

    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceComment serviceComment = new ServiceComment();

    @FXML
    public void initialize() {
        initialiserTableForums();
        initialiserTableComments();
        initialiserComboBoxes();
        initialiserSelections();
        chargerForums();
        chargerComments();
        afficherTableauDeBord();
        afficherCreationForum();
        afficherCreationComment();
    }

    @FXML
    private void afficherTableauDeBord() {
        viewForums.setVisible(true);
        viewForums.setManaged(true);
        viewComments.setVisible(true);
        viewComments.setManaged(true);
        btnModuleForumComment.getStyleClass().setAll("sidebar-nav-button", "sidebar-nav-button-active");
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
    private void rafraichirDonnees() {
        chargerForums();
        chargerComments();
    }

    @FXML
    private void ajouterForum() {
        try {
            serviceForum.add(new Forum(tfForumTitle.getText(), taForumContent.getText()));
            chargerForums();
            viderFormulaireForum();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Forum ajoute avec succes.");
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
            Forum forum = new Forum(
                    Integer.parseInt(tfForumId.getText()),
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
            if (cbCommentForum.getValue() == null) {
                throw new IllegalArgumentException("Selectionne d'abord un forum dans la liste.");
            }
            serviceComment.add(new Comment(taCommentContent.getText(), cbCommentForum.getValue()));
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
            Comment comment = new Comment(
                    Integer.parseInt(tfCommentId.getText()),
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
        initialiserColonnesForum(colForumId, colForumTitle, colForumContent);
        initialiserColonnesForum(colForumIdEdit, colForumTitleEdit, colForumContentEdit);
        initialiserColonnesForum(colForumIdDelete, colForumTitleDelete, colForumContentDelete);
    }

    private void initialiserTableComments() {
        initialiserColonnesForum(colCommentForumSelectId, colCommentForumSelectTitle, colCommentForumSelectContent);
        initialiserColonnesComment(colCommentId, colCommentContent, colCommentForum);
        initialiserColonnesComment(colCommentIdEdit, colCommentContentEdit, colCommentForumEdit);
        initialiserColonnesComment(colCommentIdDelete, colCommentContentDelete, colCommentForumDelete);
    }

    private void initialiserColonnesForum(TableColumn<Forum, Integer> id,
                                          TableColumn<Forum, String> title,
                                          TableColumn<Forum, String> content) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        content.setCellValueFactory(new PropertyValueFactory<>("content"));
    }

    private void initialiserColonnesComment(TableColumn<Comment, Integer> id,
                                            TableColumn<Comment, String> content,
                                            TableColumn<Comment, String> forumTitle) {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        content.setCellValueFactory(new PropertyValueFactory<>("content"));
        forumTitle.setCellValueFactory(new PropertyValueFactory<>("forumTitle"));
    }

    private void initialiserComboBoxes() {
        StringConverter<Forum> converter = new StringConverter<>() {
            @Override
            public String toString(Forum forum) {
                return forum == null ? "" : forum.getId() + " - " + forum.getTitle();
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
                lblForumSelectionDelete.setText(newValue == null ? "Aucun forum selectionne" : "Forum selectionne: " + newValue.getTitle()));

        tableCommentsEdit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                remplirFormulaireCommentModification(newValue);
            }
        });

        tableForumsForComment.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            cbCommentForum.setValue(newValue);
            lblSelectedForum.setText(newValue == null
                    ? "Aucun forum selectionne"
                    : "Forum choisi: " + newValue.getTitle());
        });

        tableCommentsDelete.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                lblCommentSelectionDelete.setText(newValue == null ? "Aucun commentaire selectionne" : "Commentaire selectionne: #" + newValue.getId()));
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
