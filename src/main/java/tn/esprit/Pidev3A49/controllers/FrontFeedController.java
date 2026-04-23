package tn.esprit.Pidev3A49.controllers;

import javafx.event.ActionEvent;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.Pidev3A49.Models.Comment;
import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.Models.Forum;
import tn.esprit.Pidev3A49.services.ServiceComment;
import tn.esprit.Pidev3A49.services.ServiceForum;
import tn.esprit.Pidev3A49.test.UserSession;
import tn.esprit.Pidev3A49.utils.SessionRouter;

import java.io.IOException;
import java.io.File;
import java.util.List;

public class FrontFeedController {

    @FXML private VBox feedContainer;
    @FXML private ComboBox<Forum> cbCommentForum;
    @FXML private TextField tfForumTitle;
    @FXML private TextArea taForumContent;
    @FXML private TextField tfForumImagePath;
    @FXML private TextArea taCommentContent;
    @FXML private Label lblFeedStats;
    @FXML private Label lblCommentContext;
    @FXML private Label lblWelcomeUser;
    @FXML private Label lblForumComposerHint;
    @FXML private Label lblCommentComposerHint;
    @FXML private TextField tfSearch;
    @FXML private ScrollPane feedScrollPane;
    @FXML private ImageView ivForumPreview;

    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceComment serviceComment = new ServiceComment();

    private Integer selectedForumId;
    private Integer selectedCommentId;
    private FitopiaUser currentUser;

    @FXML
    public void initialize() {
        if (!SessionRouter.ensureAuthenticated(feedContainer)) {
            return;
        }
        currentUser = UserSession.getCurrentUser();
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

        personalizeFeed();
        refreshData();
    }

    @FXML
    private void refreshData() {
        if (feedContainer == null) {
            return;
        }
        currentUser = UserSession.getCurrentUser();
        personalizeFeed();
        loadForumsIntoFeed();
        loadForumChoices();
        clearForumEditor();
        clearCommentEditor();
    }

    @FXML
    private void saveForum() {
        try {
            ensureCurrentUser();
            Forum forum = new Forum(tfForumTitle.getText(), taForumContent.getText());
            forum.setUserId(currentUser.getId());
            forum.setImagePath(readForumImagePath());
            if (selectedForumId == null) {
                serviceForum.add(forum);
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Post publie par " + displayName(currentUser) + ".");
            } else {
                Forum existing = serviceForum.getById(selectedForumId);
                ensureCanManageForum(existing);
                forum.setId(selectedForumId);
                serviceForum.update(forum);
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Post mis a jour.");
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
                throw new IllegalArgumentException("Selectionne un post depuis le feed.");
            }
            Forum forum = serviceForum.getById(selectedForumId);
            if (forum == null) {
                throw new IllegalArgumentException("Post introuvable.");
            }
            ensureCanManageForum(forum);
            serviceForum.delete(forum);
            refreshData();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Post supprime.");
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur forum", exception.getMessage());
        }
    }

