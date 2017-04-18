package eu.luminis.elastic;

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
 * Class that maintains a singleton elastic client.
 */
public class ElasticClientFactory {
    private static final int DEFAULT_ELASITCSEARCH_PORT = 9300;

    private Client client;
    private final String clusterName;
    private final String unicastHosts;

    public ElasticClientFactory(String clusterName, String unicastHosts) {
        this.clusterName = clusterName;
        this.unicastHosts = unicastHosts;
    }

    public Client obtainClient() {
        // Beware, not a thread safe solution.
        if (this.client == null) {
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

            // TODO add logging
            throw new ElasticConfigException(e.getMessage());
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
