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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import fi.vrk.xroad.monitor.parser.SharedParamsParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ResultCollectorActor}
 */
@Slf4j
@SpringBootTest(classes = SharedParamsParser.class)
@RunWith(SpringRunner.class)
public class ResultCollectorActorTest {

  @Autowired
  SharedParamsParser parser;

  @Test
  public void testResultCollectorActor() {
    ActorSystem system = ActorSystem.create();

    // parse global config to get security server information
    Set<SecurityServerInfo> securityServerInfos = null;
    try {
      securityServerInfos = parser.parse();
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("Failed parsing", e);
      fail("Failed parsing shared-params.xml");
    }

    // create result collector actor
    final Props props = Props.create(ResultCollectorActor.class);
    final TestActorRef<ResultCollectorActor> ref = TestActorRef.create(system, props);
    final ResultCollectorActor actor = ref.underlyingActor();

    ref.receive(securityServerInfos, ActorRef.noSender());

    // assert that we expect as many results as given in the actor constructor
    assertEquals(securityServerInfos.size(), actor.getNumExpectedResults());

    // assert that it hasn't received any results yet
    assertEquals(0, actor.getNumProcessedResults());

    // assert that it hasn't received all expected results yet
    assertEquals(false, actor.isDone());

    // send results
    securityServerInfos.stream().forEach(
            info -> ref.receive(ResultCollectorActor.MonitorDataResult.createSuccess(info))
    );
    assertEquals(securityServerInfos.size(), actor.getNumProcessedResults());

    // assert that all results have been received
    assertEquals(true, actor.isDone());
  }
}
