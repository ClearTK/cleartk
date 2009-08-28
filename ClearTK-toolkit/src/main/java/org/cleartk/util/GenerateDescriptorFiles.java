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
import org.cleartk.corpus.ace2005.Ace2005GoldAnnotator;
import org.cleartk.corpus.ace2005.Ace2005GoldReader;
import org.cleartk.corpus.ace2005.Ace2005Writer;
import org.cleartk.corpus.conll2003.Conll2003GoldReader;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.corpus.timeml.TimeMLGoldAnnotator;
import org.cleartk.corpus.timeml.TimeMLWriter;
import org.cleartk.example.pos.ExamplePOSAnnotationHandler;
import org.cleartk.example.pos.ExamplePOSPlainTextWriter;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.srl.propbank.PropbankGoldAnnotator;
import org.cleartk.srl.propbank.PropbankGoldReader;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.pos.impl.DefaultPOSHandler;
import org.cleartk.token.snowball.DefaultSnowballStemmer;
import org.cleartk.util.linereader.LineReader;
import org.cleartk.util.linewriter.LineWriter;
import org.xml.sax.SAXException;

public class GenerateDescriptorFiles {

	/**
	 * <br>
	 * Copyright (c) 2009, Regents of the University of Colorado <br>
	 * All rights reserved.
	 * @author Philip Ogren
	 */

	public static void main(String[] args) throws SAXException, IOException, ResourceInitializationException {
		String outputDirectoryName = "src/main/resources";
		File outputDirectory = new File(outputDirectoryName);
		if(!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		AnalysisEngineDescription aed = CleartkComponents.createSentencesAndTokens();
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(outputDirectory, "SentencesAndTokens.xml")));

		File descDirectory = new File(outputDirectory, "org/cleartk/example/pos");
		if(!descDirectory.exists())
			descDirectory.mkdirs();
		aed = ExamplePOSAnnotationHandler.getClassifierDescription(ExamplePOSAnnotationHandler.DEFAULT_MODEL);
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "ExamplePOSAnnotator.xml")));

		aed = ExamplePOSAnnotationHandler.getWriterDescription(ExamplePOSAnnotationHandler.DEFAULT_OUTPUT_DIRECTORY);
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "ExamplePOSDataWriter.xml")));

		descDirectory = new File(outputDirectory, "org/cleartk/token/pos/impl");
		if(!descDirectory.exists())
			descDirectory.mkdirs();
		aed = DefaultPOSHandler.getWriterDescription();
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "DefaultPOSDataWriter.xml")));
		aed = DefaultPOSHandler.getAnnotatorDescription();
		updateDescription(aed.getMetaData());
		aed.toXML(new FileWriter(new File(descDirectory, "DefaultPOSAnnotator.xml")));

		
		writePrimitiveDescription(ExamplePOSPlainTextWriter.class, outputDirectory);

		writeCollectionReader(Ace2005GoldReader.class, outputDirectory);
		writePrimitiveDescription(Ace2005GoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(Ace2005Writer.class, outputDirectory);
		writePrimitiveDescription(TreebankGoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(TimeMLWriter.class, outputDirectory);
		writePrimitiveDescription(TimeMLGoldAnnotator.class, outputDirectory);
		writeCollectionReader(PropbankGoldReader.class, outputDirectory);
		writePrimitiveDescription(PropbankGoldAnnotator.class, outputDirectory);
		writePrimitiveDescription(TokenAnnotator.class, outputDirectory);
		writePrimitiveDescription(DefaultSnowballStemmer.class, outputDirectory);
		writeCollectionReader(PennTreebankReader.class, outputDirectory);
		
		writePrimitiveDescription(LineWriter.class, outputDirectory);
		writePrimitiveDescription(OpenNLPSentenceSegmenter.class, outputDirectory);
		writeCollectionReader(Conll2003GoldReader.class, outputDirectory);
		writeCollectionReader(LineReader.class, outputDirectory);
	}
	
	private static File updateOutputDirectory(Class<?> cls, File outputDirectory) {
		String packageName = cls.getName();
		packageName = packageName.substring(0, packageName.length()- cls.getSimpleName().length() - 1);
		packageName = packageName.replace('.', File.separatorChar);
		outputDirectory = new File(outputDirectory, packageName);
		if(!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		return outputDirectory;
	}
	
	private static void writeCollectionReader(Class<? extends CollectionReader> readerClass, File outputDirectory) throws ResourceInitializationException, SAXException, IOException {
		CollectionReaderDescription crd = CleartkComponents.createCollectionReaderDescription(readerClass);
		updateDescription(crd.getMetaData());
		outputDirectory = updateOutputDirectory(readerClass, outputDirectory);
		crd.toXML(new FileWriter(new File(outputDirectory, readerClass.getSimpleName()+".xml")));
	}
	
	private static void writePrimitiveDescription(Class<? extends AnalysisComponent> componentClass, File outputDirectory) throws ResourceInitializationException, SAXException, IOException {
		AnalysisEngineDescription aed = CleartkComponents.createPrimitiveDescription(componentClass);
		updateDescription(aed.getMetaData());
		outputDirectory = updateOutputDirectory(componentClass, outputDirectory);
		aed.toXML(new FileWriter(new File(outputDirectory, componentClass.getSimpleName()+".xml")));
	}
	
	private static void updateDescription(ResourceMetaData rmd) {
		String description = rmd.getDescription();
		if(description == null)
			description = "";
		rmd.setDescription("This descriptor file was generated automatically by "+GenerateDescriptorFiles.class.getName()+". \n\n"+
				license+description);
	}

	public static final String newline = "\n";//System.getProperty("line.separator");
	public static final String license = 
		  "Copyright (c) 2009, Regents of the University of Colorado "
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
		+ newline + newline
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


