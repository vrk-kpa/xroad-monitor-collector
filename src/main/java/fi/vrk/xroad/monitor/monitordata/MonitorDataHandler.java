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

import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import fi.vrk.xroad.monitor.util.MonitorCollectorPropertyKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Handler for monitordata request, response and parsing
 */
@Slf4j
@Component
public class MonitorDataHandler {

    @Autowired
    private Environment environment;

    //@Value("${xroad-monitor-collector-url.client-url}")
    //private String clientUrl;

    /**
     * Will handle getting metric data and saving it to elasticseach
     *
     * @param securityServerInfo information of securityserver what metric to get
     */
    public String handleMonitorDataRequestAndResponse(SecurityServerInfo securityServerInfo) {
        MonitorDataRequest request = new MonitorDataRequest();
        MonitorDataResponse response = new MonitorDataResponse();
        String responseXml = makeRequest(request.getRequestXML(securityServerInfo));
        return response.getMetricInformation(responseXml);
    }

    /**
     * Makes request to get securityserver metric information
     *
     * @param xmlRequest to posted in body to securityserver
     * @return securityserver metric information response
     */
    public String makeRequest(String xmlRequest) {
        RestTemplate rt = new RestTemplate();
        rt.getMessageConverters().add(new Jaxb2RootElementHttpMessageConverter());
        rt.getMessageConverters().add(new StringHttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> entity = new HttpEntity<>(xmlRequest, headers);

        String clientUrl = environment != null ? environment.getProperty(MonitorCollectorPropertyKeys.CLIENT_URL) : null;
        log.info("clientUrl:" + clientUrl);
        return rt.postForObject(clientUrl, entity, String.class);
    }

}
