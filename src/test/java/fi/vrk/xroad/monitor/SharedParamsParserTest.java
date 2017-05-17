package fi.vrk.xroad.monitor;

import fi.vrk.xroad.monitor.parser.SecurityServerInfo;
import fi.vrk.xroad.monitor.parser.SharedParamsParser;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SharedParamsParser}
 */
public class SharedParamsParserTest {

  private final SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
  private final SecurityServerInfo exampleInfo = new SecurityServerInfo(
          "gdev-ss1.i.palveluvayla.com",
          "gdev-ss1.i.palveluvayla.com",
          "GOV",
          "1710128-9",
          "Gofore");

  @Test
  public void testParse() throws IOException, SAXException, ParserConfigurationException {
    List<SecurityServerInfo> resultList = parser.parse();
    assertNotNull(resultList);
    assertThat(resultList.size(), not(is(0)));
    assertTrue(resultList.contains(exampleInfo));
  }
}