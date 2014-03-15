/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.timeml;

import java.io.File;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.UimaContextFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.corpus.timeml.TempEval2007Writer;
import org.cleartk.opennlp.tools.ParserAnnotator;
import org.cleartk.opennlp.tools.PosTaggerAnnotator;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.timeml.event.EventAnnotator;
import org.cleartk.timeml.event.EventAspectAnnotator;
import org.cleartk.timeml.event.EventClassAnnotator;
import org.cleartk.timeml.event.EventModalityAnnotator;
import org.cleartk.timeml.event.EventPolarityAnnotator;
import org.cleartk.timeml.event.EventTenseAnnotator;
import org.cleartk.timeml.time.TimeAnnotator;
import org.cleartk.timeml.time.TimeTypeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkEventToDocumentCreationTimeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkEventToSameSentenceTimeAnnotator;
import org.cleartk.timeml.tlink.TemporalLinkEventToSubordinatedEventAnnotator;
import org.cleartk.timeml.type.DocumentCreationTime;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.cr.FilesCollectionReader;

/**
 * Command line utility for annotating plain text files with TimeML annotations. Usage:
 * 
 * <pre>
 * java org.cleartk.timeml.TimeMLAnnotate input-file-or-dir [output-dir]
 * </pre>
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TimeMlAnnotate {

  private static void error(String message) throws Exception {
    Logger logger = UimaContextFactory.createUimaContext().getLogger();
    logger.log(
        Level.SEVERE,
        String.format(
            "%s\nusage: java %s input-file-or-dir [output-dir]",
            TimeMlAnnotate.class.getName(),
            message));
    System.exit(1);
  }

  public static void main(String... args) throws Exception {
    // check arguments
    if (args.length != 1 && args.length != 2) {
      error("wrong number of arguments");
    } else if (!new File(args[0]).exists()) {
      error("file or directory not found: " + args[0]);
    }

    // parse arguments
    String inputFileOrDir = args[0];
    File outputDir = new File(args.length == 2 ? args[1] : ".");
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }

    // run the components on the selected documents
    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReader(inputFileOrDir),
        SentenceAnnotator.getDescription(),
        TokenAnnotator.getDescription(),
        PosTaggerAnnotator.getDescription(),
        DefaultSnowballStemmer.getDescription("English"),
        ParserAnnotator.getDescription(),
        TimeAnnotator.FACTORY.getAnnotatorDescription(),
        TimeTypeAnnotator.FACTORY.getAnnotatorDescription(),
        EventAnnotator.FACTORY.getAnnotatorDescription(),
        EventTenseAnnotator.FACTORY.getAnnotatorDescription(),
        EventAspectAnnotator.FACTORY.getAnnotatorDescription(),
        EventClassAnnotator.FACTORY.getAnnotatorDescription(),
        EventPolarityAnnotator.FACTORY.getAnnotatorDescription(),
        EventModalityAnnotator.FACTORY.getAnnotatorDescription(),
        AnalysisEngineFactory.createEngineDescription(AddEmptyDCT.class),
        TemporalLinkEventToDocumentCreationTimeAnnotator.FACTORY.getAnnotatorDescription(),
        TemporalLinkEventToSameSentenceTimeAnnotator.FACTORY.getAnnotatorDescription(),
        TemporalLinkEventToSubordinatedEventAnnotator.FACTORY.getAnnotatorDescription(),
        TempEval2007Writer.getDescription(outputDir.getPath()));
  }
  
  public static class AddEmptyDCT extends JCasAnnotator_ImplBase {

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
      DocumentCreationTime dct = new DocumentCreationTime(jCas, 0, 0);
      dct.setFunctionInDocument("CREATION_TIME");
      dct.setId("t0");
      dct.addToIndexes();
    }
    
  }
}
