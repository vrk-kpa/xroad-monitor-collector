package fi.vrk.xroad.monitor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
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

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

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
            ActorRef supervisor = system.actorOf(ext.props("supervisor", securityServerInfos));
            log.info("supervisor {}", supervisor);
            supervisor.tell(new Supervisor.StartCollectingMonitorDataCommand(), ActorRef.noSender());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("failed parsing", e);
        }
    }
}
