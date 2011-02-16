/** 
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
package org.cleartk.syntax.berkeley;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.constituent.type.TerminalTreebankNode;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.types.OutputTypesHelper;
import org.cleartk.util.UIMAUtil;

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
    topNode.setTerminals(UIMAUtil.toFSArray(jCas, leafNodes));
    topNode.addToIndexes();
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
        leafNode.setNodeType(berkeleyNode.getLabel());
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
    int nodeBegin = uimaChildren.get(0).getBegin();
    int nodeEnd = uimaChildren.get(uimaChildren.size() - 1).getEnd();

    TreebankNode uimaNode;

    if (isTop) {
      uimaNode = new TopTreebankNode(jCas);
    } else {
      uimaNode = new TreebankNode(jCas, nodeBegin, nodeEnd);
    }

    uimaNode.setNodeType(berkeleyNode.getLabel());
    uimaNode.setChildren(UIMAUtil.toFSArray(jCas, uimaChildren));
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
