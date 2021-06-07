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
package fi.vrk.xroad.monitor.elasticsearch;

import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;

import java.io.IOException;

/**
 * Interface for loading/saving env monitor data
 */
public interface EnvMonitorDataStorageDao {

  /**
   * Save data
   */
  IndexResponse save(String index, String type, String json) throws IOException;

  /**
   * Load data
   */
  GetResponse load(String index, String type, String key) throws IOException;

  /**
   * Create alias for given index
   * @param index
   * @param alias
   */
  boolean addIndexToAlias(String index, String alias) throws  IOException;

  /**
   * Remove all indexes from alias
   * @param alias
   * @return
   */
  boolean removeAllIndexesFromAlias(String alias) throws IOException;

  /**
   * Tests if given alias exists
   * @param alias
   * @return
   */
  boolean aliasExists(String alias) throws IOException;

  /**
   * Tests if given index exists
   * @param index
   * @return
   */
  boolean indexExists(String index) throws IOException;

  /**
   * Removes given index
   * @param index
   * @return
   */
  boolean removeIndex(String index) throws IOException;

  /**
   * Find all documents from given index and type
   * @param index
   * @param type
   * @return search response
   */
  SearchResponse findAll(String index, String type) throws IOException;

  /**
   * Flush index operations
   * @return flush response
   */
  FlushResponse flush() throws IOException;

  /**
   * Create index
   * @return create index response
   */
  CreateIndexResponse createIndex(String index) throws IOException;

}
