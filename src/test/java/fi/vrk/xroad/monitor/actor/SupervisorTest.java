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
import akka.testkit.javadsl.TestKit;
import fi.vrk.xroad.monitor.MonitorCollectorApplication;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.monitordata.MonitorDataHandler;
import fi.vrk.xroad.monitor.monitordata.MonitorDataRequestBuilder;
import fi.vrk.xroad.monitor.monitordata.MonitorDataResponseParser;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link Supervisor}
 */
@Slf4j
@SpringBootTest(classes = {
        SpringExtension.class,
        Supervisor.class,
        ApplicationContext.class,
        MonitorDataActor.class,
        ResultCollectorActor.class,
        MonitorDataHandler.class,
        MonitorDataRequestBuilder.class,
        MonitorDataResponseParser.class})
@RunWith(SpringRunner.class)
public class SupervisorTest {

    @Autowired
    SpringExtension springExtension;

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

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
                        springExtension.props(
                                //MonitorDataActor.class.getName()
                                "monitorDataActor"
                                , resultCollectorActor))
                );

        // create supervisor
        final Props supervisorProps = springExtension.props("supervisor");
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
