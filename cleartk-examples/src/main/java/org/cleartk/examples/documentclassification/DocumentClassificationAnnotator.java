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
package org.cleartk.examples.documentclassification;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.simple.BagExtractor;
import org.cleartk.classifier.feature.extractor.simple.CountsExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.token.type.Token;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philipp G. Wetzler
 * 
 */
@Deprecated
public class DocumentClassificationAnnotator extends CleartkAnnotator<String> {

  public static final String PREDICTION_VIEW_NAME = "ExampleDocumentClassificationPredictionView";

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    SimpleFeatureExtractor subExtractor = new TypePathExtractor(
        Token.class,
        "stem",
        false,
        false,
        true);
    extractor = new CountsExtractor(new BagExtractor(Token.class, subExtractor));
  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();

    Instance<String> instance = new Instance<String>();
    instance.addAll(extractor.extract(jCas, doc));

    try {
      if (isTraining()) {
        JCas goldView = jCas.getView(GoldAnnotator.GOLD_VIEW_NAME);
        instance.setOutcome(goldView.getSofaDataString());
        this.dataWriter.write(instance);
      } else {
        String result = this.classifier.classify(instance.getFeatures());
        JCas predictionView = jCas.createView(PREDICTION_VIEW_NAME);
        predictionView.setSofaDataString(result, "text/plain");
        System.out.println("classified " + ViewURIUtil.getURI(jCas) + " as " + result + ".");
      }
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  private CountsExtractor extractor;

  public static AnalysisEngineDescription getClassifierDescription(File classifierJarFile)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        DocumentClassificationAnnotator.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        classifierJarFile.toString());
  }

}
