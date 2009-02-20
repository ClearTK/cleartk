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
import org.cleartk.type.Document;
import org.cleartk.type.Token;
import org.cleartk.util.EmptyAnnotator;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 * @author Philip Ogren
 */
public class FeatureProliferatorTests {

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
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				EmptyAnnotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription(Document.class, Token.class));
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
	@SuppressWarnings("deprecation")
	public void testDeprecatedLowerCaseProliferator() throws UIMAException, IOException {
		Feature feature = new Feature("OrigName", "HI");

		FeatureProliferator proliferator = new LowerCaseProliferator();
		Feature lowerCaseFeature = proliferator.proliferate(feature, "NewName");
		Assert.assertEquals("hi", lowerCaseFeature.getValue());
		Assert.assertEquals("NewName_OrigName", lowerCaseFeature.getName());

		feature.setValue("");
		lowerCaseFeature = proliferator.proliferate(feature, "lowercase spanned text extractor");
		Assert.assertEquals("", lowerCaseFeature.getValue());
		Assert.assertEquals("lowercase spanned text extractor_OrigName", lowerCaseFeature.getName());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testDeprecatedCapitalTypeProliferator() throws UIMAException, IOException {
		FeatureProliferator proliferator = new CapitalTypeProliferator();
		Feature feature = new Feature("OrigName", "HI");
		Feature caseTypeFeature = proliferator.proliferate(feature, "NewName");
		Assert.assertEquals(CapitalTypeProliferator.ALL_UPPERCASE, caseTypeFeature.getValue());
		Assert.assertEquals("NewName_OrigName", caseTypeFeature.getName());

		feature.setValue("hi");
		caseTypeFeature = proliferator.proliferate(feature, "NewName");
		Assert.assertEquals(CapitalTypeProliferator.ALL_LOWERCASE, caseTypeFeature.getValue());

		feature.setValue("Hi");
		caseTypeFeature = proliferator.proliferate(feature, "NewName");
		Assert.assertEquals(CapitalTypeProliferator.INITIAL_UPPERCASE, caseTypeFeature.getValue());

		feature.setValue("HigH");
		caseTypeFeature = proliferator.proliferate(feature, "NewName");
		Assert.assertEquals(CapitalTypeProliferator.MIXED_CASE, caseTypeFeature.getValue());

		feature.setValue("!234");
		caseTypeFeature = proliferator.proliferate(feature, "NewName");
		Assert.assertEquals(null, caseTypeFeature);

		feature.setValue("!@#a@#\\$");
		caseTypeFeature = proliferator.proliferate(feature, "NewName");
		Assert.assertEquals(CapitalTypeProliferator.ALL_LOWERCASE, caseTypeFeature.getValue());

		feature.setValue("\t\n");
		caseTypeFeature = proliferator.proliferate(feature, "NewName");
		Assert.assertEquals(null, caseTypeFeature);

	}

	@Test
	@SuppressWarnings("deprecation")
	public void testDeprecatedNumericTypeProliferator() throws UIMAException, IOException {
		FeatureProliferator proliferator = new NumericTypeProliferator();

		Feature feature = new Feature("OrigName", "HI");
		Feature numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(null, numericTypeFeature);

		feature.setValue("");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(null, numericTypeFeature);

		feature.setValue("\t\t");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(null, numericTypeFeature);

		feature.setValue("HI2");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.ALPHANUMERIC, numericTypeFeature.getValue());
		Assert.assertEquals("NumericName_OrigName", numericTypeFeature.getName());

		feature.setValue("222");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.DIGITS, numericTypeFeature.getValue());

		feature.setValue("2222");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.DIGITS, numericTypeFeature.getValue());

		feature.setValue("2122");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.YEAR_DIGITS, numericTypeFeature.getValue());

		feature.setValue("2022");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.YEAR_DIGITS, numericTypeFeature.getValue());

		feature.setValue("1022");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.YEAR_DIGITS, numericTypeFeature.getValue());

		feature.setValue("0022");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.DIGITS, numericTypeFeature.getValue());

		feature.setValue("0");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.DIGITS, numericTypeFeature.getValue());

		feature.setValue("asdfASDF1234");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.ALPHANUMERIC, numericTypeFeature.getValue());

		feature.setValue("1F1234");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.ALPHANUMERIC, numericTypeFeature.getValue());

		feature.setValue("10-1234");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.SOME_DIGITS, numericTypeFeature.getValue());

		feature.setValue("1F1234!");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.SOME_DIGITS, numericTypeFeature.getValue());

		feature.setValue("!!12!");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.SOME_DIGITS, numericTypeFeature.getValue());

		feature.setValue("10,000");
		numericTypeFeature = proliferator.proliferate(feature, "NumericName");
		Assert.assertEquals(NumericTypeProliferator.SOME_DIGITS, numericTypeFeature.getValue());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testDeprecatedCharacterNGramProliferator() throws UIMAException, IOException {
		FeatureProliferator proliferator = new CharacterNGramProliferator(CharacterNGramProliferator.RIGHT_TO_LEFT, 0,
				3, 7, false);

		Feature feature = new Feature("OrigName", "emotion");
		Feature ngramFeature = proliferator.proliferate(feature, "CharNGram");
		Assert.assertEquals("ion", ngramFeature.getValue());

		feature = new Feature("OrigName", "motion");
		ngramFeature = proliferator.proliferate(feature, "CharNGram");
		Assert.assertEquals(null, ngramFeature);

		feature = new Feature("OrigName", "locomotive");
		ngramFeature = proliferator.proliferate(feature, "CharNGram");
		Assert.assertEquals("ive", ngramFeature.getValue());

		proliferator = new CharacterNGramProliferator(CharacterNGramProliferator.LEFT_TO_RIGHT, 0, 3, 3, false);
		feature = new Feature("OrigName", "locomotive");
		ngramFeature = proliferator.proliferate(feature, "CharNGram");
		Assert.assertEquals("loc", ngramFeature.getValue());

		feature = new Feature("OrigName", "loc");
		ngramFeature = proliferator.proliferate(feature, "CharNGram");
		Assert.assertEquals("loc", ngramFeature.getValue());

		feature = new Feature("OrigName", "lo");
		ngramFeature = proliferator.proliferate(feature, "CharNGram");
		Assert.assertEquals(null, ngramFeature);

		proliferator = new CharacterNGramProliferator(CharacterNGramProliferator.LEFT_TO_RIGHT, 1, 3, 8, false);
		feature = new Feature("OrigName", "locomotive");
		ngramFeature = proliferator.proliferate(feature, "CharNGram");
		Assert.assertEquals("oc", ngramFeature.getValue());

		proliferator = new CharacterNGramProliferator(CharacterNGramProliferator.LEFT_TO_RIGHT, 5, 6, 8, false);
		feature = new Feature("OrigName", "abcdefghi");
		ngramFeature = proliferator.proliferate(feature, "CharNGram");
		Assert.assertEquals("f", ngramFeature.getValue());

	}
}
