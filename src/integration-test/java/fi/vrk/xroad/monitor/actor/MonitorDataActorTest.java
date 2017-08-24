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
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.monitordata.MonitorDataHandler;
import fi.vrk.xroad.monitor.monitordata.MonitorDataRequestBuilder;
import fi.vrk.xroad.monitor.monitordata.MonitorDataResponseParser;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MonitorDataActor}
 */
@Slf4j
@SpringBootTest(classes = {MonitorCollectorApplication.class, MonitorDataHandler.class,
    MonitorDataRequestBuilder.class, MonitorDataResponseParser.class})
@RunWith(SpringRunner.class)
public class MonitorDataActorTest {

  @Autowired
  ActorSystem system;

  @Autowired
  SpringExtension ext;
  /**
   * Tests that the monitor data actor sends processing results to result collector actor.
   */
  @Test
  public void testMonitorDataActor() {

      // create result collector actor
      final Props resultCollectorActorProps = Props.create(ResultCollectorActor.class);
      final TestActorRef<ResultCollectorActor> resultCollectorRef =
              TestActorRef.create(system, resultCollectorActorProps, "testA");
      ResultCollectorActor resultCollectorActor = resultCollectorRef.underlyingActor();

      // create monitor data actor
      final Props monitorDataActorProps = ext.props("monitorDataActor", resultCollectorRef);
      final TestActorRef<MonitorDataActor> monitorDataRef = TestActorRef.create(system, monitorDataActorProps, "testB");
      Set<SecurityServerInfo> infos = new HashSet<>();
      infos.add(new SecurityServerInfo("gdev-ss1.i.palveluvayla.com", "gdev-ss1.i.palveluvayla.com",
          "GOV", "1710128-9"));

      // Initialize resultcollertor
      resultCollectorRef.receive(infos);

      // process all requests
      for (SecurityServerInfo info : infos) {
          monitorDataRef.receive(new MonitorDataActor.MonitorDataRequest(info));
      }

      // assert that result collector actor has received 2 results
      assertEquals(1, resultCollectorActor.getNumProcessedResults());
    }
}
