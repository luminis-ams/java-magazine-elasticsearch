package eu.luminis.elastic;

/**
 * Exception used when trying to execute something against the elastic cluster.
 */
public class ElasticExecutionException extends RuntimeException {
    public ElasticExecutionException(String s) {
        super(s);
    }
}
