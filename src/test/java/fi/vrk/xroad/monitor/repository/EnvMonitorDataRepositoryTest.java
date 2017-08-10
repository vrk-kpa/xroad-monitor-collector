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
package fi.vrk.xroad.monitor.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests for {@link EnvMonitorDataRepository}
 */
@SpringBootTest(classes = EnvMonitorDataRepository.class)
@RunWith(SpringRunner.class)
public class EnvMonitorDataRepositoryTest {

  @Autowired
  private EnvMonitorDataRepository sampleArticleRepository;

  @Before
  public void emptyData() {
    sampleArticleRepository.deleteAll();
  }

  @Test
  public void shouldIndexSingleEnvMonitorDataEntity() {

//    Article article = new Article();
//    article.setId("123455");
//    article.setTitle("Spring Data Elasticsearch Test Article");
//    List<String> authors = new ArrayList<String>();
//    authors.add("Author1");
//    authors.add("Author2");
//    article.setAuthors(authors);
//    List<String> tags = new ArrayList<String>();
//    tags.add("tag1");
//    tags.add("tag2");
//    tags.add("tag3");
//    article.setTags(tags);
//    //Indexing using sampleArticleRepository
//    sampleArticleRepository.save(article);
//    //lets try to search same record in elasticsearch
//    Article indexedArticle = sampleArticleRepository.findOne(article.getId());
//    assertThat(indexedArticle,is(notNullValue()));
//    assertThat(indexedArticle.getId(),is(article.getId()));
//    assertThat(indexedArticle.getAuthors().size(),is(authors.size()));
//    assertThat(indexedArticle.getTags().size(),is(tags.size()));
  }
}
