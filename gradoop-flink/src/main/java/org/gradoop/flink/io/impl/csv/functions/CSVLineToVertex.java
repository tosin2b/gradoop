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
package org.gradoop.flink.io.impl.csv.functions;

import org.gradoop.common.model.api.entities.VertexFactory;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.metadata.MetaData;
import org.gradoop.common.model.impl.pojo.EPGMVertex;
import org.gradoop.flink.io.api.metadata.MetaDataSource;

/**
 * Creates a {@link EPGMVertex} from a CSV string. The function uses a
 * {@link MetaData} object to correctly parse the property values.
 *
 * The string needs to be encoded in the following format:
 *
 * {@code vertex-id;[graph-ids];vertex-label;value_1|value_2|...|value_n}
 */
public class CSVLineToVertex extends CSVLineToElement<EPGMVertex> {
  /**
   * Used to instantiate the vertex.
   */
  private final VertexFactory<EPGMVertex> vertexFactory;

  /**
   * Constructor
   *
   * @param epgmVertexFactory EPGM vertex factory
   */
  public CSVLineToVertex(VertexFactory<EPGMVertex> epgmVertexFactory) {
    this.vertexFactory = epgmVertexFactory;
  }

  @Override
  public EPGMVertex map(String csvLine) throws Exception {
    String[] tokens = split(csvLine, 4);
    String label = StringEscaper.unescape(tokens[2]);
    return vertexFactory.initVertex(
      GradoopId.fromString(tokens[0]),
      label,
      parseProperties(MetaDataSource.VERTEX_TYPE, label, tokens[3]),
      parseGradoopIds(tokens[1])
    );

  }
}
