package fi.vrk.xroad.monitor.actor;

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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link MonitorDataActor}
 */
@Slf4j
public class MonitorDataActorTest {

  @Test
  public void testResultCollectorActor() {
    ActorSystem system = ActorSystem.create();

    SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
    List<SecurityServerInfo> securityServerInfos = null;
    try {
      securityServerInfos = parser.parse();
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("Failed parsing", e);
      fail("Failed parsing shared-params.xml");
    }

    final Props resultCollectorActorProps = Props.create(ResultCollectorActor.class, securityServerInfos);
    final TestActorRef<ResultCollectorActor> resultCollectorRef = TestActorRef.create(system, resultCollectorActorProps, "testA");
    ResultCollectorActor resultCollectorActor = resultCollectorRef.underlyingActor();

    final Props monitorDataActorProps = Props.create(MonitorDataActor.class, resultCollectorRef);
    final TestActorRef<MonitorDataActor> monitorDataRef = TestActorRef.create(system, monitorDataActorProps, "testB");
    MonitorDataActor monitorDataActor = monitorDataRef.underlyingActor();

    monitorDataRef.receive(new MonitorDataActor.MonitorDataRequest(
        new SecurityServerInfo("gdev-ss1.i.palveluvayla.com", "gdev-ss1.i.palveluvayla.com", "GOV", "1710128-9", "Gofore")));
    monitorDataRef.receive(new MonitorDataActor.MonitorDataRequest(
        new SecurityServerInfo("gdev-ss2.i.palveluvayla.com", "gdev-ss2.i.palveluvayla.com", "GOV", "1710128-9", "Gofore")));
    assertEquals(12, resultCollectorActor.getNumExpectedResults());
    assertEquals(2, resultCollectorActor.getNumProcessedResults());
  }
}
