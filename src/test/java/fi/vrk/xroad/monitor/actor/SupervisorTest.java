package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;
import akka.testkit.TestActorRef;
import fi.vrk.xroad.monitor.MonitorCollectorApplication;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import fi.vrk.xroad.monitor.parser.SharedParamsParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.PortableInterceptor.ACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static fi.vrk.xroad.monitor.util.MonitorCollectorConstants.SUPERVISOR_MONITOR_DATA_ACTOR_POOL_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
    securityServerInfos.add(new SecurityServerInfo("", "","",""));
    securityServerInfos.add(new SecurityServerInfo("Toka", "osoite", "memberClass", "memberCode"));

    final TestActorRef<ResultCollectorActor> resultCollectorActor = TestActorRef.create(
            system, Props.create(ResultCollectorActor.class));

    final TestActorRef<MonitorDataActor> monitorDataRequestPoolRouter =
            TestActorRef.create(system, new SmallestMailboxPool(2).props(Props.create(MonitorDataActor.class, resultCollectorActor)));

    // create supervisor
    final Props supervisorProps = ext.props("supervisor", resultCollectorActor, monitorDataRequestPoolRouter);
    final TestActorRef<Supervisor> supervisorRef = TestActorRef.create(system, supervisorProps, "supervisor");

    // send message to supervisor to start processing
    supervisorRef.receive(new Supervisor.StartCollectingMonitorDataCommand(securityServerInfos), ActorRef.noSender());

    // assert that all the results have been received
    assertEquals(securityServerInfos.size(), resultCollectorActor.underlyingActor().getNumProcessedResults());

  }
}
