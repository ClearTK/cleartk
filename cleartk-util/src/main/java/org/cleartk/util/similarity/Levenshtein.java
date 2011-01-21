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

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 *         <p>
 * 
 *         Portions of this code were derived from pseudocode located at
 *         http://en.wikipedia.org/wiki/Levenshtein_distance
 * 
 * 
 */

public class Levenshtein {

  public static float similarity(String string1, String string2) {
    if (string1.length() == 0 && string2.length() == 0) {
      return 1.0f;
    }
    int editDistance = editDistance(string1, string2);

    float similarity = (float) editDistance / (string1.length() + string2.length());
    return 1 - Math.min(similarity, 1.0f);
  }

  public static int editDistance(String string1, String string2) {
    int rowCount = string1.length() + 1;
    int columnCount = string2.length() + 1;

    int[][] matrix = new int[rowCount][columnCount];

    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      matrix[rowIndex][0] = rowIndex;
    }

    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      matrix[0][columnIndex] = columnIndex;
    }

    for (int rowIndex = 1; rowIndex < matrix.length; rowIndex++) {
      for (int columnIndex = 1; columnIndex < matrix[0].length; columnIndex++) {
        char char1 = string1.charAt(rowIndex - 1);
        char char2 = string2.charAt(columnIndex - 1);
        if (char1 == char2) {
          matrix[rowIndex][columnIndex] = matrix[rowIndex - 1][columnIndex - 1];
        } else {
          int left = matrix[rowIndex][columnIndex - 1];
          int up = matrix[rowIndex - 1][columnIndex];
          int leftUp = matrix[rowIndex - 1][columnIndex - 1];
          int distance = Math.min(left, up);
          distance = Math.min(distance, leftUp);
          matrix[rowIndex][columnIndex] = distance + 1;
        }
      }
    }

    return matrix[rowCount - 1][columnCount - 1];
  }

}
