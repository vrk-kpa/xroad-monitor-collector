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
package fi.vrk.xroad.monitor.base;

import fi.vrk.xroad.monitor.elasticsearch.EnvMonitorDataStorageDao;
import fi.vrk.xroad.monitor.util.MonitorCollectorDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

/**
 * Base class for Elasticsearch tests
 */
@Slf4j
@SpringBootTest // the used classes are listed in the inherited test
@RunWith(SpringRunner.class)
public abstract class ElasticsearchTestBase {

  @Autowired
  protected EnvMonitorDataStorageDao envMonitorDataStorageDao;

  @Autowired
  protected Environment environment;

  protected void removeIndex(String index) {
    if (envMonitorDataStorageDao.indexExists(index).isExists()) {
      envMonitorDataStorageDao.removeIndex(index);
    }
  }

  protected void removeCurrentIndexAndAlias() throws ExecutionException, InterruptedException {
    removeAlias(environment.getProperty("xroad-monitor-collector-elasticsearch.alias"));
    removeIndex(MonitorCollectorDataUtils.getIndexName(environment));
  }

  protected void removeAlias(String alias) throws ExecutionException, InterruptedException {
    if (envMonitorDataStorageDao.aliasExists(alias).isExists()) {
      envMonitorDataStorageDao.removeAllIndexesFromAlias(alias);
    }
  }
}
