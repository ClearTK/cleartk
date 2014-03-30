/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.ml.util.featurevector;

import java.util.ArrayList;
import java.util.List;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * 
 */

public class ArrayFeatureVector extends FeatureVector {

  ArrayList<Double> features;

  public ArrayFeatureVector() {
    features = new ArrayList<Double>();
  }

  public double get(int index) {
    if (index < features.size())
      return features.get(index);
    else
      return 0.0;
  }

  public java.util.Iterator<Entry> iterator() {
    return new Iterator(features);
  }

  public void set(int index, double value) throws InvalidFeatureVectorValueException {
    if (Double.isInfinite(value) || Double.isNaN(value))
      throw new InvalidFeatureVectorValueException(index, value);

    for (int i = features.size(); i <= index; i++)
      features.add(0.0);

    features.set(index, value);
  }

  public double[] toArray() {
    double[] result = new double[features.size()];
    int index = 0;

    for (Double d : features) {
      result[index] = d;
      index += 1;
    }

    return result;
  }

  static class Iterator implements java.util.Iterator<FeatureVector.Entry> {

    int currentIndex;

    java.util.ListIterator<Double> subIterator;

    public Iterator(List<Double> features) {
      currentIndex = 0;
      subIterator = features.listIterator();
      moveToNext();
    }

    public boolean hasNext() {
      return subIterator.hasNext();
    }

    public Entry next() {
      int index = subIterator.nextIndex();
      Double value = subIterator.next();
      moveToNext();

      return new FeatureVector.Entry(index, value.doubleValue());
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    private void moveToNext() {
      while (subIterator.hasNext()) {
        Double d = subIterator.next();
        if (d != null && d.doubleValue() != 0.0) {
          subIterator.previous();
          return;
        }
      }
    }

  }

}
