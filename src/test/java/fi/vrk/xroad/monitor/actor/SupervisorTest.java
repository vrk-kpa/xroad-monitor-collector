package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;
import akka.util.Timeout;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import fi.vrk.xroad.monitor.parser.SharedParamsParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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
    List<SecurityServerInfo> securityServerInfos = null;
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
