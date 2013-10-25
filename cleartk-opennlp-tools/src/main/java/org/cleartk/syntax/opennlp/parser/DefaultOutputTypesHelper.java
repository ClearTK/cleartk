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

package org.cleartk.syntax.opennlp.parser;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.constituent.type.TerminalTreebankNode;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.apache.uima.fit.util.FSCollectionFactory;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */
@Beta
public class DefaultOutputTypesHelper<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation>
    implements OutputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE, Parse, TreebankNode> {

  @Override
  public TreebankNode addParse(
      JCas jCas,
      Parse parse,
      SENTENCE_TYPE sentence,
      List<TOKEN_TYPE> tokens) {

    TopTreebankNode node = new TopTreebankNode(jCas);
    this.setAttributes(node, parse, null, jCas);
    node.addToIndexes();

    StringBuffer sb = new StringBuffer();
    parse.show(sb);
    node.setTreebankParse(sb.toString());
    List<TreebankNode> terminals = getTerminals(node);
    node.setTerminals(new FSArray(jCas, terminals.size()));
    FSCollectionFactory.fillArrayFS(node.getTerminals(), terminals);
    return node;
  }

  private void setAttributes(TreebankNode node, Parse parse, TreebankNode parent, JCas jCas) {
    node.setParent(parent);
    node.setNodeType(parse.getType());
    node.setBegin(parse.getSpan().getStart());
    node.setEnd(parse.getSpan().getEnd());

    // leaf node
    if (isLeaf(parse)) {
      node.setLeaf(true);
      node.setNodeValue(parse.getChildren()[0].toString());
      node.setChildren(new FSArray(jCas, 0));
    }

    // branch node
    else {
      node.setLeaf(false);
      node.setNodeValue(null);
      List<TreebankNode> childNodes = new ArrayList<TreebankNode>();
      for (Parse childParse : parse.getChildren()) {
        TreebankNode childNode = isLeaf(childParse)
            ? new TerminalTreebankNode(jCas)
            : new TreebankNode(jCas);
        this.setAttributes(childNode, childParse, node, jCas);
        childNode.addToIndexes();
        childNodes.add(childNode);
      }
      node.setChildren(new FSArray(jCas, childNodes.size()));
      FSCollectionFactory.fillArrayFS(node.getChildren(), childNodes);
    }
  }

  protected List<TreebankNode> getTerminals(TreebankNode node) {
    List<TreebankNode> tList = new ArrayList<TreebankNode>();
    int nChildren = node.getChildren().size();
    if (nChildren == 0) {
      tList.add(node);
    }
    for (int i = 0; i < nChildren; ++i) {
      tList.addAll(getTerminals(node.getChildren(i)));
    }
    return tList;
  }

  private boolean isLeaf(Parse parse) {
    Parse[] childParses = parse.getChildren();
    return childParses.length == 1 && childParses[0].getType() == AbstractBottomUpParser.TOK_NODE;
  }
}
