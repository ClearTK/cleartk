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
package org.cleartk.classifier.feature.transform;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * <br>
 * Copyright (c) 2011-2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Data structure for looking up (presumably) a feature's mean and standard deviation
 * 
 * @author Lee Becker
 */
public class MeanStddevMap<KEY_T> {
  public static class MeanStddevTuple {

    public MeanStddevTuple(double mean, double stddev) {
      this.mean = mean;
      this.stddev = stddev;
    }

    public double mean;

    public double stddev;
  }

  private Map<KEY_T, MeanStddevTuple> table;

  public MeanStddevMap() {
    this.table = new HashMap<KEY_T, MeanStddevTuple>();

  }

  public double getMean(KEY_T key) {
    return this.table.get(key).mean;
  }

  public double getStddev(KEY_T key) {
    return this.table.get(key).stddev;
  }

  public MeanStddevTuple getValues(KEY_T key) {
    return this.table.get(key);
  }

  public void setValues(KEY_T key, double mean, double stddev) {
    this.table.put(key, new MeanStddevTuple(mean, stddev));
  }

}
