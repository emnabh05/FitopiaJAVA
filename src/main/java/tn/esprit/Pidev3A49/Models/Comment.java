package tn.esprit.Pidev3A49.Models;

public class Comment {

    private int id;
    private Integer userId;
    private String content;
    private Forum forum;
    private String authorName;
    private String authorEmail;

    public Comment() {
    }

    public Comment(int id, String content, Forum forum) {
        this.id = id;
        this.content = content;
        this.forum = forum;
    }

    public Comment(int id, Integer userId, String content, Forum forum) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.forum = forum;
    }

    public Comment(String content, Forum forum) {
        this.content = content;
        this.forum = forum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public Integer getForumId() {
        return forum == null ? null : forum.getId();
    }

    public String getForumTitle() {
        return forum == null ? "" : forum.getTitle();
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorDisplayName() {
        if (authorName != null && !authorName.isBlank()) {
            return authorName;
        }
        if (authorEmail != null && !authorEmail.isBlank()) {
            return authorEmail;
        }
        return "Utilisateur inconnu";
    }

    public boolean isOwnedBy(Integer candidateUserId) {
        return candidateUserId != null && userId != null && candidateUserId.equals(userId);
    }
}
