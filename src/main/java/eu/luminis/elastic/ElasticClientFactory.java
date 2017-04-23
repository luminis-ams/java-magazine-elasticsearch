package eu.luminis.elastic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Class that maintains a singleton elastic client.</p>
 * <p>BEWARE: This class is not meant to be a thread safe singleton implementation.</p>
 */
public class ElasticClientFactory {
    private static final Logger logger = LogManager.getLogger(ElasticClientFactory.class);
    private static final int DEFAULT_ELASITCSEARCH_PORT = 9300;

    private Client client;
    private final String clusterName;
    private final String unicastHosts;

    /**
     * Create the factory to maintain an elasticsearch client. The name of the cluster to connect to as well as the hosts
     * are mandatory. Valid format for the hosts is:
     * host1:port,host2:port
     * @param clusterName String containing the name of the cluster to connect to.
     * @param unicastHosts String that can contain one or more hosts to connect to. Format of the string is
     *                     host:port
     */
    public ElasticClientFactory(String clusterName, String unicastHosts) {
        this.clusterName = clusterName;
        this.unicastHosts = unicastHosts;
    }

    /**
     * Return the created Client object, if not available yet create it.
     *
     * @return Client object
     */
    public Client obtainClient() {
        // Beware, not a thread safe solution.
        if (this.client == null) {
            logger.info("Create a new elasticsearch client");
            this.client = createClient();
        }

        return this.client;
    }

    private Client createClient() {
        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .build();

        TransportClient client = new PreBuiltTransportClient(settings);

        try {
            client.addTransportAddresses(getTransportAddresses(unicastHosts));
        } catch (UnknownHostException e) {
            logger.error("Problem while creating a client for elasticsearch", e);
            throw new ElasticConfigException("Could not create elasticsearch client");
        }

        return client;
    }

    private TransportAddress[] getTransportAddresses(String unicastHosts) throws UnknownHostException {
        List<TransportAddress> transportAddresses = new ArrayList<TransportAddress>();

        for (String unicastHost : unicastHosts.split(",")) {
            int port = DEFAULT_ELASITCSEARCH_PORT;
            String serverName = unicastHost;
            if (unicastHost.contains(":")) {
                String[] splitted = unicastHost.split(":");
                serverName = splitted[0];
                port = Integer.parseInt(splitted[1].trim());
            }
            transportAddresses.add(new InetSocketTransportAddress(InetAddress.getByName(serverName.trim()), port));
        }

        return transportAddresses.toArray(new TransportAddress[transportAddresses.size()]);
    }

}
