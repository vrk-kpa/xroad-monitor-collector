/**
 * The MIT License
 * Copyright (c) 2017, Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
import java.util.concurrent.TimeUnit;

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
        .match(Result.class, this::handleMonitorDataResult)
        .matchAny(obj -> log.error("Unhandled message: {}", obj))
        .build();
  }

  private void handleInitialization(Set<SecurityServerInfo> infos) {
    log.debug("Initializing resultCollerActor: {}", infos);
    this.startTime = System.nanoTime();
    this.awaitedResults = new HashSet<>(infos);
    this.numAwaitedResults = infos.size();
    getSender().tell("Initializing done", getSelf());
  }

  private void handleMonitorDataResult(Result result) {
    awaitedResults.remove(result.getSecurityServerInfo());
    if (!(awaitedResults.size() > 0)) {
      log.info("All request handled in time of: {} seconds",
              TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS));
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
  public static final class Result {

    private final SecurityServerInfo securityServerInfo;
    private final boolean success;
    private final String errorMsg;

    private Result(SecurityServerInfo securityServerInfo, boolean success, String errorMsg) {
      this.securityServerInfo = securityServerInfo;
      this.success = success;
      this.errorMsg = errorMsg;
    }
    public static Result createSuccess(SecurityServerInfo securityServerInfo) {
      return new Result(securityServerInfo, true, "");
    }
    public static Result createError(SecurityServerInfo securityServerInfo, String error) {
      return new Result(securityServerInfo, false, error);
    }
  }
}
