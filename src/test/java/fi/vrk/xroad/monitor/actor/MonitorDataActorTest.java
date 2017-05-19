package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MonitorDataActor}
 */
@Slf4j
public class MonitorDataActorTest {

  @Test
  public void testResultCollectorActor() {
    ActorSystem system = ActorSystem.create();

    final Props resultCollectorActorProps = Props.create(ResultCollectorActor.class, 3);
    final TestActorRef<ResultCollectorActor> resultCollectorRef = TestActorRef.create(system, resultCollectorActorProps, "testA");
    ResultCollectorActor resultCollectorActor = resultCollectorRef.underlyingActor();

    final Props monitorDataActorProps = Props.create(MonitorDataActor.class, resultCollectorRef);
    final TestActorRef<MonitorDataActor> monitorDataRef = TestActorRef.create(system, monitorDataActorProps, "testB");
    MonitorDataActor monitorDataActor = monitorDataRef.underlyingActor();

    monitorDataRef.receive(new MonitorDataActor.MonitorDataRequest(
        new SecurityServerInfo("", "", "", "", "")));
    monitorDataRef.receive(new MonitorDataActor.MonitorDataRequest(
        new SecurityServerInfo("", "", "", "", "")));
    assertEquals(3, resultCollectorActor.getNumExpectedResults());
    assertEquals(2, resultCollectorActor.getNumProcessedResults());
  }
}
