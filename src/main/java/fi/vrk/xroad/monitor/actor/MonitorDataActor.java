package fi.vrk.xroad.monitor.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

  protected final ActorRef resultCollectorActor;

  public MonitorDataActor(ActorRef resultCollectorActor) {
    this.resultCollectorActor = resultCollectorActor;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(MonitorDataRequest.class, this::handleMonitorDataRequest)
        .matchAny(obj -> log.error("Unhandled message: {}", obj))
        .build();
  }

  protected void handleMonitorDataRequest(MonitorDataRequest request) {
    if (request.getSecurityServerInfo().getServerCode() == "") {
      resultCollectorActor.tell(ResultCollectorActor.MonitorDataResult.createError(request.getSecurityServerInfo(), "NullPointer"), getSelf());
      throw new NullPointerException();
    }
    log.info("start handleMonitorDataRequest {}", request.getSecurityServerInfo().toString());
    resultCollectorActor.tell(ResultCollectorActor.MonitorDataResult.createSuccess(request.getSecurityServerInfo()),
        getSelf());
    log.info("end handleMonitorDataRequest");
  }

  /**
   * Request for fetching monitoring data from single security server
   */
  @RequiredArgsConstructor
  @Getter
  public static class MonitorDataRequest {
    private final SecurityServerInfo securityServerInfo;
  }
}
