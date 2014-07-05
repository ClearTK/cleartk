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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.cleartk.ml.tksvmlight.TreeFeatureVector;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2007-2013, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * This class implements the partial tree kernel as described in Moschitti's 2004
 * paper "A study on Convolution Kernels for Shallow Semantic Parsing," ACL.
 * It is not as heavily optimized as the official implementation so it will not
 * be suitable for large problems but it is correct relative to that version. 
 * 
 * @author Tim Miller
 */

public class PartialTreeKernel implements TreeKernel {
  public static final int MAX_CHILDREN = 5;
  public static final double LAMBDA_DEFAULT = 0.4;
  
  private double lambda = LAMBDA_DEFAULT;
  private double lambdaSquared = lambda * lambda;
  private double[] lambdaPowers = new double[10];
  
  public static final double MU_DEFAULT = 0.4;
  
  private double mu = MU_DEFAULT;
  
  private boolean normalize = false;
  
  private boolean useCache = true;
//  private int cacheHits = 0;
  HashMap<SimpleDepTree,HashMap<SimpleDepTree,Double>> cache = new HashMap<SimpleDepTree, HashMap<SimpleDepTree,Double>>();
  
  private ForestSumMethod sumMethod = ForestSumMethod.SEQUENTIAL;

  private ConcurrentHashMap<String, Double> normalizers = new ConcurrentHashMap<String, Double>();

  HashMap<String, SimpleDepTree> depTrees = null;

  public PartialTreeKernel(      
      double lambda,
      double mu,
      ForestSumMethod sumMethod,
      boolean normalize) {
    this.lambda = lambda;
    this.lambdaSquared = lambda * lambda;
    this.mu = mu;
    this.sumMethod = sumMethod;
    this.normalize = normalize;
    depTrees = new HashMap<String, SimpleDepTree>();
    initExponents();
  }
  
  private final void initExponents(){
    for(int i = 0; i < lambdaPowers.length; i++){
      lambdaPowers[i] = Math.pow(this.lambda, i);
    }
  }

  public double evaluate(TreeFeatureVector fv1, TreeFeatureVector fv2) {
    double sim = 0.0;
    if (sumMethod == ForestSumMethod.SEQUENTIAL) {
      List<String> fv1Trees = new ArrayList<String>(fv1.getTrees().values());
      List<String> fv2Trees = new ArrayList<String>(fv2.getTrees().values());
      for (int i = 0; i < fv1Trees.size(); i++) {
        String tree1Str = fv1Trees.get(i);
        String tree2Str = fv2Trees.get(i);
        sim += ptk(tree1Str, tree2Str);
      }
    } else {
      throw new NotImplementedException("The only summation method implemented is Sequential!");
    }
    return sim;
  }

  private double ptk(String tree1Str, String tree2Str){
    SimpleDepTree node1 = null;
    if (!depTrees.containsKey(tree1Str)) {
      node1 = SimpleDepTree.fromString(tree1Str);
      depTrees.put(tree1Str, node1);
    } else
      node1 = depTrees.get(tree1Str);

    SimpleDepTree node2 = null;
    if (!depTrees.containsKey(tree2Str)) {
      node2 = SimpleDepTree.fromString(tree2Str);
      depTrees.put(tree2Str, node2);
    } else
      node2 = depTrees.get(tree2Str);
    
    double norm1 = 0.0;
    double norm2 = 0.0;
    if (normalize) {
      if (!normalizers.containsKey(tree1Str)) {
        double norm = ptkSim(node1, node1);
        normalizers.put(tree1Str, norm);
      }
      if (!normalizers.containsKey(tree2Str)) {
        double norm = ptkSim(node2, node2);
        normalizers.put(tree2Str, norm);
      }

      norm1 = normalizers.get(tree1Str);
      norm2 = normalizers.get(tree2Str);
      return (ptkSim(node1, node2) / Math.sqrt(norm1 * norm2));
    } else {
      return ptkSim(node1, node2);
    }
  }
  
  private double ptkSim(SimpleDepTree t1, SimpleDepTree t2){
    double sim = 0.0;
    List<SimpleDepTree> t1Nodes = getDepNodeList(t1);
    List<SimpleDepTree> t2Nodes = getDepNodeList(t2);
    
    for(int i = 0; i < t1Nodes.size(); i++){
      SimpleDepTree t1Node = t1Nodes.get(i);
      for(int j = 0; j < t2Nodes.size(); j++){
        SimpleDepTree t2Node = t2Nodes.get(j);
        double nodeSim = 0.0;
        if(t1Node.cat.equals(t2Node.cat)){
          if(t1Node.isLeaf()){  // && t2Node.isLeaf() is unnecessary since we already know they are the same category
            if(t1Node.cat.equals(t2Node.cat)){
              nodeSim += mu * lambdaSquared;
            }
          }else{
            nodeSim = ptkDelta(t1Node, t2Node);            
          }
        }
        sim += nodeSim;
      }
    }
    return sim;
  }
  
