package eu.luminis.elastic;

/**
 * Exception used when connecting to elastic does not work.
 */
public class ElasticConfigException extends RuntimeException {
    public ElasticConfigException(String message) {
        super(message);
    }
}
