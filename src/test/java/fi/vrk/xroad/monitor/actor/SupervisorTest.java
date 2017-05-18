package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
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
@RunWith(SpringRunner.class)
@SpringBootApplication
@Slf4j
@ComponentScan(basePackages = {
    "fi.vrk.xroad.monitor" })
public class SupervisorTest {

  @Autowired
  ActorSystem actorSystem;

  @Autowired
  SpringExtension springExtension;

  @Test
  public void testSupervisor() {
    SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
    List<SecurityServerInfo> securityServerInfos = null;
    try {
      securityServerInfos = parser.parse();
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("failed parsing", e);
      fail("failed parsing shared-params.xml");
    }
    ActorRef supervisor = actorSystem.actorOf(
        springExtension.props("supervisor", securityServerInfos, "misbehavingMonitorDataActor"));
    final Timeout timeout = new Timeout(300, TimeUnit.SECONDS);
    Future<Object> ask = Patterns.ask(supervisor, new Supervisor.StartCollectingMonitorDataCommand(), timeout);
    try {
      Object result = Await.result(ask, Duration.create(300, TimeUnit.SECONDS));
      log.info("result {}", result);
      assertNotNull(result);
    } catch (Exception e) {
      log.error("error occurred", e);
      fail("exception occurred awaiting result");
    }
  }
}
