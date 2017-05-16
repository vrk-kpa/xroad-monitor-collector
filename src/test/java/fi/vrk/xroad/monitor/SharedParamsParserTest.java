package fi.vrk.xroad.monitor;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Tests for {@link SharedParamsParser}
 */
public class SharedParamsParserTest {

  private final SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");

  @Test
  public void testParse() throws IOException, SAXException, ParserConfigurationException {
    parser.parse();
  }
}
