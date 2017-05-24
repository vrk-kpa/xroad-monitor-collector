package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import fi.vrk.xroad.monitor.parser.SharedParamsParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link Supervisor}
 */
@Slf4j
public class SupervisorTest {

  /**
   * Tests the system logic so that when supervisor starts processing
   * the collector receives the processing results.
   */
  @Test
  public void testSupervisor() {
    // parse global config to get security server information
    SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
    Set<SecurityServerInfo> securityServerInfos = null;
    try {
      securityServerInfos = parser.parse();
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("Failed parsing", e);
      fail("Failed parsing shared-params.xml");
    }

    ActorSystem system = ActorSystem.create();

    // create result collector actor
    final Props resultCollectorActorProps = Props.create(ResultCollectorActor.class, securityServerInfos);
    final TestActorRef<ResultCollectorActor> resultCollectorRef = TestActorRef.create(system, resultCollectorActorProps, "testA");
    ResultCollectorActor resultCollectorActor = resultCollectorRef.underlyingActor();

    // create monitor data actor
    final Props monitorDataActorProps = Props.create(MonitorDataActor.class, resultCollectorRef);
    final TestActorRef<MonitorDataActor> monitorDataRef = TestActorRef.create(system, monitorDataActorProps, "testB");

    // create supervisor
    final Props supervisorProps = Props.create(Supervisor.class, monitorDataRef, "monitorDataActor");
    final TestActorRef<Supervisor> supervisorRef = TestActorRef.create(system, supervisorProps, "testC");

    // assert that no results have been received yet
    assertEquals(0, resultCollectorActor.getNumProcessedResults());

    // send message to supervisor to start processing
    supervisorRef.receive(new Supervisor.StartCollectingMonitorDataCommand(securityServerInfos), ActorRef.noSender());

    // assert that all the results have been received
    assertEquals(12, resultCollectorActor.getNumProcessedResults());
  }
}
