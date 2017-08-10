package fi.vrk.xroad.monitor.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * Entity for storing environmental monitoring data
 */
@Document(indexName = "test-contact-test",type = "contact-test-type", shards = 1, replicas = 0)
public class EnvMonitorData {

  @Id
  private String id;
  private String name;
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
}
