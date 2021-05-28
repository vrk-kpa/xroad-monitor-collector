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
import fi.vrk.xroad.monitor.elasticsearch.EnvMonitorDataStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Actor for initializing Elasticsearch index and alias
 */
@Component
@Scope("prototype")
@Slf4j
public class ElasticsearchInitializerActor extends AbstractActor {

  @Autowired
  private EnvMonitorDataStorageService envMonitorDataStorageService;

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(String.class, this::handleInitialization)
        .matchAny(obj -> log.error("Unhandled message: {}", obj))
        .build();
  }

  private void handleInitialization(String s) throws IOException {
    // for tests it is ok for envMonitorDataStorageService to be null
    if (envMonitorDataStorageService != null) {
      log.info("Create index and update alias");
      envMonitorDataStorageService.createIndexAndUpdateAlias();
    } else {
      log.info("Skipping index and alias creation");
    }
    getSender().tell("Initializing done", getSelf());
  }
}
