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
package org.cleartk.feature.syntax;

import java.util.List;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.test.DefaultTestBase;
import org.cleartk.token.type.Token;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */

public class HeadWordExtractorTest extends DefaultTestBase {

  @Test
  public void testNoTreebankNode() throws Throwable {
    HeadWordExtractor extractor = new HeadWordExtractor(null);
    jCas.setDocumentText("foo");
    Token token = new Token(jCas, 0, 3);
    token.addToIndexes();

    this.checkFeatures(extractor.extract(jCas, token), (String) null);
  }

  @Test
  public void testNoTokens() throws Throwable {
    HeadWordExtractor extractor = new HeadWordExtractor(new CoveredTextExtractor(), true);
    jCas.setDocumentText("foo");
    TreebankNode node = TreebankNodeUtil.newNode(jCas, 0, 3, "NN");

    this.checkFeatures(extractor.extract(jCas, node), "HeadWord", "foo");
  }

  @Test
  public void testNoNodeTypes() {
    try {
      HeadWordExtractor extractor = new HeadWordExtractor(null);
      jCas.setDocumentText("foo");
      TreebankNode parent = TreebankNodeUtil.newNode(
          jCas,
          null,
          TreebankNodeUtil.newNode(jCas, 0, 3, null));

      this.checkFeatures(extractor.extract(jCas, parent));
    } catch (NullPointerException e) {
      // that's what we should expect when passing in a null extractor!
      return;
    } catch (CleartkExtractorException e) {
      // this is ok, too
      return;
    }

    Assert.fail("should throw an exception when configuring a null extractor");
  }

  @Test
  public void testSimpleSentence() throws Throwable {
    tokenBuilder.buildTokens(jCas, "I ran home", "I ran home", "PRP VBD NN");
    TreebankNode iNode = TreebankNodeUtil.newNode(jCas, 0, 1, "PRP");
    TreebankNode ranNode = TreebankNodeUtil.newNode(jCas, 2, 5, "VBD");
    TreebankNode homeNode = TreebankNodeUtil.newNode(jCas, 6, 10, "NN");
    TreebankNode vpNode = TreebankNodeUtil.newNode(jCas, "VP", ranNode, homeNode);

    CoveredTextExtractor textExtractor = new CoveredTextExtractor();
    TypePathExtractor posExtractor = new TypePathExtractor(TreebankNode.class, "nodeType");
    HeadWordExtractor extractor = new HeadWordExtractor(textExtractor);

    this.checkFeatures(extractor.extract(jCas, iNode), "HeadWord", "I");

    this.checkFeatures(extractor.extract(jCas, vpNode), "HeadWord", "ran");

    this.checkFeatures(
        new HeadWordExtractor(textExtractor, false).extract(jCas, vpNode),
        "HeadWord",
        "ran");

    this.checkFeatures(
        new HeadWordExtractor(posExtractor).extract(jCas, vpNode),
        "HeadWord_TypePath(NodeType)",
        "VBD");
  }

  @Test
  public void testNPandPP() throws Throwable {
    tokenBuilder.buildTokens(
        jCas,
        "cat's toy under the box",
        "cat 's toy under the box",
        "NN POS NN IN DT NN");
    TreebankNode catNode = TreebankNodeUtil.newNode(jCas, 0, 3, "NN");
    TreebankNode sNode = TreebankNodeUtil.newNode(jCas, 3, 5, "POS");
    TreebankNode catsNode = TreebankNodeUtil.newNode(jCas, "NP", catNode, sNode);
    TreebankNode toyNode = TreebankNodeUtil.newNode(jCas, 6, 9, "NN");
    TreebankNode catstoyNode = TreebankNodeUtil.newNode(jCas, "NP", catsNode, toyNode);
    TreebankNode underNode = TreebankNodeUtil.newNode(jCas, 10, 15, "IN");
    TreebankNode theNode = TreebankNodeUtil.newNode(jCas, 16, 19, "DT");
    TreebankNode boxNode = TreebankNodeUtil.newNode(jCas, 20, 23, "NN");
    TreebankNode theboxNode = TreebankNodeUtil.newNode(jCas, "NP", theNode, boxNode);
    TreebankNode undertheboxNode = TreebankNodeUtil.newNode(jCas, "PP", underNode, theboxNode);
    TreebankNode tree = TreebankNodeUtil.newNode(jCas, "NP", catstoyNode, undertheboxNode);

    CoveredTextExtractor textExtractor = new CoveredTextExtractor();
    TypePathExtractor posExtractor = new TypePathExtractor(TreebankNode.class, "nodeType");
    HeadWordExtractor extractor;

    extractor = new HeadWordExtractor(posExtractor, true);
    this.checkFeatures(extractor.extract(jCas, tree), "HeadWord_TypePath(NodeType)", "NN");
    this.checkFeatures(
        extractor.extract(jCas, undertheboxNode),
        "HeadWord_TypePath(NodeType)",
        "IN",
        "PPHeadWord_TypePath(NodeType)",
        "NN");

    extractor = new HeadWordExtractor(textExtractor, true);
    this.checkFeatures(extractor.extract(jCas, tree), "HeadWord", "toy");

    List<Feature> features = extractor.extract(jCas, undertheboxNode);
    Assert.assertEquals(2, features.size());
    Assert.assertEquals("HeadWord", features.get(0).getName());
    Assert.assertEquals("under", features.get(0).getValue());
    Assert.assertEquals("PPHeadWord", features.get(1).getName());
    Assert.assertEquals("box", features.get(1).getValue());

  }

  // private void checkFeatures(List<Feature> features, String expectedName, Object ...
  // expectedValues) {
  // List<Object> actualValues = new ArrayList<Object>();
  // for (Feature feature: features) {
  // Assert.assertEquals(expectedName, feature.getName());
  // actualValues.add(feature.getValue());
  // }
  // Assert.assertEquals(Arrays.asList(expectedValues), actualValues);
  //
  // }

  private void checkFeatures(List<Feature> features, Object... expected) {
    Assert.assertEquals(expected.length / 2, features.size());
    for (int i = 0; i < expected.length / 2; i++) {
      Assert.assertEquals(expected[i * 2], features.get(i).getName());
      Assert.assertEquals(expected[i * 2 + 1], features.get(i).getValue());
    }
  }

}
