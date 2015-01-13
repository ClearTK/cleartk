package org.cleartk.ml.tksvmlight.kernel;


import java.util.Iterator;
import java.util.Map;

import org.cleartk.ml.tksvmlight.TreeFeature;
import org.cleartk.ml.tksvmlight.TreeFeatureVector;

public class ArrayTreeKernel implements TreeKernel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  Map<String,ComposableTreeKernel> kernels = null;
  
  public ArrayTreeKernel(Map<String,ComposableTreeKernel> kernels){
    this.kernels = kernels;
  }
  
  @Override
  public double evaluate(TreeFeatureVector fv1, TreeFeatureVector fv2) {
    double score = 0.0;
    Iterator<TreeFeature> trees1 = fv1.getTrees().values().iterator();
    Iterator<TreeFeature> trees2 = fv2.getTrees().values().iterator();
    
    while(trees1.hasNext() && trees2.hasNext()){
      TreeFeature tf1 = trees1.next();
      TreeFeature tf2 = trees2.next();
      assert tf1.getName().equals(tf2.getName());
      score += this.kernels.get(tf1.getName()).evaluate(tf1, tf2);
    }
    
    return score;
  }

  public ComposableTreeKernel getKernel(String key){
    return kernels.get(key);
  }
}
