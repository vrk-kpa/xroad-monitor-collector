package fi.vrk.xroad.monitor.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxPool;
import akka.util.Timeout;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static akka.actor.SupervisorStrategy.resume;
import static fi.vrk.xroad.monitor.util.MonitorCollectorConstants.SUPERVISOR_MONITOR_DATA_ACTOR_POOL_SIZE;

/**
 * Supervisor for actors
 */
@Component
@Scope("prototype")
@Slf4j
public class Supervisor extends AbstractActor {

  @Getter
  private ActorRef resultCollectorActor;
  private ActorRef monitorDataRequestPoolRouter;

  @Autowired
  SpringExtension ext;

  public Supervisor (ActorRef resultCollectorActor, ActorRef monitorDataRequestPoolRouter) {
    this.resultCollectorActor = resultCollectorActor;
    this.monitorDataRequestPoolRouter = monitorDataRequestPoolRouter;
  }

  public Supervisor () {}

  @Override
  public void preStart() throws Exception {
    log.info("preStart");

    if (resultCollectorActor == null) {
      resultCollectorActor = getContext().actorOf(ext.props("resultCollectorActor"));
    }

    if (monitorDataRequestPoolRouter == null) {
      monitorDataRequestPoolRouter = getContext()
              .actorOf(new SmallestMailboxPool(SUPERVISOR_MONITOR_DATA_ACTOR_POOL_SIZE)
                      .props(ext.props("monitorDataActor", resultCollectorActor)));
    }

    super.preStart();
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
    Timeout timeout = new Timeout(1, TimeUnit.MINUTES);

    try {
      Await.ready(Patterns.ask(resultCollectorActor, request.getSecurityServerInfos(), timeout), timeout.duration());
    } catch (TimeoutException | InterruptedException e) {
      log.error("Failed to initialize the ResultCollectorActor, {}", e);
    }

    request.getSecurityServerInfos().stream()
        .forEach(info -> {
          log.info("Process SecurityServerInfo {}", info);
          monitorDataRequestPoolRouter.tell(new MonitorDataActor.MonitorDataRequest(info), getSelf());
        });
  }

  /**
   * Request for collecting monitoring data from security servers
   */
  @RequiredArgsConstructor
  @Getter
  public static class StartCollectingMonitorDataCommand {
    private final Set<SecurityServerInfo> securityServerInfos;
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
      new OneForOneStrategy(3, Duration.create("1 minute"), DeciderBuilder
          .match(NullPointerException.class, e -> { log.info("NullPointer!!"); return resume(); })
          .matchAny(e -> resume()).build());

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }
}
