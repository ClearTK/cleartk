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

package org.cleartk.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.cleartk.CleartkComponents;
import org.cleartk.classifier.svmlight.DefaultOVASVMlightDataWriterFactory;
import org.cleartk.classifier.svmlight.DefaultSVMlightDataWriterFactory;
import org.cleartk.corpus.ace2005.Ace2005GoldAnnotator;
import org.cleartk.corpus.ace2005.Ace2005GoldReader;
import org.cleartk.corpus.ace2005.Ace2005Writer;
import org.cleartk.corpus.conll2003.Conll2003GoldReader;
import org.cleartk.corpus.genia.GeniaPosGoldReader;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.corpus.timeml.PlainTextTLINKGoldAnnotator;
import org.cleartk.corpus.timeml.TimeMLGoldAnnotator;
import org.cleartk.corpus.timeml.TimeMLWriter;
import org.cleartk.corpus.timeml.TreebankAligningAnnotator;
import org.cleartk.example.pos.ExamplePOSAnnotator;
import org.cleartk.example.pos.ExamplePOSPlainTextWriter;
import org.cleartk.ne.term.TermFinderAnnotator;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.srl.ArgumentAnnotator;
import org.cleartk.srl.PredicateAnnotator;
import org.cleartk.srl.SRLWriter;
import org.cleartk.srl.conll2005.Conll2005GoldAnnotator;
import org.cleartk.srl.conll2005.Conll2005GoldReader;
import org.cleartk.srl.conll2005.Conll2005Writer;
import org.cleartk.srl.propbank.PropbankGoldAnnotator;
import org.cleartk.srl.propbank.PropbankGoldReader;
import org.cleartk.syntax.opennlp.OpenNLPTreebankParser;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.temporal.VerbClauseTemporalAnnotator;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.chunk.ChunkTokenizerFactory;
import org.cleartk.token.opennlp.OpenNLPPOSTagger;
import org.cleartk.token.pos.impl.DefaultPOSAnnotator;
import org.cleartk.token.snowball.DefaultSnowballStemmer;
import org.cleartk.util.linereader.LineReader;
import org.cleartk.util.linereader.SimpleLineHandler;
import org.cleartk.util.linewriter.LineWriter;
import org.uimafit.component.JCasAnnotatorAdapter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.xml.sax.SAXException;

public class GenerateDescriptorFiles {

	/**
	 * <br>
	 * Copyright (c) 2009, Regents of the University of Colorado <br>
	 * All rights reserved.
	 * 
	 * @author Philip Ogren
	 */

