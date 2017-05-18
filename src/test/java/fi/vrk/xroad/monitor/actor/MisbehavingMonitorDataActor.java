package fi.vrk.xroad.monitor.actor;

import fi.vrk.xroad.monitor.parser.actor.MonitorDataActor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Actor for simulating failures in collecting monitoring data
 */
@Component
@Scope("prototype")
@Slf4j
public class MisbehavingMonitorDataActor extends MonitorDataActor {

  @Override
  protected void handleMonitorDataRequest(MonitorDataRequest request) {
    log.info("start handleMonitorDataRequest {}", request.getSecurityServerInfo().toString());
    if (request.getSecurityServerInfo().getServerCode().equals("gdev-ss1.i.palveluvayla.com")) {
      log.info("sending response");
      getSender().tell(MonitorDataActorResponseMessage.class, getSelf());
    } else if (request.getSecurityServerInfo().getServerCode().equals("gdev-loadtest-ss1.i.palveluvayla.com")) {
      log.info("hanging now");
      try {
        Thread.sleep(100000);
      } catch (InterruptedException e) {
        log.error("error occurred ", e);
      }
    } else {
      log.info("throwing exception");
      throw new RuntimeException();
    }
    log.info("end handleMonitorDataRequest");
  }
}
