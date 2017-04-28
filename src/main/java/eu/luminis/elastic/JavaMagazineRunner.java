package eu.luminis.elastic;

import org.elasticsearch.client.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static eu.luminis.elastic.Constants.CLUSTER_NAME;
import static eu.luminis.elastic.Constants.HOSTS;

/**
 * Main class with a command line interface for the article search demo.
 */
public class JavaMagazineRunner {
    private final static Locale NL = Locale.forLanguageTag("nl-NL");
    private static final int STOP_ACTION = 9;
    private static final int FIND_AUTHORS = 1;
    private static final int FIND_ISSUES = 2;
    private static final int AUTHORS_ARTICLES = 3;
    private static final int ISSUE_ARTICLES = 4;
    private static final int SEARCH_ARTICLES = 5;

    private DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", NL);

    private ArticleRepository repository;

    public JavaMagazineRunner() {
        ElasticClientFactory factory = new ElasticClientFactory(CLUSTER_NAME, HOSTS);
        Client client = factory.obtainClient();
        this.repository = new ArticleRepository(client);
    }

    public void start() {
        int action = 0;
        while (action != STOP_ACTION) {
            System.out.println(FIND_AUTHORS + ". Find authors");
            System.out.println(FIND_ISSUES + ". Find issues");
            System.out.println(AUTHORS_ARTICLES + ". All articles for author");
            System.out.println(ISSUE_ARTICLES + ". All articles for issue");
            System.out.println(SEARCH_ARTICLES + ". Search articles");
            System.out.println(STOP_ACTION + ". exit");
            System.out.println("Please type you option: ");
            action = readOption();
            executeAction(action);
            System.out.println();
        }

    }

    public int readOption() {
        try {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String line = bufferRead.readLine();
            return Integer.parseInt(line);
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    public void executeAction(int action) {
        switch (action) {
            case FIND_AUTHORS:
                printAuthors();
                break;
            case FIND_ISSUES:
                printIssues();
                break;
            case AUTHORS_ARTICLES:
                printArticlesForAuthor();
                break;
            case ISSUE_ARTICLES:
                printArticlesForIssue();
                break;
            case STOP_ACTION:
                System.out.println("Thanks, bye bye.");
                break;
            case SEARCH_ARTICLES:
                printSearchForArticles();
                break;
            default:

        }
    }

    private void printAuthors() {
        Map<String, Long> authors = this.repository.findAuthors();
        System.out.println("Authors");
        authors.forEach((s, aLong) -> {
            System.out.println(String.format("%s (%d)", s, aLong));
        });
    }

    private void printIssues() {
        Map<String, Long> issues = this.repository.findIssues();
        System.out.println("Issues");
        issues.forEach((s, aLong) -> {
            System.out.println(String.format("%s (%d)", s, aLong));
        });
    }

    private void printArticlesForAuthor() {
        String author = "";
        try {
            System.out.println("Type the name of the author:");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            author = bufferRead.readLine();
        } catch (IOException | NumberFormatException e) {
            System.out.println(e.getMessage());
        }
        List<Article> articles = this.repository.findAllArticlesForAuthor(author);
        System.out.println(String.format("Articles for author: [%s]", author));
        articles.forEach(article -> {
            System.out.println(String.format("%s [%s]", article.getTitle(), formatter.format(article.getPostDate())));
        });
    }

    private void printArticlesForIssue() {
        String issue = "";
        try {
            System.out.println("Type the name of the issue:");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            issue = bufferRead.readLine();
        } catch (IOException | NumberFormatException e) {
            System.out.println(e.getMessage());
        }
        List<Article> articles = this.repository.findAllArticlesForIssue(issue);
        System.out.println(String.format("Articles for issue: [%s]", issue));
        articles.forEach(article -> {
            System.out.println(String.format("%s [%s]", article.getTitle(), article.getAuthor()));
        });
    }

    private void printSearchForArticles() {
        String SearchString = "";
        try {
            System.out.println("Enter term to search for:");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            SearchString = bufferRead.readLine();
        } catch (IOException | NumberFormatException e) {
            System.out.println(e.getMessage());
        }
        List<Article> articles = this.repository.searchArticlesBy(SearchString);
        System.out.println(String.format("Articles for SearchString: [%s]", SearchString));
        articles.forEach(article -> {
            System.out.println(String.format("%s [%s-%s]", article.getTitle(), article.getAuthor(), formatter.format(article.getPostDate())));
        });
    }

    public static void main(String[] args) {

        JavaMagazineRunner runner = new JavaMagazineRunner();
        runner.start();
    }
}
