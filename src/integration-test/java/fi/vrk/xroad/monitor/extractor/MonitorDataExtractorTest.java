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
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link MonitorDataExtractor}
 */
@Slf4j
@SpringBootTest(classes = {MonitorDataRequestBuilder.class, MonitorDataExtractor.class, MonitorDataResponseParser.class})
@RunWith(SpringRunner.class)
public class MonitorDataExtractorTest {

    @Autowired
    private MonitorDataExtractor handler;

    @Autowired
    private MonitorDataRequestBuilder request;

    @Autowired
    private MonitorDataResponseParser response;

    // Test requires this to be valid and accessible server
    private final SecurityServerInfo exampleInfo = new SecurityServerInfo(
            "gdev-ss1.i.palveluvayla.com",
            "http://gdev-ss1.i.palveluvayla.com",
            "GOV",
            "1710128-9");

    @Test
    public void shouldParseJsonDataFromXmlResponse() {
        String xmlRequest = request.getRequestXML(exampleInfo);
        String xmlResponse = handler.makeRequest(xmlRequest);
        log.info("xmlResponse: {}", xmlResponse);
        String jsonMetrics = response.getMetricInformation(xmlResponse, exampleInfo, "FI");
        log.info("jsonMetrics: {}", jsonMetrics);
        // test that it is valid json
        Object jsonObject = JSONParser.parseJSON(jsonMetrics);
        assertNotNull(jsonObject);
    }
}
