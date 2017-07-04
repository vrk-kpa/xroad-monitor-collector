package fi.vrk.xroad.monitor.monitordata;

import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;


/**
 * Tests for {@link MonitorDataHandler}
 */
@Slf4j
@SpringBootTest(classes = { MonitorDataRequest.class, MonitorDataHandler.class, MonitorDataResponse.class})
@RunWith(SpringRunner.class)
public class MonitorDataHandlerTest {

    @Autowired
    private MonitorDataHandler handler;

    @Autowired
    private MonitorDataRequest request;

    @Autowired
    private MonitorDataResponse response;

    private final SecurityServerInfo exampleInfo = new SecurityServerInfo(
            "gdev-ss1.i.palveluvayla.com",
            "http://gdev-ss1.i.palveluvayla.com",
            "GOV",
            "1710128-9");

    @Test
    public void makeRequest() {
        String xmlRequest = request.getRequestXML(exampleInfo);
        String root = handler.makeRequest(xmlRequest);
        log.info("result: {}", root);
        String metric = response.getMetricInformation(root);
        log.info("body: {}", metric);
    }
}
