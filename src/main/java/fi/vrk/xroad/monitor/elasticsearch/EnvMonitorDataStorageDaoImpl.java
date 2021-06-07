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
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.DeleteAliasRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

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

  private RestHighLevelClient client;

  /**
   * Initializes transport client
   * @throws UnknownHostException
   */
  @PostConstruct
  public void init() throws UnknownHostException {
      client = new RestHighLevelClient(RestClient.builder(new HttpHost(
          InetAddress.getByName(environment.getProperty("xroad-monitor-collector-elasticsearch.host")),
          Integer.parseInt(environment.getProperty("xroad-monitor-collector-elasticsearch.port")),
          environment.getProperty("xroad-monitor-collector-elasticsearch.scheme")
      )));
  }

  @Override
  public IndexResponse save(String index, String type, String json) throws IOException {
    log.debug("Elasticsearch data: {}", json);
    IndexRequest request = new IndexRequest(index, type);
    request.source(json, XContentType.JSON);
    return client.index(request, RequestOptions.DEFAULT);
  }

  @Override
  public GetResponse load(String index, String type, String json) throws IOException {
    GetRequest request = new GetRequest(index, type, json);
    return client.get(request, RequestOptions.DEFAULT);
  }

  @Override
  public boolean addIndexToAlias(String index, String alias) throws IOException {
    IndicesAliasesRequest.AliasActions aliasAction = new IndicesAliasesRequest.AliasActions(
            IndicesAliasesRequest.AliasActions.Type.ADD).index(index).alias(alias);
    IndicesAliasesRequest request = new IndicesAliasesRequest();
    request.addAliasAction(aliasAction);
    AcknowledgedResponse indicesAliasesResponse = client.indices().updateAliases(request, RequestOptions.DEFAULT);
    return indicesAliasesResponse.isAcknowledged();
  }

  @Override
  public boolean removeAllIndexesFromAlias(String alias) throws IOException {
    DeleteAliasRequest request = new DeleteAliasRequest("*", alias);
    org.elasticsearch.client.core.AcknowledgedResponse indicesAliasesResponse =
            client.indices().deleteAlias(request, RequestOptions.DEFAULT);
    return indicesAliasesResponse.isAcknowledged();
  }

  @Override
  public boolean aliasExists(String alias) throws IOException {
    GetAliasesRequest request = new GetAliasesRequest(alias);
    return client.indices().existsAlias(request, RequestOptions.DEFAULT);
  }

  @Override
  public boolean indexExists(String index) throws IOException {
    GetIndexRequest request = new GetIndexRequest(index);
    return client.indices().exists(request, RequestOptions.DEFAULT);
  }

  @Override
  public boolean removeIndex(String index) throws IOException {
    DeleteIndexRequest request = new DeleteIndexRequest(index);
    AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
    return deleteIndexResponse.isAcknowledged();
  }

  @Override
  public SearchResponse findAll(String index, String type) throws IOException {
    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(matchAllQuery());
    searchRequest.source(searchSourceBuilder);
    return client.search(searchRequest, RequestOptions.DEFAULT);
  }

  @Override
  public FlushResponse flush() throws IOException {
    FlushRequest request = new FlushRequest();
    return client.indices().flush(request, RequestOptions.DEFAULT);
  }

  @Override
  public CreateIndexResponse createIndex(String index) throws IOException {
    CreateIndexRequest request = new CreateIndexRequest(index);
    return client.indices().create(request, RequestOptions.DEFAULT);
  }

  /**
   * Closes transport client
   */
  @PreDestroy
  public void shutdown() throws IOException {
    client.close();
  }
}
