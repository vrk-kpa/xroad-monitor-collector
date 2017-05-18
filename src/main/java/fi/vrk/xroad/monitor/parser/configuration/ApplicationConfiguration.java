package fi.vrk.xroad.monitor.parser.configuration;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fi.vrk.xroad.monitor.parser.extensions.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Spring configuration for application
 */
@Configuration
@Lazy
@ComponentScan(basePackages = {
    "fi.vrk.xroad.monitor" })
public class ApplicationConfiguration {

  // The application context is needed to initialize the Akka Spring
  // Extension
  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private SpringExtension springExtension;

  /**
   * Actor system singleton for this application.
   */
  @Bean
  public ActorSystem actorSystem() {

    ActorSystem system = ActorSystem
        .create("AkkaTaskProcessing", akkaConfiguration());

    // Initialize the application context in the Akka Spring Extension
    springExtension.initialize(applicationContext);
    return system;
  }

  /**
   * Read configuration from application.conf file
   */
  @Bean
  public Config akkaConfiguration() {
    return ConfigFactory.load();
  }

}
