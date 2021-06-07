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
import fi.vrk.xroad.monitor.elasticsearch.EnvMonitorDataStorageService;
import fi.vrk.xroad.monitor.extractor.MonitorDataExtractor;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

/**
 * Actor for requesting and saving monitoring data from single security server
 */
@Component
@Scope("prototype")
@Slf4j
public class MonitorDataHandlerActor extends AbstractActor {

    protected final ActorRef resultCollectorActor;

    @Autowired
    MonitorDataExtractor extractor;

    @Autowired
    private EnvMonitorDataStorageService envMonitorDataStorageService;

    public MonitorDataHandlerActor(ActorRef resultCollectorActor) {
        this.resultCollectorActor = resultCollectorActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MonitorDataRequest.class, this::handleMonitorDataRequest)
                .matchAny(obj -> log.error("Unhandled message: {}", obj))
                .build();
    }

    protected void handleMonitorDataRequest(MonitorDataRequest request)
        throws ExecutionException, InterruptedException, CertificateException, UnrecoverableKeyException,
        NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        // query data from security server
        String json = extractor.handleMonitorDataRequestAndResponse(request.getSecurityServerInfo());
        boolean shouldSaveDefaultData = false;
        String errorString = "";
        if (json != null) {
            try {
                // save security server's monitoring data
                envMonitorDataStorageService.save(json);
                resultCollectorActor.tell(ResultCollectorActor.Result.createSuccess(
                    request.getSecurityServerInfo()), getSelf());
            } catch (Exception ex) {
                log.error("Exception saving monitoring data ", ex);
                log.error("Data: {}", json);
                shouldSaveDefaultData = true;
                errorString = ex.toString();
            }
        } else {
            shouldSaveDefaultData = true;
            errorString = extractor.getLastErrorDescription();
        }
        if (shouldSaveDefaultData) {
            // monitoring data was not received from security server or save operation failed
            // store only default data
            log.info("save default data for security server {}", request.getSecurityServerInfo());
            envMonitorDataStorageService.save(extractor.getDefaultJSON(request.getSecurityServerInfo(), errorString));
            resultCollectorActor.tell(ResultCollectorActor.Result.createError(
                request.getSecurityServerInfo(), errorString), getSelf());
        }
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
