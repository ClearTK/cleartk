package org.cleartk.examples.linewriter;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.examples.ExampleComponents;
import org.cleartk.syntax.opennlp.PosTaggerAnnotator;
import org.cleartk.syntax.opennlp.SentenceAnnotator;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.token.type.Token;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ae.linewriter.AnnotationWriter;
import org.cleartk.util.ae.linewriter.LineWriter;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

public class Docs2Tokens {

  public static class Options extends Options_ImplBase {
    @Option(name = "-i", aliases = "--inputFileName", usage = "specify the directory to read plain text files from", required = false)
    public String inputDirectoryName = "src/test/resources/data/twain";

    @Option(name = "-o", aliases = "--outputFileName", usage = "specify the file to write tokens to", required = false)
    public String outputFileName = "target/test/twain-tokens.txt";

  }

  public static void main(String[] args) throws UIMAException, IOException {

    Options options = new Options();
    options.parseOptions(args);

    CollectionReader filesReader = FilesCollectionReader.getCollectionReader(
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        options.inputDirectoryName);

    AnalysisEngineDescription sentences = SentenceAnnotator.getDescription();

    AnalysisEngineDescription tokenizer = TokenAnnotator.getDescription();

    AnalysisEngineDescription posTagger = PosTaggerAnnotator.getDescription();

    AnalysisEngineDescription lineWriter = AnalysisEngineFactory.createPrimitiveDescription(
        LineWriter.class,
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        LineWriter.PARAM_OUTPUT_FILE_NAME,
        options.outputFileName,
        LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
        Token.class.getName(),
        LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME,
        TokenAnnotationWriter.class.getName());

    SimplePipeline.runPipeline(filesReader, sentences, tokenizer, posTagger, lineWriter);
    System.out.println("results written to " + options.outputFileName);
  }

  public static class TokenAnnotationWriter implements AnnotationWriter<Token> {

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
    }

    @Override
    public String writeAnnotation(JCas jCas, Token token) throws AnalysisEngineProcessException {
      return token.getCoveredText() + "\t" + token.getPos();
    }

  }
}