  private double ptkDelta(SimpleDepTree node1, SimpleDepTree node2){
    double delta = 0.0;// lambdaSquared;
    
    if(!node1.cat.equals(node2.cat)) return 0.0;
    
    if(useCache && cache.containsKey(node1) && cache.get(node1).containsKey(node2)){
//      cacheHits++;
      return cache.get(node1).get(node2);
    }
    
    int l1 = node1.children.size();
    int l2 = node2.children.size();

    delta = 1.0;
    for(int p = 1; p <= Math.min(Math.min(l1,  l2), MAX_CHILDREN); p++){

      double contrP = ptkDeltaP(node1.children, node2.children, p);
      delta += contrP;
    }
    
    double score = mu * lambdaSquared * delta;
    
    if(useCache){
      if(!cache.containsKey(node1)){
        cache.put(node1, new HashMap<SimpleDepTree,Double>());
      }
      cache.get(node1).put(node2, score);
    }
    
    return score;
  }

  private double ptkDeltaP(List<SimpleDepTree> c1, List<SimpleDepTree> c2, int p){
    return ptkDeltaP(c1, c2, c1.size()-1, c2.size()-1, p);
  }
  
  private double ptkDeltaP(List<SimpleDepTree> c1, List<SimpleDepTree> c2, int s1len, int s2len, int p){
    double delta = ptkDelta(c1.get(s1len), c2.get(s2len));
    int exp;
    double lambdaPow;
    
    for(int i = 0; i < s1len; i++){
      for(int r = 0; r < s2len; r++){
        // FYI -- sublist second argument is exclusive -- so sublist (i, i) gets you 0 elements,
        // sublist(i, i+1) gets you 1 element.
        exp = s1len-i+s2len-r;
        if(exp < lambdaPowers.length-1){
          lambdaPow = lambdaPowers[exp];
        }else{
          lambdaPow = Math.pow(lambda, exp);
        }
        delta +=  lambdaPow * //ptkDeltaP(c1.subList(0, i+1), c2.subList(0, r+1), p-1);
                ptkDeltaP(c1, c2, i, r, p-1);
      }
    }
    return delta;
  }

  private static final List<SimpleDepTree> getDepNodeList(SimpleDepTree tree){
    ArrayList<SimpleDepTree> list = Lists.newArrayList();
    list.add(tree);
    for(int i = 0; i < list.size(); i++){
      list.addAll(list.get(i).children);
    }
    return list;
  }
  
  public void setUseCache(boolean use){
    this.useCache = use;
  }
  
  public boolean getUseCache(){
    return this.useCache;
  }
}

class SimpleDepTree {
  public String cat;
  public ArrayList<SimpleDepTree> children;
  public SimpleDepTree parent = null;
  static Pattern ptPatt = Pattern.compile("\\(([^ (]+) +([^ )]+)\\)");
  static Pattern orphanPatt = Pattern.compile("\\(([^ (]+) \\)");
  
  public SimpleDepTree(String s){
    this(s, null);
  }
  
  public SimpleDepTree(String s, SimpleDepTree p){
    cat = escapeCat(s);
    children = new ArrayList<SimpleDepTree>();
    parent = p;
  }
  
  public static String escapeCat(String c) {
    c = c.replaceAll("\\(", "LPAREN");
    c = c.replaceAll("\\)", "RPAREN");
    return c;
  }

  public void addChild(SimpleDepTree t){
    children.add(t);
  }
  
  private static String[] splitChildren(String s){
    ArrayList<String> children = new ArrayList<String>();
    char[] chars = s.toCharArray();
    int numParens = 0;
    int startIndex = 0;
    for(int i = 0; i < chars.length; i++){
      if(chars[i] == '('){
        numParens++;
        if(numParens == 1){
          startIndex = i;
        }
      }else if(chars[i] == ')'){
        numParens--;
        if(numParens == 0){
          children.add(s.substring(startIndex, i+1));
        }else if(numParens < 0){
          break;
        }
      }
    }
    return children.toArray(new String[]{});
  }

  static SimpleDepTree fromString(String string){
    SimpleDepTree tree = null;
    
    // pre-terminal case is the base case:
    Matcher pretermMatcher = ptPatt.matcher(string);
    Matcher orphanMatcher = orphanPatt.matcher(string);
    
    if(pretermMatcher.matches()){
      tree = new SimpleDepTree(pretermMatcher.group(1));
      SimpleDepTree leaf = new SimpleDepTree(pretermMatcher.group(2));
      tree.addChild(leaf);
      leaf.parent = tree;
    }else if(orphanMatcher.matches()){
      tree = new SimpleDepTree(orphanMatcher.group(1));
    }else{
      int firstWS = string.indexOf(' ');
      tree = new SimpleDepTree(string.substring(1, firstWS));
      String[] childStrings = splitChildren(string.substring(firstWS+1, string.length()-1));
      for(int i = 0; i < childStrings.length; i++){
        SimpleDepTree child = fromString(childStrings[i]);
        child.parent = tree;
        tree.addChild(child);
      }
    }
    return tree;
  }
  
  public final boolean isLeaf(){
    return children.size() == 0;
  }
}
