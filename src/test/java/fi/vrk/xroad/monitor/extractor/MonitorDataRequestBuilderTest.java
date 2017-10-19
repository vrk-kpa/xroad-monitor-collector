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
package fi.vrk.xroad.monitor.extractor;

import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link MonitorDataRequestBuilder}
 */
@Slf4j
@SpringBootTest(classes = MonitorDataRequestBuilder.class)
@RunWith(SpringRunner.class)
public class MonitorDataRequestBuilderTest {

    @Autowired
    private MonitorDataRequestBuilder request;

    @Autowired
    private Environment environment;

    @Test
    public void getRequestXMLTest() {
        // Test requires this to be valid and accessible server
        final SecurityServerInfo exampleInfo = new SecurityServerInfo(
            environment.getProperty("xroad-monitor-collector.test1.servercode"),
            environment.getProperty("xroad-monitor-collector.test1.address"),
            environment.getProperty("xroad-monitor-collector.test1.memberclass"),
            environment.getProperty("xroad-monitor-collector.test1.membercode"));
        // Runtime exceptions (DOMException) are thrown if DOM creation fails.
        String xmlRequest = request.getRequestXML(exampleInfo);
        log.info(xmlRequest);
        // Assert that request contains member class, member code and server code
        assertTrue(xmlRequest.contains(environment.getProperty("xroad-monitor-collector.test1.membercode")));
        assertTrue(xmlRequest.contains(environment.getProperty("xroad-monitor-collector.test1.membercode")));
        assertTrue(xmlRequest.contains(environment.getProperty("xroad-monitor-collector.test1.servercode")));
    }

    @Test
    public void metricRequestPayloadTest() throws ParserConfigurationException, IOException, SAXException {
        Node payload = request.metricRequestPayload(
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        );

        payload = payload.getChildNodes().item(0);

        assertEquals(payload.getNodeName(), "m:outputSpec");
        assertEquals(payload.getChildNodes().getLength(), 2);
        assertEquals(payload.getChildNodes().item(0).getTextContent(), "OperatingSystem");
        assertEquals(payload.getChildNodes().item(1).getTextContent(), "Processes");
    }
}
