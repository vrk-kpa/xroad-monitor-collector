package fi.vrk.xroad.monitor.parser;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Tests for {@link SharedParamsParser}
 */
public class SharedParamsParserTest {

  private final SharedParamsParser parser = new SharedParamsParser("src/test/resources/shared-params.xml");
  private final SecurityServerInfo exampleInfo = new SecurityServerInfo(
          "servername-6.com",
          "servername-6.com",
          "GOV",
          "13775550");

  @Test
  public void testParse() throws IOException, SAXException, ParserConfigurationException {
    Set<SecurityServerInfo> resultList = parser.parse();
    assertNotNull(resultList);
    assertThat(resultList.size(), not(is(0)));
    assertTrue(resultList.contains(exampleInfo));
  }
}