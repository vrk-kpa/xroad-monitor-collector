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
package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import fi.vrk.xroad.monitor.MonitorCollectorApplication;
import fi.vrk.xroad.monitor.base.ElasticsearchTestBase;
import fi.vrk.xroad.monitor.elasticsearch.EnvMonitorDataStorageDao;
import fi.vrk.xroad.monitor.elasticsearch.EnvMonitorDataStorageDaoImpl;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.extractor.MonitorDataExtractor;
import fi.vrk.xroad.monitor.extractor.MonitorDataRequestBuilder;
import fi.vrk.xroad.monitor.extractor.MonitorDataResponseParser;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static fi.vrk.xroad.monitor.util.MonitorCollectorDataUtils.getIndexName;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MonitorDataHandlerActor}
 */
@Slf4j
@SpringBootTest(classes = {MonitorCollectorApplication.class, MonitorDataExtractor.class,
    MonitorDataRequestBuilder.class, MonitorDataResponseParser.class, EnvMonitorDataStorageDao.class,
    EnvMonitorDataStorageDaoImpl.class})
@RunWith(SpringRunner.class)
public class MonitorDataHandlerActorTest extends ElasticsearchTestBase {

  @Autowired
  ActorSystem system;

  @Autowired
  SpringExtension ext;

  @Autowired
  private EnvMonitorDataStorageDao envMonitorDataStorageDao;

  /**
   * Cleanup test data
   */
  @Before
  @After
  public void cleanup() throws IOException {
    removeCurrentIndexAndAlias();
  }

  /**
   * Tests that the monitor data actor sends processing results to result collector actor.
   * Tests that all queries servers leave mark in Elasticsearch although monitoring data may have
   * not been received.
   */
  @Test
  public void testMonitorDataActor() throws IOException {

    // create result collector actor
    final Props resultCollectorActorProps = Props.create(ResultCollectorActor.class);
    final TestActorRef<ResultCollectorActor> resultCollectorRef =
        TestActorRef.create(system, resultCollectorActorProps, "testA");
    ResultCollectorActor resultCollectorActor = resultCollectorRef.underlyingActor();

    // create monitor data actor
    final Props monitorDataActorProps = ext.props("monitorDataHandlerActor", resultCollectorRef);
    final TestActorRef<MonitorDataHandlerActor> monitorDataRef = TestActorRef.create(system, monitorDataActorProps,
        "testB");
    Set<SecurityServerInfo> infos = new HashSet<>();
    // this is a fully functioning security server that gives monitoring data
    infos.add(new SecurityServerInfo(
        environment.getProperty("xroad-monitor-collector.test1.servercode"),
        environment.getProperty("xroad-monitor-collector.test1.address"),
        environment.getProperty("xroad-monitor-collector.test1.memberclass"),
        environment.getProperty("xroad-monitor-collector.test1.membercode")));
    // this security server is registered to X-Road instance but does not give monitoring data
    infos.add(new SecurityServerInfo(
        environment.getProperty("xroad-monitor-collector.test2.servercode"),
        environment.getProperty("xroad-monitor-collector.test2.address"),
        environment.getProperty("xroad-monitor-collector.test2.memberclass"),
        environment.getProperty("xroad-monitor-collector.test2.membercode")));

    // Initialize result collector
    resultCollectorRef.receive(infos);

    // process all requests
    for (SecurityServerInfo info : infos) {
      monitorDataRef.receive(new MonitorDataHandlerActor.MonitorDataRequest(info));
    }

    // assert that result collector actor has received 2 results
    assertEquals(2, resultCollectorActor.getNumProcessedResults());

    // ensure all pending data has been written to Elasticsearch
    envMonitorDataStorageDao.flush();

    // expect 2 documents in Elasticsearch even though only one security server gave the data
    // (default data should be written to Elasticsearch when monitoring data query fails)
    SearchResponse searchResponse = envMonitorDataStorageDao.findAll(getIndexName(environment),
        environment.getProperty("xroad-monitor-collector-elasticsearch.type"));
    assertEquals(2, searchResponse.getHits().getTotalHits());
  }
}
