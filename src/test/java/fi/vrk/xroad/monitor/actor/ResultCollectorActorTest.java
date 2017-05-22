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
 * Tests for {@link ResultCollectorActor}
 */
@Slf4j
public class ResultCollectorActorTest {

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

    final Props props = Props.create(ResultCollectorActor.class, securityServerInfos);
    final TestActorRef<ResultCollectorActor> ref = TestActorRef.create(system, props, "testA");
    final ResultCollectorActor actor = ref.underlyingActor();
    assertEquals(securityServerInfos.size(), actor.getNumExpectedResults());
    assertEquals(0, actor.getNumProcessedResults());
    assertEquals(false, actor.isDone());
    for (int i=0; i<securityServerInfos.size(); i++) {
      ref.receive(ResultCollectorActor.MonitorDataResult.createSuccess(securityServerInfos.get(i)));
      assertEquals(i+1, actor.getNumProcessedResults());
    }
    assertEquals(true, actor.isDone());
  }
}