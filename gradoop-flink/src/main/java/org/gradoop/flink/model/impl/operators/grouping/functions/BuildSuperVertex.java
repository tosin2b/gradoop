/*
 * Copyright © 2014 - 2020 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradoop.flink.model.impl.operators.grouping.functions;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.FunctionAnnotation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.gradoop.common.model.api.entities.Vertex;
import org.gradoop.common.model.api.entities.VertexFactory;
import org.gradoop.flink.model.impl.operators.grouping.tuples.VertexGroupItem;

/**
 * Creates a new super vertex representing a vertex group. The vertex stores the
 * group label, the group property value and the aggregate values for its group.
 *
 * @param <V> The vertex type.
 */
@FunctionAnnotation.ForwardedFields("f1->id;f2->label")
@FunctionAnnotation.ReadFields("f1;f2;f3;f4;f6")
public class BuildSuperVertex<V extends Vertex> extends BuildBase
  implements MapFunction<VertexGroupItem, V>, ResultTypeQueryable<V> {

  /**
   * EPGMVertex vertexFactory.
   */
  private final VertexFactory<V> vertexFactory;

  /**
   * Creates map function.
   *
   * @param useLabel true, if vertex label shall be considered
   * @param epgmVertexFactory vertex factory
   */
  public BuildSuperVertex(boolean useLabel, VertexFactory<V> epgmVertexFactory) {
    super(useLabel);
    this.vertexFactory = epgmVertexFactory;
  }

  @Override
  public V map(VertexGroupItem groupItem) throws Exception {
    V supVertex = vertexFactory.initVertex(groupItem.getSuperVertexId());

    supVertex.setLabel(groupItem.getGroupLabel());
    setGroupProperties(supVertex, groupItem.getGroupingValues(), groupItem.getLabelGroup());
    setAggregateProperties(
      supVertex,
      groupItem.getAggregateValues(),
      groupItem.getLabelGroup().getAggregateFunctions());

    return supVertex;
  }

  @Override
  public TypeInformation<V> getProducedType() {
    return TypeExtractor.createTypeInfo(vertexFactory.getType());
  }
}
