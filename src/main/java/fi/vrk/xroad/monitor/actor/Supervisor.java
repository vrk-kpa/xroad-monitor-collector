package fi.vrk.xroad.monitor.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.List;

import static akka.actor.SupervisorStrategy.resume;

/**
 * Supervisor for actors
 */
@Component
@Scope("prototype")
@Slf4j
public class Supervisor extends AbstractActor {

  private ActorRef monitorDataRequestPoolRouter;

  public Supervisor(ActorRef monitorDataRequestPoolRouter, String workerActorName) {
    this.monitorDataRequestPoolRouter = monitorDataRequestPoolRouter;
  }

  @Override
  public void preStart() throws Exception {
    super.preStart();
    log.info("preStart");
  }

  @Override
  public Receive createReceive() {
    log.info("createReceive");
    return receiveBuilder()
        .match(StartCollectingMonitorDataCommand.class, this::handleMonitorDataRequest)
        .matchAny(obj -> log.error("Unhandled message: {}", obj))
        .build();
  }

  private void handleMonitorDataRequest (StartCollectingMonitorDataCommand request) {
    for (int i=0; i<request.getSecurityServerInfos().size(); i++) {
      SecurityServerInfo info = request.getSecurityServerInfos().get(i);
      log.info("Process SecurityServerInfo i={}", i);
      monitorDataRequestPoolRouter.tell(new MonitorDataActor.MonitorDataRequest(info), getSelf());
    }
  }

  /**
   * Request for collecting monitoring data from security servers
   */
  @RequiredArgsConstructor
  @Getter
  public static class StartCollectingMonitorDataCommand {
    private final List<SecurityServerInfo> securityServerInfos;
  }

  //  Default Supervisor Strategy
  //  Escalate is used if the defined strategy doesn't cover the exception that was thrown.
  //
  //  When the supervisor strategy is not defined for an actor the following exceptions are handled by default:
  //
  //  ActorInitializationException will stop the failing child actor
  //  ActorKilledException will stop the failing child actor
  //  DeathPactException will stop the failing child actor
  //  Exception will restart the failing child actor
  //  Other types of Throwable will be escalated to parent actor
  //  If the exception escalate all the way up to the root guardian it will handle it in the same way as the default strategy defined above.`
  private static SupervisorStrategy strategy =
      new OneForOneStrategy(3, Duration.create("1 minute"), DeciderBuilder.
          matchAny(e -> resume()).build());

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }
}
