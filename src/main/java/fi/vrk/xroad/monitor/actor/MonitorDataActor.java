package fi.vrk.xroad.monitor.actor;

import akka.actor.AbstractActor;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Actor for requesting monitoring data from security servers
 */
@Component
@Scope("prototype")
@Slf4j
public class MonitorDataActor extends AbstractActor {
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(MonitorDataRequest.class, this::handleMonitorDataRequest)
        .build();
  }

  private void handleMonitorDataRequest(MonitorDataRequest request) {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      log.error("error occurred ", e);
    }
    log.info("received request {}", request.getSecurityServerInfo().toString());
  }

  @Getter
  public static class MonitorDataRequest {

    private final SecurityServerInfo securityServerInfo;

    public MonitorDataRequest(SecurityServerInfo securityServerInfo) {
      this.securityServerInfo = securityServerInfo;
    }
  }

  public static class ResponseMessage {}
}
