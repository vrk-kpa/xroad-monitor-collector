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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

/**
 * Elasticsearch data storage service implementation
 */
@Slf4j
@Service
public class EnvMonitorDataStorageServiceImpl implements EnvMonitorDataStorageService {

  private static final String INDEX_NAME = "envdata";
  private static final String TYPE_NAME = "envdata";
  private static final String ALIAS_NAME = "envdata-latest";

  @Autowired
  private EnvMonitorDataStorageDao envMonitorDataStorageDao;

  @Override
  public void saveAndUpdateAlias(String json) throws ExecutionException, InterruptedException {
    final String index = getIndexName();
    envMonitorDataStorageDao.save(index, getTypeName(), json);
    if (envMonitorDataStorageDao.aliasExists(ALIAS_NAME).exists()) {
      envMonitorDataStorageDao.removeAllIndexesFromAlias(ALIAS_NAME);
    }
    envMonitorDataStorageDao.addIndexToAlias(index, ALIAS_NAME);
  }

  private String getTypeName() {
    return TYPE_NAME;
  }

  private String getIndexName() {
    Calendar calendar = Calendar.getInstance();
    return String.format("%s-%d-%02d-%02d", INDEX_NAME, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DATE));
  }
}
