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
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link MonitorDataActor}
 */
@Slf4j
public class MonitorDataActorTest {

  /**
   * Tests that the monitor data actor sends processing results to result collector actor.
   */
  @Test
  public void testMonitorDataActor() {
    ActorSystem system = ActorSystem.create();

    // create result collector actor
    final Props resultCollectorActorProps = Props.create(ResultCollectorActor.class);
    final TestActorRef<ResultCollectorActor> resultCollectorRef = TestActorRef.create(system, resultCollectorActorProps, "testA");
    ResultCollectorActor resultCollectorActor = resultCollectorRef.underlyingActor();

    // create monitor data actor
    final Props monitorDataActorProps = Props.create(MonitorDataActor.class, resultCollectorRef);
    final TestActorRef<MonitorDataActor> monitorDataRef = TestActorRef.create(system, monitorDataActorProps, "testB");
    MonitorDataActor monitorDataActor = monitorDataRef.underlyingActor();

    Set<SecurityServerInfo> infos = new HashSet<>();
    infos.add(new SecurityServerInfo("gdev-ss1.i.palveluvayla.com", "gdev-ss1.i.palveluvayla.com", "GOV", "1710128-9"));
    infos.add(new SecurityServerInfo("gdev-ss2.i.palveluvayla.com", "gdev-ss2.i.palveluvayla.com", "GOV", "1710128-9"));

    // Initialize resultcollertor
    resultCollectorRef.receive(infos);

    // process all requests
    for(SecurityServerInfo info : infos ){
      monitorDataRef.receive(new MonitorDataActor.MonitorDataRequest(info));

    }


    // assert that result collector actor has received 2 results
    assertEquals(2, resultCollectorActor.getNumProcessedResults());
  }
}
