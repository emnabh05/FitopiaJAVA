package tn.esprit.Pidev3A49.Models;

public class Forum {

    private int id;
    private Integer userId;
    private String title;
    private String content;
    private String imagePath;
    private String authorName;
    private String authorEmail;
    private int likeCount;
    private int repostCount;
    private boolean likedByCurrentUser;
    private boolean repostedByCurrentUser;

    public Forum() {
    }

    public Forum(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public Forum(int id, Integer userId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    public Forum(String title, String content) {
        this.title = title;
        this.content = content;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(int repostCount) {
        this.repostCount = repostCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public boolean isRepostedByCurrentUser() {
        return repostedByCurrentUser;
    }

    public void setRepostedByCurrentUser(boolean repostedByCurrentUser) {
        this.repostedByCurrentUser = repostedByCurrentUser;
    }

    public boolean isOwnedBy(Integer candidateUserId) {
        return candidateUserId != null && userId != null && candidateUserId.equals(userId);
    }

    @Override
    public String toString() {
        return id + " - " + title;
    }
}
