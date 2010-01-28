/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.token.opennlp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.maxent.MaxentModel;
import opennlp.maxent.io.SuffixSensitiveGISModelReader;
import opennlp.tools.postag.DefaultPOSContextGenerator;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.uutuc.descriptor.ConfigurationParameter;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.ConfigurationParameterFactory;
import org.uutuc.util.InitializeUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class OpenNLPPOSTagger extends JCasAnnotator_ImplBase {

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(OpenNLPPOSTagger.class,
				CleartkComponents.TYPE_SYSTEM_DESCRIPTION, CleartkComponents.TYPE_PRIORITIES,
				PARAM_POSTAG_DICTIONARY_FILE, CleartkComponents.getParameterValue(PARAM_POSTAG_DICTIONARY_FILE,
						"resources/models/OpenNLP.TagDict.txt"), PARAM_POSTAG_MODEL_FILE, CleartkComponents
						.getParameterValue(PARAM_POSTAG_MODEL_FILE, "resources/models/OpenNLP.POSTags.English.bin.gz"));
	}

	public static final String PARAM_POSTAG_MODEL_FILE = ConfigurationParameterFactory
			.createConfigurationParameterName(OpenNLPPOSTagger.class, "postagModelFile");

	@ConfigurationParameter(mandatory = true, description = "provides the path of the OpenNLP part-of-speech tagger model file, e.g.  resources/models/OpenNLP.POSTags.English.bin.gz.  See javadoc for opennlp.maxent.io.SuffixSensitiveGISModelReader.")
	private String postagModelFile;

	public static final String PARAM_POSTAG_DICTIONARY_FILE = ConfigurationParameterFactory
			.createConfigurationParameterName(OpenNLPPOSTagger.class, "postagDictionaryFile");

	@ConfigurationParameter(mandatory = true, description = "provides the path of the OpenNLP part-of-speech tagger dictionary file, e.g. resources/models/OpenNLP.TagDict.txt.  See javadoc for opennlp.tools.postag.POSDictionary.")
	private String postagDictionaryFile;

	public static final String PARAM_CASE_SENSITIVE = ConfigurationParameterFactory
			.createConfigurationParameterName(OpenNLPPOSTagger.class, "caseSensitive");

	@ConfigurationParameter(defaultValue = "true", description = "when false indicates that the POSDictionary should ignore case.  See javadoc for opennlp.tools.postag.POSDictionary.")
	private boolean caseSensitive;

	protected POSTagger posTagger;

	protected long processTime = 0;

	protected long tagTime = 0;

	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);
		InitializeUtil.initialize(this, uimaContext);
		try {
			MaxentModel model = new SuffixSensitiveGISModelReader(new File(postagModelFile)).getModel();
			POSDictionary posDictionary = new POSDictionary(postagDictionaryFile, caseSensitive);
			posTagger = new POSTaggerME(model, new DefaultPOSContextGenerator(null), posDictionary);
		}
		catch (IOException ioe) {
			throw new ResourceInitializationException(ioe);
		}
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		FSIterator sentenceIterator = jCas.getAnnotationIndex(Sentence.type).iterator();
		while (sentenceIterator.hasNext()) {
			Sentence sentence = (Sentence) sentenceIterator.next();
			FSIterator tokenIterator = jCas.getAnnotationIndex(Token.type).subiterator(sentence);
			List<String> tokens = new ArrayList<String>();
			while (tokenIterator.hasNext())
				tokens.add(((Token) tokenIterator.next()).getCoveredText());
			long startT = System.nanoTime();
			List<?> tags = posTagger.tag(tokens);
			long stopT = System.nanoTime();
			tagTime += (stopT - startT);
			tokenIterator.moveToFirst();
			for (int i = 0; tokenIterator.hasNext() && i < tags.size(); i++) {
				((Token) tokenIterator.next()).setPos((String) tags.get(i));
			}
		}
	}
}
