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
package org.gradoop.flink.algorithms.fsm.dimspan.functions.mining;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.gradoop.flink.algorithms.fsm.dimspan.tuples.GraphWithPatternEmbeddingsMap;
import org.gradoop.flink.model.impl.tuples.WithCount;

/**
 * {@code (graph, pattern -> embedding) => pattern, ...}
 */
public class ExpandFrequentPatterns
  implements FlatMapFunction<GraphWithPatternEmbeddingsMap, WithCount<int[]>> {

  @Override
  public void flatMap(GraphWithPatternEmbeddingsMap graphWithPatternEmbeddingsMap,
    Collector<WithCount<int[]>> collector) throws Exception {

    for (int i = 0; i < graphWithPatternEmbeddingsMap.getMap().getPatternCount(); i++) {
      int[] pattern = graphWithPatternEmbeddingsMap.getMap().getPattern(i);
      int frequency = graphWithPatternEmbeddingsMap.getMap().getValues()[i][0];
      collector.collect(new WithCount<>(pattern, frequency));
    }
  }
}
