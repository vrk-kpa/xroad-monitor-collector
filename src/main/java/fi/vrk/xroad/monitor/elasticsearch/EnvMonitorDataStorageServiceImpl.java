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
import org.elasticsearch.action.index.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static fi.vrk.xroad.monitor.util.MonitorCollectorDataUtils.getIndexName;

/**
 * Elasticsearch data storage service implementation
 */
@Slf4j
@Service
public class EnvMonitorDataStorageServiceImpl implements EnvMonitorDataStorageService {

  @Autowired
  private EnvMonitorDataStorageDao envMonitorDataStorageDao;

  @Autowired
  private Environment environment;

  @Override
  public synchronized void save(String json) throws ExecutionException, InterruptedException {
    log.info("SERVICE Object {} Thread {}", this.toString(), Thread.currentThread().getId());
    final String index = getIndexName(environment);
    final String type = environment.getProperty("xroad-monitor-collector-elasticsearch.type");
    log.debug("Store data to index: {}", index);
    IndexResponse save = envMonitorDataStorageDao.save(index, type, json);
    log.debug("Save response: {}", save);
  }

  @Override
  public synchronized void createIndexAndUpdateAlias() throws ExecutionException, InterruptedException {
    final String index = getIndexName(environment);
    final String alias = environment.getProperty("xroad-monitor-collector-elasticsearch.alias");
    if (!envMonitorDataStorageDao.indexExists(index).isExists()) {
      log.info("Create index {}", index);
      envMonitorDataStorageDao.createIndex(index);
      if (envMonitorDataStorageDao.aliasExists(alias).exists()) {
        log.info("Alias exists, remove all indexes from alias {}", alias);
        envMonitorDataStorageDao.removeAllIndexesFromAlias(alias);
      } else {
        log.info("Alias {} does not yet exist", alias);
      }
      log.info("Create alias, add index {} to alias {}", index, alias);
      envMonitorDataStorageDao.addIndexToAlias(index, alias);
      envMonitorDataStorageDao.flush();
    }
  }
}
