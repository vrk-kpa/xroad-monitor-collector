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
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Handles response and returns only body
 */
@Slf4j
@Component
public class MonitorDataResponse {

    /**
     * Parse metric information from respose string
     * @param response xml string what is gotten from securityserver
     * @return metric data in xml string
     */
    public String getMetricInformation(String response) {
        Document root = parseResponseDocument(response);

        if (root != null) {
            root.normalizeDocument();

            NodeList nodeList = root.getElementsByTagName("m:getSecurityServerMetricsResponse");

            return nodeToString(nodeList.item(0));
        }
        return null;
    }

    /**
     * Parse xml node to string
     * @param item xml node
     * @return xml string
     */
    private String nodeToString(Node item) {
        try {
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(item), result);
            return writer.toString();
        } catch (TransformerException e) {
            log.error("Failed to parse string from metric node: {}", e);
            return "";
        }
    }

    /**
     * Parse respose string to xml document
     * @param response string
     * @return xml document
     */
    private Document parseResponseDocument(String response) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(response));
            return builder.parse(is);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Failed to parse response document from string: {}", e);
            return null;
        }
    }
}
