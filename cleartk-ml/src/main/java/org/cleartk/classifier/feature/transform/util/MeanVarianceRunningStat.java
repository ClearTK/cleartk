/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.classifier.feature.transform.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Iterative algorithm for computing mean and variance For more information on the algorithm refer
 * to the Knuth's The Art of Computer Programming vol 2, 3rd edition, page 232
 * <P>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Lee Becker
 * 
 */
public class MeanVarianceRunningStat implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public MeanVarianceRunningStat() {
    this.clear();
  }

  public void init(int n, double mean, double variance) {
    this.numSamples = n;
    this.meanNew = mean;
    this.varNew = variance;
  }

  public void add(double x) {
    numSamples++;

    if (this.numSamples == 1) {
      meanOld = meanNew = x;
      varOld = 0.0;
    } else {
      meanNew = meanOld + (x - meanOld) / numSamples;
      varNew = varOld + (x - meanOld) * (x - meanNew);

      // set up for next iteration
      meanOld = meanNew;
      varOld = varNew;
    }
  }

  public void clear() {
    this.numSamples = 0;
  }

  public int getNumSamples() {
    return this.numSamples;
  }

  public double mean() {
    return (this.numSamples > 0) ? meanNew : 0.0;
  }

  public double variance() {
    return (this.numSamples > 1) ? varNew / (this.numSamples) : 0.0;
  }

  public double stddev() {
    return Math.sqrt(this.variance());
  }

  public double variancePop() {
    return (this.numSamples > 1) ? varNew / (this.numSamples - 1) : 0.0;
  }

  public double stddevPop() {
    return Math.sqrt(this.variancePop());
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeInt(numSamples);
    out.writeDouble(meanNew);
    out.writeDouble(varNew);

  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    numSamples = in.readInt();
    meanOld = meanNew = in.readDouble();
    varOld = varNew = in.readDouble();
  }

  private int numSamples;

  private double meanOld;

  private double meanNew;

  private double varOld;

  private double varNew;

}