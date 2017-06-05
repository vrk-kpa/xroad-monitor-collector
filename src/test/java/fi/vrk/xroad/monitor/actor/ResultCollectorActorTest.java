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
 * Tests for {@link ResultCollectorActor}
 */
@Slf4j
public class ResultCollectorActorTest {

  @Test
  public void testResultCollectorActor() {
    ActorSystem system = ActorSystem.create();

    // parse global config to get security server information
    SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
    Set<SecurityServerInfo> securityServerInfos = null;
    try {
      securityServerInfos = parser.parse();
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("Failed parsing", e);
      fail("Failed parsing shared-params.xml");
    }

    // create result collector actor
    final Props props = Props.create(ResultCollectorActor.class);
    final TestActorRef<ResultCollectorActor> ref = TestActorRef.create(system, props);
    final ResultCollectorActor actor = ref.underlyingActor();

    ref.receive(securityServerInfos, ActorRef.noSender());

    // assert that we expect as many results as given in the actor constructor
    assertEquals(securityServerInfos.size(), actor.getNumExpectedResults());

    // assert that it hasn't received any results yet
    assertEquals(0, actor.getNumProcessedResults());

    // assert that it hasn't received all expected results yet
    assertEquals(false, actor.isDone());

    // send results
    securityServerInfos.stream().forEach(info -> ref.receive(ResultCollectorActor.MonitorDataResult.createSuccess(info)));
    assertEquals(securityServerInfos.size(), actor.getNumProcessedResults());

    // assert that all results have been received
    assertEquals(true, actor.isDone());
  }
}
