package tn.esprit.Pidev3A49.Models;

public class Comment {

    private int id;
    private String content;
    private Forum forum;

    public Comment() {
    }

    public Comment(int id, String content, Forum forum) {
        this.id = id;
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
}
