package fi.vrk.xroad.monitor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.SmallestMailboxPool;
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
import java.util.Collections;
import java.util.Set;

import static fi.vrk.xroad.monitor.util.MonitorCollectorConstants.SUPERVISOR_MONITOR_DATA_ACTOR_POOL_SIZE;

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

        SharedParamsParser parser = new SharedParamsParser("/etc/xroad/globalconf/FI/shared-params.xml");
        Set<SecurityServerInfo> securityServerInfos;
        try {
            securityServerInfos = parser.parse();
            log.info("Parsed results: {}", securityServerInfos.toString());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Failed parsing", e);
            throw new RuntimeException(e);
        }

        ActorRef supervisor = system.actorOf(ext.props("supervisor"));
        supervisor.tell(new Supervisor.StartCollectingMonitorDataCommand(securityServerInfos), ActorRef.noSender());
    }
}
