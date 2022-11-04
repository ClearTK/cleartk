/** 
 * Copyright (c) 2007-2015, Regents of the University of Colorado 
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
package org.cleartk.ml.tksvmlight.kernel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.cleartk.ml.tksvmlight.TreeFeature;
import org.cleartk.ml.tksvmlight.TreeFeatureVector;
/**
 * <br>
 * Copyright (c) 2007-2015, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Tim Miller
 */

public abstract class TreeKernel_ImplBase implements ComposableTreeKernel {

  /**
   * 
   */
  private static final long serialVersionUID = 7301165397474109861L;

  protected ForestSumMethod sumMethod = ForestSumMethod.SEQUENTIAL;

  public double evaluate(TreeFeatureVector fv1, TreeFeatureVector fv2) {
    double sim = 0.0;
    if (sumMethod == ForestSumMethod.SEQUENTIAL) {
      List<TreeFeature> fv1Trees = new ArrayList<>(fv1.getTrees().values());
      List<TreeFeature> fv2Trees = new ArrayList<>(fv2.getTrees().values());
      for (int i = 0; i < fv1Trees.size(); i++) {
        TreeFeature tf1 = fv1Trees.get(i);
        TreeFeature tf2 = fv2Trees.get(i);        
        sim += evaluate(tf1, tf2);
      }
    } else {
      throw new NotImplementedException("The only summation method implemented is Sequential!");
    }
    return sim;
  }

  public abstract double evaluate(TreeFeature tf1, TreeFeature tf2);

}
