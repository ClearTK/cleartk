package org.cleartk.ml.libsvm.tk;

import org.chboston.cnlp.kernel.CustomKernel;
import org.chboston.cnlp.libsvm.svm_node;
import org.cleartk.classifier.svmlight.model.Kernel;
import org.cleartk.classifier.tksvmlight.TreeFeatureVector;
import org.cleartk.classifier.tksvmlight.model.CompositeKernel;
import org.cleartk.classifier.tksvmlight.model.TreeKernel;

public class CustomCompositeKernel extends CompositeKernel implements CustomKernel<TreeFeatureVector> {

  public CustomCompositeKernel(Kernel fk, TreeKernel tk, ComboOperator op,
      double tkWeight, Normalize normalize) {
    super(fk, tk, op, tkWeight, normalize);
  }

  @Override
  public double evaluate(svm_node<TreeFeatureVector> x,
      svm_node<TreeFeatureVector> y) {
    return super.evaluate(x.data, y.data);
  }

}
