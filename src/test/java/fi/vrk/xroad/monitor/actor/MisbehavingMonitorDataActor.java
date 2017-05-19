package fi.vrk.xroad.monitor.actor;

import akka.actor.ActorRef;
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

  public MisbehavingMonitorDataActor(ActorRef resultCollectorActor) {
    super(resultCollectorActor);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(MonitorDataRequest.class, this::handleMonitorDataRequest)
        .build();
  }

  @Override
  protected void handleMonitorDataRequest(MonitorDataRequest request) {
    log.info("start handleMonitorDataRequest {}", request.getSecurityServerInfo().toString());
    log.info("end handleMonitorDataRequest");
  }
}
