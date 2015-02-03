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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.cleartk.ml.tksvmlight.TreeFeature;
import org.cleartk.ml.tksvmlight.model.LexicalFunctionModel;
import org.cleartk.util.treebank.TopTreebankNode;
import org.cleartk.util.treebank.TreebankFormatParser;
import org.cleartk.util.treebank.TreebankNode;

/**
 * <br>
 * Copyright (c) 2007-2015, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Tim Miller
 */

public class SyntacticSemanticTreeKernel extends TreeKernel_ImplBase {

  /**
   * 
   */
  private static final long serialVersionUID = -4333998714675622519L;
  
  
  public static final double LAMBDA_DEFAULT = 0.4;
  private double lambda = LAMBDA_DEFAULT;

  private boolean normalize = false;
  
  private ConcurrentHashMap<String, Double> normalizers = new ConcurrentHashMap<String, Double>();

  HashMap<String, TopTreebankNode> trees = null;

  private LexicalFunctionModel lexModel = null;
  
  public SyntacticSemanticTreeKernel(LexicalFunctionModel lex,
      double lambda,
      boolean normalize) {
    this.lexModel = lex;
    this.lambda = lambda;
    this.normalize = normalize;
    trees = new HashMap<String, TopTreebankNode>();
  }

  public double evaluate(TreeFeature tf1, TreeFeature tf2) {
    return sstk(tf1.getValue().toString(), tf2.getValue().toString());
  }
  
  private double sstk(String tree1Str, String tree2Str){
    TopTreebankNode node1 = null;
    if (!trees.containsKey(tree1Str)) {
      node1 = TreebankFormatParser.parse(tree1Str);
      trees.put(tree1Str, node1);
    } else
      node1 = trees.get(tree1Str);

    TopTreebankNode node2 = null;
    if (!trees.containsKey(tree2Str)) {
      node2 = TreebankFormatParser.parse(tree2Str);
      trees.put(tree2Str, node2);
    } else
      node2 = trees.get(tree2Str);

    double norm1 = 0.0;
    double norm2 = 0.0;
    if (normalize) {
      if (!normalizers.containsKey(tree1Str)) {
        double norm = sim(node1, node1);
        normalizers.put(tree1Str, norm);
      }
      if (!normalizers.containsKey(tree2Str)) {
        double norm = sim(node2, node2);
        normalizers.put(tree2Str, norm);
      }

      norm1 = normalizers.get(tree1Str);
      norm2 = normalizers.get(tree2Str);
    }
    if (normalize) {
      return (sim(node1, node2) / Math.sqrt(norm1 * norm2));
    } else {
      return sim(node1, node2);
    }
  }

  private double sim(TreebankNode node1, TreebankNode node2) {
    double sim = 0.0;
    List<TreebankNode> N1 = TreeKernelUtils.getNodeList(node1);
    List<TreebankNode> N2 = TreeKernelUtils.getNodeList(node2);
    for (TreebankNode n1 : N1) {
      for (TreebankNode n2 : N2) {
        sim += numCommonSubtrees(n1, n2);
      }
    }
    return sim;
  }

  private double numCommonSubtrees(TreebankNode n1, TreebankNode n2) {
    double retVal = 1.0;
    List<TreebankNode> children1 = n1.getChildren();
    List<TreebankNode> children2 = n2.getChildren();
    int c1size = children1.size();
    int c2size = children2.size();
    if (c1size != c2size) {
      retVal = 0;
    } else if (!n1.getType().equals(n2.getType())) {
      retVal = 0;
    } else if (n1.isLeaf() && n2.isLeaf()) {
      // both are preterminals, and we know they have the same type, need to check value (word)
      // Collins & Duffy tech report says lambdaSquared, but Nips 02 paper uses lambda
      // Moschitti's papers also use lambda
      // retVal = lambdaSquared;
      retVal = lambda * lexModel.getLexicalSimilarity(n1.getValue(), n2.getValue());
    } else {
      // At this point they have the same label and same # children. Check if children the same.
      boolean sameProd = true;
      for (int i = 0; i < c1size; i++) {
        String l1 = children1.get(i).getType();
        String l2 = children2.get(i).getType();
        if (!l1.equals(l2)) {
          sameProd = false;
          break;
        }
      }
      if (sameProd == true) {
        for (int i = 0; i < c1size; i++) {
          retVal *= (1 + numCommonSubtrees(children1.get(i), children2.get(i)));
        }
        // again, some disagreement in the literature, with Collins and Duffy saying
        // lambdaSquared here in tech report and lambda here in nips 02. We'll stick with
        // lambda b/c that's what moschitti's code (which was presumably used for model-building)
        // uses.
        retVal = lambda * retVal;
      } else {
        retVal = 0;
      }
    }
    return retVal;
  }

}
