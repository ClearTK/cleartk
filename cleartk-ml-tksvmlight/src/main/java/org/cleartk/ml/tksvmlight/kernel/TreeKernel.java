package org.cleartk.ml.tksvmlight.kernel;

import org.cleartk.ml.tksvmlight.TreeFeatureVector;

public interface TreeKernel {
  public static enum ForestSumMethod {
    SEQUENTIAL, ALL_PAIRS
  }

  public double evaluate(TreeFeatureVector fv1, TreeFeatureVector fv2);
}
