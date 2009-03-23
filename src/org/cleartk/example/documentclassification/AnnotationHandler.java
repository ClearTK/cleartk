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
package org.cleartk.example.documentclassification;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.extractor.CountsExtractor;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.type.Token;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philipp G. Wetzler
 *
 */
public class AnnotationHandler implements org.cleartk.classifier.AnnotationHandler<String> {

	public static final String PREDICTION_VIEW_NAME = "ExampleDocumentClassificationPredictionView";

	public void initialize(UimaContext context) throws ResourceInitializationException {
		SimpleFeatureExtractor subExtractor = new TypePathExtractor(Token.class, "stem", false, false, true);
		extractor = new CountsExtractor(Token.class, subExtractor);
	}

	public void process(JCas jCas, InstanceConsumer<String> consumer) throws AnalysisEngineProcessException {
		try {
			DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();

			Instance<String> instance = new Instance<String>();
			instance.addAll(extractor.extract(jCas, doc));

			if( consumer.expectsOutcomes() ) {
				JCas goldView = jCas.getView(GoldAnnotator.GOLD_VIEW_NAME);
				instance.setOutcome(goldView.getSofaDataString());
			}

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
