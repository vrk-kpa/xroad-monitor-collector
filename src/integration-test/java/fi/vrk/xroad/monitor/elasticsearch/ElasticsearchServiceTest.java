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
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 * Tests for Elasticsearch data access
 */
@Slf4j
@SpringBootTest(classes = {EnvMonitorDataStorageDao.class, EnvMonitorDataStorageDaoImpl.class})
@RunWith(SpringRunner.class)
public class ElasticsearchServiceTest {

  private static final String COMPLEX_JSON_FILE = "src/test/resources/data.json";

  @Autowired
  private EnvMonitorDataStorageDao envMonitorDataStorageDao;

  @Test
  public void shouldSaveAndLoadJson() {
    String json = "{"
        + "\"user\":\"kimchy\","
        + "\"postDate\":\"2013-01-30\","
        + "\"message\":\"trying out Elasticsearch\""
        + "}";
    IndexResponse save = envMonitorDataStorageDao.save("twitter", "tweet", json);
    log.info("save: {}", save);
    assertEquals(save.getResult(), DocWriteResponse.Result.CREATED);
    GetResponse load = envMonitorDataStorageDao.load("twitter", "tweet", save.getId());
    log.info("load: {}", load);
    assertEquals(load.getId(), save.getId());
  }

  @Test
  public void shouldSaveAndLoadComplexJson() throws IOException {
    try (FileInputStream inputStream = new FileInputStream(COMPLEX_JSON_FILE)) {
      String json = IOUtils.toString(inputStream, Charset.defaultCharset());
      IndexResponse save = envMonitorDataStorageDao.save("test", "test", json);
      log.info("save: {}", save);
      assertEquals(save.getResult(), DocWriteResponse.Result.CREATED);
      GetResponse load = envMonitorDataStorageDao.load("test", "test", save.getId());
      log.info("load: {}", load);
      assertEquals(load.getId(), save.getId());
    }
  }
}
