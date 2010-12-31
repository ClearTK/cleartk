/*
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
package org.cleartk.syntax.constituent.ptb.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.syntax.SyntaxComponents;
import org.cleartk.syntax.constituent.TreebankGoldAnnotator;
import org.cleartk.syntax.constituent.ptb.PennTreebankReader;
import org.cleartk.token.type.Sentence;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ViewURIFileNamer;
import org.kohsuke.args4j.Option;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 */

public class PrepareSentenceData {

	public static class Options extends Options_ImplBase{
		
		@Option(name="-i", aliases="--inputDirectoryName", usage="specify the name of the input directory for the wsj data.")
		public String inputDirectoryName = "../ClearTK Data/data/treebank/wsj";

		@Option(name="-o", aliases="--outputDirectoryName", usage="specify the name of the output directory for the sentence data.")
		public String outputDirectoryName = "../cleartk-token/src/main/resources/org/cleartk/sentence/english/ptb";

		@Option(name="-s", aliases="--sectionsSpecifier", usage="specify the sections that will be used.")
		public String sectionsSpecifier = "00-24";

	}
	
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.parseOptions(args);
		
		String inputDirectoryName = options.inputDirectoryName;
		String outputDirectoryName = options.outputDirectoryName;
		String sectionsSpecifier = options.sectionsSpecifier;
		
		TypeSystemDescription typeSystemDescription = SyntaxComponents.TYPE_SYSTEM_DESCRIPTION;
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(PennTreebankReader.class, typeSystemDescription,
				PennTreebankReader.PARAM_CORPUS_DIRECTORY_NAME, inputDirectoryName, PennTreebankReader.PARAM_SECTIONS_SPECIFIER, sectionsSpecifier);
		AnalysisEngine treebankFormatter = AnalysisEngineFactory.createPrimitive(TreebankGoldAnnotator.class, typeSystemDescription,
				TreebankGoldAnnotator.PARAM_POST_TREES, false);
		AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(XWriter.class, typeSystemDescription, 
				XWriter.PARAM_OUTPUT_DIRECTORY_NAME, outputDirectoryName,
				XWriter.PARAM_FILE_NAMER_CLASS_NAME, ViewURIFileNamer.class.getName());
		AnalysisEngine sentencePrinter = AnalysisEngineFactory.createPrimitive(SentencePrinter.class, typeSystemDescription);
		
		SimplePipeline.runPipeline(reader, treebankFormatter, sentencePrinter, xWriter);
	}
	
	public static class SentencePrinter extends JCasAnnotator_ImplBase {

		Map<Character, Integer> charCounts = new HashMap<Character, Integer>();
		int sentenceCount = 0;
		
		@Override
		public void process(JCas jCas) throws AnalysisEngineProcessException {
			for(Sentence sentence : JCasUtil.iterate(jCas, Sentence.class)) {
				String sentenceText = sentence.getCoveredText();
				char lastChar = sentenceText.charAt(sentenceText.length()-1);
				int count = 0;
				if(charCounts.containsKey(lastChar)) {
					count = charCounts.get(lastChar);
				}
				count++;
				charCounts.put(lastChar, count);
				sentenceCount++;
				if(lastChar != '.' && lastChar != '\"' &&  lastChar != '?') {
					System.out.println(sentenceText);
				}
			}
		}

		@Override
		public void collectionProcessComplete() throws AnalysisEngineProcessException {
			List<Entry<Character, Integer>> entries = new ArrayList<Entry<Character, Integer>>(charCounts.entrySet());
			Collections.sort(entries, new Comparator<Entry<Character, Integer>>() {
			public int compare(Entry<Character, Integer> o1,Entry<Character, Integer> o2) {
			  int comparison = o1.getValue().compareTo(o2.getValue());
			  if(comparison == 0) {
			    return o1.getKey().compareTo(o2.getKey());
			  } else {
			    return -comparison;
			  }
			}
			});

			for(Entry<Character, Integer> entry : entries) {
				System.out.println(entry.getKey()+"\t"+entry.getValue());
			}
			System.out.println("total number of sentences: "+sentenceCount);
			super.collectionProcessComplete();
		}
		
		
	}
}
