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

/**
 * This data structure is a simplified version of FCollections. Here you simply count true
 * positives, false positives, and false negatives without stratifying them across any
 * dimension/label/category.
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class FCollection {

  protected int truePositives = 0;

  protected int falsePositives = 0;

  protected int falseNegatives = 0;

  public void addTruePositive() {
    truePositives++;
  }

  public void addTruePositives(int countIncrement) {
    truePositives += countIncrement;
  }

  public int getTruePositivesCount() {
    return truePositives;
  }

  public void addFalsePositive() {
    falsePositives++;
  }

  public void addFalsePositives(int countIncrement) {
    falsePositives += countIncrement;
  }

  public int getFalsePositivesCount() {
    return falsePositives;
  }

  public void addFalseNegative() {
    falseNegatives++;
  }

  public void addFalseNegatives(int countIncrement) {
    falseNegatives += countIncrement;
  }

  public int getFalseNegativesCount() {
    return falseNegatives;
  }

  public double getPrecision() {
    return FCollections.precision(truePositives, falsePositives);
  }

  public double getRecall() {
    return FCollections.recall(truePositives, falseNegatives);
  }

  public double getF() {
    return FCollections.F(truePositives, falsePositives, falseNegatives);
  }

  public double getF(double B) {
    return FCollections.F(truePositives, falsePositives, falseNegatives, B);
  }

}
