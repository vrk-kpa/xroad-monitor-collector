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
import javax.xml.transform.TransformerException;

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
            "servername-6.com",
            "servername-6.com",
            "GOV",
            "13775550");

    @Test
    public void getRequestXMLTest() throws ParserConfigurationException {
        Document doc = request.getRequestXML(exampleInfo);
        log.info(doc.toString());
    }
}