    @FXML
    private void saveComment() {
        try {
            ensureCurrentUser();
            Forum forum = cbCommentForum.getValue();
            if (forum == null) {
                throw new IllegalArgumentException("Choisis un forum pour le commentaire.");
            }

            Comment comment = new Comment(taCommentContent.getText(), forum);
            comment.setUserId(currentUser.getId());
            if (selectedCommentId == null) {
                serviceComment.add(comment);
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Commentaire ajoute.");
            } else {
                Comment existing = serviceComment.getById(selectedCommentId);
                ensureCanManageComment(existing);
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
            ensureCanManageComment(comment);
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
        if (tfForumTitle != null) {
            tfForumTitle.clear();
        }
        if (taForumContent != null) {
            taForumContent.clear();
        }
        if (tfForumImagePath != null) {
            tfForumImagePath.clear();
        }
        updateForumImagePreview(null);
        if (lblForumComposerHint != null && currentUser != null) {
            lblForumComposerHint.setText("You are posting as " + displayName(currentUser) + ".");
        }
    }

    @FXML
    private void clearCommentEditor() {
        selectedCommentId = null;
        if (taCommentContent != null) {
            taCommentContent.clear();
        }
        if (cbCommentForum != null) {
            cbCommentForum.getSelectionModel().clearSelection();
        }
        if (lblCommentContext != null) {
            lblCommentContext.setText("No comment selected. Your reply will be linked to your connected account.");
        }
        if (lblCommentComposerHint != null && currentUser != null) {
            lblCommentComposerHint.setText("Commenting as " + displayName(currentUser) + ".");
        }
    }

    private void personalizeFeed() {
        if (currentUser == null) {
            return;
        }
        if (lblWelcomeUser != null) {
            lblWelcomeUser.setText("Connected as " + displayName(currentUser) + " | you can post, comment, like and repost.");
        }
        if (lblForumComposerHint != null) {
            lblForumComposerHint.setText("You are posting as " + displayName(currentUser) + ".");
        }
        if (lblCommentComposerHint != null) {
            lblCommentComposerHint.setText("Commenting as " + displayName(currentUser) + ".");
        }
    }

    private void loadForumsIntoFeed() {
        if (feedContainer == null) {
            return;
        }
        feedContainer.setAlignment(Pos.TOP_CENTER);
        List<Forum> forums = serviceForum.getAll();
        String normalized = tfSearch == null || tfSearch.getText() == null
                ? ""
                : tfSearch.getText().trim().toLowerCase();

        feedContainer.getChildren().clear();

        int visibleForums = 0;
        int totalComments = 0;

        for (Forum forum : forums) {
            if (!normalized.isBlank()
                    && !forum.getTitle().toLowerCase().contains(normalized)
                    && !forum.getContent().toLowerCase().contains(normalized)
                    && !forum.getAuthorDisplayName().toLowerCase().contains(normalized)) {
                continue;
            }

            List<Comment> comments = serviceComment.getByForumId(forum.getId());
            visibleForums++;
            totalComments += comments.size();
            feedContainer.getChildren().add(wrapFeedCard(buildForumCard(forum, comments)));
        }

        if (visibleForums == 0) {
            Label empty = new Label("No posts yet. The feed is ready for the new personalized social experience.");
            empty.getStyleClass().add("empty-feed");
            empty.setWrapText(true);
            feedContainer.getChildren().add(empty);
        }

        if (lblFeedStats != null) {
            String ownerLabel = currentUser == null ? "" : " | viewer: " + displayName(currentUser);
            lblFeedStats.setText(visibleForums + " posts visibles | " + totalComments + " commentaires" + ownerLabel);
        }
    }

    private VBox buildForumCard(Forum forum, List<Comment> comments) {
        VBox card = new VBox(12);
        card.getStyleClass().add("feed-card");
        card.setMaxWidth(780);
        card.setFillWidth(true);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("post-header");

        Label avatar = new Label(initialsOf(forum.getAuthorDisplayName()));
        avatar.getStyleClass().add("avatar-badge");

        VBox authorBox = new VBox(4);
        Label author = new Label(forum.getAuthorDisplayName());
        author.getStyleClass().add("author-name");
        String ownerMeta = forum.isOwnedBy(currentUser == null ? null : currentUser.getId()) ? "your post" : "community post";
        String email = forum.getAuthorEmail() == null ? "" : " | " + forum.getAuthorEmail();
        Label meta = new Label("Post #" + forum.getId() + " | " + ownerMeta + email);
        meta.getStyleClass().add("author-meta");
        authorBox.getChildren().addAll(author, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, authorBox, spacer);
        if (canManageForum(forum)) {
            HBox ownerActions = new HBox(8);
            ownerActions.getStyleClass().add("owner-actions");
            ownerActions.getChildren().addAll(
                    createGhostButton("Edit", () -> startEditForum(forum)),
                    createGhostButton("Delete", () -> deleteForum(forum))
            );
            header.getChildren().add(ownerActions);
        }

        VBox body = new VBox(10);
        body.getStyleClass().add("post-body");

        Label title = new Label(forum.getTitle());
        title.getStyleClass().add("post-title");
        title.setWrapText(true);

        Label content = new Label(forum.getContent());
        content.getStyleClass().add("post-content");
        content.setWrapText(true);

        body.getChildren().addAll(title, content);

        StackPane media = buildForumMedia(forum);
        if (forum.getImagePath() != null && !forum.getImagePath().isBlank()) {
            body.getChildren().add(media);
        }

        HBox actions = new HBox(8);
        actions.getStyleClass().add("post-footer");
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.getChildren().addAll(
                createMetricButton((forum.isLikedByCurrentUser() ? "Unlike" : "Like") + " " + forum.getLikeCount(),
                        () -> toggleLike(forum)),
                createMetricButton(comments.size() + " comments", () -> prepareCommentForForum(forum)),
                createMetricButton((forum.isRepostedByCurrentUser() ? "Undo repost" : "Repost") + " " + forum.getRepostCount(),
                        () -> toggleRepost(forum))
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
        quickReply.setPromptText("Write a quick comment...");
        quickReply.getStyleClass().add("quick-reply-input");
        quickReply.setPrefRowCount(2);

        HBox quickReplyActions = new HBox(8);
        quickReplyActions.setAlignment(Pos.CENTER_LEFT);
        Button addComment = new Button("Post comment");
        addComment.getStyleClass().add("primary-pill");
        addComment.setOnAction(event -> addQuickComment(forum, quickReply));
        Button syncEditor = createGhostButton("Open editor", () -> prepareCommentForForum(forum));
        quickReplyActions.getChildren().addAll(addComment, syncEditor);
        quickReplyBox.getChildren().addAll(quickReply, quickReplyActions);

        card.getChildren().addAll(header, body, actions, commentsBox, quickReplyBox);
        return card;
    }

    private HBox wrapFeedCard(VBox card) {
        HBox wrapper = new HBox(card);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("feed-card-shell");
        return wrapper;
    }

    private VBox buildCommentRow(Comment comment) {
        VBox box = new VBox(8);
        box.getStyleClass().add("comment-row");

        Label author = new Label(comment.getAuthorDisplayName());
        author.getStyleClass().add("author-name");

        Label body = new Label(comment.getContent());
        body.getStyleClass().add("comment-body");
        body.setWrapText(true);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label tag = new Label("on " + comment.getForumTitle());
        tag.getStyleClass().add("comment-tag");
        footer.getChildren().add(tag);
        if (canManageComment(comment)) {
            footer.getChildren().addAll(
                    createGhostButton("Edit", () -> startEditComment(comment)),
                    createGhostButton("Delete", () -> deleteComment(comment))
            );
        }

        box.getChildren().addAll(author, body, footer);
        return box;
    }

    private Button createMetricButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("metric-pill");
        button.setOnAction(event -> action.run());
        return button;
    }

    private Button createGhostButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("ghost-pill");
        button.setOnAction(event -> action.run());
        return button;
    }

    private void startEditForum(Forum forum) {
        try {
            ensureCanManageForum(forum);
            selectedForumId = forum.getId();
            tfForumTitle.setText(forum.getTitle());
            taForumContent.setText(forum.getContent());
            if (tfForumImagePath != null) {
                tfForumImagePath.setText(forum.getImagePath() == null ? "" : forum.getImagePath());
            }
            updateForumImagePreview(forum.getImagePath());
            if (lblForumComposerHint != null) {
                lblForumComposerHint.setText("Editing your post as " + displayName(currentUser) + ".");
            }
            feedScrollPane.setVvalue(0);
        } catch (Exception exception) {
            showAlert(Alert.AlertType.WARNING, "Edition refusee", exception.getMessage());
        }
    }

    private void prepareCommentForForum(Forum forum) {
        selectedCommentId = null;
        cbCommentForum.setValue(forum);
        taCommentContent.clear();
        lblCommentContext.setText("New comment for " + forum.getAuthorDisplayName() + "'s post: " + forum.getTitle());
    }

    private void startEditComment(Comment comment) {
        try {
            ensureCanManageComment(comment);
            selectedCommentId = comment.getId();
            cbCommentForum.setValue(comment.getForum());
            taCommentContent.setText(comment.getContent());
            lblCommentContext.setText("Editing your comment #" + comment.getId());
            feedScrollPane.setVvalue(0);
        } catch (Exception exception) {
            showAlert(Alert.AlertType.WARNING, "Edition refusee", exception.getMessage());
        }
    }

    private void deleteForum(Forum forum) {
        try {
            ensureCanManageForum(forum);
            serviceForum.delete(forum);
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur forum", exception.getMessage());
        }
    }

    private void deleteComment(Comment comment) {
        try {
            ensureCanManageComment(comment);
            serviceComment.delete(comment);
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    private void addQuickComment(Forum forum, TextArea quickReply) {
        try {
            ensureCurrentUser();
            Comment comment = new Comment(quickReply.getText(), forum);
            comment.setUserId(currentUser.getId());
            serviceComment.add(comment);
            quickReply.clear();
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur commentaire", exception.getMessage());
        }
    }

    private void toggleLike(Forum forum) {
        try {
            ensureCurrentUser();
            serviceForum.toggleLike(forum.getId(), currentUser.getId());
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur interaction", exception.getMessage());
        }
    }

    private void toggleRepost(Forum forum) {
        try {
            ensureCurrentUser();
            serviceForum.toggleRepost(forum.getId(), currentUser.getId());
            refreshData();
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Erreur interaction", exception.getMessage());
        }
    }

    private void loadForumChoices() {
        if (cbCommentForum == null) {
            return;
        }
        List<Forum> forums = serviceForum.getAll();
        cbCommentForum.getItems().setAll(forums);
        if (selectedCommentId == null && !forums.isEmpty() && cbCommentForum.getValue() == null) {
            cbCommentForum.setValue(forums.get(0));
        }
    }

    @FXML
    private void chooseForumImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose post image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.bmp")
        );
        File file = chooser.showOpenDialog(feedContainer == null || feedContainer.getScene() == null
                ? null
                : (Stage) feedContainer.getScene().getWindow());
        if (file == null) {
            return;
        }
        if (tfForumImagePath != null) {
            tfForumImagePath.setText(file.getAbsolutePath());
        }
        updateForumImagePreview(file.getAbsolutePath());
    }

    @FXML
    private void clearForumImage() {
        if (tfForumImagePath != null) {
            tfForumImagePath.clear();
        }
        updateForumImagePreview(null);
    }

    @FXML
    private void ouvrirAdminInterface(ActionEvent event) {
        SessionRouter.logoutToSignIn((Button) event.getSource());
    }

    @FXML
    private void openDietPlanner(ActionEvent event) {
        if (feedContainer == null || feedContainer.getScene() == null) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir le diet planner.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontregime.fxml"));
            Scene scene = new Scene(loader.load());
            MainController controller = loader.getController();
            controller.ouvrirFrontRegimes();

            Stage stage = (Stage) feedContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Impossible d'ouvrir le diet planner : " + exception.getMessage());
        }
    }

