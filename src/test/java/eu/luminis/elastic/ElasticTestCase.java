package eu.luminis.elastic;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Basic test class, has been tested up to version 5.3
 */
public abstract class ElasticTestCase {
    private static Node node = null;
    private static Client client = null;

    @BeforeClass
    public static void setupOnce() throws NodeValidationException, ExecutionException, InterruptedException {
        Settings settings = Settings.builder()
                .put("path.home", "target/elasticsearch")
                .put("transport.type", "local")
                .put("http.enabled", false)
                .put("transport.tcp.port", 19300)
                .put("cluster.routing.allocation.disk.threshold_enabled", false)
                .build();

        node = new Node(settings).start();
        client = node.client();

        waitForGreen();
    }

    @AfterClass
    public static void teardownOnce() throws IOException {
        client.close();
        node.close();
    }

    static Client client() {
        return client;
    }

    private static void waitForGreen() throws InterruptedException, ExecutionException {
        ClusterHealthRequest request = new ClusterHealthRequest();
        request.waitForGreenStatus();
        client.admin().cluster().health(request).get();
    }

}
