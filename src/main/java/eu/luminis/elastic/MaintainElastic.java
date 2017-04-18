package eu.luminis.elastic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;

import java.util.concurrent.ExecutionException;

/**
 * Class used to interact with elastic API like cluster, index, etc.
 */
public class MaintainElastic {
    private static final Logger logger = LogManager.getLogger(MaintainElastic.class);
    private final Client client;

    public MaintainElastic(Client client) {
        this.client = client;
    }

    /**
     * Returns the cluster status (GREEN, YELLOW, RED)
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
}
