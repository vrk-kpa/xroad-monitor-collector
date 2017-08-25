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

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.alias.exists.AliasesExistResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;

import java.util.concurrent.ExecutionException;

/**
 * Interface for loading/saving env monitor data
 */
public interface EnvMonitorDataStorageDao {

  /**
   * Save data
   */
  IndexResponse save(String index, String type, String json);

  /**
   * Load data
   */
  GetResponse load(String index, String type, String key);

  /**
   * Create alias for given index
   * @param index
   * @param alias
   */
  IndicesAliasesResponse addIndexToAlias(String index, String alias) throws ExecutionException, InterruptedException;

  /**
   * Remove all indexes from alias
   * @param alias
   * @return
   */
  IndicesAliasesResponse removeAllIndexesFromAlias(String alias);

  /**
   * Tests if given alias exists
   * @param alias
   * @return
   */
  AliasesExistResponse aliasExists(String alias) throws ExecutionException, InterruptedException;

  /**
   * Tests if given index exists
   * @param index
   * @return
   */
  IndicesExistsResponse indexExists(String index);

  /**
   * Removes given index
   * @param index
   * @return
   */
  DeleteIndexResponse removeIndex(String index);

}
