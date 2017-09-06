/**
 * The MIT License
 * Copyright (c) 2017, Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vrk.xroad.monitor.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.alias.exists.AliasesExistResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

/**
 * Default implementation for {@link EnvMonitorDataStorageDao} interface
 * Loads and saves data to Elasticsearch
 */
@Slf4j
@Repository
public class EnvMonitorDataStorageDaoImpl implements EnvMonitorDataStorageDao {

  @Autowired
  private Environment environment;

  private TransportClient client;

  /**
   * Initializes transport client
   * @throws UnknownHostException
   */
  @PostConstruct
  public void init() throws UnknownHostException {
    Settings settings = Settings.builder()
        .put("cluster.name", environment.getProperty("xroad-monitor-collector-elasticsearch.cluster")).build();
    client = new PreBuiltTransportClient(settings)
        .addTransportAddress(new InetSocketTransportAddress(
            InetAddress.getByName(environment.getProperty("xroad-monitor-collector-elasticsearch.host")),
            Integer.parseInt(environment.getProperty("xroad-monitor-collector-elasticsearch.port"))));
  }

  @Override
  public IndexResponse save(String index, String type, String json) {
    log.debug("Elasticsearch data: {}", json);
    log.info("SAVING DATA with Object {} Thread {}", client.toString(), Thread.currentThread().getId());
    return client.prepareIndex(index, type).setSource(json, XContentType.JSON).get();
  }

  @Override
  public GetResponse load(String index, String type, String json) {
    return client.prepareGet(index, type, json).get();
  }

  @Override
  public IndicesAliasesResponse addIndexToAlias(String index, String alias) {
    return client.admin().indices().prepareAliases().addAlias(index, alias).get();
  }

  @Override
  public IndicesAliasesResponse removeAllIndexesFromAlias(String alias) {
    return client.admin().indices().prepareAliases().removeAlias("*", alias).get();
  }

  @Override
  public AliasesExistResponse aliasExists(String alias) throws ExecutionException, InterruptedException {
    return client.admin().indices().aliasesExist(new GetAliasesRequest(alias)).get();
  }

  @Override
  public IndicesExistsResponse indexExists(String index) {
    return client.admin().indices().prepareExists(index).get();
  }

  @Override
  public DeleteIndexResponse removeIndex(String index) {
    return client.admin().indices().prepareDelete(index).get();
  }

  @Override
  public SearchResponse findAll(String index, String type) {
    return client.prepareSearch(index).setTypes(type).setQuery(matchAllQuery()).get();
  }

  @Override
  public FlushResponse flush() throws ExecutionException, InterruptedException {
    return client.admin().indices().flush(new FlushRequest()).get();
  }

  /**
   * Closes transport client
   */
  @PreDestroy
  public void shutdown() {
    client.close();
  }
}
