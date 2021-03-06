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
package org.gradoop.flink.model.impl.operators.layouting.functions;

import org.apache.flink.api.common.functions.util.ListCollector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.gradoop.flink.model.impl.operators.layouting.util.Force;
import org.gradoop.flink.model.impl.operators.layouting.util.LVertex;
import org.gradoop.flink.model.impl.operators.layouting.util.Vector;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.gradoop.flink.model.impl.operators.layouting.functions.Util.getDummyVertex;

public class FRAttractionFunctionTest {
  @Test
  public void testAttractionFunction() throws Exception {
    FRAttractionFunction af = new FRAttractionFunction(10);
    LVertex v1 = getDummyVertex(1, 1);
    LVertex v2 = getDummyVertex(2, 3);
    LVertex v3 = getDummyVertex(7, 5);
    LVertex v4 = getDummyVertex(1, 1);

    List<Force> collectorList = new ArrayList<>();
    ListCollector<Force> collector = new ListCollector<>(collectorList);

    af.flatMap(new Tuple3<>(v1, v2, 1), collector);
    Vector vec12 = collectorList.get(0).getValue().copy();
    Vector vec21 = collectorList.get(1).getValue().copy();
    Assert.assertNotEquals(collectorList.get(0).getId(), collectorList.get(1).getId());
    collectorList.clear();

    af.flatMap(new Tuple3<>(v1, v3, 1), collector);
    Vector vec13 = collectorList.get(0).getValue().copy();
    collectorList.clear();

    af.flatMap(new Tuple3<>(v1, v4, 1), collector);
    Vector vec14 = collectorList.get(0).getValue().copy();
    collectorList.clear();

    af.flatMap(new Tuple3<>(v1, v1, 1), collector);
    Vector vec11 = collectorList.get(0).getValue().copy();
    collectorList.clear();

    v1 = getDummyVertex(1, 1);
    v3 = getDummyVertex(7, 5);
    af.flatMap(new Tuple3<>(v1, v3, 5), collector);
    Vector vec13f5 = collectorList.get(0).getValue().copy();
    collectorList.clear();


    Assert.assertEquals(vec12, vec21.mul(-1));
    Assert.assertTrue(vec12.getX() > 0 && vec12.getY() > 0);
    Assert.assertTrue(vec12.magnitude() < vec13.magnitude());
    Assert.assertEquals(vec14, new Vector());
    Assert.assertTrue(vec11.magnitude() == 0);
    Assert.assertEquals(vec13.mul(5), vec13f5);
  }
}
