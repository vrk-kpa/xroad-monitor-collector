package fi.vrk.xroad.monitor.actor;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.routing.SmallestMailboxPool;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.List;

import static akka.actor.SupervisorStrategy.*;

/**
 * Supervisor for actors
 */
@Component
@Scope("prototype")
@Slf4j
public class Supervisor extends AbstractActor {

  private final List<SecurityServerInfo> securityServerInfos;
  private ActorRef monitorDataRequestPoolRouter;

  @Autowired
  private SpringExtension springExtension;

  public Supervisor(List<SecurityServerInfo> securityServerInfos) {
    this.securityServerInfos = securityServerInfos;
  }

  @Override
  public void preStart() throws Exception {
    super.preStart();
    log.info("prestart");
    monitorDataRequestPoolRouter = getContext().actorOf(new SmallestMailboxPool(5)
        .props(springExtension.props("monitorDataActor")));
  }

  @Override
  public Receive createReceive() {
    log.info("createReceive");
    return receiveBuilder()
        .match(StartCollectingMonitorDataCommand.class, this::handleMonitorDataRequest)
        .build();
  }

  private void handleMonitorDataRequest (StartCollectingMonitorDataCommand request) {
    log.info("Starting handling");
    for (SecurityServerInfo info : securityServerInfos) {
      monitorDataRequestPoolRouter.tell(new MonitorDataActor.MonitorDataRequest(info), getSelf());
    }
    log.info("End handling");
  }

  public static class StartCollectingMonitorDataCommand {}

  private static SupervisorStrategy strategy =
      new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder.
          match(ArithmeticException.class, e -> resume()).
          match(NullPointerException.class, e -> restart()).
          match(IllegalArgumentException.class, e -> stop()).
          matchAny(o -> escalate()).build());

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }
}
