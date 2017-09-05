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

import fi.vrk.xroad.monitor.base.ElasticsearchTestBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Tests for Elasticsearch data access
 */
@Slf4j
@SpringBootTest(classes = {EnvMonitorDataStorageDao.class, EnvMonitorDataStorageDaoImpl.class})
@RunWith(SpringRunner.class)
public class EnvMonitorDataStorageDaoTest extends ElasticsearchTestBase {

  private static final String COMPLEX_JSON_FILE = "src/test/resources/data.json";
  private static final String COMPLEX_JSON_FILE_2 = "src/test/resources/data2.json";
  private static final String INDEXTYPE_TWITTER = "integrationtest-twitter";
  private static final String INDEXTYPE_ENVDATA = "integrationtest-complex";
  private static final String INDEXTYPE_ALIAS = "integrationtest-alias";
  private static final String INDEXTYPE_FOOBARBAZ = "integrationtest-foobarbaz";
  private static final String INDEXTYPE_SIMPLESEARCH = "integrationtest-simplesearch";
  private static final String INDEXTYPE_MAPPING1 = "integrationtest-mapping1";
  private static final String INDEXTYPE_MAPPING2 = "integrationtest-mapping2";

  /**
   * Cleanup test data
   */
  @Before
  public void cleanup() {
    removeIndex(INDEXTYPE_TWITTER);
    removeIndex(INDEXTYPE_ENVDATA);
    removeIndex(INDEXTYPE_ALIAS);
    removeIndex(INDEXTYPE_FOOBARBAZ);
    removeIndex(INDEXTYPE_SIMPLESEARCH);
    removeIndex(INDEXTYPE_MAPPING1);
    removeIndex(INDEXTYPE_MAPPING2);
  }

  @Test
  public void shouldSaveAndLoadJson() {
    String json = "{"
        + "\"user\":\"kimchy\","
        + "\"postDate\":\"2013-01-30\","
        + "\"message\":\"trying out Elasticsearch\""
        + "}";
    IndexResponse save = envMonitorDataStorageDao.save(INDEXTYPE_TWITTER, INDEXTYPE_TWITTER, json);
    log.info("saveAndUpdateAlias: {}", save);
    assertEquals(save.getResult(), DocWriteResponse.Result.CREATED);
    GetResponse load = envMonitorDataStorageDao.load(INDEXTYPE_TWITTER, INDEXTYPE_TWITTER, save.getId());
    log.info("load: {}", load);
    assertEquals(load.getId(), save.getId());
  }

  @Test
  public void shouldSaveAndLoadComplexJson() throws IOException {
    try (FileInputStream inputStream = new FileInputStream(COMPLEX_JSON_FILE)) {
      String json = IOUtils.toString(inputStream, Charset.defaultCharset());
      IndexResponse save = envMonitorDataStorageDao.save(INDEXTYPE_ENVDATA, INDEXTYPE_ENVDATA, json);
      log.info("saveAndUpdateAlias: {}", save);
      assertEquals(save.getResult(), DocWriteResponse.Result.CREATED);
      GetResponse load = envMonitorDataStorageDao.load(INDEXTYPE_ENVDATA, INDEXTYPE_ENVDATA, save.getId());
      log.info("load: {}", load);
      assertEquals(load.getId(), save.getId());
    }
  }

  @Test
  public void shouldCreateAndRemoveAlias() throws ExecutionException, InterruptedException {
    final String testAlias = "testAlias";
    String json = "{"
        + "\"user\":\"kimchy\","
        + "\"postDate\":\"2013-01-30\","
        + "\"message\":\"trying out Elasticsearch\""
        + "}";
    envMonitorDataStorageDao.save(INDEXTYPE_ALIAS, INDEXTYPE_ALIAS, json);
    envMonitorDataStorageDao.addIndexToAlias(INDEXTYPE_ALIAS, testAlias);
    assertTrue(envMonitorDataStorageDao.aliasExists(testAlias).exists());
    envMonitorDataStorageDao.removeAllIndexesFromAlias(testAlias);
    assertFalse(envMonitorDataStorageDao.aliasExists(testAlias).exists());
    assertTrue(envMonitorDataStorageDao.indexExists(INDEXTYPE_ALIAS).isExists());
  }

