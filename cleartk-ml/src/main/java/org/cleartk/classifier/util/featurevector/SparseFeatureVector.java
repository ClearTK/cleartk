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
package org.cleartk.classifier.util.featurevector;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * 
 * @author Philipp Wetzler
 * 
 */
public class SparseFeatureVector extends FeatureVector {

  SortedMap<Integer, Double> values;

  public SparseFeatureVector() {
    values = new TreeMap<Integer, Double>();
  }

  public SparseFeatureVector(FeatureVector fv) throws InvalidFeatureVectorValueException {
    this();
    for (FeatureVector.Entry entry : fv) {
      this.set(entry.index, entry.value);
    }
  }

  public double get(int index) {
    Double returnValue = this.values.get(index);

    if (returnValue != null)
      return returnValue;
    else
      return 0.0;
  }

  public java.util.Iterator<Entry> iterator() {
    return new Iterator(this.values);
  }

  public void set(int index, double value) throws InvalidFeatureVectorValueException {
    if (Double.isInfinite(value) || Double.isNaN(value))
      throw new InvalidFeatureVectorValueException(index, value);

    if (value != 0.0)
      this.values.put(index, value);
    else {
      if (this.values.containsKey(index))
        this.values.remove(index);
    }
  }

  class Iterator implements java.util.Iterator<FeatureVector.Entry> {

    java.util.Iterator<Map.Entry<Integer, Double>> subIterator;

    SortedMap<Integer, Double> itValues;

    Iterator(SortedMap<Integer, Double> values) {
      this.itValues = values;
      this.subIterator = values.entrySet().iterator();
    }

    public boolean hasNext() {
      return this.subIterator.hasNext();
    }

    public FeatureVector.Entry next() {
      Map.Entry<Integer, Double> nextEntry = this.subIterator.next();

      int key = nextEntry.getKey();
      double value = nextEntry.getValue();

      return new FeatureVector.Entry(key, value);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  public double innerProduct(FeatureVector other) {
    double result = 0.0;

    for (FeatureVector.Entry entry : this) {
      result += entry.value * other.get(entry.index);
    }

    return result;
  }
}
