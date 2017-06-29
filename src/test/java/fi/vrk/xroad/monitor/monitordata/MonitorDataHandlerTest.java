package fi.vrk.xroad.monitor.monitordata;

import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


/**
 * Tests for {@link MonitorDataHandler}
 */
@Slf4j
public class MonitorDataHandlerTest {


    private final SecurityServerInfo exampleInfo = new SecurityServerInfo(
            "servername-6.com",
            "servername-6.com",
            "GOV",
            "13775550");

    @Test
    public void handleMonitorDataRequestAndResponseTest() throws ParserConfigurationException, TransformerException {
        MonitorDataHandler handler = new MonitorDataHandler();
        handler.handleMonitorDataRequestAndResponse(exampleInfo);
    }
}
