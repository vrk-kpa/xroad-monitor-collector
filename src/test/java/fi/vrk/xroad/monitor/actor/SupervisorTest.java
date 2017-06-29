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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;
import akka.testkit.TestActorRef;
import fi.vrk.xroad.monitor.MonitorCollectorApplication;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
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
 * Tests for {@link Supervisor}
 */
@Slf4j
@SpringBootTest(classes = MonitorCollectorApplication.class)
@RunWith(SpringRunner.class)
public class SupervisorTest {

  @Autowired
  ActorSystem system;

  @Autowired
  SpringExtension ext;

  /**
   * Tests the system logic so that when supervisor starts processing
   * the collector receives the processing results.
   */
  @Test
  public void testSupervisor() {

    Set<SecurityServerInfo> securityServerInfos = new HashSet<>();
    securityServerInfos.add(new SecurityServerInfo("Eka", "Osoite", "memberClass", "memberCode"));
    securityServerInfos.add(new SecurityServerInfo("", "", "", ""));
    securityServerInfos.add(new SecurityServerInfo("Toka", "osoite", "memberClass", "memberCode"));

    final TestActorRef<ResultCollectorActor> resultCollectorActor = TestActorRef.create(
            system, Props.create(ResultCollectorActor.class));

    final TestActorRef<MonitorDataActor> monitorDataRequestPoolRouter =
            TestActorRef.create(system, new SmallestMailboxPool(2).props(
                    Props.create(MonitorDataActor.class, resultCollectorActor))
            );

    // create supervisor
    final Props supervisorProps = ext.props("supervisor");
    final TestActorRef<Supervisor> supervisorRef = TestActorRef.create(system, supervisorProps, "supervisor");
    Supervisor underlying = supervisorRef.underlyingActor();
    underlying.overrideResultCollectorActor(resultCollectorActor);
    underlying.overrideMonitorDataRequestPoolRouter(monitorDataRequestPoolRouter);

    // send message to supervisor to start processing
    supervisorRef.receive(new Supervisor.StartCollectingMonitorDataCommand(securityServerInfos), ActorRef.noSender());

    // assert that all the results have been received
    assertEquals(securityServerInfos.size(), resultCollectorActor.underlyingActor().getNumProcessedResults());

  }

}
