/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.syntax.constituent.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.cleartk.syntax.constituent.type.TerminalTreebankNode;
import org.uimafit.util.FSCollectionFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class TreebankNodeUtility {
  public static org.cleartk.syntax.constituent.type.TopTreebankNode convert(
      TopTreebankNode pojoNode,
      JCas jCas,
      boolean addToIndexes) {
    org.cleartk.syntax.constituent.type.TopTreebankNode uimaNode = new org.cleartk.syntax.constituent.type.TopTreebankNode(
        jCas,
        pojoNode.getTextBegin(),
        pojoNode.getTextEnd());
    convert(pojoNode, jCas, uimaNode, null, addToIndexes);
    uimaNode.setTreebankParse(pojoNode.getTreebankParse());
    initTerminalNodes(uimaNode, jCas);
    if (addToIndexes)
      uimaNode.addToIndexes();
    return uimaNode;
  }

  public static void initTerminalNodes(
      org.cleartk.syntax.constituent.type.TopTreebankNode uimaNode,
      JCas jCas) {
    List<TerminalTreebankNode> terminals = new ArrayList<org.cleartk.syntax.constituent.type.TerminalTreebankNode>();
    _initTerminalNodes(uimaNode, terminals);

    for (int i = 0; i < terminals.size(); i++) {
      TerminalTreebankNode terminal = terminals.get(i);
      terminal.setIndex(i);
    }

    FSArray terminalsFSArray = new FSArray(jCas, terminals.size());
    terminalsFSArray.copyFromArray(
        terminals.toArray(new FeatureStructure[terminals.size()]),
        0,
        0,
        terminals.size());
    uimaNode.setTerminals(terminalsFSArray);
  }

  private static void _initTerminalNodes(
      org.cleartk.syntax.constituent.type.TreebankNode node,
      List<TerminalTreebankNode> terminals) {
    FSArray children = node.getChildren();
    for (int i = 0; i < children.size(); i++) {
      org.cleartk.syntax.constituent.type.TreebankNode child = (org.cleartk.syntax.constituent.type.TreebankNode) children.get(i);
      if (child instanceof TerminalTreebankNode) {
        terminals.add((TerminalTreebankNode) child);
      } else
        _initTerminalNodes(child, terminals);
    }
  }

  public static org.cleartk.syntax.constituent.type.TreebankNode convert(
      TreebankNode pojoNode,
      JCas jCas,
      org.cleartk.syntax.constituent.type.TreebankNode uimaNode,
      org.cleartk.syntax.constituent.type.TreebankNode parentNode,
      boolean addToIndexes) {
    uimaNode.setNodeType(pojoNode.getType());
    StringArray nodeTags = (StringArray) (FSCollectionFactory.fillArrayFS(new StringArray(
        jCas,
        pojoNode.getTags().length), pojoNode.getTags()));
    uimaNode.setNodeTags(nodeTags);
    uimaNode.setNodeValue(pojoNode.getValue());
    uimaNode.setLeaf(pojoNode.isLeaf());
    uimaNode.setParent(parentNode);

    List<org.cleartk.syntax.constituent.type.TreebankNode> uimaChildren = new ArrayList<org.cleartk.syntax.constituent.type.TreebankNode>();
    for (TreebankNode child : pojoNode.getChildren()) {
      org.cleartk.syntax.constituent.type.TreebankNode childNode;
      if (child.isLeaf()) {
        childNode = new TerminalTreebankNode(jCas, child.getTextBegin(), child.getTextEnd());
      } else {
        childNode = new org.cleartk.syntax.constituent.type.TreebankNode(
            jCas,
            child.getTextBegin(),
            child.getTextEnd());
      }
      uimaChildren.add(convert(child, jCas, childNode, uimaNode, addToIndexes));
      if (addToIndexes)
        childNode.addToIndexes();
    }
    FSArray uimaChildrenFSArray = new FSArray(jCas, uimaChildren.size());
    uimaChildrenFSArray.copyFromArray(
        uimaChildren.toArray(new FeatureStructure[uimaChildren.size()]),
        0,
        0,
        uimaChildren.size());
    uimaNode.setChildren(uimaChildrenFSArray);
    return uimaNode;
  }

  public static org.cleartk.syntax.constituent.type.TopTreebankNode getTopNode(
      org.cleartk.syntax.constituent.type.TreebankNode node) {
    if (node instanceof org.cleartk.syntax.constituent.type.TopTreebankNode)
      return (org.cleartk.syntax.constituent.type.TopTreebankNode) node;

    org.cleartk.syntax.constituent.type.TreebankNode parent = node.getParent();
    while (parent != null) {
      if (parent instanceof org.cleartk.syntax.constituent.type.TopTreebankNode)
        return (org.cleartk.syntax.constituent.type.TopTreebankNode) parent;
      node = parent;
      parent = node.getParent();
    }
    return null;
  }

  /**
   * A "pretty print" of this node that may be useful for e.g. debugging.
   */
  public static void print(PrintStream out, org.cleartk.syntax.constituent.type.TreebankNode node) {
    out.println(print(node, 0));
  }

  private static String print(org.cleartk.syntax.constituent.type.TreebankNode node, int tabs) {
    StringBuffer returnValue = new StringBuffer();
    String tabString = getTabs(tabs);
    returnValue.append(tabString + node.getNodeType());
    if (node.getNodeValue() != null)
      returnValue.append(":" + node.getNodeValue() + "\n");
    else {
      returnValue.append(":" + node.getCoveredText() + "\n");
    }
    if (node.getChildren().size() > 0) {
      Collection<org.cleartk.syntax.constituent.type.TreebankNode> children = JCasUtil.select(
          node.getChildren(),
          org.cleartk.syntax.constituent.type.TreebankNode.class);
      for (org.cleartk.syntax.constituent.type.TreebankNode child : children) {
        returnValue.append(print(child, (tabs + 1)));
      }
    }
    return returnValue.toString();
  }

  private static String getTabs(int tabs) {
    char[] chars = new char[tabs];
    Arrays.fill(chars, ' ');
    return new String(chars);
  }

  /**
   * Create a leaf TreebankNode in a JCas.
   * 
   * @param jCas
   *          The JCas which the annotation should be added to.
   * @param begin
   *          The begin offset of the node.
   * @param end
   *          The end offset of the node.
   * @param nodeType
   *          The part of speech tag of the node.
   * @return The TreebankNode which was added to the JCas.
   */
  public static org.cleartk.syntax.constituent.type.TreebankNode newNode(
      JCas jCas,
      int begin,
      int end,
      String nodeType) {
    org.cleartk.syntax.constituent.type.TreebankNode node = new org.cleartk.syntax.constituent.type.TreebankNode(
        jCas,
        begin,
        end);
    node.setNodeType(nodeType);
    node.setChildren(new FSArray(jCas, 0));
    node.setLeaf(true);
    node.addToIndexes();
    return node;
  }

  /**
   * Create a branch TreebankNode in a JCas. The offsets of this node will be determined by its
   * children.
   * 
   * @param jCas
   *          The JCas which the annotation should be added to.
   * @param nodeType
   *          The phrase type tag of the node.
   * @param children
   *          The TreebankNode children of the node.
   * @return The TreebankNode which was added to the JCas.
   */
  public static org.cleartk.syntax.constituent.type.TreebankNode newNode(
      JCas jCas,
      String nodeType,
      org.cleartk.syntax.constituent.type.TreebankNode... children) {
    int begin = children[0].getBegin();
    int end = children[children.length - 1].getEnd();
    org.cleartk.syntax.constituent.type.TreebankNode node = new org.cleartk.syntax.constituent.type.TreebankNode(
        jCas,
        begin,
        end);
    node.setNodeType(nodeType);
    node.addToIndexes();
    FSArray fsArray = new FSArray(jCas, children.length);
    fsArray.copyFromArray(children, 0, 0, children.length);
    node.setChildren(fsArray);
    for (org.cleartk.syntax.constituent.type.TreebankNode child : children) {
      child.setParent(node);
    }
    return node;
  }

}
