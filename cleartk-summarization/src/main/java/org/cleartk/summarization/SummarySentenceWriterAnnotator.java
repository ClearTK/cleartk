package org.cleartk.summarization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.summarization.type.SummarySentence;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

public class SummarySentenceWriterAnnotator extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription(File outputFile, boolean writeScores)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        SummarySentenceWriterAnnotator.class,
        SummarySentenceWriterAnnotator.PARAM_OUTPUT_URI,
        outputFile.toURI(),
        SummarySentenceWriterAnnotator.PARAM_WRITE_SCORES,
        writeScores);
  }

  public static final String PARAM_OUTPUT_URI = "outputUri";

  @ConfigurationParameter(name = PARAM_OUTPUT_URI, mandatory = true)
  protected URI outputUri;

  public static final String PARAM_WRITE_SCORES = "writeScores";

  @ConfigurationParameter(mandatory = false)
  protected Boolean writeScores = false;

  protected BufferedWriter buffer;

  @Override
  public void initialize(org.apache.uima.UimaContext context)
      throws ResourceInitializationException {
    super.initialize(context);

    try {
      buffer = new BufferedWriter(new FileWriter(new File(outputUri)));
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    for (SummarySentence sumSent : JCasUtil.select(aJCas, SummarySentence.class)) {
      try {
        buffer.write(sumSent.getCoveredText());
        if (writeScores) {
          buffer.write(String.format("\t%f", sumSent.getScore()));
        }
        buffer.write("\n");
      } catch (IOException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      buffer.close();
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

}
