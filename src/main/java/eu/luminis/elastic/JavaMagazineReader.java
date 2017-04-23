package eu.luminis.elastic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Responsibility for this class is to read the Java Magazine Articles.
 */
public class JavaMagazineReader {
    private final static Locale NL = Locale.forLanguageTag("nl-NL");
    public static final String CLUSTER_NAME = "playground";
    public static final String HOSTS = "localhost:9300";

    /**
     * Obtain all articles extracted from the provided page number.
     *
     * @param page represents the number of the page to extract articles from
     * @return List containing the found articles
     * @throws IOException Thrown when reading or parsing the page went wrong
     */
    public List<Article> startReading(int page) throws IOException {
        Document doc = Jsoup.connect("http://www.nljug.org/databasejava/?page=" + page).get();
        Elements articlesElement = doc.select("li.databasejava");

        List<Article> articles = articlesElement.stream().map(element -> {
            String title = element.select("article").first().select("h3").text();
            Element headerLink = element.select("header").first().select("a").first();
            String link = headerLink.attr("href");

            Element meta = element.select("div.meta").first();
            Elements spans = meta.select("span");
            String author = spans.get(0).select("a").text();
            String issue = "unknown";
            if (spans.size() > 1) {
                issue = spans.get(1).select("a").text();
            }
            String time = meta.select("time").first().text();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", NL);


            LocalDate localDate = LocalDate.parse(time, DateTimeFormatter.ofPattern("dd MMMM yyyy", NL));
            Date postDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            String description = element.getElementsByAttributeValue("itemprop", "description").first().text();


            return new Article().setTitle(title).setAuthor(author).setIssue(issue).setPostDate(postDate).setLink(link).setDescription(description);
        }).collect(Collectors.toCollection(ArrayList::new));

        return articles;
    }

    /**
     * First removes all "article-*" indexes, than creates the new index articles-[timestamp] as well as an alias articles
     * to the same index. Next it parses the nljug java magazine website to obtain all available articles and stores them
     * into elasticsearch.
     *
     * @param args None are used.
     * @throws IOException Thrown if the parsing gives and error.
     */
    public static void main(String[] args) throws IOException {
        ElasticClientFactory clientFactory = new ElasticClientFactory(CLUSTER_NAME, HOSTS);
        ArticleRepository articleRepository = new ArticleRepository(clientFactory.obtainClient());

        articleRepository.deleteAllIndexes();

        articleRepository.createIndex();

        int numPages = 23;
        JavaMagazineReader reader = new JavaMagazineReader();

        for (int page = 1; page <= numPages; page++) {
            System.out.println("Start page " + page);
            List<Article> articles = reader.startReading(page);
            articles.forEach(articleRepository::indexArticle);
        }
    }
}
