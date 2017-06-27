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

package fi.vrk.xroad.monitor.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
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
@SpringBootTest(classes = SharedParamsParser.class)
@RunWith(SpringRunner.class)
public class SharedParamsParserTest {

  @Autowired
  private SharedParamsParser parser;

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
