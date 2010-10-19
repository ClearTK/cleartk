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
package org.cleartk.temporal.timeml;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.ViewNames;
import org.cleartk.classifier.jar.Train;
import org.cleartk.corpus.timeml.TimeMLGoldAnnotator;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.opennlp.OpenNLPPOSTagger;
import org.cleartk.token.snowball.DefaultSnowballStemmer;
import org.cleartk.util.FilesCollectionReader;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * Train the TimeML event identification and attribute classification models.
 * 
 * @author Steven Bethard
 */
public class EventTrain {

  private static void error(String message) throws Exception {
    Logger logger = UimaContextFactory.createUimaContext().getLogger();
    String format = "%s\nusage: EventTrain timebank-dir";
    logger.log(Level.SEVERE, String.format(format, message));
    System.exit(1);
  }

  public static void main(String[] args) throws Exception {
    // check arguments
    if (args.length != 1) {
      error("wrong number of arguments");
    } else if (!new File(args[0]).exists()) {
      error("TimeBank directory not found: " + args[0]);
    }
    String timebankDirectory = args[0];

    // run pipeline to extract features and write training data
    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReaderWithView(timebankDirectory, ViewNames.TIMEML),
        TimeMLGoldAnnotator.getDescriptionNoTLINKs(),
        OpenNLPSentenceSegmenter.getDescription(),
        TokenAnnotator.getDescription(), 
        OpenNLPPOSTagger.getDescription(),
        DefaultSnowballStemmer.getDescription("English"),
        EventAnnotator.getWriterDescription(),
        EventTenseAnnotator.getWriterDescription(),
        EventAspectAnnotator.getWriterDescription(),
        EventClassAnnotator.getWriterDescription(),
        EventPolarityAnnotator.getWriterDescription(),
        EventModalityAnnotator.getWriterDescription());
    
    // train models for each aspect of event identification
    Train.main(EventAnnotator.MODEL_DIR, "--forbidden", "O,I-Event");
    Train.main(EventTenseAnnotator.MODEL_DIR);
    Train.main(EventAspectAnnotator.MODEL_DIR);
    Train.main(EventClassAnnotator.MODEL_DIR);
    Train.main(EventPolarityAnnotator.MODEL_DIR);
    Train.main(EventModalityAnnotator.MODEL_DIR);
    
    // clean up unnecessary files
    List<String> modelDirs = Arrays.asList(
        EventAnnotator.MODEL_DIR,
        EventTenseAnnotator.MODEL_DIR,
        EventAspectAnnotator.MODEL_DIR,
        EventClassAnnotator.MODEL_DIR,
        EventPolarityAnnotator.MODEL_DIR,
        EventModalityAnnotator.MODEL_DIR);
    for (String dir: modelDirs) {
      for (File file: new File(dir).listFiles()) {
        if (!file.isDirectory() && !file.getName().equals("model.jar")) {
          file.delete();
        }
      }
    }
  }
}
