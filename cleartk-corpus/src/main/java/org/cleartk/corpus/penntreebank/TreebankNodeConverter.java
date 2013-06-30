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
package org.cleartk.corpus.penntreebank;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.cleartk.syntax.constituent.type.TerminalTreebankNode;
import org.cleartk.util.treebank.TopTreebankNode;
import org.cleartk.util.treebank.TreebankNode;
import org.uimafit.util.FSCollectionFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class TreebankNodeConverter {
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
}
