package fi.vrk.xroad.monitor.monitordata;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Tests env monitor XML data conversion to JSON
 */
public class MonitorDataConverterTest {

  @Test
  public void testConversion() throws IOException {
    String soapmessageString;

    try(FileInputStream inputStream = new FileInputStream("src/test/resources/envmonitor.xml")) {
      soapmessageString = IOUtils.toString(inputStream);
    }

    JSONObject soapDatainJsonObject = XML.toJSONObject(soapmessageString);
    System.out.println(soapDatainJsonObject);
  }
}
