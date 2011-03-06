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

import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class ConfusionMatrixTest {
  public enum TestClassType {
    TAG1, TAG2
  }

  @Test
  public void countsTest() {
    ConfusionMatrix<String> confusionMatrix = new ConfusionMatrix<String>();
    confusionMatrix.add("a", "a", 10);
    confusionMatrix.add("a", "b");
    confusionMatrix.add("a", "c", 2);
    // skip b,a
    confusionMatrix.add("b", "b", 20);
    // skip b,a
    confusionMatrix.add("c", "a", 2);
    confusionMatrix.add("c", "b", 1);
    confusionMatrix.add("c", "c", 30);

    assertEquals(10, confusionMatrix.getCount("a", "a"));
    assertEquals(1, confusionMatrix.getCount("a", "b"));
    assertEquals(2, confusionMatrix.getCount("a", "c"));
    assertEquals(0, confusionMatrix.getCount("b", "a"));
    assertEquals(20, confusionMatrix.getCount("b", "b"));
    assertEquals(0, confusionMatrix.getCount("b", "c"));
    assertEquals(2, confusionMatrix.getCount("c", "a"));
    assertEquals(1, confusionMatrix.getCount("c", "b"));
    assertEquals(30, confusionMatrix.getCount("c", "c"));

    // Check totals
    assertEquals(12, confusionMatrix.getPredictedTotal("a"));
    assertEquals(22, confusionMatrix.getPredictedTotal("b"));
    assertEquals(32, confusionMatrix.getPredictedTotal("c"));
    assertEquals(13, confusionMatrix.getActualTotal("a"));
    assertEquals(20, confusionMatrix.getActualTotal("b"));
    assertEquals(33, confusionMatrix.getActualTotal("c"));
  }

  @Test
  public void enumTest() {
    ConfusionMatrix<TestClassType> confusionMatrix = new ConfusionMatrix<TestClassType>();
    confusionMatrix.add(TestClassType.TAG1, TestClassType.TAG1);
    confusionMatrix.add(TestClassType.TAG1, TestClassType.TAG1);
    confusionMatrix.add(TestClassType.TAG2, TestClassType.TAG2);
    confusionMatrix.add(TestClassType.TAG2, TestClassType.TAG2);
    assertEquals(2, confusionMatrix.getCount(TestClassType.TAG1, TestClassType.TAG1));
    assertEquals(0, confusionMatrix.getCount(TestClassType.TAG1, TestClassType.TAG2));
    assertEquals(0, confusionMatrix.getCount(TestClassType.TAG2, TestClassType.TAG1));
    assertEquals(2, confusionMatrix.getCount(TestClassType.TAG2, TestClassType.TAG2));
  }

  @Test
  public void copyTest() {
    ConfusionMatrix<String> confusionMatrix = new ConfusionMatrix<String>();
    confusionMatrix.add("a", "a");
    confusionMatrix.add("b", "b");

    ConfusionMatrix<String> confusionMatrix2 = new ConfusionMatrix<String>(confusionMatrix);
    for (String actual : confusionMatrix2.getClasses()) {
      for (String predicted : confusionMatrix2.getClasses()) {
        assertEquals(
            confusionMatrix.getCount(actual, predicted),
            confusionMatrix2.getCount(actual, predicted));
      }
    }
  }

}
