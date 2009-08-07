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
package org.cleartk.classifier.feature.proliferate;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.SpannedTextExtractor;
import org.cleartk.type.test.Token;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.JCasAnnotatorAdapter;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public class FeatureProliferatorTest {

	private void testOne(FeatureProliferator proliferator, String origName, String origValue, String newName,
			String newValue) {
		Feature origFeature = new Feature(origName, origValue);
		List<Feature> newFeatures = proliferator.proliferate(origFeature);
		String value = newFeatures.size() == 0 ? null : newFeatures.get(0).getValue().toString();
		String name = newFeatures.size() == 0 ? null : newFeatures.get(0).getName();
		Assert.assertTrue(newFeatures.size() >= 0 && newFeatures.size() <= 1);
		Assert.assertEquals(newValue, value);
		Assert.assertEquals(newName, name);
	}

	@Test
	public void testLowerCaseProliferator() throws UIMAException, IOException {
		this.testOne(new LowerCaseProliferator(), null, "HI", "LowerCase", "hi");
		this.testOne(new LowerCaseProliferator("TestName"), "OrigName", "", "TestName_OrigName", "");
	}

	@Test
	public void testCapitalTypeProliferator() throws UIMAException, IOException {
		FeatureProliferator caps = new CapitalTypeProliferator();
		FeatureProliferator foo = new CapitalTypeProliferator("Foo");

		this.testOne(caps, null, "HI", "CapitalType", CapitalTypeProliferator.ALL_UPPERCASE);
		this.testOne(caps, "OrigName", "hi", "CapitalType_OrigName", CapitalTypeProliferator.ALL_LOWERCASE);
		this.testOne(foo, null, "hi", "Foo", CapitalTypeProliferator.ALL_LOWERCASE);
		this.testOne(foo, "OrigName", "Hi", "Foo_OrigName", CapitalTypeProliferator.INITIAL_UPPERCASE);
		this.testOne(caps, null, "HigH", "CapitalType", CapitalTypeProliferator.MIXED_CASE);
		this.testOne(caps, "OrigName", "!234", null, null);
		this.testOne(foo, "OrigName", "!@#a@#\\$", "Foo_OrigName", CapitalTypeProliferator.ALL_LOWERCASE);
		this.testOne(foo, null, "\t\n", null, null);

	}

	@Test
	public void testNumericTypeProliferator() throws UIMAException, IOException {
		FeatureProliferator nums = new NumericTypeProliferator();
		FeatureProliferator bar = new NumericTypeProliferator("BAR");

		this.testOne(nums, null, "HI", null, null);
		this.testOne(nums, "OrigName", "", null, null);
		this.testOne(bar, null, "\t\t", null, null);
		this.testOne(bar, "OrigName", "HI2", "BAR_OrigName", NumericTypeProliferator.ALPHANUMERIC);
		this.testOne(nums, null, "222", "NumericType", NumericTypeProliferator.DIGITS);
		this.testOne(nums, "OrigName", "2222", "NumericType_OrigName", NumericTypeProliferator.DIGITS);
		this.testOne(bar, null, "2122", "BAR", NumericTypeProliferator.YEAR_DIGITS);
		this.testOne(bar, "OrigName", "2022", "BAR_OrigName", NumericTypeProliferator.YEAR_DIGITS);
		this.testOne(nums, null, "1022", "NumericType", NumericTypeProliferator.YEAR_DIGITS);
		this.testOne(nums, "OrigName", "0022", "NumericType_OrigName", NumericTypeProliferator.DIGITS);
		this.testOne(bar, null, "0", "BAR", NumericTypeProliferator.DIGITS);
		this.testOne(bar, "OrigName", "asdfASDF1234", "BAR_OrigName", NumericTypeProliferator.ALPHANUMERIC);
		this.testOne(nums, null, "1F1234", "NumericType", NumericTypeProliferator.ALPHANUMERIC);
		this.testOne(nums, "OrigName", "10-1234", "NumericType_OrigName", NumericTypeProliferator.SOME_DIGITS);
		this.testOne(bar, null, "1F1234!", "BAR", NumericTypeProliferator.SOME_DIGITS);
		this.testOne(bar, "OrigName", "!!12!", "BAR_OrigName", NumericTypeProliferator.SOME_DIGITS);
		this.testOne(nums, null, "10,000", "NumericType", NumericTypeProliferator.SOME_DIGITS);
	}

	@Test
	public void testCharacterNGramProliferator() throws UIMAException, IOException {
		FeatureProliferator triSuff = new CharacterNGramProliferator(CharacterNGramProliferator.RIGHT_TO_LEFT, 0, 3, 7,
				false);
		this.testOne(triSuff, "OrigName", "emotion", "NGram_Right_0_3_7_OrigName", "ion");
		this.testOne(triSuff, null, "motion", null, null);
		this.testOne(triSuff, "OrigName", "locomotive", "NGram_Right_0_3_7_OrigName", "ive");

		FeatureProliferator triPre = new CharacterNGramProliferator("TriPre", CharacterNGramProliferator.LEFT_TO_RIGHT,
				0, 3, 3, false);
		this.testOne(triPre, "OrigName", "LOCOMOTIVE", "NGram_Left_0_3_3_TriPre_OrigName", "LOC");
		this.testOne(triPre, null, "LOC", "NGram_Left_0_3_3_TriPre", "LOC");
		this.testOne(triPre, "OrigName", "lo", null, null);

		FeatureProliferator left12 = new CharacterNGramProliferator(CharacterNGramProliferator.LEFT_TO_RIGHT, 1, 3, 8,
				false);
		this.testOne(left12, null, "locomotive", "NGram_Left_1_3_8", "oc");

		FeatureProliferator left5 = new CharacterNGramProliferator("FooBar", CharacterNGramProliferator.LEFT_TO_RIGHT,
				5, 6, 8, false);
		this.testOne(left5, "OrigName", "abcdefghi", "NGram_Left_5_6_8_FooBar_OrigName", "f");

		FeatureProliferator right46lower = new CharacterNGramProliferator(CharacterNGramProliferator.RIGHT_TO_LEFT, 2,
				4, 6, true);
		this.testOne(right46lower, null, "abcdefghi", "NGram_Right_2_4_6_lower", "fg");

		FeatureProliferator right3 = new CharacterNGramProliferator(CharacterNGramProliferator.RIGHT_TO_LEFT, 0, 3);
		this.testOne(right3, null, "Foo", "NGram_Right_0_3_3", "Foo");
		this.testOne(right3, null, "Fo", null, null);
		this.testOne(right3, "OrigName", "Food", "NGram_Right_0_3_3_OrigName", "ood");
	}

	@Test
	public void testProliferatingExtractor() throws UIMAException, IOException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				JCasAnnotatorAdapter.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TestTypeSystem"));
		JCas jCas = engine.newJCas();

		jCas.setDocumentText("Hello World 2008!");
		Token hello = new Token(jCas, 0, 5);
		Token year = new Token(jCas, 12, 16);

		SimpleFeatureExtractor textAndLower = new ProliferatingExtractor(new SpannedTextExtractor(),
				new LowerCaseProliferator());
		List<Feature> features = textAndLower.extract(jCas, hello);
		Assert.assertEquals(2, features.size());
		Assert.assertEquals("SpannedText", features.get(0).getName());
		Assert.assertEquals("Hello", features.get(0).getValue());
		Assert.assertEquals("LowerCase_SpannedText", features.get(1).getName());
		Assert.assertEquals("hello", features.get(1).getValue());

		String yearDigits = NumericTypeProliferator.YEAR_DIGITS;
		SimpleFeatureExtractor textAndCapsAndNums = new ProliferatingExtractor(new SpannedTextExtractor("SpannedText"),
				new CapitalTypeProliferator(), new NumericTypeProliferator());
		features = textAndCapsAndNums.extract(jCas, year);
		Assert.assertEquals(2, features.size());
		Assert.assertEquals("SpannedText", features.get(0).getName());
		Assert.assertEquals("2008", features.get(0).getValue());
		Assert.assertEquals("NumericType_SpannedText", features.get(1).getName());
		Assert.assertEquals(yearDigits, features.get(1).getValue());

		String initialUpper = CapitalTypeProliferator.INITIAL_UPPERCASE;
		SimpleFeatureExtractor textAndCapsAndLower = new ProliferatingExtractor(
				new SpannedTextExtractor("SpannedText"), new CapitalTypeProliferator(), new LowerCaseProliferator());
		features = textAndCapsAndLower.extract(jCas, hello);
		Assert.assertEquals(3, features.size());
		Assert.assertEquals("SpannedText", features.get(0).getName());
		Assert.assertEquals("Hello", features.get(0).getValue());
		Assert.assertEquals("CapitalType_SpannedText", features.get(1).getName());
		Assert.assertEquals(initialUpper, features.get(1).getValue());
		Assert.assertEquals("LowerCase_SpannedText", features.get(2).getName());
		Assert.assertEquals("hello", features.get(2).getValue());
	}

	@Test
	public void testDeprecatedLowerCaseProliferator() throws UIMAException, IOException {
		Feature feature = new Feature("OrigName", "HI");

		FeatureProliferator proliferator = new LowerCaseProliferator("NewName");
		List<Feature> lowerCaseFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, lowerCaseFeatures.size());
		Assert.assertEquals("hi", lowerCaseFeatures.get(0).getValue());
		Assert.assertEquals("NewName_OrigName", lowerCaseFeatures.get(0).getName());

		feature.setValue("");
		lowerCaseFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, lowerCaseFeatures.size());
		Assert.assertEquals("", lowerCaseFeatures.get(0).getValue());
		Assert.assertEquals("NewName_OrigName", lowerCaseFeatures.get(0).getName());
	}

	@Test
	public void testDeprecatedCapitalTypeProliferator() throws UIMAException, IOException {
		FeatureProliferator proliferator = new CapitalTypeProliferator("NewName");
		Feature feature = new Feature("OrigName", "HI");
		List<Feature> caseTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, caseTypeFeatures.size());
		Assert.assertEquals(CapitalTypeProliferator.ALL_UPPERCASE, caseTypeFeatures.get(0).getValue());
		Assert.assertEquals("NewName_OrigName", caseTypeFeatures.get(0).getName());

		feature.setValue("hi");
		caseTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, caseTypeFeatures.size());
		Assert.assertEquals(CapitalTypeProliferator.ALL_LOWERCASE, caseTypeFeatures.get(0).getValue());

		feature.setValue("Hi");
		caseTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, caseTypeFeatures.size());
		Assert.assertEquals(CapitalTypeProliferator.INITIAL_UPPERCASE, caseTypeFeatures.get(0).getValue());

		feature.setValue("HigH");
		caseTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, caseTypeFeatures.size());
		Assert.assertEquals(CapitalTypeProliferator.MIXED_CASE, caseTypeFeatures.get(0).getValue());

		feature.setValue("!234");
		caseTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(0, caseTypeFeatures.size());

		feature.setValue("!@#a@#\\$");
		caseTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, caseTypeFeatures.size());
		Assert.assertEquals(CapitalTypeProliferator.ALL_LOWERCASE, caseTypeFeatures.get(0).getValue());

		feature.setValue("\t\n");
		caseTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(0, caseTypeFeatures.size());

	}

	@Test
	public void testDeprecatedNumericTypeProliferator() throws UIMAException, IOException {
		FeatureProliferator proliferator = new NumericTypeProliferator("NumericName");

		Feature feature = new Feature("OrigName", "HI");
		List<Feature> numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(0, numericTypeFeatures.size());

		feature.setValue("");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(0, numericTypeFeatures.size());

		feature.setValue("\t\t");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(0, numericTypeFeatures.size());

		feature.setValue("HI2");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, numericTypeFeatures.size());
		Assert.assertEquals(NumericTypeProliferator.ALPHANUMERIC, numericTypeFeatures.get(0).getValue());
		Assert.assertEquals("NumericName_OrigName", numericTypeFeatures.get(0).getName());

		feature.setValue("222");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("2222");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("2122");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.YEAR_DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("2022");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.YEAR_DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("1022");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.YEAR_DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("0022");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("0");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("asdfASDF1234");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.ALPHANUMERIC, numericTypeFeatures.get(0).getValue());

		feature.setValue("1F1234");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.ALPHANUMERIC, numericTypeFeatures.get(0).getValue());

		feature.setValue("10-1234");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.SOME_DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("1F1234!");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.SOME_DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("!!12!");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.SOME_DIGITS, numericTypeFeatures.get(0).getValue());

		feature.setValue("10,000");
		numericTypeFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(NumericTypeProliferator.SOME_DIGITS, numericTypeFeatures.get(0).getValue());
	}

	@Test
	public void testDeprecatedCharacterNGramProliferator() throws UIMAException, IOException {
		FeatureProliferator proliferator = new CharacterNGramProliferator( "CharNGram", CharacterNGramProliferator.RIGHT_TO_LEFT, 0,
				3, 7, false);

		Feature feature = new Feature("OrigName", "emotion");
		List<Feature> ngramFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(1, ngramFeatures.size());
		Assert.assertEquals("ion", ngramFeatures.get(0).getValue());

		feature = new Feature("OrigName", "motion");
		ngramFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(0, ngramFeatures.size());

		feature = new Feature("OrigName", "locomotive");
		ngramFeatures = proliferator.proliferate(feature);
		Assert.assertEquals("ive", ngramFeatures.get(0).getValue());

		proliferator = new CharacterNGramProliferator(CharacterNGramProliferator.LEFT_TO_RIGHT, 0, 3, 3, false);
		feature = new Feature("OrigName", "locomotive");
		ngramFeatures = proliferator.proliferate(feature);
		Assert.assertEquals("loc", ngramFeatures.get(0).getValue());

		feature = new Feature("OrigName", "loc");
		ngramFeatures = proliferator.proliferate(feature);
		Assert.assertEquals("loc", ngramFeatures.get(0).getValue());

		feature = new Feature("OrigName", "lo");
		ngramFeatures = proliferator.proliferate(feature);
		Assert.assertEquals(0, ngramFeatures.size());

		proliferator = new CharacterNGramProliferator(CharacterNGramProliferator.LEFT_TO_RIGHT, 1, 3, 8, false);
		feature = new Feature("OrigName", "locomotive");
		ngramFeatures = proliferator.proliferate(feature);
		Assert.assertEquals("oc", ngramFeatures.get(0).getValue());

		proliferator = new CharacterNGramProliferator(CharacterNGramProliferator.LEFT_TO_RIGHT, 5, 6, 8, false);
		feature = new Feature("OrigName", "abcdefghi");
		ngramFeatures = proliferator.proliferate(feature);
		Assert.assertEquals("f", ngramFeatures.get(0).getValue());

	}
}
