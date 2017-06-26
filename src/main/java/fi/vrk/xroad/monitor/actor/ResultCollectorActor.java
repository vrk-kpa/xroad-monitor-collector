package fi.vrk.xroad.monitor.actor;

import akka.actor.AbstractActor;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Actor for collecting security server monitoring data processing results
 */
@Component
@Scope("prototype")
@Slf4j
public class ResultCollectorActor extends AbstractActor {

  private Set<SecurityServerInfo> awaitedResults;
  private int numAwaitedResults;
  private long startTime;

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Set.class, this::handleInitialization)
        .match(MonitorDataResult.class, this::handleMonitorDataResult)
        .matchAny(obj -> log.error("Unhandled message: {}", obj))
        .build();
  }

  // TODO this should be tested!!! Or Try-Catch or something
  private void handleInitialization(Set<SecurityServerInfo> infos) {
    log.info("Initializing resultCollerActor: {}", infos);

    this.startTime = System.nanoTime();
    this.awaitedResults = new HashSet<>(infos);
    this.numAwaitedResults = infos.size();
    getSender().tell("Initializing done", getSelf());
  }

  private void handleMonitorDataResult(MonitorDataResult result) {
    awaitedResults.remove(result.getSecurityServerInfo());
    if (!(awaitedResults.size() > 0)) {
      log.info("All request handled in time of: {} seconds", ((double)System.nanoTime() - startTime) /  1000000000.0);
    }
    if (result.isSuccess()) {
      log.info("received success with data {}", result.toString());
    } else {
      log.error("received error with data {}", result.toString());
    }
  }

  @Override
  public void postStop() throws Exception {
    super.postStop();
    if (awaitedResults != null) { // may be null if never initialized (tests)
      for (SecurityServerInfo securityServerInfo : awaitedResults) {
        log.error("Result for {} has not been received", securityServerInfo);
      }
    }
  }

  /**
   * For testing purposes.
   * Indicates if all expected results have been received.
   */
  boolean isDone() {
    return awaitedResults.isEmpty();
  }

  /**
   * For testing purposes.
   * The number of expected results.
   */
  int getNumExpectedResults() {
    return this.numAwaitedResults;
  }

  /**
   * For testing purposes.
   * The number of processed results.
   */
  int getNumProcessedResults() {
    return numAwaitedResults - awaitedResults.size();
  }

  /**
   * Contains processing result for single monitor data
   */
  @Getter
  @ToString
  @EqualsAndHashCode
  public static final class MonitorDataResult {

    private final SecurityServerInfo securityServerInfo;
    private final boolean success;
    private final String errorMsg;

    private MonitorDataResult(SecurityServerInfo securityServerInfo, boolean success, String errorMsg) {
      this.securityServerInfo = securityServerInfo;
      this.success = success;
      this.errorMsg = errorMsg;
    }
    public static MonitorDataResult createSuccess(SecurityServerInfo securityServerInfo) {
      return new MonitorDataResult(securityServerInfo, true, "");
    }
    public static MonitorDataResult createError(SecurityServerInfo securityServerInfo, String error) {
      return new MonitorDataResult(securityServerInfo, false, error);
    }
  }
}
