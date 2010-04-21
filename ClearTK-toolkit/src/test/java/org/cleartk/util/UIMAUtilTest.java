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
package org.cleartk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.type.Token;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.util.JCasAnnotatorAdapter;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 */
public class UIMAUtilTest {

	@Test
	public void testToList() {
		
		List<Token> tokens = UIMAUtil.toList(null, Token.class);
		assertEquals(0, tokens.size());
		
	}
	
	@Test
	public void testToFSArray() throws ResourceInitializationException {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				JCasAnnotatorAdapter.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
		JCas jCas = engine.newJCas();

		FSArray tokens = UIMAUtil.toFSArray(jCas, null); 
		assertEquals(0, tokens.size());
		
	}
	
	@Test
	public void testGetDefaultingConfigParamValue() throws Exception{
		UimaContext context = UimaContextFactory.createUimaContext(FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES, new String[] {""});
		
		String[] stringArray = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context, FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES, null);
		assertNull(stringArray);

		context = UimaContextFactory.createUimaContext(FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES, new String[0]);
		stringArray = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context, FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES, null);
		assertNull(stringArray);

		context = UimaContextFactory.createUimaContext(FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES, new String[] {"asdf"});
		stringArray = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context, FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES, null);
		assertEquals(1, stringArray.length);
		assertEquals("asdf", stringArray[0]);

		context = UimaContextFactory.createUimaContext();
		stringArray = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context, FilesCollectionReader.PARAM_NAME_FILES_FILE_NAMES, null);
		assertNull(stringArray);

	}


	public class TestAnnotator extends CleartkAnnotator<String> {
		public boolean initialized = false;
		@Override
		public void initialize(UimaContext context) throws ResourceInitializationException {
			this.initialized = true;
		}
		@Override
		public void process(JCas aJCas) throws AnalysisEngineProcessException {}
	}

	public static class TestAnnotator2 extends CleartkAnnotator<String>{
		public boolean initialized = false;
		@Override
		public void initialize(UimaContext context) throws ResourceInitializationException {
			this.initialized = true;
		}
		@Override
		public void process(JCas aJCas) throws AnalysisEngineProcessException {}
	}

	@Test
	public void testCreate() throws ResourceInitializationException {
		UimaContext context = UimaContextFactory.createUimaContext("StringParam", "java.lang.String");
		String createdString = UIMAUtil.create(context, "StringParam", String.class);
		assertNotNull(createdString);
		
		String paramName = "AnnotatorParam";
		context = UimaContextFactory.createUimaContext(paramName, TestAnnotator.class.getName());
		CleartkAnnotator<?> annotator = UIMAUtil.create(context, paramName, CleartkAnnotator.class);
		assertNotNull(annotator);
		assertTrue(annotator instanceof TestAnnotator);
		assertTrue(((TestAnnotator)annotator).initialized);

		context = UimaContextFactory.createUimaContext(paramName, TestAnnotator2.class.getName());
		annotator = UIMAUtil.create(context, paramName, CleartkAnnotator.class);
		assertNotNull(annotator);
		assertTrue(annotator instanceof TestAnnotator2);
		assertTrue(((TestAnnotator2)annotator).initialized);

		context = UimaContextFactory.createUimaContext(paramName, TestAnnotator2.class.getName());
		annotator = UIMAUtil.create(context, paramName, CleartkAnnotator.class);
		assertNotNull(annotator);
		assertTrue(annotator instanceof TestAnnotator2);
		assertTrue(((TestAnnotator2)annotator).initialized);
	}
}
