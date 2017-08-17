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

import ee.ria.xroad.proxymonitor.message.*;
import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Handles responseParser and returns only body
 */
@Slf4j
@Component
public class MonitorDataResponseParser {

    @Getter
    private String lastErrorDescription;

    /**
     * Parse metric information from respose string
     *
     * @param response xml string what is gotten from securityserver
     * @return metric data in xml string
     */
    public String getMetricInformation(String response, SecurityServerInfo securityServerInfo) {
        lastErrorDescription = "";

        Document root = parseResponseDocument(response);
        String resultString = "Empty";

        if (root != null) {
            root.normalizeDocument();

            NodeList nodeList = root.getElementsByTagName("m:getSecurityServerMetricsResponse");
            if (nodeList.getLength() == 0) {
                NodeList faultCode = root.getElementsByTagName("faultcode");
                NodeList faultString = root.getElementsByTagName("faultstring");
                log.error("Faultcode in responseParser: {} faultstring: {} responseParser: {}",
                        nodeToString(faultCode.item(0)), nodeToString(faultString.item(0)), response);
                lastErrorDescription = String.format("%s %s", nodeToString(faultCode.item(0)),
                    nodeToString(faultString.item(0)));
                resultString = "This should be fauflt message in somekind, probably in JOSN or failure?";
            } else {
                try {
                    Unmarshaller jaxbUnmarshaller =
                            JAXBContext.newInstance(GetSecurityServerMetricsResponse.class).createUnmarshaller();
                    GetSecurityServerMetricsResponse responseObject
                            = (GetSecurityServerMetricsResponse) jaxbUnmarshaller.unmarshal(nodeList.item(0));
                    resultString = getFormatedJSONObject(responseObject, securityServerInfo).toString();
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }


            return resultString;
        }
        return null;
    }

    private JSONObject getFormatedJSONObject(GetSecurityServerMetricsResponse responseObject,
                                             SecurityServerInfo securityServerInfo) {
        JSONObject json = new JSONObject();
        json.put("serverCode", securityServerInfo.getServerCode());
        json.put("memberCode", securityServerInfo.getMemberCode());
        json.put("memberClass", securityServerInfo.getMemberClass());

        MetricSetType mrt = responseObject.getMetricSet();
        json.put("name", mrt.getName());

        List<MetricType> metricList = mrt.getMetrics();
        StringMetricType versio = (StringMetricType) metricList.get(0);
        json.put(versio.getName(), versio.getValue());

        mrt = (MetricSetType) metricList.get(1);
        metricList = mrt.getMetrics();

        metricList.forEach(metric -> {
            if (metric instanceof HistogramMetricType) {
                HistogramMetricType histogram = (HistogramMetricType) metric;
                JSONObject histogramJson = new JSONObject();
                histogramJson.put("updated", histogram.getUpdated());
                histogramJson.put("min", histogram.getMin());
                histogramJson.put("max", histogram.getMax());
                histogramJson.put("mean", histogram.getMean());
                histogramJson.put("median", histogram.getMean());
                histogramJson.put("stddev", histogram.getStddev());
                json.put(histogram.getName(), histogramJson);
            } else if (metric instanceof NumericMetricType) {
                NumericMetricType numeric = (NumericMetricType) metric;
                json.put(numeric.getName(), numeric.getValue());
            } else if (metric instanceof StringMetricType) {
                StringMetricType string = (StringMetricType) metric;
                json.put(string.getName(), string.getValue());
            } else if (metric instanceof MetricSetType) {
                MetricSetType metricSetType = (MetricSetType) metric;
                ArrayList<JSONObject> arrayList = new ArrayList<>();
                metricSetType.getMetrics().forEach(metricType -> {
                    JSONObject metricJson = new JSONObject();
                    if (metricType instanceof MetricSetType) {
                        ((MetricSetType) metricType).getMetrics().forEach(m -> {
                            StringMetricType stringMetricType = (StringMetricType) m;
                            metricJson.put(stringMetricType.getName(), stringMetricType.getValue());
                        });
                        arrayList.add(metricJson);
                    } else if (metricType instanceof StringMetricType) {
                        metricJson.put(metricType.getName(), ((StringMetricType) metricType).getValue());
                        arrayList.add(metricJson);
                    }
                });
                json.put(metricSetType.getName(), arrayList);
            }
        });


        return json;
    }

    /**
     * Parse xml node to string
     *
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
     *
     * @param response string
     * @return xml document
     */
    private Document parseResponseDocument(String response) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(response));
            return builder.parse(is);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.error("Failed to parse responseParser document from string: {}", e);
            return null;
        }
    }
}
