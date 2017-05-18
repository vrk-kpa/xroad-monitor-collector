package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import fi.vrk.xroad.monitor.parser.actor.MonitorDataActor;
import fi.vrk.xroad.monitor.parser.extensions.SpringExtension;
import fi.vrk.xroad.monitor.parser.parser.SecurityServerInfo;
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
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for {@link MonitorDataActor}
 */
@RunWith(SpringRunner.class)
@SpringBootApplication
@Slf4j
@ComponentScan(basePackages = {
    "fi.vrk.xroad.monitor" })
public class MonitorDataActorTest {

  @Autowired
  ActorSystem actorSystem;

  @Autowired
  SpringExtension springExtension;

  @Test
  public void testMonitorDataActor() throws IOException, SAXException, ParserConfigurationException {
    ActorRef monitorDataActor = actorSystem.actorOf(springExtension.props("monitorDataActor"));
    final Timeout timeout = new Timeout(30, TimeUnit.SECONDS);
    SecurityServerInfo info =
        new SecurityServerInfo("", "", "", "", "");
    Future<Object> ask = Patterns.ask(monitorDataActor, new MonitorDataActor.MonitorDataRequest(info), timeout);
    try {
      Object result = Await.result(ask, Duration.create(10, TimeUnit.SECONDS));
      log.info("result {}", result);
      assertNotNull(result);
    } catch (Exception e) {
      log.error("error occurred", e);
      fail("exception occurred awaiting result");
    }
  }
}
