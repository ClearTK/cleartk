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
package org.cleartk.classifier.feature.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.TypePathFeature;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.JCasFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;

import de.julielab.jules.types.DependencyRelation;
import de.julielab.jules.types.Lemma;
import de.julielab.jules.types.POSTag;
import de.julielab.jules.types.Token;
/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Philip Ogren
 */
public class TypePathExtractorTests {

	public static class Annotator extends JCasAnnotator_ImplBase {
		/**
		 * Assumes that we are processing the text: "Whatever it is you might think
		 * you have You have nothing to lose Through every dead and living thing
		 * Time runs like a fuse."
		 */
		public void process(JCas jCas) throws AnalysisEngineProcessException {
			Token token1 = new Token(jCas, 0, 8);
			token1.addToIndexes();
			Lemma lemma = new Lemma(jCas);
			lemma.setValue("wwwwwwww");
			// covered text of lemma is "hat"
			lemma.setBegin(1);
			lemma.setEnd(4);
			lemma.setTestFS(new StringArray(jCas, 3));
			lemma.setTestFS(0, "A");
			lemma.setTestFS(1, "B");
			lemma.setTestFS(2, "A");
			
			lemma.addToIndexes();
			token1.setLemma(lemma);
			POSTag posTag1 = new POSTag(jCas,0,8);
			posTag1.setValue("pos1");
			posTag1.addToIndexes();
			POSTag posTag2 = new POSTag(jCas,0,8);
			posTag2.setValue("pos2");
			posTag2.addToIndexes();
			FSArray token1Poses = new FSArray(jCas, 2);
			token1Poses.copyFromArray(new FeatureStructure[] { posTag1, posTag2 }, 0, 0, 2);
			token1.setPosTag(token1Poses);

			Token token2 = new Token(jCas, 9, 11);
			token2.addToIndexes();
			POSTag posTag3 = new POSTag(jCas);
			posTag3.setValue("pos3");
			posTag3.addToIndexes();
			FSArray token2Poses = new FSArray(jCas, 1);
			token2Poses.copyFromArray(new FeatureStructure[] { posTag3 }, 0, 0, 1);
			token2.setPosTag(token2Poses);

			Token token3 = new Token(jCas, 12, 14);
			token3.addToIndexes();

			DependencyRelation depRel1 = new DependencyRelation(jCas);
			depRel1.setHead(token1);
			depRel1.setProjective(true);
			depRel1.setLabel("deprel token1");
			depRel1.addToIndexes();
			DependencyRelation depRel2 = new DependencyRelation(jCas);
			depRel2.setHead(token2);
			depRel2.setProjective(true);
			depRel2.setLabel("deprel token2");
			depRel2.addToIndexes();
			FSArray depRels = new FSArray(jCas, 2);
			depRels.copyFromArray(new FeatureStructure[] { depRel1, depRel2 }, 0, 0, 2);
			token3.setDepRel(depRels);

		}

	}

	@Test
	public void testExtract() throws IOException, UIMAException {
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				TypePathExtractorTests.Annotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("test/desc/JulieTypeSystem.xml"));
		
		//The text here was once upon a time some lyrics by a favorite singer which have since been obfuscated to avoid any copyright issues.
		JCas jCas = AnalysisEngineFactory.process(engine,
				"Wwwwwwww ii ss yyy mmmmm ttttt yyy hhhh " + "Yyy hhhh nnnnnnn tt llll "
				+ "Ttttttt eeeee dddd aaa llllll ttttt " + "Tttt rrrr llll a ffff.");
		FSIndex fsIndex = jCas.getAnnotationIndex(Token.type);

		assertTrue(jCas.getTypeSystem().subsumes(jCas.getTypeSystem().getType("uima.tcas.Annotation"),
				jCas.getCasType(POSTag.type)));

		Token targetToken = new Token(jCas);
		targetToken.setBegin(0);
		targetToken.setEnd(8);
		Token token1 = (Token) fsIndex.find(targetToken);

