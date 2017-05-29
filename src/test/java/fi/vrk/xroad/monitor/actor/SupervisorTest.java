package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
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
    // parse global config to get security server information
    /*SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
    Set<SecurityServerInfo> securityServerInfos = null;
    try {
      securityServerInfos = parser.parse();
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("Failed parsing", e);
      fail("Failed parsing shared-params.xml");
    }*/

    Set<SecurityServerInfo> securityServerInfos = new HashSet<>();
    securityServerInfos.add(new SecurityServerInfo("Eka", "Osoite", "memberClass", "memberCode"));
    securityServerInfos.add(new SecurityServerInfo("", "","",""));
    securityServerInfos.add(new SecurityServerInfo("Toka", "osoite", "memberClass", "memberCode"));

    final TestActorRef<ResultCollectorActor> resultCollectorActor = TestActorRef.create(
            system, Props.create(ResultCollectorActor.class));

    // create supervisor
    final Props supervisorProps = ext.props("supervisor", resultCollectorActor);
    final TestActorRef<Supervisor> supervisorRef = TestActorRef.create(system, supervisorProps, "supervisor");

    resultCollectorActor.receive(securityServerInfos, ActorRef.noSender());
    // assert that no results have been received yet
    assertEquals(0, resultCollectorActor.underlyingActor().getNumProcessedResults());

    // send message to supervisor to start processing
    supervisorRef.receive(new Supervisor.StartCollectingMonitorDataCommand(securityServerInfos), ActorRef.noSender());

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // assert that all the results have been received
    assertEquals(securityServerInfos.size(), resultCollectorActor.underlyingActor().getNumProcessedResults());

  }
}
