package org.cleartk.ml.feature.transform.extractor;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.cleartk.test.util.DefaultTestBase;
import org.junit.Test;

public class CosineSimilarityTest extends DefaultTestBase {

  @Test
  public void testBasic() throws Exception {
    Map<String, Double> vector1 = new HashMap<String, Double>();
    Map<String, Double> vector2 = new HashMap<String, Double>();

    vector1.put("a", 2d);
    vector1.put("b", 3d);
    vector1.put("c", 4d);
    vector1.put("d", 5d);

    vector2.put("a", 2d);
    vector2.put("b", 3d);
    vector2.put("d", 4d);
    vector2.put("e", 6d);

    // 2*2 + 3*3 + 5*4 = 33
    assertEquals(33d, CosineSimilarity.dotProduct(vector1, vector2), 0.0001d);
    // 2*2 + 3*3 + 4*4 + 5*5 =
    assertEquals(54d, CosineSimilarity.dotProduct(vector1, vector1), 0.0001d);
    // 2*2 + 3*3 + 4*4 + 6*6 =
    assertEquals(65d, CosineSimilarity.dotProduct(vector2, vector2), 0.0001d);

    // sqrt(54)
    assertEquals(7.348469228349534d, CosineSimilarity.magnitude(vector1), 0.0001d);
    // sqrt(65)
    assertEquals(8.06225774829855d, CosineSimilarity.magnitude(vector2), 0.0001d);
    // 33d / (7.348469228349534*8.06225774829855)
    assertEquals(0.557006652, new CosineSimilarity().distance(vector1, vector2), 0.0001d);
  }
}
