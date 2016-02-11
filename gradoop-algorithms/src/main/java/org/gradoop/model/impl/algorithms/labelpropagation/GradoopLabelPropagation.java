/*
 * This file is part of Gradoop.
 *
 * Gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gradoop. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gradoop.model.impl.algorithms.labelpropagation;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.types.NullValue;
import org.gradoop.model.api.EPGMEdge;
import org.gradoop.model.api.EPGMGraphHead;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.impl.algorithms.labelpropagation.functions.LPMessageFunction;
import org.gradoop.model.impl.algorithms.labelpropagation.functions.LPUpdateFunction;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.properties.PropertyValue;

/**
 * Executes the label propagation integrated in Gradoop.
 *
 * @param <G> EPGM graph head type
 * @param <V> EPGM vertex type
 * @param <E> EPGM edge type
 */
public class GradoopLabelPropagation
  <G extends EPGMGraphHead, V extends EPGMVertex, E extends EPGMEdge>
  extends LabelPropagation<G, V, E> {

  /**
   * Constructor
   *
   * @param maxIterations Counter to define maximal iteration for the algorithm
   * @param propertyKey   Property key to access the label value
   */
  protected GradoopLabelPropagation(int maxIterations, String propertyKey) {
    super(maxIterations, propertyKey);
  }

  @Override
  protected DataSet<Vertex<GradoopId, PropertyValue>> executeInternal(
    Graph<GradoopId, PropertyValue, NullValue> gellyGraph) {
    return gellyGraph.runScatterGatherIteration(
      new LPUpdateFunction(), new LPMessageFunction(), getMaxIterations())
      .getVertices();
  }
}
