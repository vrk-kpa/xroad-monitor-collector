package fi.vrk.xroad.monitor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import fi.vrk.xroad.monitor.actor.Supervisor;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import fi.vrk.xroad.monitor.parser.SharedParamsParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Main class of the application
 */
@Slf4j
@Configuration
@EnableAutoConfiguration
@ComponentScan("fi.vrk.xroad.monitor")
public class MonitorCollectorApplication {

    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        log.info("xroad-monitor-collector started");

        ApplicationContext context =
            SpringApplication.run(MonitorCollectorApplication.class, args);
        ActorSystem system = context.getBean(ActorSystem.class);
        SpringExtension ext = context.getBean(SpringExtension.class);

        SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
        try {
            List<SecurityServerInfo> securityServerInfos = parser.parse();
            log.info("parsed results: {}", securityServerInfos.toString());
            ActorRef supervisor = system.actorOf(ext.props("supervisor", securityServerInfos,
                "monitorDataActor"));
            log.info("supervisor {}", supervisor);
            final Timeout timeout = new Timeout(300, TimeUnit.SECONDS);
            Future<Object> ask = Patterns.ask(supervisor, new Supervisor.StartCollectingMonitorDataCommand(), timeout);
            try {
                Await.result(ask, Duration.create(300, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.error("error occurred", e);
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("failed parsing", e);
        }
        system.terminate();
    }
}
