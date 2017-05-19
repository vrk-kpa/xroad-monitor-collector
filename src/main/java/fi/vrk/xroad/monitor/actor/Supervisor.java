package fi.vrk.xroad.monitor.actor;

import akka.actor.*;
import akka.dispatch.Futures;
import akka.japi.pf.DeciderBuilder;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxPool;
import akka.util.Timeout;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
  private String workerActorName;

  @Autowired
  private SpringExtension springExtension;

  public Supervisor(List<SecurityServerInfo> securityServerInfos, String workerActorName) {
    this.securityServerInfos = securityServerInfos;
    this.workerActorName = workerActorName;
  }

  @Override
  public void preStart() throws Exception {
    super.preStart();
    log.info("prestart");
    monitorDataRequestPoolRouter = getContext().actorOf(new SmallestMailboxPool(5)
        .props(springExtension.props(workerActorName)));
  }

  @Override
  public Receive createReceive() {
    log.info("createReceive");
    return receiveBuilder()
        .match(StartCollectingMonitorDataCommand.class, this::handleMonitorDataRequest)
        .build();
  }

  private void handleMonitorDataRequest (StartCollectingMonitorDataCommand request) {
    log.info("Starting handling StartCollectingMonitorDataCommand");
    List<Future<Object>> futures = new ArrayList<>();
    final Timeout timeout = new Timeout(30, TimeUnit.SECONDS);
    for (int i=0; i<securityServerInfos.size(); i++) {
      SecurityServerInfo info = securityServerInfos.get(i);
      log.info("Starting ask i={}", i);
      Future<Object> future = Patterns.ask(monitorDataRequestPoolRouter,
          new MonitorDataActor.MonitorDataRequest(info), timeout);
      futures.add(future);
    }
    Future<Iterable<Object>> sequence = Futures.sequence(futures, getContext().system().dispatcher());
    try {
      log.info("Starting await");
      Await.result(sequence, timeout.duration());
      log.info("Result sequence {}", sequence);
    } catch (Exception e) {
      log.error("error occurred", e);
    }
    getSender().tell(SupervisorResponse.class, getSelf());
    log.info("End handling StartCollectingMonitorDataCommand");
  }

  public static class StartCollectingMonitorDataCommand {}

  public static class SupervisorResponse {}

  private static SupervisorStrategy strategy =
      new OneForOneStrategy(3, Duration.create("1 minute"), DeciderBuilder.
          match(TimeoutException.class, e -> stop()).
          matchAny(o -> escalate()).build());

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }
}
