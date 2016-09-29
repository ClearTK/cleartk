/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.berkeleyparser;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.constituent.type.TerminalTreebankNode;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.apache.uima.fit.util.FSCollectionFactory;

import edu.berkeley.nlp.syntax.Tree;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class DefaultOutputTypesHelper<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation>
    implements OutputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE, Tree<String>, TopTreebankNode> {

  @Override
  public TopTreebankNode addParse(
      JCas jCas,
      Tree<String> berkeleyNode,
      SENTENCE_TYPE sentence,
      List<TOKEN_TYPE> tokens) {
    List<TerminalTreebankNode> leafNodes = new ArrayList<TerminalTreebankNode>();
    TopTreebankNode topNode = (TopTreebankNode) convertNode(
        jCas,
        berkeleyNode,
        tokens,
        new TokenIndex(),
        leafNodes,
        true);
    topNode.setTerminals(new FSArray(jCas, leafNodes.size()));
    FSCollectionFactory.fillArrayFS(topNode.getTerminals(), leafNodes);
    return topNode;
  }

  private List<TreebankNode> convertChildren(
      JCas jCas,
      Tree<String> berkeleyNode,
      List<TOKEN_TYPE> tokens,
      TokenIndex tokenIndex,
      List<TerminalTreebankNode> leafNodes) {
    List<TreebankNode> uimaChildren = new ArrayList<TreebankNode>();
    convertChildren(jCas, berkeleyNode, tokens, tokenIndex, leafNodes, uimaChildren);
    return uimaChildren;
  }

  private void convertChildren(
      JCas jCas,
      Tree<String> berkeleyNode,
      List<TOKEN_TYPE> tokens,
      TokenIndex tokenIndex,
      List<TerminalTreebankNode> leafNodes,
      List<TreebankNode> uimaChildren) {

    List<Tree<String>> berkeleyChildren = berkeleyNode.getChildren();
    for (Tree<String> child : berkeleyChildren) {
      String nodeType = child.getLabel();
      if (nodeType.startsWith("@")) {
        convertChildren(jCas, child, tokens, tokenIndex, leafNodes, uimaChildren);
      } else {
        uimaChildren.add(convertNode(jCas, child, tokens, tokenIndex, leafNodes, false));
      }
    }
  }

  private TreebankNode convertNode(
      JCas jCas,
      Tree<String> berkeleyNode,
      List<TOKEN_TYPE> tokens,
      TokenIndex tokenIndex,
      List<TerminalTreebankNode> leafNodes,
      boolean isTop) {
    if (berkeleyNode.isLeaf()) {
      if (!isTop) {
        TOKEN_TYPE token = tokens.get(tokenIndex.index);
        TerminalTreebankNode leafNode = new TerminalTreebankNode(
            jCas,
            token.getBegin(),
            token.getEnd());
//        leafNode.setNodeType(berkeleyNode.getLabel());
        leafNode.setTokenIndex(tokenIndex.index);
        leafNode.setNodeValue(berkeleyNode.toString());
        leafNode.addToIndexes();
        tokenIndex.index++;
        leafNodes.add(leafNode);
        return leafNode;
      } else {
        TOKEN_TYPE token = tokens.get(tokenIndex.index);
        TopTreebankNode topNode = new TopTreebankNode(jCas, token.getBegin(), token.getEnd());
        topNode.addToIndexes();
        return topNode;
      }
    }

    List<TreebankNode> uimaChildren = convertChildren(
        jCas,
        berkeleyNode,
        tokens,
        tokenIndex,
        leafNodes);
    
    TreebankNode terminal = null;
    if (uimaChildren.size() == 1 && (terminal = uimaChildren.get(0)) instanceof TerminalTreebankNode 
        && terminal.getNodeType() == null){
      terminal.setNodeType(berkeleyNode.getLabel());
      return terminal;
    }
    
    int nodeBegin = uimaChildren.get(0).getBegin();
    int nodeEnd = uimaChildren.get(uimaChildren.size() - 1).getEnd();

    TreebankNode uimaNode;

    if (isTop) {
      uimaNode = new TopTreebankNode(jCas, nodeBegin, nodeEnd);
    } else {
      uimaNode = new TreebankNode(jCas, nodeBegin, nodeEnd);
    }

    uimaNode.setNodeType(berkeleyNode.getLabel());
    uimaNode.setChildren(new FSArray(jCas, uimaChildren.size()));
    FSCollectionFactory.fillArrayFS(uimaNode.getChildren(), uimaChildren);
    uimaNode.addToIndexes();
    for (TreebankNode child : uimaChildren) {
      child.setParent(uimaNode);
    }
    return uimaNode;
  }

  public class TokenIndex {
    public int index = 0;
  }
}
