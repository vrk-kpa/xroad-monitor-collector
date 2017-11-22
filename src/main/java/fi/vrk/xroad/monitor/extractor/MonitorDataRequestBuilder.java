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
import fi.vrk.xroad.monitor.util.MonitorCollectorPropertyKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.UUID;

/**
 * Creates extractor request xml.
 */
@Slf4j
@Component
public class MonitorDataRequestBuilder {

    private final String instance;
    private final String clientMemberClass;
    private final String clientMemberCode;
    private final String clientSubsystemCode;
    private final String[] queryParameters;

    /**
     * Constructor
     * @param environment
     */
    public MonitorDataRequestBuilder(Environment environment) {
        instance = environment.getProperty(MonitorCollectorPropertyKeys.INSTANCE);
        clientMemberClass = environment.getProperty(MonitorCollectorPropertyKeys.CLIENT_MEMBER_CLASS);
        clientMemberCode = environment.getProperty(MonitorCollectorPropertyKeys.CLIENT_MEMBER_CODE);
        clientSubsystemCode = environment.getProperty(MonitorCollectorPropertyKeys.CLIENT_SUBSYSTEM);
        queryParameters = environment.getProperty(MonitorCollectorPropertyKeys.QUERY_PARAMETERS).split(",");
    }

    /**
     * Makes xml string what is request for securityserver monitoring metrics
     *
     * @param serverInfo server information what is target of request
     * @return xml string request
     */
    public String getRequestXML(SecurityServerInfo serverInfo) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;

        // This should not be ever exception.
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Failed to create document builder factory {}", e);
        }

        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("SOAP-ENV:Envelope");
        document.appendChild(rootElement);

        rootElement.setAttribute("xmlns:SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
        rootElement.setAttribute("xmlns:id", "http://x-road.eu/xsd/identifiers");
        rootElement.setAttribute("xmlns:xrd", "http://x-road.eu/xsd/xroad.xsd");
        rootElement.setAttribute("xmlns:m", "http://x-road.eu/xsd/monitoring");

        Element headerElement = document.createElement("SOAP-ENV:Header");
        rootElement.appendChild(headerElement);

        Element clientServer = document.createElement("xrd:client");
        headerElement.appendChild(clientServer);

        clientServer.setAttribute("id:objectType", "SUBSYSTEM");
        clientServer.appendChild(createElementWithValue(document, "id:xRoadInstance", instance));
        clientServer.appendChild(createElementWithValue(document, "id:memberClass", clientMemberClass));
        clientServer.appendChild(createElementWithValue(document, "id:memberCode", clientMemberCode));
        clientServer.appendChild(createElementWithValue(document, "id:subsystemCode", clientSubsystemCode));

        Element service = document.createElement("xrd:service");
        headerElement.appendChild(service);

        service.setAttribute("id:objectType", "SERVICE");
        service.appendChild(createElementWithValue(document, "id:xRoadInstance", instance));
        service.appendChild(createElementWithValue(document, "id:memberClass", serverInfo.getMemberClass()));
        service.appendChild(createElementWithValue(document, "id:memberCode", serverInfo.getMemberCode()));
        service.appendChild(createElementWithValue(document, "id:serviceCode", "getSecurityServerMetrics"));

        Element securityServerElement = document.createElement("xrd:securityServer");
        headerElement.appendChild(securityServerElement);

        securityServerElement.setAttribute("id:objectType", "SERVER");
        securityServerElement.appendChild(createElementWithValue(document, "id:xRoadInstance", instance));
        securityServerElement.appendChild(
                createElementWithValue(document, "id:memberClass", serverInfo.getMemberClass())
        );
        securityServerElement.appendChild(
                createElementWithValue(document, "id:memberCode", serverInfo.getMemberCode())
        );
        securityServerElement.appendChild(
                createElementWithValue(document, "id:serverCode", serverInfo.getServerCode())
        );

        headerElement.appendChild(createElementWithValue(document, "xrd:id", UUID.randomUUID().toString()));
        headerElement.appendChild(createElementWithValue(document, "xrd:protocolVersion", "4.0"));

        Element bodyElement = document.createElement("SOAP-ENV:Body");
        rootElement.appendChild(bodyElement);

        bodyElement.appendChild(metricRequestPayload(document));

        document.normalizeDocument();

        return getStringFromDocument(document);
    }

    /**
     * Helper function for building metricdata request body. For testing purposes this is protected.
     * @param document
     * @return
     */
    protected Node metricRequestPayload(Document document) {
        Element metricRequestRoot = document.createElement("m:getSecurityServerMetrics");
        if (!queryParameters[0].equals("")) {
            Element outputSpec = document.createElement("m:outputSpec");
            Arrays.stream(queryParameters).forEach(parameter -> {
                outputSpec.appendChild(createElementWithValue(document, "m:outputField", parameter));
            });
            metricRequestRoot.appendChild(outputSpec);
        } else {
            log.info("No query parameters given, requesting full metric data.");
        }
        return metricRequestRoot;
    }

    /**
     * Helper for create elementwith values
     *
     * @param doc
     * @param name
     * @param value
     * @return new element
     */
    private Element createElementWithValue(Document doc, String name, String value) {
        Element el = doc.createElement(name);
        el.appendChild(doc.createTextNode(value));
        return el;
    }

    /**
     * Parsing document to string
     *
     * @param doc
     * @return
     */
    private String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException ex) {
            log.error("Error parsing document to string", ex);
            return "";
        }
    }

}
