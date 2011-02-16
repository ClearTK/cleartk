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
import java.util.Arrays;
import java.util.List;

import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.types.OutputTypesHelper;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class DefaultOutputTypesHelper<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation> implements
    OutputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE, Parse, TreebankNode> {

  @Override
  public TreebankNode addParse(
      JCas jCas,
      Parse parse,
      SENTENCE_TYPE sentence,
      List<TOKEN_TYPE> tokens) {

    TreebankNode myNode;
      if (parse.getType() == AbstractBottomUpParser.TOP_NODE) {
        TopTreebankNode topNode = new TopTreebankNode(jCas);
        topNode.setParent(null);

        StringBuffer sb = new StringBuffer();
        parse.show(sb);
        topNode.setTreebankParse(sb.toString());

        myNode = topNode;
      } else {
        myNode = new TreebankNode(jCas);
      }

      myNode.setNodeType(parse.getType());
      myNode.setBegin(parse.getSpan().getStart());
      myNode.setEnd(parse.getSpan().getEnd());

      if (parse.getChildCount() == 1 && parse.getChildren()[0].getType() == AbstractBottomUpParser.TOK_NODE) {
        myNode.setLeaf(true);
        myNode.setNodeValue(parse.getChildren()[0].toString());
        myNode.setChildren(new FSArray(jCas, 0));
      } else {
        myNode.setNodeValue(null);
        myNode.setLeaf(false);

        List<FeatureStructure> cArray = new ArrayList<FeatureStructure>(parse.getChildCount());

        for (Parse cp : parse.getChildren()) {
          TreebankNode cNode = addParse(jCas, cp, sentence, tokens);
          cNode.setParent(myNode);
          cNode.addToIndexes();
          cArray.add(cNode);
        }

        FSArray cFSArray = new FSArray(jCas, cArray.size());
        cFSArray.copyFromArray(
            cArray.toArray(new FeatureStructure[cArray.size()]),
            0,
            0,
            cArray.size());
        myNode.setChildren(cFSArray);
      }

      if (parse.getType() == AbstractBottomUpParser.TOP_NODE) {
        List<TreebankNode> tList = getTerminals(myNode);
        FSArray tfsa = new FSArray(jCas, tList.size());
        tfsa.copyFromArray(tList.toArray(new FeatureStructure[tList.size()]), 0, 0, tList.size());
        ((TopTreebankNode) myNode).setTerminals(tfsa);
      }
      myNode.addToIndexes();
      return myNode;

  }

  protected List<TreebankNode> getTerminals(TreebankNode node) {
    List<TreebankNode> tList = new ArrayList<TreebankNode>();

    if (node.getChildren().size() == 0) {
      tList.add(node);
      return tList;
    }

    TreebankNode[] children = Arrays.asList(node.getChildren().toArray()).toArray(
        new TreebankNode[node.getChildren().size()]);

    for (TreebankNode child : children) {
      tList.addAll(getTerminals(child));
    }
    return tList;
  }

}
