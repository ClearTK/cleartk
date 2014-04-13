/** 
 * Copyright (c) 2014, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.ml.feature.transform.extractor;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.cleartk.test.util.DefaultTestBase;
import org.junit.Test;

/**
 * Copyright (c) 2014, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class FixedCosineSimilarityTest extends DefaultTestBase {

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

    SimilarityFunction similarity = new FixedCosineSimilarity(vector1);
    assertEquals(0.557006652, similarity.distance(vector2, vector1), 0.0001d);
    assertEquals(0.557006652, ((FixedCosineSimilarity) similarity).distance(vector2), 0.0001d);

  }
}
