package eu.luminis.elastic;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * Representation of an article on the website.
 */
public class Article {
    private String title;
    private String author;
    private String issue;
    private String link;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date postDate;

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Article setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getIssue() {
        return issue;
    }

    public Article setIssue(String issue) {
        this.issue = issue;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Article setLink(String link) {
        this.link = link;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Article setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getPostDate() {
        return postDate;
    }

    public Article setPostDate(Date postDate) {
        this.postDate = postDate;
        return this;
    }

    @Override
    public String toString() {
        return "Article{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", issue='" + issue + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                ", postDate=" + postDate +
                '}';
    }
}
