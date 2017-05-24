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
  private final int numAwaitedResults;

  public ResultCollectorActor(Set<SecurityServerInfo> awaitedResults) {
    this.awaitedResults = new HashSet<>(awaitedResults);
    this.numAwaitedResults = awaitedResults.size();
  }

  @Override
  public AbstractActor.Receive createReceive() {
    return receiveBuilder()
        .match(MonitorDataResult.class, this::handleMonitorDataResult)
        .matchAny(obj -> log.error("Unhandled message: {}", obj))
        .build();
  }

  private void handleMonitorDataResult(MonitorDataResult result) {
    awaitedResults.remove(result.getSecurityServerInfo());
    if (result.isSuccess()) {
      log.info("received success with data {}", result.toString());
    } else {
      log.error("received error with data {}", result.toString());
    }
  }

  @Override
  public void postStop() throws Exception {
    super.postStop();
    for (SecurityServerInfo securityServerInfo: awaitedResults) {
      log.error("Result for {} has not been received", securityServerInfo);
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
  public static class MonitorDataResult {

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
