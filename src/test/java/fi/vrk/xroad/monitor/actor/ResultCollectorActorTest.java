package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ResultCollectorActor}
 */
@Slf4j
public class ResultCollectorActorTest {

  @Test
  public void testResultCollectorActor() {
    ActorSystem system = ActorSystem.create();
    final Props props = Props.create(ResultCollectorActor.class, 3);
    final TestActorRef<ResultCollectorActor> ref = TestActorRef.create(system, props, "testA");
    final ResultCollectorActor actor = ref.underlyingActor();
    assertEquals(3, actor.getNumExpectedResults());
    assertEquals(0, actor.getNumProcessedResults());
    assertEquals(false, actor.isDone());
    ref.receive(ResultCollectorActor.MonitorDataResult.createSuccess());
    assertEquals(1, actor.getNumProcessedResults());
    ref.receive(ResultCollectorActor.MonitorDataResult.createSuccess());
    assertEquals(2, actor.getNumProcessedResults());
    ref.receive(ResultCollectorActor.MonitorDataResult.createError("strange error"));
    assertEquals(3, actor.getNumProcessedResults());
    assertEquals(true, actor.isDone());
  }
}
