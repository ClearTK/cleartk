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

import java.util.Arrays;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil.TreebankNodePath;
import org.cleartk.test.CleartkTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TreebankNodeUtilTest extends CleartkTestBase {

  @Test
  public void testSelectMatchingLeaf() throws Exception {
    this.jCas.setDocumentText("The cat chased the mouse .");

    top(
        "S",
        jCas,
        node("NP", jCas, leaf("DT", "The", jCas), leaf("NN", "cat", jCas)),
        node(
            "VP",
            jCas,
            leaf("VBD", "chased", jCas),
            node("NP", jCas, leaf("DT", "the", jCas), leaf("NN", "mouse", jCas))),
        leaf(".", ".", jCas));

    TreebankNode node;
    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(0, 3));
    Assert.assertEquals("The", node.getCoveredText());
    Assert.assertEquals("DT", node.getNodeType());
    Assert.assertTrue(node.getLeaf());

    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(8, 14));
    Assert.assertEquals("chased", node.getCoveredText());
    Assert.assertEquals("VBD", node.getNodeType());
    Assert.assertTrue(node.getLeaf());

    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(0, 4));
    Assert.assertNull(node);

    int end = this.jCas.getDocumentText().length();
    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(0, end));
    Assert.assertNull(node);
  }

  @Test
  public void testSelectHighestMatchingTreebankNode() throws Exception {
    this.jCas.setDocumentText("The cat chased mice .");

    top(
        "S",
        jCas,
        node("NP", jCas, leaf("DT", "The", jCas), leaf("NN", "cat", jCas)),
        node("VP", jCas, leaf("VBD", "chased", jCas), node("NP", jCas, leaf("NNS", "mice", jCas))),
        leaf(".", ".", jCas));

    TreebankNode node;
    node = TreebankNodeUtil.selectHighestMatchingTreebankNode(this.jCas, this.newSpan(0, 3));
    Assert.assertEquals("The", node.getCoveredText());
    Assert.assertEquals("DT", node.getNodeType());
    Assert.assertTrue(node.getLeaf());

    node = TreebankNodeUtil.selectHighestMatchingTreebankNode(this.jCas, this.newSpan(15, 19));
    Assert.assertEquals("mice", node.getCoveredText());
    Assert.assertEquals("NP", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    node = TreebankNodeUtil.selectHighestMatchingTreebankNode(this.jCas, this.newSpan(8, 19));
    Assert.assertEquals("chased mice", node.getCoveredText());
    Assert.assertEquals("VP", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    int end = this.jCas.getDocumentText().length();
    node = TreebankNodeUtil.selectHighestMatchingTreebankNode(this.jCas, this.newSpan(0, end));
    Assert.assertEquals("The cat chased mice .", node.getCoveredText());
    Assert.assertEquals("S", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    node = TreebankNodeUtil.selectHighestMatchingTreebankNode(this.jCas, this.newSpan(0, 4));
    Assert.assertNull(node);

    node = TreebankNodeUtil.selectHighestMatchingTreebankNode(this.jCas, this.newSpan(0, end - 1));
    Assert.assertNull(node);
  }

  @Test
  public void testSelectHighestCoveredTreebankNode() throws Exception {
    this.jCas.setDocumentText("The cat chased mice .");

    top(
        "S",
        jCas,
        node("NP", jCas, leaf("DT", "The", jCas), leaf("NN", "cat", jCas)),
        node("VP", jCas, leaf("VBD", "chased", jCas), node("NP", jCas, leaf("NNS", "mice", jCas))),
        leaf(".", ".", jCas));

    TreebankNode node;
    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(0, 3));
    Assert.assertEquals("The", node.getCoveredText());
    Assert.assertEquals("DT", node.getNodeType());
    Assert.assertTrue(node.getLeaf());

    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(15, 19));
    Assert.assertEquals("mice", node.getCoveredText());
    Assert.assertEquals("NP", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(8, 19));
    Assert.assertEquals("chased mice", node.getCoveredText());
    Assert.assertEquals("VP", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    int end = this.jCas.getDocumentText().length();
    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(0, end));
    Assert.assertEquals("The cat chased mice .", node.getCoveredText());
    Assert.assertEquals("S", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(0, 4));
    Assert.assertEquals("The", node.getCoveredText());
    Assert.assertEquals("DT", node.getNodeType());
    Assert.assertTrue(node.getLeaf());

    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(4, end - 1));
    Assert.assertEquals("chased mice", node.getCoveredText());
    Assert.assertEquals("VP", node.getNodeType());
    Assert.assertFalse(node.getLeaf());
  }

  @Test
  public void testGetDepth() throws Exception {
    this.jCas.setDocumentText("The cat chased mice .");

    top(
        "S",
        jCas,
        node("NP", jCas, leaf("DT", "The", jCas), leaf("NN", "cat", jCas)),
        node("VP", jCas, leaf("VBD", "chased", jCas), node("NP", jCas, leaf("NNS", "mice", jCas))),
        leaf(".", ".", jCas));

    for (TreebankNode node : JCasUtil.select(this.jCas, TreebankNode.class)) {
      String text = node.getCoveredText();
      int depth = TreebankNodeUtil.getDepth(node);
      if (text.equals("The")) {
        Assert.assertEquals(2, depth);
      } else if (text.equals("cat")) {
        Assert.assertEquals(2, depth);
      } else if (text.equals("The cat")) {
        Assert.assertEquals(1, depth);
      } else if (text.equals("chased")) {
        Assert.assertEquals(2, depth);
      } else if (text.equals("mice")) {
        if (node.getLeaf()) {
          Assert.assertEquals(3, depth);
        } else {
          Assert.assertEquals(2, depth);
        }
      } else if (text.equals("chased mice")) {
        Assert.assertEquals(1, depth);
      } else if (text.equals(".")) {
        Assert.assertEquals(1, depth);
      } else if (text.equals("The cat chased mice .")) {
        Assert.assertEquals(0, depth);
      } else {
        Assert.fail("Unexpected node: " + node);
      }
    }
  }

  @Test
  public void testGetPathToRoot() throws Exception {
    this.jCas.setDocumentText("The cat chased mice .");

    top(
        "S",
        jCas,
        node("NP", jCas, leaf("DT", "The", jCas), leaf("NN", "cat", jCas)),
        node("VP", jCas, leaf("VBD", "chased", jCas), node("NP", jCas, leaf("NNS", "mice", jCas))),
        leaf(".", ".", jCas));

    TopTreebankNode root = JCasUtil.selectSingle(this.jCas, TopTreebankNode.class);
    Assert.assertEquals(Arrays.asList(root), TreebankNodeUtil.getPathToRoot(root));

    TreebankNode np = root.getChildren(0);
    TreebankNode vp = root.getChildren(1);
    TreebankNode period = root.getChildren(2);
    Assert.assertEquals(Arrays.asList(np, root), TreebankNodeUtil.getPathToRoot(np));
    Assert.assertEquals(Arrays.asList(vp, root), TreebankNodeUtil.getPathToRoot(vp));
    Assert.assertEquals(Arrays.asList(period, root), TreebankNodeUtil.getPathToRoot(period));

    TreebankNode the = np.getChildren(0);
    TreebankNode cat = np.getChildren(1);
    Assert.assertEquals(Arrays.asList(the, np, root), TreebankNodeUtil.getPathToRoot(the));
    Assert.assertEquals(Arrays.asList(cat, np, root), TreebankNodeUtil.getPathToRoot(cat));

    TreebankNode chased = vp.getChildren(0);
    TreebankNode np2 = vp.getChildren(1);
    Assert.assertEquals(Arrays.asList(chased, vp, root), TreebankNodeUtil.getPathToRoot(chased));
    Assert.assertEquals(Arrays.asList(np2, vp, root), TreebankNodeUtil.getPathToRoot(np2));

    TreebankNode mice = np2.getChildren(0);
    Assert.assertEquals(Arrays.asList(mice, np2, vp, root), TreebankNodeUtil.getPathToRoot(mice));
  }

  @Test
  public void testGetPath() throws Exception {
    this.jCas.setDocumentText("The cat chased mice .");

    top(
        "S",
        jCas,
        node("NP", jCas, leaf("DT", "The", jCas), leaf("NN", "cat", jCas)),
        node("VP", jCas, leaf("VBD", "chased", jCas), node("NP", jCas, leaf("NNS", "mice", jCas))),
        leaf(".", ".", jCas));

    TopTreebankNode root = JCasUtil.selectSingle(this.jCas, TopTreebankNode.class);
    TreebankNode np = root.getChildren(0);
    TreebankNode vp = root.getChildren(1);
    TreebankNode period = root.getChildren(2);
    TreebankNode the = np.getChildren(0);
    TreebankNode cat = np.getChildren(1);
    TreebankNode chased = vp.getChildren(0);
    TreebankNode np2 = vp.getChildren(1);
    TreebankNode mice = np2.getChildren(0);

    TreebankNodePath path = TreebankNodeUtil.getPath(cat, mice);
    Assert.assertEquals(root, path.getCommonAncestor());
    Assert.assertEquals(Arrays.asList(cat, np), path.getSourceToAncestorPath());
    Assert.assertEquals(Arrays.asList(mice, np2, vp), path.getTargetToAncestorPath());

    path = TreebankNodeUtil.getPath(mice, chased);
    Assert.assertEquals(vp, path.getCommonAncestor());
    Assert.assertEquals(Arrays.asList(mice, np2), path.getSourceToAncestorPath());
    Assert.assertEquals(Arrays.asList(chased), path.getTargetToAncestorPath());

    path = TreebankNodeUtil.getPath(the, the);
    Assert.assertEquals(the, path.getCommonAncestor());
    Assert.assertEquals(Arrays.asList(), path.getSourceToAncestorPath());
    Assert.assertEquals(Arrays.asList(), path.getTargetToAncestorPath());

    path = TreebankNodeUtil.getPath(root, period);
    Assert.assertEquals(root, path.getCommonAncestor());
    Assert.assertEquals(Arrays.asList(), path.getSourceToAncestorPath());
    Assert.assertEquals(Arrays.asList(period), path.getTargetToAncestorPath());
  }

  @Test
  public void testToTreebankString() throws Exception {
    this.jCas.setDocumentText("The cat chased mice .");
    String expected1 = "(S (NP (DT The) (NN cat)) (VP (VBD chased) (NP (NNS mice))) (. .))";
    top(
        "S",
        jCas,
        node("NP", jCas, leaf("DT", "The", jCas), leaf("NN", "cat", jCas)),
        node("VP", jCas, leaf("VBD", "chased", jCas), node("NP", jCas, leaf("NNS", "mice", jCas))),
        leaf(".", ".", jCas));
    TopTreebankNode root1 = JCasUtil.selectSingle(this.jCas, TopTreebankNode.class);
    Assert.assertEquals(expected1, TreebankNodeUtil.toTreebankString(root1));

    this.jCas.reset();
    this.jCas.setDocumentText("The skunk thought the stump stunk .");
    String expected2 = "(S (NP (DT The) (NN skunk)) (VP (VBD thought) (S (NP (DT the) (NN stump)) (VP (VBD stunk)))) (. .))";
    top(
        "S",
        jCas,
        node("NP", jCas, leaf("DT", "The", jCas), leaf("NN", "skunk", jCas)),
        node(
            "VP",
            jCas,
            leaf("VBD", "thought", jCas),
            node(
                "S",
                jCas,
                node("NP", jCas, leaf("DT", "the", jCas), leaf("NN", "stump", jCas)),
                node("VP", jCas, leaf("VBD", "stunk", jCas)))),
        leaf(".", ".", jCas));
    TopTreebankNode root2 = JCasUtil.selectSingle(this.jCas, TopTreebankNode.class);
    Assert.assertEquals(expected2, TreebankNodeUtil.toTreebankString(root2));
  }

  private Annotation newSpan(int begin, int end) {
    return new Annotation(this.jCas, begin, end);
  }

  private static TopTreebankNode top(String type, JCas jCas, TreebankNode... children) {
    TopTreebankNode top = new TopTreebankNode(jCas);
    top.setNodeType(type);
    top.setLeaf(false);
    top.setChildren(new FSArray(jCas, children.length));
    for (int i = 0; i < children.length; ++i) {
      top.setChildren(i, children[i]);
      children[i].setParent(top);
    }
    setOffsetsAndAddToIndexes(top, 0);
    return top;
  }

  private static TreebankNode node(String type, JCas jCas, TreebankNode... children) {
    TreebankNode node = new TreebankNode(jCas);
    node.setNodeType(type);
    node.setLeaf(false);
    node.setChildren(new FSArray(jCas, children.length));
    for (int i = 0; i < children.length; ++i) {
      node.setChildren(i, children[i]);
      children[i].setParent(node);
    }
    return node;
  }

  private static TerminalTreebankNode leaf(String type, String value, JCas jCas) {
    TerminalTreebankNode leaf = new TerminalTreebankNode(jCas);
    leaf.setNodeType(type);
    leaf.setNodeValue(value);
    leaf.setLeaf(true);
    leaf.setChildren(new FSArray(jCas, 0));
    return leaf;
  }

  private static void setOffsetsAndAddToIndexes(TreebankNode node, int begin) {
    if (node.getLeaf()) {
      node.setBegin(begin);
      node.setEnd(begin + node.getNodeValue().length());
    } else {
      int offset = begin;
      for (int i = 0; i < node.getChildren().size(); ++i) {
        TreebankNode child = node.getChildren(i);
        setOffsetsAndAddToIndexes(child, offset);
        offset = child.getEnd() + 1;
      }
      node.setBegin(begin);
      node.setEnd(offset - 1);
    }
    node.addToIndexes();
  }
}
