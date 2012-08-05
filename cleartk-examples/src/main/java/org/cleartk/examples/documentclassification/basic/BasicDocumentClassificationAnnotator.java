/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.examples.documentclassification.basic;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Count;
import org.cleartk.classifier.feature.extractor.CleartkExtractor.Covered;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.examples.type.UsenetDocument;
import org.cleartk.token.type.Token;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * Simple class to demonstrate how to write a new CleartkAnnotator. This class implements a basic
 * document classification annotator whose only features are the counts of the words in the
 * document.
 * 
 * @author Lee Becker
 * 
 */
public class BasicDocumentClassificationAnnotator extends CleartkAnnotator<String> {

  private CombinedExtractor extractor;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // Create an extractor that gives word counts for a document
    this.extractor = new CombinedExtractor(new CleartkExtractor(
        Token.class,
        new CoveredTextExtractor(),
        new Count(new Covered())));
  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();

    Instance<String> instance = new Instance<String>();
    instance.addAll(this.extractor.extract(jCas, doc));

    if (isTraining()) {
      UsenetDocument document = JCasUtil.selectSingle(jCas, UsenetDocument.class);
      instance.setOutcome(document.getCategory());
      this.dataWriter.write(instance);
    } else {
      // This is classification, so classify and create UsenetDocument annotation
      String result = this.classifier.classify(instance.getFeatures());
      UsenetDocument document = new UsenetDocument(jCas, 0, jCas.getDocumentText().length());
      document.setCategory(result);
      document.addToIndexes();
    }
  }

  public static AnalysisEngineDescription getClassifierDescription(File classifierJarFile)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        BasicDocumentClassificationAnnotator.class,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        classifierJarFile.toString());
  }

}
