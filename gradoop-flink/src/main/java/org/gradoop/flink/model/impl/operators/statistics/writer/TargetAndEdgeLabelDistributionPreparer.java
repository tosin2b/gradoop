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
package org.gradoop.flink.model.impl.operators.statistics.writer;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.operators.MapOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.api.operators.UnaryGraphToValueOperator;
import org.gradoop.flink.model.impl.operators.statistics.TargetLabelAndEdgeLabelDistribution;
import org.gradoop.flink.model.impl.tuples.WithCount;

/**
 * Computes {@link TargetLabelAndEdgeLabelDistribution} for a given logical graph.
 */
public class TargetAndEdgeLabelDistributionPreparer implements
UnaryGraphToValueOperator<MapOperator<WithCount<Tuple2<String, String>>,
Tuple3<String, String, Long>>> {

  @Override
  public MapOperator<WithCount<Tuple2<String, String>>, Tuple3<String, String, Long>>
  execute(final LogicalGraph graph) {
    return new TargetLabelAndEdgeLabelDistribution()
        .execute(graph)
        .map(value -> Tuple3.of(value.f0.f0, value.f0.f1, value.f1))
        .returns(new TypeHint<Tuple3<String, String, Long>>() { });
  }
}