  @Test
  public void shouldFindExistingIndex() {
    assertFalse(envMonitorDataStorageDao.indexExists(INDEXTYPE_FOOBARBAZ).isExists());
    String json = "{"
        + "\"user\":\"kimchy\","
        + "\"postDate\":\"2013-01-30\","
        + "\"message\":\"trying out Elasticsearch\""
        + "}";
    IndexResponse save = envMonitorDataStorageDao.save(INDEXTYPE_FOOBARBAZ, INDEXTYPE_FOOBARBAZ, json);
    assertTrue(envMonitorDataStorageDao.indexExists(INDEXTYPE_FOOBARBAZ).isExists());
    envMonitorDataStorageDao.removeIndex(INDEXTYPE_FOOBARBAZ);
    assertFalse(envMonitorDataStorageDao.indexExists(INDEXTYPE_FOOBARBAZ).isExists());
  }

  @Test
  public void shouldGetSearchHits() throws ExecutionException, InterruptedException {
    assertFalse(envMonitorDataStorageDao.indexExists(INDEXTYPE_SIMPLESEARCH).isExists());
    String json = "{"
        + "\"user\":\"kimchy\","
        + "\"postDate\":\"2013-01-30\","
        + "\"message\":\"trying out Elasticsearch\""
        + "}";
    IndexResponse save = envMonitorDataStorageDao.save(INDEXTYPE_SIMPLESEARCH, INDEXTYPE_SIMPLESEARCH, json);
    envMonitorDataStorageDao.flush();
    SearchResponse searchResponse = envMonitorDataStorageDao.findAll(INDEXTYPE_SIMPLESEARCH, INDEXTYPE_SIMPLESEARCH);
    assertEquals(1, searchResponse.getHits().getTotalHits());
  }

  @Test
  public void shouldNotThrowMappingException1() throws IOException, ExecutionException, InterruptedException {
    assertFalse(envMonitorDataStorageDao.indexExists(INDEXTYPE_MAPPING1).isExists());
    for (int i=0; i<100; i++) {
      try (FileInputStream inputStream = new FileInputStream(COMPLEX_JSON_FILE)) {
        String json = IOUtils.toString(inputStream, Charset.defaultCharset());
        IndexResponse save = envMonitorDataStorageDao.save(INDEXTYPE_MAPPING1, INDEXTYPE_MAPPING1, json);
      }
      try (FileInputStream inputStream = new FileInputStream(COMPLEX_JSON_FILE_2)) {
        String json = IOUtils.toString(inputStream, Charset.defaultCharset());
        IndexResponse save = envMonitorDataStorageDao.save(INDEXTYPE_MAPPING1, INDEXTYPE_MAPPING1, json);
      }
    }
    envMonitorDataStorageDao.flush();
    assertEquals(200, envMonitorDataStorageDao.findAll(INDEXTYPE_MAPPING1, INDEXTYPE_MAPPING1)
        .getHits().getTotalHits());
  }

  @Test
  public void shouldNotThrowMappingException2() throws IOException, ExecutionException, InterruptedException {
    assertFalse(envMonitorDataStorageDao.indexExists(INDEXTYPE_MAPPING2).isExists());
    for (int i=1; i<9; i++) {
      String filename = String.format("src/test/resources/test%d.json", i);
      try (FileInputStream inputStream = new FileInputStream(filename)) {
        String json = IOUtils.toString(inputStream, Charset.defaultCharset());
        IndexResponse save = envMonitorDataStorageDao.save(INDEXTYPE_MAPPING2, INDEXTYPE_MAPPING2, json);
      }
      envMonitorDataStorageDao.flush();
    }
    assertEquals(8, envMonitorDataStorageDao.findAll(INDEXTYPE_MAPPING2, INDEXTYPE_MAPPING2)
        .getHits().getTotalHits());
  }
}
