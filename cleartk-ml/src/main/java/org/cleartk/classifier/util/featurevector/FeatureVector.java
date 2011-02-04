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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * 
 */
public abstract class FeatureVector implements Iterable<FeatureVector.Entry> {

  public void add(FeatureVector other) throws InvalidFeatureVectorValueException {
    for (FeatureVector.Entry entry : other) {
      this.set(entry.index, this.get(entry.index) + entry.value);
    }
  }

  public void multiply(double factor) throws InvalidFeatureVectorValueException {
    for (FeatureVector.Entry entry : this) {
      this.set(entry.index, this.get(entry.index) * factor);
    }
  }

  public double l2Norm() {
    double l = 0.0;

    for (FeatureVector.Entry entry : this) {
      l += entry.value * entry.value;
    }

    return Math.sqrt(l);
  }

  @Override
  public boolean equals(Object o) {
    FeatureVector other;
    try {
      other = (FeatureVector) o;
    } catch (ClassCastException e) {
      return false;
    }

    Iterator<Entry> thisIt = this.iterator();
    Iterator<Entry> otherIt = other.iterator();
    while (thisIt.hasNext() || otherIt.hasNext()) {
      Entry thisEntry;
      Entry otherEntry;
      try {
        thisEntry = thisIt.next();
        otherEntry = otherIt.next();
      } catch (NoSuchElementException e) {
        return false;
      }

      if (!thisEntry.equals(otherEntry))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = 1451;
    for (Entry e : this) {
      result = 757 * result + e.hashCode();
    }
    return result;
  }

  /**
   * Set the feature at index to value.
   * 
   * @param index
   * @param value
   */
  public abstract void set(int index, double value) throws InvalidFeatureVectorValueException;

  /**
   * Return the value at index.
   * 
   * @param index
   * @return value at index
   */
  public abstract double get(int index);

  /**
   * Gives an iterator over the non-zero features.
   */
  public abstract Iterator<Entry> iterator();

  public double innerProduct(FeatureVector other) {
    double result = 0.0;

    for (FeatureVector.Entry entry : other) {
      result += this.get(entry.index) * entry.value;
    }

    return result;
  }

  public static class Entry {
    public Entry(int index, double value) {
      this.index = index;
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      Entry other;
      try {
        other = (Entry) o;
      } catch (ClassCastException e) {
        return false;
      }

      if (this.index != other.index)
        return false;

      if (this.value != other.value)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = 83;
      result = 47 * result + this.index;
      result = 47 * result + new Double(this.value).hashCode();
      return result;
    }

    public final int index;

    public final double value;
  }

}
