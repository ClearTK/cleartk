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
package org.cleartk.syntax;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.syntax.constituent.type.TreebankNode;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip Ogren
 * @author Steven Bethard
 */
public class TreebankTestsUtil {

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
  public static TreebankNode newNode(JCas jCas, int begin, int end, String nodeType) {
    TreebankNode node = new TreebankNode(jCas, begin, end);
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
  public static TreebankNode newNode(JCas jCas, String nodeType, TreebankNode... children) {
    int begin = children[0].getBegin();
    int end = children[children.length - 1].getEnd();
    TreebankNode node = new TreebankNode(jCas, begin, end);
    node.setNodeType(nodeType);
    node.addToIndexes();
    FSArray fsArray = new FSArray(jCas, children.length);
    fsArray.copyFromArray(children, 0, 0, children.length);
    node.setChildren(fsArray);
    for (TreebankNode child : children) {
      child.setParent(node);
    }
    return node;
  }
}
