package org.cleartk.syntax.dependency.malt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;

public class MaltParserTest extends CleartkTestBase {

	@Override
	public String[] getTypeSystemDescriptorNames() {
		return new String[] { "org.cleartk.token.TypeSystem",
				"org.cleartk.syntax.dependency.TypeSystem" };
	}

	@Test
	public void test() throws UIMAException {
		TokenBuilder<Token, Sentence> tokenBuilder = new TokenBuilder<Token, Sentence>(
			Token.class,
			Sentence.class,
			"pos",
			"stem");
		tokenBuilder.buildTokens(
			this.jCas,
			"The dog chased the fox down the road.",
			"The dog chased the fox down the road .",
			"DT NN VBD DT NN IN DT NN .");

		AnalysisEngineDescription desc = MaltParser.getDescription();
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(desc);
		engine.process(this.jCas);
		engine.collectionProcessComplete();

		// The -det-> dog
		// dog -nsubj-> chased
		// chased -> ROOT
		// the -det-> fox
		// fox -dobj-> chased
		// down -advmod-> chased
		// the -det-> road
		// road -pobj-> down
		// . -punct-> chased
		List<DependencyNode> nodes;
		nodes = AnnotationRetrieval.getAnnotations(jCas, DependencyNode.class);

		// check node spans
		String[] texts = "The dog chased the fox down the road .".split(" ");
		assertEquals(texts.length, nodes.size());
		for (int i = 0; i < texts.length; ++i) {
			assertEquals(texts[i], nodes.get(i).getCoveredText());
		}

		// node aliases
		DependencyNode the1 = nodes.get(0);
		DependencyNode dog = nodes.get(1);
		DependencyNode chased = nodes.get(2);
		DependencyNode the2 = nodes.get(3);
		DependencyNode fox = nodes.get(4);
		DependencyNode down = nodes.get(5);
		DependencyNode the3 = nodes.get(6);
		DependencyNode road = nodes.get(7);
		DependencyNode period = nodes.get(8);

		// check node heads and dependency types
		assertEquals(dog, the1.getHead());
		assertEquals("det", the1.getDependencyType());
		assertEquals(chased, dog.getHead());
		assertEquals("nsubj", dog.getDependencyType());
		assertNull(chased.getHead());
		assertNull(chased.getDependencyType());
		assertEquals(fox, the2.getHead());
		assertEquals("det", the2.getDependencyType());
		assertEquals(chased, fox.getHead());
		assertEquals("dobj", fox.getDependencyType());
		assertEquals(chased, down.getHead());
		assertEquals("advmod", down.getDependencyType());
		assertEquals(road, the3.getHead());
		assertEquals("det", the3.getDependencyType());
		assertEquals(down, road.getHead());
		assertEquals("pobj", road.getDependencyType());
		assertEquals(chased, period.getHead());
		assertEquals("punct", period.getDependencyType());

		// check node children
		List<DependencyNode> emptyList = Collections.emptyList();
		assertEquals(emptyList, getChildren(the1));
		assertEquals(Arrays.asList(the1), getChildren(dog));
		assertEquals(Arrays.asList(dog, fox, down, period), getChildren(chased));
		assertEquals(emptyList, getChildren(the2));
		assertEquals(Arrays.asList(the2), getChildren(fox));
		assertEquals(Arrays.asList(road), getChildren(down));
		assertEquals(emptyList, getChildren(the3));
		assertEquals(Arrays.asList(the3), getChildren(road));
		assertEquals(emptyList, getChildren(period));
	}

	private static List<DependencyNode> getChildren(DependencyNode node) {
		return UIMAUtil.toList(node.getChildren(), DependencyNode.class);
	}
}
