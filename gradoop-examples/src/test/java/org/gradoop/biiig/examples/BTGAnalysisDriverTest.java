package org.gradoop.biiig.examples;

import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.OrderedBytes;
import org.apache.hadoop.hbase.util.PositionedByteRange;
import org.apache.hadoop.hbase.util.SimplePositionedByteRange;
import org.apache.log4j.Logger;
import org.gradoop.GradoopClusterTest;
import org.gradoop.algorithms.SelectAndAggregate;
import org.gradoop.algorithms.Sort;
import org.gradoop.model.Graph;
import org.gradoop.model.Vertex;
import org.gradoop.storage.GraphStore;
import org.gradoop.utils.ConfigurationUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Tests the pipeline described in
 * {@link org.gradoop.biiig.examples.BTGAnalysisDriver}.
 */
public class BTGAnalysisDriverTest extends GradoopClusterTest {

  @Test
  public void driverTest() throws Exception {
    Configuration conf = utility.getConfiguration();

    String graphFile = "btg2.graph";
    String sortTable = "graphs_sorted";

    String[] args = new String[] {
      "-" + ConfigurationUtils.OPTION_WORKERS, "1",
      "-" + ConfigurationUtils.OPTION_REDUCERS, "1",
      "-" + ConfigurationUtils.OPTION_HBASE_SCAN_CACHE, "500",
      "-" + ConfigurationUtils.OPTION_GRAPH_INPUT_PATH, graphFile,
      "-" + ConfigurationUtils.OPTION_GRAPH_OUTPUT_PATH, "/output/import/biiig",
      "-" + ConfigurationUtils.OPTION_DROP_TABLES,
      "-" + ConfigurationUtils.OPTION_SORT_TABLE_NAME, sortTable
    };

    copyFromLocal(graphFile);

    // create table for sort output
    createTable(sortTable);

    BTGAnalysisDriver btgAnalysisDriver = new BTGAnalysisDriver();
    btgAnalysisDriver.setConf(conf);

    // run the pipeline
    int exitCode = btgAnalysisDriver.run(args);

    // tests
    assertThat(exitCode, is(0));
    GraphStore graphStore = openGraphStore();
    // BTG results
    validateBTGComputation(graphStore);
    // Select and Aggregate results
    validateSelectAndAggregate(graphStore);
    // sort results
    validateSort(conf);

    graphStore.close();
  }

  private void validateBTGComputation(GraphStore graphStore) {
    // vertices
    validateBTGs(graphStore.readVertex(0L), 4L, 9L, 16L);
    validateBTGs(graphStore.readVertex(1L), 4L, 9L, 16L);
    validateBTGs(graphStore.readVertex(2L), 4L, 9L, 16L);
    validateBTGs(graphStore.readVertex(3L), 4L, 9L, 16L);
    validateBTGs(graphStore.readVertex(4L), 4L);
    validateBTGs(graphStore.readVertex(5L), 4L);
    validateBTGs(graphStore.readVertex(6L), 4L);
    validateBTGs(graphStore.readVertex(7L), 4L);
    validateBTGs(graphStore.readVertex(8L), 4L);
    validateBTGs(graphStore.readVertex(9L), 9L);
    validateBTGs(graphStore.readVertex(10L), 9L);
    validateBTGs(graphStore.readVertex(11L), 9L);
    validateBTGs(graphStore.readVertex(12L), 9L);
    validateBTGs(graphStore.readVertex(13L), 9L);
    validateBTGs(graphStore.readVertex(14L), 9L);
    validateBTGs(graphStore.readVertex(15L), 9L);
    validateBTGs(graphStore.readVertex(16L), 16L);
    validateBTGs(graphStore.readVertex(17L), 16L);
    validateBTGs(graphStore.readVertex(18L), 16L);
    validateBTGs(graphStore.readVertex(19L), 16L);
    validateBTGs(graphStore.readVertex(20L), 16L);

    // graphs
    validateGraph(graphStore.readGraph(4L), 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
    validateGraph(graphStore.readGraph(9L), 0L, 1L, 2L, 3L, 9L, 10L, 11L, 12L,
      13L, 14L, 15L);
    validateGraph(graphStore.readGraph(16L), 0L, 1L, 2L, 3L, 16L, 17L, 18L, 19L,
      20L);

  }

  private void validateBTGs(Vertex vertex, long... expectedBTGs) {
    assertNotNull(vertex);
    assertEquals(expectedBTGs.length, vertex.getGraphCount());
    List<Long> graphIDs = Lists.newArrayList(vertex.getGraphs());
    for (long expectedBTG : expectedBTGs) {
      assertTrue(graphIDs.contains(expectedBTG));
    }
  }

  private void validateSelectAndAggregate(GraphStore graphStore) {
    Graph g1 = graphStore.readGraph(4L);
    Graph g3 = graphStore.readGraph(16L);

    // g1 has the aggregated value stored
    assertEquals(1, g1.getPropertyCount());
    Object prop =
      g1.getProperty(SelectAndAggregate.DEFAULT_AGGREGATE_RESULT_PROPERTY_KEY);
    assertNotNull(prop);
    assertEquals(-147.25, prop);

    // g3 has the aggregated value stored
    assertEquals(1, g3.getPropertyCount());
    prop =
      g3.getProperty(SelectAndAggregate.DEFAULT_AGGREGATE_RESULT_PROPERTY_KEY);
    assertNotNull(prop);
    assertEquals(163.91, prop);
  }

  private void validateGraph(Graph g, Long... expectedVertices) {
    assertNotNull(g);
    assertEquals(expectedVertices.length, g.getVertexCount());
    List<Long> actualVertices = Lists.newArrayList(g.getVertices());
    for (Long vertexID : expectedVertices) {
      assertTrue(actualVertices.contains(vertexID));
    }
  }

  private void validateSort(Configuration config) throws IOException {
    HTable table = new HTable(config, "graphs_sorted");

    byte[] cf = Bytes.toBytes(Sort.COLUMN_FAMILY_NAME);
    byte[] col = Bytes.toBytes(Sort.COLUMN_NAME);

    Scan scan = new Scan();
    scan.addColumn(cf, col);

    ResultScanner sc = table.getScanner(scan);
    Result res;
    int rowCount = 0;
    /**
     * Scan through sorted table and check order.
     */
    while ((res = sc.next()) != null) {
      if (rowCount == 0) {
        assertEquals(16L, Bytes.toLong(res.getValue(cf, col)));
        assertEquals(Double.valueOf(163.91), decodeDouble(res.getRow()));
      } else if (rowCount == 1) {
        assertEquals(4L, Bytes.toLong(res.getValue(cf, col)));
        assertEquals(Double.valueOf(-147.25), decodeDouble(res.getRow()));
      }
      rowCount++;
    }
    sc.close();
  }

  private Double decodeDouble(byte[] value) {
    PositionedByteRange buf = new SimplePositionedByteRange(value);
    return OrderedBytes.decodeNumericAsDouble(buf);
  }
}
