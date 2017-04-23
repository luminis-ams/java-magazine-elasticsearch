package eu.luminis.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

/**
 * Class used to interact with elastic API like cluster, index, etc.
 */
public class ArticleRepository {
    private static final Logger logger = LogManager.getLogger(ArticleRepository.class);
    private static final String INDEX_BASE = "articles";

    private final Client client;
    private final ObjectMapper objectMapper;

    public ArticleRepository(Client client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Returns the cluster status (GREEN, YELLOW, RED)
     *
     * @return String with the cluster status
     */
    public String getStatus() {
        try {
            ClusterHealthResponse clusterHealthResponse =
                    this.client.admin().cluster().health(new ClusterHealthRequest()).get();
            return clusterHealthResponse.getStatus().name();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error when obtain cluster health", e);
            throw new ElasticExecutionException("Error when trying to obtain the server status");
        }
    }

    /**
     * Removes all indexes starting with <em>articles-</em>
     */
    public void deleteAllIndexes() {
        this.client.admin().indices().prepareDelete(INDEX_BASE + "-*").get();
    }

    /**
     * Creates a new index with a name <em>articles-[timestamp]</em> and creates an Alias with the name articles. The
     * index is created with a shard and no replicas.
     */
    public void createIndex() {
        String indexName = INDEX_BASE + "-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Settings settings = Settings.builder()
                .put("number_of_shards", 1)
                .put("number_of_replicas", 0)
                .build();
        CreateIndexRequest request = new CreateIndexRequest(indexName, settings);

        String mapping = "{\n" +
                "    \"article\": {\n" +
                "      \"properties\": {\n" +
                "        \"title\": {\n" +
                "          \"type\": \"text\"\n" +
                "        },\n" +
                "        \"author\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"issue\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"link\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"text\"\n" +
                "        },\n" +
                "        \"postDate\": {\n" +
                "          \"type\": \"date\",\n" +
                "          \"format\": \"yyyy-MM-dd\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }";

        request.mapping("article", mapping, XContentType.JSON);
        request.alias(new Alias(INDEX_BASE));

        try {
            CreateIndexResponse createIndexResponse = this.client.admin().indices().create(request).get();
            if (!createIndexResponse.isAcknowledged()) {
                throw new ElasticExecutionException("Create java_magazine index was not acknowledged");
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error while creating an index", e);
            throw new ElasticExecutionException("Error when trying to create an index");
        }
    }

    /**
     * Index the provided article in the index the alias articles is pointing to.
     *
     * @param article Article object to index.
     */
    public void indexArticle(Article article) {
        try {
            String articleAsString = objectMapper.writeValueAsString(article);
            client.prepareIndex(INDEX_BASE, "article").setSource(articleAsString, XContentType.JSON).get();
        } catch (IOException e) {
            throw new ElasticExecutionException("Error indexing document");
        }
    }

    /**
     * Accepts a search string and searches the title and description fields for occurrences. If no search string
     * is provided we return all documents.
     *
     * @param searchString String to search for, if empty we return everything
     * @return List of Articles found by the query.
     */
    public List<Article> searchArticlesBy(String searchString) {
        QueryBuilder queryBuilder;
        if (searchString == null || searchString.isEmpty()) {
            queryBuilder = matchAllQuery();
        } else {
            queryBuilder = multiMatchQuery(searchString, "description", "title");
        }
        SearchResponse searchResponse = client.prepareSearch(INDEX_BASE).setQuery(queryBuilder).get();
        SearchHit[] hits = searchResponse.getHits().hits();

        return Arrays.stream(hits)
                .map(this::parseHitIntoArticle)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns all articles available from the provided issue.
     *
     * @param issue String containing the issue to find articles for
     * @return List of Articles from the specified issue.
     */
    public List<Article> findAllArticlesForIssue(String issue) {
        SearchResponse searchResponse = client.prepareSearch(INDEX_BASE)
                .setQuery(termsQuery("issue", issue))
                .get();
        SearchHit[] hits = searchResponse.getHits().hits();

        return Arrays.stream(hits)
                .map(this::parseHitIntoArticle)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns all articles available from the provided author.
     *
     * @param author String containing the issue to find articles for
     * @return List of Articles from the specified issue.
     */
    public List<Article> findAllArticlesForAuthor(String author) {
        SearchResponse searchResponse = client.prepareSearch(INDEX_BASE)
                .setQuery(termsQuery("author", author))
                .get();
        SearchHit[] hits = searchResponse.getHits().hits();

        return Arrays.stream(hits)
                .map(this::parseHitIntoArticle)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns all found articles filtered by the provided issue and searched using the searchString.
     *
     * @param issue        String containing the issue to filter by
     * @param searchString String containing the string to search for
     * @return List of found Articles
     */
    public List<Article> searchAndFilterIssueArticlesBy(String issue, String searchString) {
        if (issue == null || issue.isEmpty()) {
            return searchArticlesBy(searchString);
        }
        if (searchString == null || searchString.isEmpty()) {
            return findAllArticlesForIssue(issue);
        }

        QueryBuilder queryBuilder = boolQuery()
                .must(multiMatchQuery(searchString, "description", "title"))
                .filter(termsQuery("issue", issue));

        SearchResponse searchResponse = client.prepareSearch(INDEX_BASE).setQuery(queryBuilder).get();
        SearchHit[] hits = searchResponse.getHits().hits();

        return Arrays.stream(hits)
                .map(this::parseHitIntoArticle)
                .collect(Collectors.toCollection(ArrayList::new));

    }

    /**
     * Returns all found articles filtered by the provided author and searched using the searchString.
     *
     * @param author       String containing the author to filter by
     * @param searchString String containing the string to search for
     * @return List of found Articles
     */
    public List<Article> searchAndFilterAuthorArticlesBy(String author, String searchString) {
        if (author == null || author.isEmpty()) {
            return searchArticlesBy(searchString);
        }
        if (searchString == null || searchString.isEmpty()) {
            return findAllArticlesForAuthor(author);
        }

        QueryBuilder queryBuilder = boolQuery()
                .must(multiMatchQuery(searchString, "description", "title"))
                .filter(termsQuery("author", author));

        SearchResponse searchResponse = client.prepareSearch(INDEX_BASE).setQuery(queryBuilder).get();
        SearchHit[] hits = searchResponse.getHits().hits();

        return Arrays.stream(hits)
                .map(this::parseHitIntoArticle)
                .collect(Collectors.toCollection(ArrayList::new));
    }


    /**
     * Returns all found Issues and the the amount of articles in each issue.
     *
     * @return Map with the issue as a key and the number of articles as a value
     */
    public Map<String, Long> findIssues() {
        SearchResponse searchResponse = client.prepareSearch(INDEX_BASE)
                .addAggregation(terms("issues").field("issue"))
                .setSize(0)
                .get();

        Terms issues = searchResponse.getAggregations().get("issues");
        Map<String, Long> foundIssues = new HashMap<>();
        issues.getBuckets().forEach(bucket -> {
            foundIssues.put(bucket.getKeyAsString(), bucket.getDocCount());
        });

        return foundIssues;
    }

    /**
     * Returns all found authors and the amount of articles each author has written.
     *
     * @return Map with the author as a key and the number of articles as a value
     */
    public Map<String, Long> findAuthors() {
        SearchResponse searchResponse = client.prepareSearch(INDEX_BASE)
                .addAggregation(terms("authors").field("author"))
                .setSize(0)
                .get();

        Terms issues = searchResponse.getAggregations().get("authors");
        Map<String, Long> foundAuthors = new TreeMap<>();

        issues.getBuckets().forEach(bucket -> {
            foundAuthors.put(bucket.getKeyAsString(), bucket.getDocCount());
        });

        return foundAuthors;
    }

    private Article parseHitIntoArticle(SearchHit hit) {
        try {
            return objectMapper.readValue(hit.getSourceAsString(), Article.class);
        } catch (IOException e) {
            logger.error("Error parsing article", e);
        }
        return null;
    }
}
