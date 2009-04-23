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

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.type.Token;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.factory.UimaContextFactory;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 */
public class UIMAUtilTests {

	@Test
	public void testToList() {
		
		List<Token> tokens = UIMAUtil.toList(null, Token.class);
		assertEquals(0, tokens.size());
		
	}
	
	@Test
	public void testToFSArray() throws ResourceInitializationException {
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				EmptyAnnotator.class,
				TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
		JCas jCas = engine.newJCas();

		FSArray tokens = UIMAUtil.toFSArray(jCas, null); 
		assertEquals(0, tokens.size());
		
	}
	
	@Test
	public void testGetDefaultingConfigParamValue() throws Exception{
		UimaContext context = UimaContextFactory.createUimaContext(FilesCollectionReader.PARAM_FILE_NAMES_FILES, new String[] {""});
		
		String[] stringArray = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context, FilesCollectionReader.PARAM_FILE_NAMES_FILES, null);
		assertNull(stringArray);

		context = UimaContextFactory.createUimaContext(FilesCollectionReader.PARAM_FILE_NAMES_FILES, new String[0]);
		stringArray = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context, FilesCollectionReader.PARAM_FILE_NAMES_FILES, null);
		assertNull(stringArray);

		context = UimaContextFactory.createUimaContext(FilesCollectionReader.PARAM_FILE_NAMES_FILES, new String[] {"asdf"});
		stringArray = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context, FilesCollectionReader.PARAM_FILE_NAMES_FILES, null);
		assertEquals(1, stringArray.length);
		assertEquals("asdf", stringArray[0]);

		context = UimaContextFactory.createUimaContext();
		stringArray = (String[]) UIMAUtil.getDefaultingConfigParameterValue(context, FilesCollectionReader.PARAM_FILE_NAMES_FILES, null);
		assertNull(stringArray);

	}


	public class TestHandler implements AnnotationHandler<String>{

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException {
		}
		
	}

	public static class TestHandler2 implements AnnotationHandler<String>{

		public void process(JCas cas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException {
		}
		
	}

	@Test
	public void testCreate() throws ResourceInitializationException {
		UimaContext context = UimaContextFactory.createUimaContext("StringParam", "java.lang.String");
		String createdString = UIMAUtil.create(context, "StringParam", String.class);
		assertNotNull(createdString);
		
		context = UimaContextFactory.createUimaContext(InstanceConsumer.PARAM_ANNOTATION_HANDLER, TestHandler.class.getName());
		AnnotationHandler<?> annotationHandler = UIMAUtil.create(context, InstanceConsumer.PARAM_ANNOTATION_HANDLER, AnnotationHandler.class);
		assertNotNull(annotationHandler);

		context = UimaContextFactory.createUimaContext(InstanceConsumer.PARAM_ANNOTATION_HANDLER, "org.cleartk.util.UIMAUtilTests$TestHandler");
		annotationHandler = UIMAUtil.create(context, InstanceConsumer.PARAM_ANNOTATION_HANDLER, AnnotationHandler.class);
		assertNotNull(annotationHandler);

		context = UimaContextFactory.createUimaContext(InstanceConsumer.PARAM_ANNOTATION_HANDLER, "org.cleartk.util.UIMAUtilTests$TestHandler2");
		annotationHandler = UIMAUtil.create(context, InstanceConsumer.PARAM_ANNOTATION_HANDLER, AnnotationHandler.class);
		assertNotNull(annotationHandler);

	}
}
