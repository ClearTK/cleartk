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
package org.cleartk.syntax.feature;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.ToolkitTestBase;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.syntax.TreebankTestsUtil;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Token;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.JCasFactory;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */

public class HeadWordExtractorTest extends ToolkitTestBase{

	@Test
	public void testNoTreebankNode() throws UIMAException, CleartkException {
		HeadWordExtractor extractor = new HeadWordExtractor(null);
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		jCas.setDocumentText("foo");
		Token token = new Token(jCas, 0, 3);
		token.addToIndexes();

		this.checkFeatures(extractor.extract(jCas, token), (String) null);
	}

	@Test
	public void testNoTokens() throws UIMAException, CleartkException {
		HeadWordExtractor extractor = new HeadWordExtractor(new SpannedTextExtractor(), true);
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		jCas.setDocumentText("foo");
		TreebankNode node = TreebankTestsUtil.newNode(jCas, 0, 3, "NN");

		this.checkFeatures(extractor.extract(jCas, node), "HeadWord", "foo");
	}

	@Test
	public void testNoNodeTypes() throws UIMAException, CleartkException {
		try {
			HeadWordExtractor extractor = new HeadWordExtractor(null);
			JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
			jCas.setDocumentText("foo");
			TreebankNode parent = TreebankTestsUtil.newNode(jCas, null, TreebankTestsUtil.newNode(jCas, 0, 3, null));

			this.checkFeatures(extractor.extract(jCas, parent));
		} catch( NullPointerException e ) {
			// that's what we should expect when passing in a null extractor!
			return;
		} catch( CleartkException e ) {
			// this is ok, too
			return;
		}

		Assert.fail("should throw an exception when configuring a null extractor");
	}

	@Test
	public void testSimpleSentence() throws UIMAException, CleartkException {
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		tokenBuilder.buildTokens(jCas, "I ran home", "I ran home", "PRP VBD NN");
		TreebankNode iNode = TreebankTestsUtil.newNode(jCas, 0, 1, "PRP");
		TreebankNode ranNode = TreebankTestsUtil.newNode(jCas, 2, 5, "VBD");
		TreebankNode homeNode = TreebankTestsUtil.newNode(jCas, 6, 10, "NN");
		TreebankNode vpNode = TreebankTestsUtil.newNode(jCas, "VP", ranNode, homeNode);

		SpannedTextExtractor textExtractor = new SpannedTextExtractor();
		TypePathExtractor posExtractor = new TypePathExtractor(TreebankNode.class, "nodeType");
		HeadWordExtractor extractor = new HeadWordExtractor(textExtractor);

		this.checkFeatures(
				extractor.extract(jCas, iNode),
				"HeadWord", "I");

		this.checkFeatures(
				extractor.extract(jCas, vpNode),
				"HeadWord", "ran");

		this.checkFeatures(
				new HeadWordExtractor(textExtractor, false).extract(jCas, vpNode),
				"HeadWord", "ran");

		this.checkFeatures(
				new HeadWordExtractor(posExtractor).extract(jCas, vpNode),
				"HeadWord_TypePath(NodeType)", "VBD");
	}

	@Test
	public void testNPandPP() throws UIMAException, CleartkException {
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		tokenBuilder.buildTokens(jCas,
				"cat's toy under the box", 
				"cat 's toy under the box",
				"NN POS NN IN DT NN");
		TreebankNode catNode = TreebankTestsUtil.newNode(jCas, 0, 3, "NN");
		TreebankNode sNode = TreebankTestsUtil.newNode(jCas, 3, 5, "POS");
		TreebankNode catsNode = TreebankTestsUtil.newNode(jCas, "NP", catNode, sNode);
		TreebankNode toyNode = TreebankTestsUtil.newNode(jCas, 6, 9, "NN");
		TreebankNode catstoyNode = TreebankTestsUtil.newNode(jCas, "NP", catsNode, toyNode);
		TreebankNode underNode = TreebankTestsUtil.newNode(jCas, 10, 15, "IN");
		TreebankNode theNode = TreebankTestsUtil.newNode(jCas, 16, 19, "DT");
		TreebankNode boxNode = TreebankTestsUtil.newNode(jCas, 20, 23, "NN");
		TreebankNode theboxNode = TreebankTestsUtil.newNode(jCas, "NP", theNode, boxNode);
		TreebankNode undertheboxNode = TreebankTestsUtil.newNode(jCas, "PP", underNode, theboxNode);
		TreebankNode tree = TreebankTestsUtil.newNode(jCas, "NP", catstoyNode, undertheboxNode);

		SpannedTextExtractor textExtractor = new SpannedTextExtractor();
		TypePathExtractor posExtractor = new TypePathExtractor(TreebankNode.class, "nodeType");
		HeadWordExtractor extractor;

		extractor = new HeadWordExtractor(posExtractor, true);
		this.checkFeatures(
				extractor.extract(jCas, tree),
				"HeadWord_TypePath(NodeType)", "NN");
		this.checkFeatures(
				extractor.extract(jCas, undertheboxNode),
				"HeadWord_TypePath(NodeType)", "IN",
				"PPHeadWord_TypePath(NodeType)", "NN");

		extractor = new HeadWordExtractor(textExtractor, true);
		this.checkFeatures(
				extractor.extract(jCas, tree),
				"HeadWord", "toy");

		List<Feature> features = extractor.extract(jCas, undertheboxNode);
		Assert.assertEquals(2, features.size());
		Assert.assertEquals("HeadWord", features.get(0).getName());
		Assert.assertEquals("under", features.get(0).getValue());
		Assert.assertEquals("PPHeadWord", features.get(1).getName());
		Assert.assertEquals("box", features.get(1).getValue());

	}

//	private void checkFeatures(List<Feature> features, String expectedName, Object ... expectedValues) {
//		List<Object> actualValues = new ArrayList<Object>();
//		for (Feature feature: features) {
//			Assert.assertEquals(expectedName, feature.getName());
//			actualValues.add(feature.getValue());
//		}
//		Assert.assertEquals(Arrays.asList(expectedValues), actualValues);
//
//	}

	private void checkFeatures(List<Feature> features, Object ... expected) {
		Assert.assertEquals(expected.length/2, features.size());
		for(int i=0; i<expected.length / 2; i++) {
			Assert.assertEquals(expected[i*2], features.get(i).getName());
			Assert.assertEquals(expected[i*2+1], features.get(i).getValue());
		}
	}

}