		TypePathExtractor typePathExtractor = new TypePathExtractor(Token.class, "lemma/value");
		List<Feature> features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 1);
		TypePathFeature feature = (TypePathFeature) (TypePathFeature) features.get(0);
		assertEquals(feature.getValue().toString(), "wwwwwwww");
		assertEquals(feature.getTypePath(), "lemma/value");

		// test the covered text of the last member of a path (lemma is not a
		// primitive value);
		typePathExtractor = new TypePathExtractor(Token.class, "lemma");
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 1);
		feature = (TypePathFeature) features.get(0);
		assertEquals(feature.getValue().toString(), "www");
		assertEquals(feature.getTypePath(), "lemma");

		targetToken.setBegin(12);
		targetToken.setEnd(14);
		Token token3 = (Token) fsIndex.find(targetToken);

		typePathExtractor = new TypePathExtractor(Token.class, "lemma/begin");
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 1);
		feature = (TypePathFeature) features.get(0);
		assertEquals(feature.getValue(), 1);
		assertEquals(feature.getTypePath(), "lemma/begin");
		
		typePathExtractor = new TypePathExtractor(Token.class, "posTag", true, true, false);
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 2);
		assertEquals(features.get(0).getValue().toString(), "Wwwwwwww");
		assertEquals(features.get(1).getValue().toString(), "Wwwwwwww");

		typePathExtractor = new TypePathExtractor(Token.class, "posTag", true, true, true);
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 1);
		assertEquals(features.get(0).getValue().toString(), "Wwwwwwww");

		typePathExtractor = new TypePathExtractor(Token.class, "posTag", true, false, false);
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 1);
		assertEquals(features.get(0).getValue().toString(), "Wwwwwwww");

		typePathExtractor = new TypePathExtractor(Token.class, "posTag", false, false, false);
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 1);
		assertEquals(features.get(0).getValue().toString(), "Wwwwwwww");

		typePathExtractor = new TypePathExtractor(Token.class, "lemma/testFS", false, false, false);
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 1);
		assertEquals(features.get(0).getValue().toString(), "A");

		typePathExtractor = new TypePathExtractor(Token.class, "lemma/testFS", false, true, false);
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 3);
		assertEquals(features.get(0).getValue().toString(), "A");
		assertEquals(features.get(1).getValue().toString(), "B");
		assertEquals(features.get(2).getValue().toString(), "A");

		typePathExtractor = new TypePathExtractor(Token.class, "lemma/testFS", false, true, true);
		features = typePathExtractor.extract(jCas, token1);
		assertEquals(features.size(), 2);
		assertEquals(features.get(0).getValue().toString(), "A");
		assertEquals(features.get(1).getValue().toString(), "B");

		typePathExtractor = new TypePathExtractor(Token.class, "depRel/head");
		features = typePathExtractor.extract(jCas, token3);
		assertEquals(features.size(), 1);
		feature = (TypePathFeature) features.get(0);
		assertEquals(feature.getValue().toString(), "Wwwwwwww");
		assertEquals(feature.getTypePath(), "depRel/head");

		typePathExtractor = new TypePathExtractor(Token.class, "depRel/head", true, true, true);
		features = typePathExtractor.extract(jCas, token3);
		assertEquals(features.size(), 2);
		feature = (TypePathFeature) features.get(0);
		assertEquals(feature.getValue().toString(), "Wwwwwwww");
		assertEquals(feature.getTypePath(), "depRel/head");
		feature = (TypePathFeature) features.get(1);
		assertEquals(feature.getTypePath(), "depRel/head");
		assertEquals(feature.getValue().toString(), "ii");

		typePathExtractor = new TypePathExtractor(Token.class, "depRel/head/posTag/value", true, true, true);
		features = typePathExtractor.extract(jCas, token3);
		assertEquals(features.size(), 3);
		assertEquals(features.get(0).getValue().toString(), "pos1");
		assertEquals(features.get(1).getValue().toString(), "pos2");
		assertEquals(features.get(2).getValue().toString(), "pos3");

		// unfortunately, because the "value" of POSTag is a single value, this
		// will return all values for every POSTag even though
		// the posTag of Token is an FSArray. (I was hoping this extractor would
		// return two values pos1 and pos3
		typePathExtractor = new TypePathExtractor(Token.class, "depRel/head/posTag/value", true, false, true);
		features = typePathExtractor.extract(jCas, token3);
		assertEquals(features.size(), 3);
		assertEquals(features.get(0).getValue().toString(), "pos1");
		assertEquals(features.get(1).getValue().toString(), "pos2");
		assertEquals(features.get(2).getValue().toString(), "pos3");
		
		typePathExtractor = new TypePathExtractor(Token.class, "depRel/head/posTag/value", false, true, true);
		features = typePathExtractor.extract(jCas, token3);
		assertEquals(features.size(), 1);
		assertEquals(features.get(0).getValue().toString(), "pos1");

		typePathExtractor = new TypePathExtractor(Token.class, "depRel/head/posTag/value", false, false, true);
		features = typePathExtractor.extract(jCas, token3);
		assertEquals(features.size(), 1);
		assertEquals(features.get(0).getValue().toString(), "pos1");
	}

	@Test
	public void testIsValidatePath() throws IOException, UIMAException {
		JCas jCas = JCasFactory.createJCasFromPath("test/desc/JulieTypeSystem.xml"); 
		
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.ResourceEntry.type), "source",
				jCas));
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Header.type),
				"authors/lastName", jCas));
		assertTrue(!TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Header.type),
				"authors/lastNames", jCas));
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Token.type),
				"posTag/language", jCas));
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Token.type), "posTag", jCas));
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Token.type),
				"depRel/head/depRel/projective", jCas));
		assertTrue(!TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Token.type),
				"depRel/head/projective", jCas));
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Token.type),
				"depRel/head/orthogr", jCas));
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Token.type),
				"depRel/projective", jCas));
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Token.type),
				"depRel/head/depRel/projective", jCas));
		assertTrue(TypePathExtractor.isValidPath(jCas.getCasType(de.julielab.jules.types.Token.type),
				"depRel/head/posTag/tagsetId", jCas));
	}

	@Test
	public void testIsValidType() throws IOException, UIMAException {
		JCas jCas = JCasFactory.createJCasFromPath("test/desc/JulieTypeSystem.xml"); 

		assertTrue(TypePathExtractor.isValidType(jCas.getCasType(de.julielab.jules.types.ResourceEntry.type), jCas
				.getTypeSystem()));
		assertTrue(TypePathExtractor.isValidType(jCas.getCasType(de.julielab.jules.types.Annotation.type), jCas
				.getTypeSystem()));

		// subtypes of uima.cas.String do not have JCas class generated for them
		assertTrue(TypePathExtractor.isValidType(jCas.getTypeSystem().getType("de.julielab.jules.types.Language"), jCas
				.getTypeSystem()));
		assertTrue(TypePathExtractor.isValidType(jCas.getTypeSystem().getType("de.julielab.jules.types.Person"), jCas
				.getTypeSystem()));

		assertTrue(TypePathExtractor.isValidType(jCas.getTypeSystem().getType("uima.cas.String"), jCas.getTypeSystem()));
		assertTrue(TypePathExtractor
				.isValidType(jCas.getTypeSystem().getType("uima.cas.Integer"), jCas.getTypeSystem()));
	}

	@Test
	public void testTicket23() throws IOException, UIMAException {
		JCas jCas = AnalysisEngineFactory.process("org.cleartk.sentence.SentencesAndTokens", "test/data/docs/huckfinn.txt");

		// token "place" in "wide. This place was a tolerable long,");
		org.cleartk.type.Token token = AnnotationRetrieval.get(jCas, org.cleartk.type.Token.class,
				60);

		assertEquals("place", token.getCoveredText());
		token.setPos("A");
		org.cleartk.type.Token tokenL0 = AnnotationRetrieval.get(jCas,
				org.cleartk.type.Token.class, 59);
		tokenL0.setPos("B");

		TypePathExtractor posExtractor = new TypePathExtractor(org.cleartk.type.Token.class, "pos");

		Feature feature = posExtractor.extract(jCas, token).get(0);
		assertEquals("A", feature.getValue().toString());
		assertEquals("TypePath_Pos", feature.getName());
		
		feature = posExtractor.extract(jCas, tokenL0).get(0);
		assertEquals("B", feature.getValue().toString());
		assertEquals("TypePath_Pos", feature.getName());
		
	}

}
