/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.syntax.constituent.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TreebankNodeUtil {

  /**
   * Selects a single TreebankNode leaf that has the same span as the given annotation.
   * 
   * @param jCas
   *          The JCas containing the TreebankNodes.
   * @param annotation
   *          The Annotation whose span should match a TreebankNode leaf.
   * @return The single TreebankNode leaf that matches the annotation, or null if no such annotation
   *         exists.
   */
  public static TreebankNode selectMatchingLeaf(JCas jCas, Annotation annotation) {
    TreebankNode leaf = null;
    for (TreebankNode node : JCasUtil.selectCovered(jCas, TreebankNode.class, annotation)) {
      if (node.getLeaf() && node.getBegin() == annotation.getBegin()
          && node.getEnd() == annotation.getEnd()) {
        if (leaf == null) {
          leaf = node;
        } else {
          throw new IllegalArgumentException(String.format(
              "expected one leaf matching annotation %s, found %s",
              annotation,
              Arrays.asList(leaf, node)));
        }
      }
    }
    return leaf;
  }

  /**
   * Selects the highest TreebankNode in the parse tree that has the same span as the given
   * annotation.
   * 
   * @param jCas
   *          The JCas containing the TreebankNodes.
   * @param annotation
   *          The Annotation whose span should be matched.
   * @return The highest TreebankNode matching the given span, or null if no such annotation exists.
   */
  public static TreebankNode selectHighestCoveredTreebankNode(JCas jCas, Annotation annotation) {
    TreebankNode highestNode = null;
    int smallestDepth = Integer.MAX_VALUE;
    for (TreebankNode node : JCasUtil.selectCovered(jCas, TreebankNode.class, annotation)) {
      if (node.getBegin() == annotation.getBegin() && node.getEnd() == annotation.getEnd()) {
        int depth = getDepth(node);
        if (depth < smallestDepth) {
          highestNode = node;
          smallestDepth = depth;
        }
      }
    }
    return highestNode;
  }

  /**
   * Calculates the depth of the TreebankNode. The root node has depth 0, children of the root node
   * have depth 1, etc.
   * 
   * @param node
   *          The TreebankNode whose depth is to be calculated.
   * @return The depth of the TreebankNode.
   */
  public static int getDepth(TreebankNode node) {
    int depth = -1;
    while (node != null) {
      depth += 1;
      node = node.getParent();
    }
    return depth;
  }

  /**
   * Find the path from a TreebankNode to the root of the tree it belongs to.
   * 
   * @param startNode
   *          The start node of the path
   * 
   * @return A list of TreebankNodes that make up the path from <b>startNode</b> to the root of the
   *         tree
   */
  public static List<TreebankNode> getPathToRoot(TreebankNode startNode) {
    List<TreebankNode> nlist = new ArrayList<TreebankNode>(20);
    TreebankNode cursorNode = startNode;
  
    while (cursorNode != null) {
      nlist.add(cursorNode);
      cursorNode = cursorNode.getParent();
    }
  
    return nlist;
  }

}
