/**
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
package org.cleartk.ml.tksvmlight.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.cleartk.ml.tksvmlight.TreeFeature;
import org.cleartk.ml.tksvmlight.model.IdentityLexicalSimilarity;
import org.cleartk.ml.tksvmlight.model.LexicalFunctionModel;
import org.cleartk.util.treebank.TopTreebankNode;
import org.cleartk.util.treebank.TreebankFormatParser;
import org.cleartk.util.treebank.TreebankNode;

/**
 * <br>
 * Copyright (c) 2007-2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * This class implements the descending path kernel defined by Lin et. al.
 * in "Descending-Path Convolution Kernel for Syntactic Structures",
 * published at ACL 2014.
 * 
 * @author Chen Lin
 */

public class DescendingPathKernel extends TreeKernel_ImplBase {
  /**
   * 
   */
  private static final long serialVersionUID = 4500173927314841765L;
  public static final double LAMBDA_DEFAULT = 0.4;
  private double lambda = LAMBDA_DEFAULT;

  private boolean normalize = false;
  
  private ConcurrentHashMap<String, Double> normalizers = new ConcurrentHashMap<String, Double>();

  private LexicalFunctionModel lex;
  
  HashMap<String, TopTreebankNode> trees = null;

  public DescendingPathKernel(
      double lambda,
      boolean normalize,
      LexicalFunctionModel lex) {
    this.lambda = lambda;
    this.normalize = normalize;
    this.lex = lex;
    trees = new HashMap<String, TopTreebankNode>();
  }
  
  public DescendingPathKernel(
      double lambda,
      boolean normalize) {
    this(lambda, normalize, new IdentityLexicalSimilarity());
  }

  @Override
  public double evaluate(TreeFeature tf1, TreeFeature tf2) {
    return dpk(tf1.getValue().toString(), tf2.getValue().toString());
  }
  
  private double dpk(String tree1Str, String tree2Str) {

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
        sim += numCommonNodes(n1, n2);
      }
    }
    return sim;
  }

  private double numCommonNodes(TreebankNode n1, TreebankNode n2){
    if (!n1.getType().equals(n2.getType()) )
      return 0;
    else { //if the two nodes have the same label
      double retval = 1.0;
      if (n1.isLeaf() && n2.isLeaf()){ //if both n1 and n2 are pre-terminals
        retval += lambda * lex.getLexicalSimilarity(n1.getValue(), n2.getValue());
      }else if( !n1.isLeaf() && !n2.isLeaf()){ //if both n1 and n2 are not pre-terminals, find their common children
        List<TreebankNode[]> matchingNodes = new ArrayList<TreebankNode[]>();
        for ( TreebankNode child1 : n1.getChildren()){
          for ( TreebankNode child2 : n2.getChildren() ){
            if( child1.getType().equals(child2.getType()) ){
              TreebankNode[] nodePair = {child1,child2};
              matchingNodes.add(nodePair);
            }
          }
        }
        for ( TreebankNode[] nodePair : matchingNodes){
          retval += lambda*numCommonNodes(nodePair[0], nodePair[1]);
        }
      }
      return retval;
    }
  }

}
