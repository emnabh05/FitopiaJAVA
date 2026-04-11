package tn.esprit.Pidev3A49.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.Pidev3A49.Models.Comment;
import tn.esprit.Pidev3A49.Models.Forum;
import tn.esprit.Pidev3A49.controllers.MainController;
import tn.esprit.Pidev3A49.services.ServiceComment;
import tn.esprit.Pidev3A49.services.ServiceForum;

import java.io.IOException;
import java.util.List;

public class FrontFeedController {

    @FXML private VBox feedContainer;
    @FXML private ComboBox<Forum> cbCommentForum;
    @FXML private TextField tfForumTitle;
    @FXML private TextArea taForumContent;
    @FXML private TextArea taCommentContent;
    @FXML private Label lblFeedStats;
    @FXML private Label lblCommentContext;
    @FXML private TextField tfSearch;
    @FXML private ScrollPane feedScrollPane;

    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceComment serviceComment = new ServiceComment();

    private Integer selectedForumId;
    private Integer selectedCommentId;

    @FXML
    public void initialize() {
        cbCommentForum.setConverter(new StringConverter<>() {
            @Override
            public String toString(Forum forum) {
                return forum == null ? "" : forum.getTitle();
            }

            @Override
            public Forum fromString(String string) {
                return null;
            }
        });

        refreshData();
    }

    @FXML
    private void refreshData() {
        loadForumsIntoFeed();
        loadForumChoices();
        clearForumEditor();
        clearCommentEditor();
    }

    @FXML
    private void saveForum() {
        try {
            Forum forum = new Forum(tfForumTitle.getText(), taForumContent.getText());
            if (selectedForumId == null) {
                serviceForum.add(forum);
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Forum ajoute.");
            } else {
                forum.setId(selectedForumId);
                serviceForum.update(forum);
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Forum modifie.");
            }
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur forum", exception.getMessage());
        }
    }

    @FXML
    private void deleteSelectedForum() {
        try {
            if (selectedForumId == null) {
                throw new IllegalArgumentException("Selectionne un forum depuis le feed.");
            }
            Forum forum = serviceForum.getById(selectedForumId);
            if (forum == null) {
                throw new IllegalArgumentException("Forum introuvable.");
            }
            serviceForum.delete(forum);
            refreshData();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Forum supprime.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur forum", exception.getMessage());
        }
    }