    @FXML
    private void openSupplementStore(ActionEvent event) {
        try {
            SessionRouter.openFrontHome((Button) event.getSource());
        } catch (RuntimeException exception) {
            showAlert(Alert.AlertType.ERROR, "Navigation", exception.getMessage());
        }
    }

    private void ensureCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur connecte.");
        }
    }

    private String readForumImagePath() {
        if (tfForumImagePath == null || tfForumImagePath.getText() == null) {
            return null;
        }
        String value = tfForumImagePath.getText().trim();
        return value.isBlank() ? null : value;
    }

    private void updateForumImagePreview(String path) {
        if (ivForumPreview == null) {
            return;
        }
        Image image = loadImage(path);
        ivForumPreview.setImage(image);
        ivForumPreview.setVisible(image != null);
        ivForumPreview.setManaged(image != null);
    }

    private StackPane buildForumMedia(Forum forum) {
        StackPane media = new StackPane();
        media.getStyleClass().add("post-media-shell");
        media.setMinHeight(180);
        media.setPrefHeight(180);
        media.setMaxWidth(Double.MAX_VALUE);

        Image image = loadImage(forum.getImagePath());
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.getStyleClass().add("post-media-image");
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(180);
            imageView.setFitWidth(740);
            media.getChildren().add(imageView);
        } else {
            Label placeholder = new Label("Feed media preview");
            placeholder.getStyleClass().add("media-placeholder");
            placeholder.setMaxWidth(Double.MAX_VALUE);
            placeholder.setMinHeight(180);
            placeholder.setPrefHeight(180);
            media.getChildren().add(placeholder);
        }
        return media;
    }

    private Image loadImage(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:/")) {
                return new Image(path, true);
            }
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            return new Image(file.toURI().toString(), true);
        } catch (Exception exception) {
            return null;
        }
    }

    private boolean canManageForum(Forum forum) {
        return currentUser != null && forum != null
                && (forum.isOwnedBy(currentUser.getId()) || SessionRouter.isAdminSession());
    }

    private boolean canManageComment(Comment comment) {
        return currentUser != null && comment != null
                && (comment.isOwnedBy(currentUser.getId()) || SessionRouter.isAdminSession());
    }

    private void ensureCanManageForum(Forum forum) {
        if (forum == null) {
            throw new IllegalArgumentException("Post introuvable.");
        }
        if (!canManageForum(forum)) {
            throw new IllegalArgumentException("Seul l'auteur du post peut le modifier ou le supprimer.");
        }
    }

    private void ensureCanManageComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Commentaire introuvable.");
        }
        if (!canManageComment(comment)) {
            throw new IllegalArgumentException("Seul l'auteur du commentaire peut le modifier ou le supprimer.");
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

    private String initialsOf(String value) {
        if (value == null || value.isBlank()) {
            return "U";
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
