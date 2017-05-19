package fi.vrk.xroad.monitor.actor;

import akka.actor.AbstractActor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Actor for collecting security server monitoring data processing results
 */
@Component
@Scope("prototype")
@Slf4j
public class ResultCollectorActor extends AbstractActor {

  private final int numExpectedResults;
  private List<MonitorDataResult> results = new ArrayList<>();

  public ResultCollectorActor(int numExpectedResults) {
    this.numExpectedResults = numExpectedResults;
  }

  @Override
  public AbstractActor.Receive createReceive() {
    return receiveBuilder()
        .match(MonitorDataResult.class, this::handleMonitorDataResult)
        .build();
  }

  private void handleMonitorDataResult(MonitorDataResult result) {
    results.add(result);
    if (result.isSuccess()) {
      log.info("received success {}", results.size());
    } else {
      log.error("received error {}", results.size());
    }
  }

  public boolean isDone() {
    return results.size() >= numExpectedResults;
  }

  public int getNumExpectedResults() {
    return numExpectedResults;
  }

  public int getNumProcessedResults() {
    return results.size();
  }

  /**
   * Contains processing result for single monitor data
   */
  @Getter
  public static class MonitorDataResult {
    private MonitorDataResult(boolean success, String errorMsg) {
      this.success = success;
      this.errorMsg = errorMsg;
    }
    public static MonitorDataResult createSuccess() {
      return new MonitorDataResult(true, "");
    }
    public static MonitorDataResult createError(String error) {
      return new MonitorDataResult(false, error);
    }
    private final boolean success;
    private final String errorMsg;
  }
}