    @FXML
    private void saveComment() {
        try {
            Forum forum = cbCommentForum.getValue();
            if (forum == null) {
                throw new IllegalArgumentException("Choisis un forum pour le commentaire.");
            }

            Comment comment = new Comment(taCommentContent.getText(), forum);
            if (selectedCommentId == null) {
                serviceComment.add(comment);
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Commentaire ajoute.");
            } else {
                comment.setId(selectedCommentId);
                serviceComment.update(comment);
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Commentaire modifie.");
            }
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    @FXML
    private void deleteSelectedComment() {
        try {
            if (selectedCommentId == null) {
                throw new IllegalArgumentException("Selectionne un commentaire depuis le feed.");
            }
            Comment comment = serviceComment.getById(selectedCommentId);
            if (comment == null) {
                throw new IllegalArgumentException("Commentaire introuvable.");
            }
            serviceComment.delete(comment);
            refreshData();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Commentaire supprime.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    @FXML
    private void clearForumEditor() {
        selectedForumId = null;
        tfForumTitle.clear();
        taForumContent.clear();
    }

    @FXML
    private void clearCommentEditor() {
        selectedCommentId = null;
        taCommentContent.clear();
        cbCommentForum.getSelectionModel().clearSelection();
        lblCommentContext.setText("Aucun commentaire selectionne");
    }

    private void loadForumsIntoFeed() {
        List<Forum> forums = serviceForum.getAll();
        String normalized = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();

        feedContainer.getChildren().clear();

        int visibleForums = 0;
        int totalComments = 0;

        for (Forum forum : forums) {
            if (!normalized.isBlank()
                    && !forum.getTitle().toLowerCase().contains(normalized)
                    && !forum.getContent().toLowerCase().contains(normalized)) {
                continue;
            }

            List<Comment> comments = serviceComment.getByForumId(forum.getId());
            visibleForums++;
            totalComments += comments.size();
            feedContainer.getChildren().add(buildForumCard(forum, comments));
        }

        if (visibleForums == 0) {
            Label empty = new Label("Aucun forum a afficher. Cree un post ou change la recherche.");
            empty.getStyleClass().add("empty-feed");
            empty.setWrapText(true);
            feedContainer.getChildren().add(empty);
        }

        lblFeedStats.setText(visibleForums + " forums visibles | " + totalComments + " commentaires");
    }

    private VBox buildForumCard(Forum forum, List<Comment> comments) {
        VBox card = new VBox(16);
        card.getStyleClass().add("feed-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("F");
        avatar.getStyleClass().add("avatar-badge");

        VBox authorBox = new VBox(4);
        Label author = new Label("community@fitopia.com");
        author.getStyleClass().add("author-name");
        Label meta = new Label("Forum #" + forum.getId() + " | wellness feed");
        meta.getStyleClass().add("author-meta");
        authorBox.getChildren().addAll(author, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button edit = createGhostButton("Edit forum", () -> startEditForum(forum));
        Button delete = createGhostButton("Delete", () -> deleteForum(forum));

        header.getChildren().addAll(avatar, authorBox, spacer, edit, delete);

        Label title = new Label(forum.getTitle());
        title.getStyleClass().add("post-title");
        title.setWrapText(true);

        Label content = new Label(forum.getContent());
        content.getStyleClass().add("post-content");
        content.setWrapText(true);

        Label media = new Label("Feed media preview");
        media.getStyleClass().add("media-placeholder");
        media.setMinHeight(210);
        media.setPrefHeight(210);
        media.setMaxWidth(Double.MAX_VALUE);

        HBox actions = new HBox(10);
        actions.getChildren().addAll(
                createMetricButton("Like"),
                createMetricButton(comments.size() + " comments"),
                createMetricButton("Share"),
                createGhostButton("Reply", () -> prepareCommentForForum(forum))
        );

        VBox commentsBox = new VBox(10);
        commentsBox.getStyleClass().add("comments-box");
        Label commentHeader = new Label("Comments");
        commentHeader.getStyleClass().add("comments-title");
        commentsBox.getChildren().add(commentHeader);

        if (comments.isEmpty()) {
            Label none = new Label("No comments yet. Be the first to reply.");
            none.getStyleClass().add("comment-empty");
            commentsBox.getChildren().add(none);
        } else {
            for (Comment comment : comments) {
                commentsBox.getChildren().add(buildCommentRow(comment));
            }
        }

        VBox quickReplyBox = new VBox(8);
        quickReplyBox.getStyleClass().add("quick-reply-box");
        TextArea quickReply = new TextArea();
        quickReply.setPromptText("Write a quick comment for this forum...");
        quickReply.getStyleClass().add("quick-reply-input");
        quickReply.setPrefRowCount(2);

        HBox quickReplyActions = new HBox(10);
        Button addComment = new Button("Post comment");
        addComment.getStyleClass().add("primary-pill");
        addComment.setOnAction(event -> addQuickComment(forum, quickReply));
        Button syncEditor = createGhostButton("Edit in sidebar", () -> prepareCommentForForum(forum));
        quickReplyActions.getChildren().addAll(addComment, syncEditor);
        quickReplyBox.getChildren().addAll(quickReply, quickReplyActions);

        card.getChildren().addAll(header, title, content, media, actions, commentsBox, quickReplyBox);
        return card;
    }

    private VBox buildCommentRow(Comment comment) {
        VBox box = new VBox(8);
        box.getStyleClass().add("comment-row");

        Label body = new Label(comment.getContent());
        body.getStyleClass().add("comment-body");
        body.setWrapText(true);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label tag = new Label("on " + comment.getForumTitle());
        tag.getStyleClass().add("comment-tag");
        Button edit = createGhostButton("Edit", () -> startEditComment(comment));
        Button delete = createGhostButton("Delete", () -> deleteComment(comment));
        footer.getChildren().addAll(tag, edit, delete);

        box.getChildren().addAll(body, footer);
        return box;
    }

    private Button createMetricButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("metric-pill");
        return button;
    }

    private Button createGhostButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("ghost-pill");
        button.setOnAction(event -> action.run());
        return button;
    }

    private void startEditForum(Forum forum) {
        selectedForumId = forum.getId();
        tfForumTitle.setText(forum.getTitle());
        taForumContent.setText(forum.getContent());
        feedScrollPane.setVvalue(0);
    }

    private void prepareCommentForForum(Forum forum) {
        selectedCommentId = null;
        cbCommentForum.setValue(forum);
        taCommentContent.clear();
        lblCommentContext.setText("Nouveau commentaire pour: " + forum.getTitle());
    }

    private void startEditComment(Comment comment) {
        selectedCommentId = comment.getId();
        cbCommentForum.setValue(comment.getForum());
        taCommentContent.setText(comment.getContent());
        lblCommentContext.setText("Edition commentaire #" + comment.getId());
        feedScrollPane.setVvalue(0);
    }

    private void deleteForum(Forum forum) {
        try {
            serviceForum.delete(forum);
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur forum", exception.getMessage());
        }
    }

    private void deleteComment(Comment comment) {
        try {
            serviceComment.delete(comment);
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    private void addQuickComment(Forum forum, TextArea quickReply) {
        try {
            serviceComment.add(new Comment(quickReply.getText(), forum));
            quickReply.clear();
            loadForumsIntoFeed();
            loadForumChoices();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    private void loadForumChoices() {
        List<Forum> forums = serviceForum.getAll();
        cbCommentForum.getItems().setAll(forums);
        if (selectedCommentId == null && !forums.isEmpty() && cbCommentForum.getValue() == null) {
            cbCommentForum.setValue(forums.get(0));
        }
    }

    @FXML
    private void ouvrirAdminInterface() {
        if (feedContainer == null || feedContainer.getScene() == null) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir l'interface admin.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/backrepas.fxml"));
            Scene scene = new Scene(loader.load());
            MainController controller = loader.getController();
            controller.ouvrirBackRepas();

            Stage stage = (Stage) feedContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir l'interface admin : " + exception.getMessage());
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