	public static void main(String[] args) throws SAXException, IOException, ResourceInitializationException {
		String outputDirectoryName = "src/main/resources";
		File outputDirectory = new File(outputDirectoryName);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		AnalysisEngineDescription aed = CleartkComponents.createSentencesAndTokens();
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(outputDirectory, "SentencesAndTokens.xml")));

		File descDirectory = new File(outputDirectory, "org/cleartk/example/pos");
		if (!descDirectory.exists()) descDirectory.mkdirs();
		aed = ExamplePOSAnnotator.getClassifierDescription(ExamplePOSAnnotator.DEFAULT_MODEL);
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "ExamplePOSAnnotator.xml")));

		aed = ExamplePOSAnnotator.getWriterDescription(ExamplePOSAnnotator.DEFAULT_OUTPUT_DIRECTORY);
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "ExamplePOSDataWriter.xml")));

		descDirectory = new File(outputDirectory, "org/cleartk/token/pos/impl");
		if (!descDirectory.exists()) descDirectory.mkdirs();
		aed = DefaultPOSAnnotator.getWriterDescription();
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "DefaultPOSDataWriter.xml")));
		aed = DefaultPOSAnnotator.getAnnotatorDescription();
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "DefaultPOSAnnotator.xml")));

		descDirectory = new File(outputDirectory, "org/cleartk/temporal");
		if (!descDirectory.exists()) descDirectory.mkdirs();
		aed = VerbClauseTemporalAnnotator.getWriterDescription("test/data/temporal");
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "VerbClauseTemporalDataWriter.xml")));
		aed = VerbClauseTemporalAnnotator.getAnnotatorDescription();
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "VerbClauseTemporalAnnotator.xml")));

		writePrimitiveDescription(ExamplePOSPlainTextWriter.class, outputDirectory);

		writeCollectionReader(Ace2005GoldReader.class, outputDirectory);
		writePrimitiveDescription(Ace2005GoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(Ace2005Writer.class, outputDirectory);
		writePrimitiveDescription(TreebankGoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(TimeMLWriter.class, outputDirectory);
		writePrimitiveDescription(TimeMLGoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(PlainTextTLINKGoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(TreebankAligningAnnotator.class, outputDirectory);
		writeCollectionReader(PropbankGoldReader.class, outputDirectory);
		writePrimitiveDescription(PropbankGoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(TokenAnnotator.class, outputDirectory);
		writePrimitiveDescription(DefaultSnowballStemmer.class, outputDirectory);
		writeCollectionReader(PennTreebankReader.class, outputDirectory);

		writePrimitiveDescription(LineWriter.class, outputDirectory);
		writePrimitiveDescription(OpenNLPSentenceSegmenter.class, outputDirectory);
		writeCollectionReader(Conll2003GoldReader.class, outputDirectory);
		writeCollectionReader(LineReader.class, outputDirectory);
		CollectionReaderDescription crd = CleartkComponents.createCollectionReaderDescription(LineReader.class, LineReader.PARAM_LINE_HANDLER_CLASS_NAME,
				SimpleLineHandler.class.getName());
		ConfigurationParameterFactory.addConfigurationParameters(crd, SimpleLineHandler.class);
		updateDescription(crd.getMetaData());
		descDirectory = new File(outputDirectory, "org/cleartk/util/linereader");
		if (!descDirectory.exists()) descDirectory.mkdirs();
		crd.toXML(new FileWriter(new File(descDirectory, "SimpleLineReader.xml")));

		writeCollectionReader(GeniaPosGoldReader.class, outputDirectory);
		
		writePrimitiveDescription(PlainTextWriter.class, outputDirectory);
		writePrimitiveDescription(TermFinderAnnotator.class, outputDirectory);
		writePrimitiveDescription(JCasAnnotatorAdapter.class, outputDirectory);
		writeCollectionReader(XReader.class, outputDirectory);
		writeCollectionReader(FilesCollectionReader.class, outputDirectory);

		aed = CleartkComponents.createPrimitiveDescription(TokenAnnotator.class, TokenAnnotator.PARAM_TOKEN_TYPE_NAME,
				"org.cleartk.token.chunk.type.Subtoken", TokenAnnotator.PARAM_TOKENIZER_NAME,
				"org.cleartk.token.util.Subtokenizer");
		updateDescription(aed.getMetaData());
		descDirectory = new File(outputDirectory, "org/cleartk/token");
		if (!descDirectory.exists()) descDirectory.mkdirs();
		aed.toXML(new FileWriter(new File(descDirectory, "Subtokenizer.xml")));

		writePrimitiveDescription(OpenNLPTreebankParser.class, outputDirectory);
		writePrimitiveDescription(OpenNLPPOSTagger.class, outputDirectory);
		writeCollectionReader(Conll2005GoldReader.class, outputDirectory);
		writePrimitiveDescription(Conll2005GoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(Conll2005Writer.class, outputDirectory);

		aed = ChunkTokenizerFactory.createChunkTokenizerDescription();
		updateDescription(aed.getMetaData());
		descDirectory = new File(outputDirectory, "org/cleartk/token/chunk");
		if (!descDirectory.exists()) descDirectory.mkdirs();
		aed.toXML(new FileWriter(new File(descDirectory, "ChunkTokenizer.xml")));
		
		writePrimitiveDescription(SRLWriter.class, outputDirectory);
		writePrimitiveDescription(
				ArgumentAnnotator.class,
				ArgumentAnnotator.getWriterDescription(DefaultOVASVMlightDataWriterFactory.class, new File("")),
				"ArgumentDataWriter.xml", outputDirectory);
		writePrimitiveDescription(
				ArgumentAnnotator.class,
				ArgumentAnnotator.getClassifierDescription(new File("")),
				"ArgumentAnnotator.xml", outputDirectory);
		writePrimitiveDescription(
				PredicateAnnotator.class,
				PredicateAnnotator.getWriterDescription(DefaultSVMlightDataWriterFactory.class, new File("")),
				"PredicateDataWriter.xml", outputDirectory);
		writePrimitiveDescription(
				PredicateAnnotator.class,
				PredicateAnnotator.getClassifierDescription(new File("")),
				"PredicateAnnotator.xml", outputDirectory);
		
	}

	private static File updateOutputDirectory(Class<?> cls, File outputDirectory) {
		String packageName = cls.getPackage().getName();
		packageName = packageName.replace('.', File.separatorChar);
		outputDirectory = new File(outputDirectory, packageName);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		return outputDirectory;
	}

	private static void writeCollectionReader(Class<? extends CollectionReader> readerClass, File outputDirectory)
			throws ResourceInitializationException, SAXException, IOException {
		CollectionReaderDescription crd = CleartkComponents.createCollectionReaderDescription(readerClass);
		updateDescription(crd.getMetaData());
		outputDirectory = updateOutputDirectory(readerClass, outputDirectory);
		crd.toXML(new FileWriter(new File(outputDirectory, readerClass.getSimpleName() + ".xml")));
	}

	private static void writePrimitiveDescription(Class<? extends AnalysisComponent> componentClass,
			File outputDirectory) throws ResourceInitializationException, SAXException, IOException {
		AnalysisEngineDescription aed = CleartkComponents.createPrimitiveDescription(componentClass);
		String fileName = componentClass.getSimpleName() + ".xml";
		writePrimitiveDescription(componentClass, aed, fileName, outputDirectory);
	}
	
	private static void writePrimitiveDescription(
			Class<?> packageClass,
			AnalysisEngineDescription aed,
			String fileName,
			File outputDirectory) throws SAXException, IOException {
		updateDescription(aed.getMetaData());
		outputDirectory = updateOutputDirectory(packageClass, outputDirectory);
		aed.toXML(new FileWriter(new File(outputDirectory, fileName)));
	}

	private static void updateDescription(ResourceMetaData rmd) {
		String description = rmd.getDescription();
		if (description == null) description = "";
		rmd.setDescription("This descriptor file was generated automatically by "
				+ GenerateDescriptorFiles.class.getName() + ". \n\n" + license + description);
	}

	public static final String newline = "\n";// System.getProperty("line.separator");

	public static final String license = "Copyright (c) 2009, Regents of the University of Colorado "
			+ newline
			+ "All rights reserved."
			+ newline
			+ ""
			+ newline
			+ "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:"
			+ newline
			+ ""
			+ newline
			+ " - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. "
			+ newline
			+ " - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. "
			+ newline
			+ " - Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. "
			+ newline
			+ newline
			+ "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE "
			+ "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE "
			+ "ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE "
			+ "LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR "
			+ "CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF "
			+ "SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS "
			+ "INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN "
			+ "CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) "
			+ "ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE "
			+ "POSSIBILITY OF SUCH DAMAGE. " + newline + newline;

	public static final String copyright = "Copyright (c) 2009, Regents of the University of Colorado " + newline
			+ "All rights reserved." + newline;

}
