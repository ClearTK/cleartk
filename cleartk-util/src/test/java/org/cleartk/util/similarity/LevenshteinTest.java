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

package org.cleartk.util.similarity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */
public class LevenshteinTest {

  private void test(
      String string1,
      String string2,
      int expectedEditDistance,
      float expectedSimilarity) {
    int actualEditDistance = Levenshtein.editDistance(string1, string2);
    assertEquals("levenshtein edit distance", expectedEditDistance, actualEditDistance);
    float actualSimilarity = Levenshtein.similarity(string1, string2);
    assertEquals(
        "levenshtein edit distance similarity",
        expectedSimilarity,
        actualSimilarity,
        0.001f);
  }

  @Test
  public void test() {
    test("", "", 0, 1.0f);
    test("aaaa", "baaa", 1, 0.875f);
    test("aaaa", "aa", 2, 0.66667f);
    test("aaaa", "", 4, 0.0f);
    test("", "aaaa", 4, 0.0f);
    test("a", "aaaa", 3, 0.39999998f);
    test("baab", "aaaa", 2, 0.75f);
    test("baabbaab", "abbaabba", 4, 0.75f);
    test("aaabaaa", "aabaa", 2, 0.833333f);
    test("vintner", "writers", 5, 0.64285713f);
    test("D10Mit106", "D10Mit186", 1, 0.9444444f);
  }

}
