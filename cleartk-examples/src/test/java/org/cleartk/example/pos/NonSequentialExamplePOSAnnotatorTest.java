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

package org.cleartk.example.pos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.pear.util.FileUtil;
import org.cleartk.classifier.CleartkComponents;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.svmlight.DefaultOVASVMlightDataWriterFactory;
import org.cleartk.example.ExamplesTestBase;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.examples.pos.ExamplePOSPlainTextWriter;
import org.cleartk.syntax.constituent.TreebankGoldAnnotator;
import org.cleartk.syntax.constituent.TreebankConstants;
import org.cleartk.token.breakit.BreakIteratorAnnotatorFactory;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.cr.FilesCollectionReader;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.util.HideOutput;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class NonSequentialExamplePOSAnnotatorTest extends ExamplesTestBase {

  @Test
  public void testLibsvm() throws Exception {
    String libsvmDirectoryName = outputDirectory + "/libsvm";
    AnalysisEngineDescription dataWriter = CleartkComponents.createCleartkAnnotator(
            NonSequentialExamplePOSAnnotator.class, ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
            DefaultMultiClassLIBSVMDataWriterFactory.class, libsvmDirectoryName,
            (List<Class<?>>) null);
    testClassifier(dataWriter, libsvmDirectoryName, "-t", "0");

    String firstLine = FileUtil.loadListOfStrings(new File(libsvmDirectoryName
            + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
    assertTrue(firstLine
            .startsWith("2008/NN Sichuan/NN earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN The/DT 2008/NN Sichuan/NN earthquake/NN occurred/VBN at/IN 14:28/NN :/NN 01.42/NN CST/NN "));
  }

  @Test
  public void testMaxent() throws Exception {
    String maxentDirectoryName = outputDirectoryName + "/maxent";
    AnalysisEngineDescription dataWriter = CleartkComponents.createCleartkAnnotator(
            NonSequentialExamplePOSAnnotator.class, ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
            DefaultMaxentDataWriterFactory.class, maxentDirectoryName, (List<Class<?>>) null);
    testClassifier(dataWriter, maxentDirectoryName);

    String firstLine = FileUtil.loadListOfStrings(new File(maxentDirectoryName
            + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
    assertEquals(
            "2008/PRP Sichuan/MD earthquake/NNS From/IN Wikipedia/NNS ,/, the/DT free/NN encyclopedia/IN The/DT 2008/NN_SPLIT Sichuan/IN earthquake/CC occurred/VBN at/IN 14:28/JJ :/RB 01.42/JJ CST/NN (/-LRB- 06:28/NN :/NNP 01.42/JJ UTC/NN )/-RRB- on/IN 12/CD May/NN 2008/RB ,/, with/IN its/PRP$ epicenter/VBZ in/IN Wenchuan/JJ County/NN (/-LRB- Chinese/NN :/NNP ?/NN ?/NN ?/.",
            firstLine);

  }

  @Test
  public void testSVMLIGHT() throws Exception {
    String svmlightDirectoryName = outputDirectoryName + "/svmlight";
    AnalysisEngineDescription dataWriter = CleartkComponents
            .createCleartkAnnotator(NonSequentialExamplePOSAnnotator.class,
                    ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
                    DefaultOVASVMlightDataWriterFactory.class, svmlightDirectoryName,
                    (List<Class<?>>) null);
    testClassifier(dataWriter, svmlightDirectoryName);

    String firstLine = FileUtil.loadListOfStrings(new File(svmlightDirectoryName
            + "/2008_Sichuan_earthquake.txt.pos"))[0].trim();
    assertTrue(firstLine
            .startsWith("2008/NN Sichuan/NNP earthquake/NN From/IN Wikipedia/NN ,/, the/DT free/NN encyclopedia/IN The/DT 2008/NN Sichuan/NN earthquake/NN occurred/VBN at/IN 14:28/JJ :/NN 01.42/NN CST/NN (/-LRB- 06:28/NN :/NN 01.42/NN UTC/NN )/-RRB- on/IN 12/CD May/MD 2008/NN ,/, with/IN its/NNS epicenter/NN in/IN Wenchuan/JJ County/NN"));
  }

  private void testClassifier(AnalysisEngineDescription dataWriter, String outDirectoryName,
          String... trainingArgs) throws Exception {
    SimplePipeline.runPipeline(FilesCollectionReader.getCollectionReaderWithView(
            ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
            "src/test/resources/data/treebank/11597317.tree", TreebankConstants.TREEBANK_VIEW),
            TreebankGoldAnnotator.getDescriptionPOSTagsOnly(), DefaultSnowballStemmer
                    .getDescription("English"), dataWriter);

    String[] args;
    if (trainingArgs != null && trainingArgs.length > 0) {
      args = new String[trainingArgs.length + 1];
      args[0] = outDirectoryName;
      System.arraycopy(trainingArgs, 0, args, 1, trainingArgs.length);
    } else {
      args = new String[] { outDirectoryName };
    }

    HideOutput hider = new HideOutput();
    org.cleartk.classifier.jar.Train.main(args);
    hider.restoreOutput();

    AnalysisEngineDescription taggerDescription = CleartkComponents.createCleartkAnnotator(
            NonSequentialExamplePOSAnnotator.class, ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
            outDirectoryName + "/model.jar");

    SimplePipeline.runPipeline(FilesCollectionReader.getCollectionReader(
            ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
            "src/test/resources/data/2008_Sichuan_earthquake.txt"), BreakIteratorAnnotatorFactory
            .createSentenceAnnotator(Locale.US), TokenAnnotator.getDescription(),
            DefaultSnowballStemmer.getDescription("English"), taggerDescription,
            AnalysisEngineFactory.createPrimitiveDescription(ExamplePOSPlainTextWriter.class,
                    ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
                    ExamplePOSPlainTextWriter.PARAM_OUTPUT_DIRECTORY_NAME, outDirectoryName));

  }
}
