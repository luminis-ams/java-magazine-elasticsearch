package eu.luminis.elastic;

import org.elasticsearch.action.search.SearchResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ArticleRepositoryTest extends ElasticTestCase {
    private ArticleRepository articleRepository;

    @Before
    public void setup() {
        articleRepository = new ArticleRepository(client());
        articleRepository.deleteAllIndexes();
        articleRepository.createIndex();
        createTestArticles();
    }

    @Test
    public void getStatus() throws Exception {
        String status = articleRepository.getStatus();
        assertEquals("GREEN", status);
    }

    @Test
    public void checkSearchArticle_emptyTerm() {
        List<Article> articles = articleRepository.searchArticlesBy("");
        assertEquals(5, articles.size());
    }

    @Test
    public void checkSearchArticle_noTerm() {
        List<Article> articles = articleRepository.searchArticlesBy(null);
        assertEquals(5, articles.size());
    }

    @Test
    public void checkSearchArticle_FoundTerm() {
        List<Article> articles = articleRepository.searchArticlesBy("woman");
        assertEquals(3, articles.size());
    }

    @Test
    public void checkSearchArticle_NoFoundTerm() {
        List<Article> articles = articleRepository.searchArticlesBy("nonexistent");
        assertEquals(0, articles.size());
    }

    @Test
    public void checkFindAllByIssue() {
        List<Article> articles = articleRepository.findAllArticlesForIssue("issue1");

        assertEquals(2, articles.size());

        articles = articleRepository.findAllArticlesForIssue("issue_nonexistent");

        assertEquals(0, articles.size());

    }

    @Test
    public void checkFindAllByIssueAndSearchString() {
        List<Article> articles = articleRepository.searchAndFilterIssueArticlesBy("issue1", "woman");

        assertEquals(1, articles.size());

        articles = articleRepository.searchAndFilterIssueArticlesBy("issue_nonexistent", "description");

        assertEquals(0, articles.size());

        articles = articleRepository.searchAndFilterIssueArticlesBy("issue1", "dontknow");

        assertEquals(0, articles.size());

    }

    @Test
    public void checkFindAllByAuthor() {
        List<Article> articles = articleRepository.findAllArticlesForAuthor("Author 1");

        assertEquals(2, articles.size());

        articles = articleRepository.findAllArticlesForAuthor("author_nonexistent");

        assertEquals(0, articles.size());

    }

    @Test
    public void checkFindAllByAuthorAndSearchString() {
        List<Article> articles = articleRepository.searchAndFilterAuthorArticlesBy("Author 1", "Bob");

        assertEquals(1, articles.size());

        articles = articleRepository.searchAndFilterAuthorArticlesBy("author_nonexistent", "description");

        assertEquals(0, articles.size());

        articles = articleRepository.searchAndFilterAuthorArticlesBy("Author 1", "dontknow");

        assertEquals(0, articles.size());

    }

    @Test
    public void checkCreateArticle() {
        SearchResponse searchResponse = client().prepareSearch("articles").get();
        assertEquals(5, searchResponse.getHits().getTotalHits());
    }

    @Test
    public void checkFindIssues() {
        Map<String, Long> issues = articleRepository.findIssues();

        assertEquals(3, issues.size());

        assertEquals(2,issues.get("issue1").longValue());
        assertEquals(2,issues.get("issue2").longValue());
        assertEquals(1,issues.get("issue3").longValue());
    }

    @Test
    public void checkFindAuthors() {
        Map<String, Long> authors = articleRepository.findAuthors();

        assertEquals(3, authors.size());

        assertEquals(2,authors.get("Author 1").longValue());
        assertEquals(1,authors.get("Author 2").longValue());
        assertEquals(2,authors.get("Author 3").longValue());
    }

    private void createTestArticles() {
        addArticle("Test Bob","Author 1","Description about the man Bob","/link/bob","issue1");
        addArticle("Test Alice","Author 2","Description about the woman Alice","/link/alice","issue1");
        addArticle("Test Christian","Author 1","Description about the man Christian","/link/christian","issue2");
        addArticle("Test Angelina","Author 3","Description about the woman Angelina","/link/angelina","issue2");
        addArticle("Test Christa","Author 3","Description about the woman Christa","/link/christa","issue3");

        client().admin().indices().prepareRefresh().get();
    }

    private void addArticle(String title, String author, String description, String link, String issue) {
        Article article = new Article()
                .setTitle(title)
                .setAuthor(author)
                .setDescription(description)
                .setLink(link)
                .setIssue(issue)
                .setPostDate(new Date());

        articleRepository.indexArticle(article);
    }
}