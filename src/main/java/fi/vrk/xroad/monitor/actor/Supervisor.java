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

    private static final int SUPERVISOR_RETRIES = 3;

    @Autowired
    SpringExtension ext;

    public Supervisor() {
    }

    /**
     * For testing purposes
     */
    void overrideResultCollectorActor(ActorRef testingResultCollectorActor) {
        this.resultCollectorActor = testingResultCollectorActor;
    }

    /**
     * For testing purposes
     */
    void overrideMonitorDataRequestPoolRouter(ActorRef testingMonitorDataRequestPoolRouter) {
        this.monitorDataRequestPoolRouter = testingMonitorDataRequestPoolRouter;
    }

    @Override
    public void preStart() throws Exception {
        log.debug("preStart");
        resultCollectorActor = getContext().actorOf(ext.props("resultCollectorActor"));
        monitorDataRequestPoolRouter = getContext()
                .actorOf(new SmallestMailboxPool(SUPERVISOR_MONITOR_DATA_ACTOR_POOL_SIZE)
                        .props(ext.props("monitorDataHandlerActor", resultCollectorActor)));
        super.preStart();
    }

    @Override
    public Receive createReceive() {
        log.debug("createReceive");
        return receiveBuilder()
                .match(StartCollectingMonitorDataCommand.class, this::handleMonitorDataRequest)
                .matchAny(obj -> log.error("Unhandled message: {}", obj))
                .build();
    }

    private void handleMonitorDataRequest(StartCollectingMonitorDataCommand request) {
        Timeout timeout = new Timeout(1, TimeUnit.MINUTES);
        try {
            Await.ready(Patterns.ask(resultCollectorActor, request.getSecurityServerInfos(), timeout),
                timeout.duration());

            final int loopSize = 10;
            for (int i = 0; i < loopSize; i++) {
                log.info("START REQUEST SENDING LOOP {}", i + 1);
                request.getSecurityServerInfos().stream()
                    .forEach(info -> {
                        log.info("Process SecurityServerInfo {}", info);
                        monitorDataRequestPoolRouter.tell(new MonitorDataHandlerActor.MonitorDataRequest(info),
                            getSelf());
                    });
            }

        } catch (TimeoutException | InterruptedException e) {
            log.error("Failed to initialize the ResultCollectorActor, {}", e);
        }
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
    //  If the exception escalate all the way up to the root guardian it will handle it in the same way as the default
    //  strategy defined above.`
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(SUPERVISOR_RETRIES, Duration.create("1 minute"), DeciderBuilder
                    .match(Exception.class, e -> {
                        log.error("Exception ", e);
                        return resume();
                    })
                    .matchAny(e -> resume()).build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
}
