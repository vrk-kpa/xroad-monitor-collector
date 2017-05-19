package fi.vrk.xroad.monitor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
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
        log.info("X-Road Monitor Collector started");

        ApplicationContext context = SpringApplication.run(MonitorCollectorApplication.class, args);
        ActorSystem system = context.getBean(ActorSystem.class);
        SpringExtension ext = context.getBean(SpringExtension.class);

        SharedParamsParser parser = new SharedParamsParser(/*"/etc/xroad/globalconf/FI/shared-params.xml"*/"src/test/resources/shared-params.xml");
        try {
            List<SecurityServerInfo> securityServerInfos = parser.parse();
            log.info("Parsed results: {}", securityServerInfos.toString());
            ActorRef resultCollector = system.actorOf(ext.props("resultCollectorActor", securityServerInfos.size()));
            ActorRef supervisor = system.actorOf(ext.props("supervisor", "monitorDataActor", resultCollector));
            supervisor.tell(new Supervisor.StartCollectingMonitorDataCommand(securityServerInfos), ActorRef.noSender());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Failed parsing", e);
        }
    }
}
