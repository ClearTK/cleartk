/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.eval.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */
@Deprecated
public class FCollectionsTest {

  @Test
  public void testInteger() throws Exception {
    FCollections<Integer> fc = new FCollections<Integer>();
    // ten true positives for '1'
    fc.addTruePositives(1, 9);
    fc.addTruePositive(1);
    // five false positives for '1'
    fc.addFalsePositives(1, 4);
    fc.addFalsePositive(1);
    // fifteen false negatives for '1'
    fc.addFalseNegatives(1, 14);
    fc.addFalseNegative(1);

    // add one of each for '2'
    fc.addTruePositive(2);
    fc.addFalsePositive(2);
    fc.addFalseNegative(2);

    // key = 1
    assertEquals(10, fc.getTruePositivesCount(1));
    assertEquals(5, fc.getFalsePositivesCount(1));
    assertEquals(15, fc.getFalseNegativesCount(1));

    assertEquals(0.6666667d, fc.getPrecision(1), 0.0001d); // 10 / (10 + 5)
    assertEquals(0.4d, fc.getRecall(1), 0.0001d); // 10 / (10 + 15)
    assertEquals(0.5d, fc.getF(1), 0.0001d); // (2 * 10) / (2 * 10 + 10 + 15)

    // key = 2
    assertEquals(1, fc.getTruePositivesCount(2));
    assertEquals(1, fc.getFalsePositivesCount(2));
    assertEquals(1, fc.getFalseNegativesCount(2));

    assertEquals(0.5d, fc.getPrecision(2), 0.0001d); // 1 / (1 + 1)
    assertEquals(0.5d, fc.getRecall(2), 0.0001d); // 1 / (1 + 1)
    assertEquals(0.5d, fc.getF(2), 0.0001d); // (2 * 1) / (2 * 1 + 1 + 1)

    // aggregate
    assertEquals(11, fc.getTruePositivesCount());
    assertEquals(6, fc.getFalsePositivesCount());
    assertEquals(16, fc.getFalseNegativesCount());

    assertEquals(0.64706d, fc.getPrecision(), 0.0001d); // 11 / (11 + 6)
    assertEquals(0.40741d, fc.getRecall(), 0.0001d); // 11 / (11 + 16)
    assertEquals(0.5d, fc.getF(), 0.0001d); // (2 * 11) / (2 * 11 + 6 + 16)

    List<Integer> keys = fc.getSortedObjects();
    assertEquals(2, keys.size());
    assertEquals(Integer.valueOf(1), keys.get(0));
    assertEquals(Integer.valueOf(2), keys.get(1));

    assertEquals(Double.NaN, fc.getF(3), 0.0001d);
  }
}
