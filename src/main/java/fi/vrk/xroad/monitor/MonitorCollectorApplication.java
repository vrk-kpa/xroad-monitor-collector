package fi.vrk.xroad.monitor;

import akka.actor.ActorSystem;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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

        system.terminate();
    }
}
