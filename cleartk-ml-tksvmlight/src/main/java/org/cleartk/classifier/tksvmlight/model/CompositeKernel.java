/*
 * Copyright (c) 2007-2013, Regents of the University of Colorado 
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
package org.cleartk.classifier.tksvmlight.model;

import java.util.HashMap;

import org.cleartk.classifier.svmlight.model.Kernel;
import org.cleartk.classifier.tksvmlight.TreeFeatureVector;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * 
 * 
 * <br>
 * Copyright (c) 2013, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class CompositeKernel {
  private Kernel featureKernel;

  private TreeKernel treeKernel;

  public enum ComboOperator {
    SUM, PRODUCT, TREE_ONLY, VECTOR_ONLY
  }

  private ComboOperator operator = ComboOperator.SUM;

  private double tkWeight = 1.0;

  public enum Normalize {
    NEITHER, TREE, VECTOR, BOTH
  }

  private Normalize normalize;

  private HashMap<FeatureVector, Double> normalizer = new HashMap<FeatureVector, Double>();

  public CompositeKernel(Kernel fk, TreeKernel tk) {
    this(fk, tk, ComboOperator.SUM, 1.0, Normalize.BOTH);
  }

  public CompositeKernel(Kernel fk, TreeKernel tk, ComboOperator op) {
    this(fk, tk, op, 1.0, Normalize.BOTH);
  }

  public CompositeKernel(
      Kernel fk,
      TreeKernel tk,
      ComboOperator op,
      double tkWeight,
      Normalize normalize) {
    featureKernel = fk;
    treeKernel = tk;
    operator = op;
    this.tkWeight = tkWeight;
    this.normalize = normalize;
  }

  public double evaluate(TreeFeatureVector fv1, TreeFeatureVector fv2) {
    double fSim = 0.0;
    FeatureVector vec1 = fv1.getFeatures();
    FeatureVector vec2 = fv2.getFeatures();

    if (operator != ComboOperator.TREE_ONLY) {
      fSim = featureKernel.evaluate(vec1, vec2);
      if (normalize == Normalize.BOTH || normalize == Normalize.VECTOR) {
        double v1sim, v2sim;
        if (!normalizer.containsKey(vec1)) {
          normalizer.put(vec1, featureKernel.evaluate(vec1, vec1));
        }
        v1sim = normalizer.get(vec1);
        if (!normalizer.containsKey(vec2)) {
          normalizer.put(vec2, featureKernel.evaluate(vec2, vec2));
        }
        v2sim = normalizer.get(vec2);
        fSim = fSim / Math.sqrt(v1sim * v2sim);
      }
    }

    double tkSim = 0.0;
    if (operator != ComboOperator.VECTOR_ONLY) {
      tkSim = treeKernel.evaluate(fv1, fv2);
    }

    if (operator == ComboOperator.SUM) {
      return fSim + tkWeight * tkSim;
    } else if (operator == ComboOperator.PRODUCT) {
      return fSim * tkSim;
    } else if (operator == ComboOperator.TREE_ONLY) {
      return tkSim;
    } else {
      return fSim;
    }
  }
}
