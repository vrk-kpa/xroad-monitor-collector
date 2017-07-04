package fi.vrk.xroad.monitor.monitordata;

import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for {@link MonitorDataRequest}
 */
@Slf4j
@SpringBootTest(classes = MonitorDataRequest.class)
@RunWith(SpringRunner.class)
public class MonitorDataRequestTest {

    @Autowired
    private MonitorDataRequest request;

    private final SecurityServerInfo exampleInfo = new SecurityServerInfo(
            "gdev-ss1.i.palveluvayla.com",
            "http://gdev-ss1.i.palveluvayla.com",
            "GOV",
            "1710128-9");

    @Test
    public void getRequestXMLTest() {
        String xmlRequest = request.getRequestXML(exampleInfo);
        log.info(xmlRequest);
    }
}
