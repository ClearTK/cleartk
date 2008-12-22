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
package org.cleartk.syntax.treebank.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.util.FileUtils;
import org.cleartk.syntax.treebank.util.TopTreebankNode;
import org.cleartk.syntax.treebank.util.TreebankFormatParser;
import org.cleartk.syntax.treebank.util.TreebankNode;
import org.junit.Test;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 */
 public class TreebankParserTests {

	@Test
	public void testGetLeafNode() {
		TreebankNode node = TreebankFormatParser.getLeafNode("(NNP Nov.)");
		assertEquals("NNP", node.getType());
		assertEquals("Nov.", node.getValue());
		assertEquals("Nov.", node.getText());
		assertTrue(node.isLeaf());

		node = TreebankFormatParser.getLeafNode("(NNP Nov.))");
		assertEquals(null, node);
	
		node = TreebankFormatParser.getLeafNode("(QP ($ $) (CD 250) (CD million) )");
		assertEquals(null, node);
		
		node = TreebankFormatParser.getLeafNode("($ $)");
		assertEquals("$", node.getType());
		assertEquals("$", node.getValue());
		assertEquals("$", node.getText());
	
		node = TreebankFormatParser.getLeafNode("(CD 3\\/8)");
		assertEquals("CD", node.getType());
		assertEquals("3\\/8", node.getValue());
		assertEquals("3/8", node.getText());
		
		node = TreebankFormatParser.getLeafNode("(-NONE- *)");
		assertEquals("-NONE-", node.getType());
		assertEquals("*", node.getValue());
		assertEquals("", node.getText());
	
		node = TreebankFormatParser.getLeafNode("(-NONE- *-1)");
		assertEquals("-NONE-", node.getType());
		assertEquals("*-1", node.getValue());
		assertEquals("", node.getText());
		
		node = TreebankFormatParser.getLeafNode("(-LRB- -LCB-)");
		assertEquals("-LRB-", node.getType());
		assertEquals("-LCB-", node.getValue());
		assertEquals("{", node.getText());
		node = TreebankFormatParser.getLeafNode("(-LRB- -LRB-)");
		assertEquals("(", node.getText());
		node = TreebankFormatParser.getLeafNode("(-LRB- -LSB-)");
		assertEquals("[", node.getText());
		
		node = TreebankFormatParser.getLeafNode("(-RRB- -RCB-)");
		assertEquals("-RRB-", node.getType());
		assertEquals("-RCB-", node.getValue());
		assertEquals("}", node.getText());

		node = TreebankFormatParser.getLeafNode("(-RRB- -RRB-)");
		assertEquals(")", node.getText());
		node = TreebankFormatParser.getLeafNode("(-RRB- -RSB-)");
		assertEquals("]", node.getText());

		
		node = TreebankFormatParser.getLeafNode("(ASDF)");
		assertEquals(null, node);

		node = TreebankFormatParser.getLeafNode("(ASDF )");
		assertEquals(null, node);
		
		node = TreebankFormatParser.getLeafNode("(NN Complex )");
		assertEquals("NN", node.getType());
		assertEquals("Complex", node.getValue());
		assertEquals("Complex", node.getText());
		assertTrue(node.isLeaf());
		
		node = TreebankFormatParser.getLeafNode("(IN Â± )");
		assertEquals("IN", node.getType());
		assertEquals("Â±", node.getValue());
		assertEquals("Â±", node.getText());
		assertTrue(node.isLeaf());
		
		node = TreebankFormatParser.getLeafNode("(CC +\\/-)");
		assertEquals("CC", node.getType());
		assertEquals("+\\/-", node.getValue());
		assertEquals("+/-", node.getText());
		assertTrue(node.isLeaf());
		

	}

	@Test
	public void testParse1() {
		String parse = "(PRN (-LRB- -LRB- ) (S-ADV (ADVP (FW i.e. )) (, , ) (NP (NP (NP (DT a ) (NN residual )) (PP (IN of  ) (-NONE- CD_) (HYPH - ) (NP (CD 116,000 )))) (: ; ) (S (NP-SBJ (NN n )) (VP (SYM = ) (NP (CD 12 )))))) (-RRB- -RRB- ))";
		String text = "(i.e., a residual of -116,000; n = 12)";
		TopTreebankNode topNode = TreebankFormatParser.parse(parse, text, 0);
		assertEquals(text, topNode.getText());
		List<TreebankNode> children = topNode.getChildren();
		testNode(children.get(0), "(", "-LRB-", "(-LRB- -LRB- )");
		testNode(children.get(1), "i.e., a residual of -116,000; n = 12", "S", "(S-ADV (ADVP (FW i.e. )) (, , ) (NP (NP (NP (DT a ) (NN residual )) (PP (IN of ) (-NONE- CD_) (HYPH - ) (NP (CD 116,000 )))) (: ; ) (S (NP-SBJ (NN n )) (VP (SYM = ) (NP (CD 12 ))))))");
		testNode(children.get(2), ")", "-RRB-", "(-RRB- -RRB- )");
		
		children = children.get(1).getChildren();
		testNode(children.get(0), "i.e.", "ADVP", "(ADVP (FW i.e. ))");
		testNode(children.get(1), ",", ",", "(, , )");
		testNode(children.get(2), "a residual of -116,000; n = 12", "NP", "(NP (NP (NP (DT a ) (NN residual )) (PP (IN of ) (-NONE- CD_) (HYPH - ) (NP (CD 116,000 )))) (: ; ) (S (NP-SBJ (NN n )) (VP (SYM = ) (NP (CD 12 )))))");
		
		TreebankNode node1 = children.get(2);
		
		children = children.get(0).getChildren();
		testNode(children.get(0), "i.e.", "FW", "(FW i.e. )");

		children = node1.getChildren();
		testNode(children.get(0), "a residual of -116,000", "NP", "(NP (NP (DT a ) (NN residual )) (PP (IN of ) (-NONE- CD_) (HYPH - ) (NP (CD 116,000 ))))");
		testNode(children.get(1), ";", ":", "(: ; )");
		testNode(children.get(2), "n = 12", "S", "(S (NP-SBJ (NN n )) (VP (SYM = ) (NP (CD 12 ))))");
		
		node1 = children.get(0);
		TreebankNode node2 = children.get(2);

		children = node1.getChildren();
		testNode(children.get(0), "a residual", "NP", "(NP (DT a ) (NN residual ))");
		testNode(children.get(1), "of -116,000", "PP", "(PP (IN of ) (-NONE- CD_) (HYPH - ) (NP (CD 116,000 )))");
		children = children.get(0).getChildren();
		testNode(children.get(0), "a", "DT", "(DT a )");
		testNode(children.get(1), "residual", "NN", "(NN residual )");
		
		children = node1.getChildren().get(1).getChildren();
		testNode(children.get(0), "of", "IN", "(IN of )");
		testNode(children.get(1), "", "-NONE-", "(-NONE- CD_)");
		testNode(children.get(2), "-", "HYPH", "(HYPH - )");
		testNode(children.get(3), "116,000", "NP", "(NP (CD 116,000 ))");
		children = children.get(3).getChildren();
		testNode(children.get(0), "116,000", "CD", "(CD 116,000 )");
		
		children = node2.getChildren();
		testNode(children.get(0), "n", "NP", "(NP-SBJ (NN n ))");
		testNode(children.get(0).getChildren().get(0), "n", "NN", "(NN n )");
		testNode(children.get(1), "= 12", "VP", "(VP (SYM = ) (NP (CD 12 )))");
		testNode(children.get(1).getChildren().get(0), "=", "SYM", "(SYM = )");
		testNode(children.get(1).getChildren().get(1), "12", "NP", "(NP (CD 12 ))");
		testNode(children.get(1).getChildren().get(1).getChildren().get(0), "12", "CD", "(CD 12 )");
	}
	
	private void testNode(TreebankNode node, String text, String type, String expectedParse) {
		assertEquals(text, node.getText());
		assertEquals(type, node.getType());
		String actualParse = node.getTopNode().getTreebankParse().substring(node.getParseBegin(), node.getParseEnd());
		assertEquals(expectedParse, actualParse);
	}
	
	private void testNodeText(TreebankNode node, String text, String docId) {
		try {
			String text1 = node.getText();
			String text2 = text.substring(node.getTextBegin(), node.getTextEnd());
			assertEquals(docId+": "+node.getTreebankParse() +node.getTextBegin()+":"+node.getTextEnd(), text1, text2);
		} catch(StringIndexOutOfBoundsException sioobe) {
			throw new RuntimeException("exception on node - "+node.getTopNode().getTreebankParse()+node.getTextBegin()+":"+node.getTextEnd(), sioobe);
		}
	}
	
	private void testNodeTreeText(TreebankNode node, String text, String docId) {
		testNodeText(node, text, docId);
		for(TreebankNode child : node.getChildren()) {
			testNodeTreeText(child, text, docId);
		}
	}
	
	@Test
	public void testCraftDocs() throws Exception {
		testPairedFiles("test/data/docs/treebank/11319941.txt", "test/data/docs/treebank/11319941.tree");
		testPairedFiles("test/data/docs/treebank/11597317.txt", "test/data/docs/treebank/11597317.tree");
		testPairedFiles("test/data/docs/treebank/12079497.txt", "test/data/docs/treebank/12079497.tree");
		testPairedFiles("test/data/docs/treebank/12546709.txt", "test/data/docs/treebank/12546709.tree");
		testPairedFiles("test/data/docs/treebank/12585968.txt", "test/data/docs/treebank/12585968.tree");
	}

	private void testPairedFiles(String plainTextFile, String treebankFile) throws Exception {
		String plainText = FileUtils.file2String(new File(plainTextFile));
		String treebankText = FileUtils.file2String(new File(treebankFile));
		
		List<TopTreebankNode> topNodes = TreebankFormatParser.parseDocument(treebankText,0, plainText);
		
		for(TopTreebankNode topNode : topNodes) {
			testNodeTreeText(topNode, plainText, plainTextFile);
		}
	}
	
	private List<TopTreebankNode> parseFile(String treebankFile) throws Exception {
		String treebankText = FileUtils.file2String(new File(treebankFile));
		String inferredText = TreebankFormatParser.inferPlainText(treebankText);
		return TreebankFormatParser.parseDocument(treebankText, 0, inferredText);
		
	}
	
	@Test
	public void testCraftDocs2() throws Exception {
		List<TopTreebankNode> nodes = parseFile("test/data/docs/treebank/11319941.tree");
		assertEquals("Complex trait analysis of the mouse striatum: independent QTLs modulate volume and neuron number", nodes.get(0).getText());
		assertEquals("We thank Richelle Strom for generating the F2 intercross mice.", nodes.get(nodes.size()-1).getText());
		nodes = parseFile("test/data/docs/treebank/11597317.tree");
		assertEquals("BRCA2 and homologous recombination", nodes.get(0).getText());
		assertEquals("In BRCA - defective cells, Rad51 fails to associate with sites of damage due to lack of an assembly factor.", nodes.get(nodes.size()-1).getText());
		nodes = parseFile("test/data/docs/treebank/12079497.tree");
		assertEquals("Embryonic stem cells and mice expressing different GFP variants for multiple non - invasive reporter usage within a single animal", nodes.get(0).getText());
		assertEquals("This work was supported by grants from the National Cancer Institute of Canada.", nodes.get(nodes.size()-1).getText());
		nodes = parseFile("test/data/docs/treebank/12546709.tree");
		assertEquals("Morphological characterization of the AlphaA - and AlphaB - crystallin double knockout mouse lens", nodes.get(0).getText());
		assertEquals("This research was supported in part by a NIH Grant for Vision Research EY02932 awarded to LT.", nodes.get(nodes.size()-1).getText());
		nodes = parseFile("test/data/docs/treebank/12585968.tree");
		assertEquals("Brn3c null mutant mice show long - term, incomplete retention of some afferent inner ear innervation", nodes.get(0).getText());
		assertEquals("Supported by the National Eye Institute ( EY12020, MX ), the March of Dimes Birth Defects Foundation ( MX ), the Egyptian Government ( AM ), the Juselius Foundation ( UP ), the NIDCD ( 2 P01 DC00215, BF; DC04594, M.X. ), the Taub foundation ( BF ) and NASA ( 01 - OBPR - 06; BF ).", nodes.get(nodes.size()-1).getText());

		
	}
	
	@Test
	public void testGetType() {
		String type = TreebankFormatParser.getType("(ASDF)");
		assertEquals("ASDF", type);
		
		type = TreebankFormatParser.getType("(-RRB- -RCB-)");
		assertEquals("-RRB-", type);
		
		type = TreebankFormatParser.getType("(NNP Community)");
		assertEquals("NNP", type);

		type = TreebankFormatParser.getType("(: --)");
		assertEquals(":", type);

		type = TreebankFormatParser.getType("(NP-LOC (NNP CA) )");
		assertEquals("NP-LOC", type);
		type = TreebankFormatParser.getType("(NP");
		assertEquals("NP", type);
		type = TreebankFormatParser.getType("(-NONE- *U*) ) (PP (IN of)");
		assertEquals("-NONE-", type);
		type = TreebankFormatParser.getType("((-NONE- *U*) ) (PP (IN of)");
		assertEquals(null, type);
	}

	@Test
	public void testParseWithCoordination() {
		TopTreebankNode node = TreebankFormatParser.parse("(VP=3 (VB foo))");

		assertEquals("VP", node.getType());
		assertEquals("3", node.getTags()[0]);
		assertEquals(1, node.getChildren().size());
	}

	@Test
	public void testMovePastWhiteSpaceChars() {
		assertEquals(0, TreebankFormatParser.movePastWhiteSpaceChars("asdf", 0));
		assertEquals(1, TreebankFormatParser.movePastWhiteSpaceChars(" asdf", 0));
		assertEquals(5, TreebankFormatParser.movePastWhiteSpaceChars("     asdf", 0));
		assertEquals(5, TreebankFormatParser.movePastWhiteSpaceChars(" \t\t\n\nasdf", 0));
		assertEquals(5, TreebankFormatParser.movePastWhiteSpaceChars(" \t\t\n\nasdf", 2));
	}

	@Test
	public void testSplitSentences() throws IOException
	{
		String[] sentences = TreebankFormatParser.splitSentences(FileUtils.file2String(new File("test/data/docs/treebank/11319941.tree")));
		assertEquals(290, sentences.length);	
		sentences = TreebankFormatParser.splitSentences(FileUtils.file2String(new File("test/data/docs/treebank/11597317.tree")));
		assertEquals(99, sentences.length);	
		sentences = TreebankFormatParser.splitSentences(FileUtils.file2String(new File("test/data/docs/treebank/12079497.tree")));
		assertEquals(149, sentences.length);	
		sentences = TreebankFormatParser.splitSentences(FileUtils.file2String(new File("test/data/docs/treebank/12546709.tree")));
		assertEquals(243, sentences.length);	
		sentences = TreebankFormatParser.splitSentences(FileUtils.file2String(new File("test/data/docs/treebank/12585968.tree")));
		assertEquals(283, sentences.length);	
	}
	
	@Test
	public void testInferPlainText() {
		String treebankParse = "( (X (NP (NP (NML (NN Complex ) (NN trait )) (NN analysis )) (PP (IN of ) (NP (DT the ) (NN mouse ) (NN striatum )))) (: : ) (S (NP-SBJ (JJ independent ) (NNS QTLs )) (VP (VBP modulate ) (NP (NP (NN volume )) (CC and ) (NP (NN neuron ) (NN number)))))) )\n";
		String expectedText = "Complex trait analysis of the mouse striatum: independent QTLs modulate volume and neuron number";
		String actualText = TreebankFormatParser.inferPlainText(treebankParse);
		assertEquals(expectedText, actualText);
		
		treebankParse = "(T 's)(T there)(T 's)(T going)(T .)";
		expectedText = "'s there's going.";
		actualText = TreebankFormatParser.inferPlainText(treebankParse);
		assertEquals(expectedText, actualText);

		treebankParse = "(T ')(T .)(T ,)(T :)(T ;)(T 's)(T can)(T 't)";
		expectedText = "'.,:;'s can't";
		actualText = TreebankFormatParser.inferPlainText(treebankParse);
		assertEquals(expectedText, actualText);
		
		treebankParse = "(T \")(T Howdy)(T !)(T \")";
		expectedText = "\"Howdy!\"";
		actualText = TreebankFormatParser.inferPlainText(treebankParse);
		assertEquals(expectedText, actualText);
		
		treebankParse = "(QP (CD 44.4) (CC +\\/-) (CD 4.4))";
		expectedText = "44.4 +/- 4.4";
		actualText = TreebankFormatParser.inferPlainText(treebankParse);
		assertEquals(expectedText, actualText);
		
	}
	
	@Test
	public void testBadInputText() {
		IllegalArgumentException iae = null;
		try {
			String treebankParse = "(NML (NN Complex ) (NN trait ))";
			String plainText = "Complex strait";
			TreebankFormatParser.parse(treebankParse, plainText, 0);
		} catch(IllegalArgumentException e) {
			iae = e;
		}
		assertNotNull(iae);
	}

	@Test
	public void testSentenceParse() {
		String treebankParse = "( (X (NP (NP (NML (NN Complex ) (NN trait )) (NN analysis )) (PP (IN of ) (NP (DT the ) (NN mouse ) (NN striatum )))) (: : ) (S (NP-SBJ (JJ independent ) (NNS QTLs )) (VP (VBP modulate ) (NP (NP (NN volume )) (CC and ) (NP (NN neuron ) (NN number)))))) )";
		String paragraphText = "  Complex     trait analysis of the mouse striatum: independent QTLs modulate volume and neuron number  ";
		String expectedText = "Complex     trait analysis of the mouse striatum: independent QTLs modulate volume and neuron number";
		testSentenceParse(treebankParse, paragraphText, expectedText, "NN", "Complex", "NN", "number");

		treebankParse = "( (X (NP (NN Abstract))) )";
		paragraphText = "Abstract";
		expectedText = "Abstract";
		testSentenceParse(treebankParse, paragraphText, expectedText, "NN", "Abstract", "NN", "Abstract");

		treebankParse = "( (X (NP (NN Background))) )";
		paragraphText = "Background";
		expectedText = "Background";
		testSentenceParse(treebankParse, paragraphText, expectedText, "NN", "Background", "NN", "Background");

		treebankParse = "( (S (NP-SBJ-1 (DT The ) (NN striatum )) (VP (VBZ plays ) (NP (DT a ) (JJ pivotal ) (NN role )) (PP (IN in ) (S-NOM (NP-SBJ-1 (-NONE- *PRO*)) (VP (VBG modulating ) (NP (NP (NN motor ) (NN activity )) (CC and ) (NP (JJR higher ) (JJ cognitive ) (NN function ))))))) (. .)) )";
		paragraphText = "The striatum plays a pivotal role in modulating motor activity and higher cognitive function.     We analyzed variation in striatal volume and neuron number in mice and initiated a complex trait analysis to discover polymorphic genes that modulate the structure of the basal ganglia.      \n ";
		expectedText = "The striatum plays a pivotal role in modulating motor activity and higher cognitive function.";
		testSentenceParse(treebankParse, paragraphText, expectedText, "DT", "The", ".", ".");

		treebankParse = "( (S (NP-SBJ-2 (PRP We )) (VP (VP (VBD analyzed ) (NP (NP (NP (NN variation )) (PP (IN in ) (NP (NP (JJ striatal ) (NN volume )) (CC and ) (NP (NN neuron ) (NN number ))))) (PP-LOC (IN in ) (NNS mice )))) (CC and ) (VP (VBD initiated ) (NP (NP (DT a ) (NML (JJ complex ) (NN trait )) (NN analysis ))) (S-PRP (NP-SBJ-2 (-NONE- *PRO*)) (VP (TO to ) (VP (VB discover ) (NP (NP (JJ polymorphic ) (NNS genes )) (SBAR (WHNP-1 (WDT that )) (S (NP-SBJ-1 (-NONE- *T*)) (VP (VBP modulate ) (NP (NP (DT the ) (NN structure )) (PP (IN of ) (NP (DT the ) (JJ basal ) (NNS ganglia ))))))))))))) (. .)) )";
		paragraphText = "We analyzed variation in striatal volume and neuron number in mice and initiated a complex trait analysis to discover polymorphic genes that modulate the structure of the basal ganglia.      \n";
		expectedText = "We analyzed variation in striatal volume and neuron number in mice and initiated a complex trait analysis to discover polymorphic genes that modulate the structure of the basal ganglia.";
		testSentenceParse(treebankParse, paragraphText, expectedText, "PRP", "We", ".", ".");

		treebankParse = "(NP (QP (CD 84,800 ) (NN ± ) (CD 3,500 )) (NN_SPLIT neurons))";
		paragraphText = "84,800 ± 3,500 neurons";
		expectedText = "84,800 ± 3,500 neurons";
		testSentenceParse(treebankParse, paragraphText, expectedText, "CD", "84,800", "NN_SPLIT", "neurons");
		
	}

	private void testSentenceParse(String treebankParse, String paragraphText, String expectedText,
			String firstNodeType, String firstNodeText, String lastNodeType, String lastNodeText) {
		TopTreebankNode topNode = TreebankFormatParser.parse(treebankParse, paragraphText, 0);
		assertEquals(expectedText, topNode.getText());
		TreebankNode node = topNode.getTerminal(0);
		assertEquals(firstNodeType, node.getType());
		assertEquals(firstNodeText, node.getText());
		node = topNode.getTerminal(topNode.getTerminalCount() - 1);
		assertEquals(lastNodeType, node.getType());
		assertEquals(lastNodeText, node.getText());
	}

	@Test
	public void test1() throws Exception {
		String treebankParse = "( (X (NP (NP (NML (NN Complex ) (NN trait )) (NN analysis )) (PP (IN of ) (NP (DT the ) (NN mouse ) (NN striatum )))) (: : ) (S (NP-SBJ (JJ independent ) (NNS QTLs )) (VP (VBP modulate ) (NP (NP (NN volume )) (CC and ) (NP (NN neuron ) (NN number)))))) )";
		String expectedText = "Complex trait analysis of the mouse striatum: independent QTLs modulate volume and neuron number";
		TopTreebankNode topNode = TreebankFormatParser.parse(treebankParse);
		assertEquals(expectedText, topNode.getText());
		TreebankNode node = topNode.getTerminal(0);
		assertEquals("NN", node.getType());
		assertEquals("Complex", node.getText());
		node = topNode.getTerminal(1);
		assertEquals("NN", node.getType());
		assertEquals("trait", node.getText());
		
		int i = 0;
		assertEquals("Complex", topNode.getTerminal(i++).getText());
		assertEquals("trait", topNode.getTerminal(i++).getText());
		assertEquals("analysis", topNode.getTerminal(i++).getText());
		assertEquals("of", topNode.getTerminal(i++).getText());
		assertEquals("the", topNode.getTerminal(i++).getText());
		assertEquals("mouse", topNode.getTerminal(i++).getText());
		assertEquals("striatum", topNode.getTerminal(i++).getText());
		assertEquals(":", topNode.getTerminal(i++).getText());
		assertEquals("independent", topNode.getTerminal(i++).getText());
		assertEquals("QTLs", topNode.getTerminal(i++).getText());
		assertEquals("modulate", topNode.getTerminal(i++).getText());
		assertEquals("volume", topNode.getTerminal(i++).getText());
		assertEquals("and", topNode.getTerminal(i++).getText());
		assertEquals("neuron", topNode.getTerminal(i++).getText());
		assertEquals("number", topNode.getTerminal(i++).getText());
		
		i=0;
		node = topNode.getTerminal(i++);
		assertEquals("Complex", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("trait", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("analysis", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("of", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("the", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("mouse", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("striatum", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals(":", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("independent", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("QTLs", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("modulate", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("volume", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("and", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("neuron", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
		node = topNode.getTerminal(i++);
		assertEquals("number", expectedText.substring(node.getTextBegin(), node.getTextEnd()));
	}

	@Test
	public void testOffset() {
		String parse = "(S (NP (NNS dogs )) (VP (VB chase ) (NP (NNS cats ))) (. . ))";
		String text = "I believe that dogs chase cats.";
		TopTreebankNode topNode = TreebankFormatParser.parse(parse, text, 14);
		testNode(topNode, "dogs chase cats.", "S", parse);
		List<TreebankNode> children = topNode.getChildren();
		testNode(children.get(0), "dogs", "NP", "(NP (NNS dogs ))");
		testNode(children.get(1), "chase cats", "VP", "(VP (VB chase ) (NP (NNS cats )))");
	}

	@Test
	public void testParseDocument() {
		String treebankText = "((S (VP (VB Run)) (. !))) ((S (RB Now) (. !)))";
		String inferredText = "Run!\nNow!";
		assertEquals(inferredText, TreebankFormatParser.inferPlainText(treebankText));
		
		List<TopTreebankNode> nodes;
		nodes = TreebankFormatParser.parseDocument(treebankText, 0, inferredText);
		assertEquals(0, nodes.get(0).getTextBegin());
		assertEquals(4, nodes.get(0).getTextEnd());
		assertEquals(5, nodes.get(1).getTextBegin());
		assertEquals(9, nodes.get(1).getTextEnd());
	}
	
	@Test
	public void testSpecialCasePeriods() {
		String parse = "(S (NP (DT The) (NNP U.S.)) (. .))";
		String text = "The U.S. ";
		TopTreebankNode topNode = TreebankFormatParser.parse(parse, text, 0);
		testNode(topNode, "The U.S..", "S", parse);
		testNode(topNode.getChildren().get(0).getChildren().get(1), "U.S.", "NNP", "(NNP U.S.)");

		text = "The U.S..";
		topNode = TreebankFormatParser.parse(parse, text, 0);
		testNode(topNode, "The U.S..", "S", parse);
		testNode(topNode.getChildren().get(0).getChildren().get(1), "U.S.", "NNP", "(NNP U.S.)");
	}
}
