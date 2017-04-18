package eu.luminis.elastic;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaintainElasticTest extends ElasticTestCase {
    private MaintainElastic maintainElastic;

    @Before
    public void setup() {
        maintainElastic = new MaintainElastic(client());

//        client().admin().indices().create(new CreateIndexRequest("test"));
    }

    @Test
    public void getStatus() throws Exception {
        String status = maintainElastic.getStatus();
        assertEquals("GREEN", status);
    }

}