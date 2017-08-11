package fi.vrk.xroad.monitor.elasticsearch;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Tests for ES access
 */
public class ElasticsearchServiceTest {

  private TransportClient client;
  private static final int PORT = 9300;

  @Before
  public void createClient() throws UnknownHostException {
//    Settings settings = Settings.builder()
//        .put("client.transport.sniff", true)
//        .put("cluster.name", "elasticsearch")
//        .put("client.transport.ignore_cluster_name", "true")
//        .put("client.transport.nodes_sampler_interval", 10, TimeUnit.SECONDS)
//        .put("client.transport.ping_timeout", 20, TimeUnit.SECONDS).build();
    client = new PreBuiltTransportClient(Settings.EMPTY)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), PORT));
  }

  @Test
  public void shouldSaveJson() {

    String json = "{" +
        "\"user\":\"kimchy\"," +
        "\"postDate\":\"2013-01-30\"," +
        "\"message\":\"trying out Elasticsearch\"" +
        "}";

    IndexResponse response = client.prepareIndex("twitter", "tweet")
        .setSource(json, XContentType.JSON)
        .get();
  }

  @After
  public void closeClient() {
    client.close();
  }
}
