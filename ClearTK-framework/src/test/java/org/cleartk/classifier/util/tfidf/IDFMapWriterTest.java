/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier.util.tfidf;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.CleartkException;
import org.cleartk.Initializable;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.extractor.CountsExtractor;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.type.test.Token;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.TearDownUtil;
/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
 *
 */

public class IDFMapWriterTest {

	private File outputDirectory = new File("test/data/documentclassification");
	
	@Before
	public void setUp() {
		if(!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
	}

	@After
	public void tearDown() {
		TearDownUtil.removeDirectory(outputDirectory);
		Assert.assertFalse(outputDirectory.exists());
	}

	
	@Test
	public void testDescriptor() throws Exception {
		TypeSystemDescription typeSystemDescription = 
			TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TestTypeSystem");
		try {
			AnalysisEngineFactory.createPrimitive(
					IDFMapWriter.class, 
					typeSystemDescription);
			Assert.fail("Expected exception with no value for "+IDFMapWriter.PARAM_IDFMAP_FILE+" specified");
		} catch (ResourceInitializationException e) {}
		
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
				IDFMapWriter.class, typeSystemDescription,
				IDFMapWriter.PARAM_IDFMAP_FILE, new File(outputDirectory, "idfmap").getPath(),
				IDFMapWriter.PARAM_ANNOTATION_HANDLER, AnnotationHandler.class.getName());
		String fileName = (String)engine.getConfigParameterValue(
				IDFMapWriter.PARAM_IDFMAP_FILE);
		Assert.assertEquals(new File(outputDirectory, "idfmap").getPath(), fileName);
	}
	
	public static class AnnotationHandler implements org.cleartk.classifier.AnnotationHandler<String> , Initializable{

		public static final String PREDICTION_VIEW_NAME = "ExampleDocumentClassificationPredictionView";

		public void initialize(UimaContext context) throws ResourceInitializationException {
			SimpleFeatureExtractor subExtractor = new TypePathExtractor(Token.class, "stem", false, false, true);
			extractor = new CountsExtractor(Token.class, subExtractor);
		}

		public void process(JCas jCas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException, CleartkException {
			try {
				DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();

				Instance<String> instance = new Instance<String>();
				instance.addAll(extractor.extract(jCas, doc));

				String result = consumer.consume(instance);
				
				if( result != null ) {
					JCas predictionView = jCas.createView(PREDICTION_VIEW_NAME);
					predictionView.setSofaDataString(result, "text/plain");
				}
			} catch (CASException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}

		private CountsExtractor extractor;

	}
}
