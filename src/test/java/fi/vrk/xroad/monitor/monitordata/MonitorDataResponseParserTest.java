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
package fi.vrk.xroad.monitor.monitordata;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link MonitorDataResponseParser}
 */
@Slf4j
@SpringBootTest(classes = MonitorDataResponseParser.class)
@RunWith(SpringRunner.class)
public class MonitorDataResponseParserTest {

    String exampleResponseXmlFile = "src/test/resources/exampleResponse.xml";

    @Test
    public void testEmpty() {
        // Placeholder for tests if parser features are extended.
        // No use testing Java's DocumentBuilderFactory converting String -> Document -> String
    }

    @Test
    public void parseResponseMetricsToJsonTest() throws IOException {
        String responseString;
        try (FileInputStream inputStream = new FileInputStream(exampleResponseXmlFile)) {
            responseString = IOUtils.toString(inputStream);
            MonitorDataResponseParser monitorDataResponseParser = new MonitorDataResponseParser();
            String json = monitorDataResponseParser.getMetricInformation(responseString);

            String soapmessageString;
            try (FileInputStream inputStream = new FileInputStream("src/test/resources/envmonitor.xml")) {
                soapmessageString = IOUtils.toString(inputStream);
            }

            assertTrue(json.equals());
        }
    }
}
