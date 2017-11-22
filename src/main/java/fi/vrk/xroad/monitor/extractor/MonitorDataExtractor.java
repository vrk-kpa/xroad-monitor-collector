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
import fi.vrk.xroad.monitor.util.MonitorCollectorConstants;
import fi.vrk.xroad.monitor.util.MonitorCollectorPropertyKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Handler for extractor requestBuilder, responseParser and parsing
 */
@Slf4j
@Component
public class MonitorDataExtractor {

    private RestTemplate rt;

    @Autowired
    private Environment environment;

    @Autowired
    private MonitorDataRequestBuilder requestBuilder;

    @Autowired
    private MonitorDataResponseParser responseParser;

    private void initRestTemplate() throws KeyStoreException, IOException, CertificateException,
        NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        final String keystorePath = environment.getProperty("xroad-monitor-collector-client.ssl-keystore");
        final String truststorePath = environment.getProperty("xroad-monitor-collector-client.ssl-truststore");
        File keystoreFile = new File(keystorePath);
        File truststoreFile = new File(truststorePath);
        if (keystoreFile.exists() && truststoreFile.exists()) {
            final String keystorePassword =
                environment.getProperty("xroad-monitor-collector-client.ssl-keystore-password");
            final String truststorePassword =
                environment.getProperty("xroad-monitor-collector-client.ssl-truststore-password");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(new File(keystorePath)), keystorePassword.toCharArray());
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                new SSLContextBuilder()
                    .loadKeyMaterial(keyStore, keystorePassword.toCharArray())
                    .loadTrustMaterial(truststoreFile, truststorePassword.toCharArray())
                    .build(),
                NoopHostnameVerifier.INSTANCE);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
                .setDefaultRequestConfig(getRequestConfig()).build();
            ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            rt = new RestTemplate(requestFactory);
        } else {
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(getRequestConfig()).build();
            ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            rt = new RestTemplate(requestFactory);
        }
        rt.getMessageConverters().add(new Jaxb2RootElementHttpMessageConverter());
        rt.getMessageConverters().add(new StringHttpMessageConverter());
    }

    private RequestConfig getRequestConfig() {
        RequestConfig result = RequestConfig.custom()
            .setConnectionRequestTimeout(MonitorCollectorConstants.CONNECT_TIMEOUT)
            .setConnectTimeout(MonitorCollectorConstants.CONNECT_TIMEOUT)
            .setSocketTimeout(MonitorCollectorConstants.CONNECT_TIMEOUT)
            .build();
        return result;
    }

    /**
     * Will handle getting metric data and saving it to elasticseach
     *
     * @param securityServerInfo information of securityserver what metric to get
     */
    public String handleMonitorDataRequestAndResponse(SecurityServerInfo securityServerInfo) throws
        CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
        KeyManagementException, IOException {
        return responseParser.getMetricInformation(makeRequest(requestBuilder.getRequestXML(securityServerInfo)),
            securityServerInfo, environment.getProperty(MonitorCollectorPropertyKeys.INSTANCE));
    }

    /**
     * Get default environmental monitoring data for security server as JSON
     * @param info security server information
     * @return default JSON
     */
    public String getDefaultJSON(SecurityServerInfo info) {
        return responseParser.getDefaultJSON(info, environment.getProperty(MonitorCollectorPropertyKeys.INSTANCE));
    }

    /**
     * Makes request to get securityserver metric information
     * @param xmlRequest to posted in body to securityserver
     * @return securityserver metric information response as xml string
     */
    public String makeRequest(String xmlRequest) throws CertificateException, UnrecoverableKeyException,
        NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        if (rt == null) {
            initRestTemplate();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        HttpEntity<String> entity = new HttpEntity<>(xmlRequest, headers);
        String clientUrl = environment != null
            ? environment.getProperty(MonitorCollectorPropertyKeys.CLIENT_URL) : null;
        log.debug("posting soap request, clientUrl: {} request: {}", clientUrl, xmlRequest);
        return rt.postForObject(clientUrl, entity, String.class);
    }

    /**
     * @return last error description string
     */
    public String getLastErrorDescription() {
        return responseParser.getLastErrorDescription();
    }
}
