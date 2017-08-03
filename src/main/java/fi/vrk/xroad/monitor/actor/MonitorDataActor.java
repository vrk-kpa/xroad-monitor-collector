/**
 *  The MIT License
 *  Copyright (c) 2017, Population Register Centre (VRK)
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
import fi.vrk.xroad.monitor.monitordata.MonitorDataHandler;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    MonitorDataHandler handler;

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
        log.info("start handleMonitorDataRequest {}", request.getSecurityServerInfo().toString());
        String xml = handler.handleMonitorDataRequestAndResponse(request.getSecurityServerInfo());
        log.info("Response metric: {}", xml);
        if (xml.startsWith("<m:getSecurityServerMetricsResponse>")) {
            saveMonitorData(xml, request.getSecurityServerInfo());
            resultCollectorActor.tell(ResultCollectorActor.MonitorDataResult.createSuccess(
                request.getSecurityServerInfo()), getSelf());
        } else {
            resultCollectorActor.tell(ResultCollectorActor.MonitorDataResult.createError(
                request.getSecurityServerInfo(), handler.getLastErrorDescription()), getSelf());
        }
        log.info("end handleMonitorDataRequest");
    }

    /**
     * Will save monitordata as json to elasticsearch. With securityserverinfo
     * @param xml
     * @param securityServerInfo
     */
    private void saveMonitorData(String xml, SecurityServerInfo securityServerInfo) {
        // TODO create json element wchich has all data elasticsearch wants
        // TODO make post to elasticsearch API
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
