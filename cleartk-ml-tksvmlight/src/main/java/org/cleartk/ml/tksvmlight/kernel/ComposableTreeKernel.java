package org.cleartk.ml.tksvmlight.kernel;

import org.cleartk.ml.tksvmlight.TreeFeature;

public interface ComposableTreeKernel extends TreeKernel {
  public double evaluate(TreeFeature tf1, TreeFeature tf2);
}
