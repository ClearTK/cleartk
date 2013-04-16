/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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
package org.cleartk.srl.conll2005;

import java.io.File;

import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.classifier.jar.JarClassifierBuilder;
import org.cleartk.srl.ArgumentClassifier;
import org.cleartk.srl.ArgumentIdentifier;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philipp Wetzler
 * @deprecated This code is no longer maintained
 */
@Deprecated
public class TestConll2005Models {

  private static final File conll2005File = new File("../../ClearTK-data/data/conll2005/devset");

  private static final File argumentIdentificationModel = JarClassifierBuilder.getModelJarFile("scratch/CoNLL2005/argumentIdentification");

  private static final File argumentClassificationModel = JarClassifierBuilder.getModelJarFile("scratch/CoNLL2005/argumentClassification");

  private static final File outputFile = new File("scratch/CoNLL2005/results/props-devset");

  public static void main(String[] args) throws Exception {
    outputFile.getParentFile().mkdirs();

    SimplePipeline.runPipeline(
        Conll2005GoldReader.getCollectionReader(conll2005File.toString()),
        AnalysisEngineFactory.createPrimitiveDescription(
            Conll2005GoldAnnotator.class,
            Conll2005GoldAnnotator.PARAM_HAS_VERB_SENSES,
            false),
        DefaultSnowballStemmer.getDescription("English"),
        // CleartkComponents.createCleartkAnnotator(
        // PredicateAnnotator.class,
        // predicateIdentificationModel.toString()),
        AnalysisEngineFactory.createPrimitiveDescription(
            ArgumentIdentifier.class,
            GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
            argumentIdentificationModel.toString()),
        AnalysisEngineFactory.createPrimitiveDescription(
            ArgumentClassifier.class,
            GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
            argumentClassificationModel.toString()),
        AnalysisEngineFactory.createPrimitiveDescription(
            Conll2005Writer.class,
            Conll2005Writer.PARAM_OUTPUT_FILE,
            outputFile.toString()));
  }

}
