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
package org.cleartk.corpus.genia;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.CleartkComponents;
import org.cleartk.ViewNames;
import org.cleartk.corpus.genia.util.GeniaPOSParser;
import org.cleartk.corpus.genia.util.GeniaParse;
import org.cleartk.corpus.genia.util.GeniaSentence;
import org.cleartk.corpus.genia.util.GeniaTag;
import org.cleartk.corpus.genia.util.Span;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.UIMAUtil;
import org.cleartk.util.ViewURIUtil;
import org.jdom.JDOMException;
import org.uutuc.factory.CollectionReaderFactory;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philip V. Ogren
 * @see GeniaPOSParser
 */
public class GeniaPosGoldReader extends CollectionReader_ImplBase {

	/**
	 * "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_ARTICLE_IDS_LIST"
	 * is a single, optional, string parameter that names the file used to specify
	 * the article ids that should be read in.
	 */
	public static final String PARAM_ARTICLE_IDS_LIST = "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_ARTICLE_IDS_LIST";

	/**
	 * "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_GENIA_CORPUS"
	 * is a single, required, string parameter that names the file
	 * that is the Genia corpus to be loaded. A good value is probably
	 * "data/genia/GENIAcorpus3.02.pos.xml".  Please see README in this directory for edits that you may need to make to this file manually.  
	 */
	public static final String PARAM_GENIA_CORPUS = "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_GENIA_CORPUS";

	/**
	 * "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_LOAD_SENTENCES"
	 * is a single, optional, boolean parameter that determines
	 * whether sentence annotations will be added from the Genia corpus. The
	 * default value of "true" is used if this parameter is unspecified.
	 */
	public static final String PARAM_LOAD_SENTENCES = "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_LOAD_SENTENCES";

	/**
	 * "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_LOAD_TOKENS"
	 * is a single, optional, boolean parameter that determines
	 * whether tokens annotations will be added from the Genia corpus. The
	 * default value of "true" is used if this parameter is unspecified.
	 */
	public static final String PARAM_LOAD_TOKENS = "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_LOAD_TOKENS";

	/**
	 * "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_LOAD_POS_TAGS"
	 * is a single, optional, boolean parameter that determines
	 * whether the part of speech tags assigned to each token in the genia
	 * corpus will be loaded. The default value of "true" is used if this
	 * parameter is unspecified. If "LoadTokens" is 'false', then "LoadPOSTags"
	 * will also be 'false' regardless of what is given in the descriptor file.
	 */
	public static final String PARAM_LOAD_POS_TAGS = "org.cleartk.corpus.genia.GeniaPosGoldReader.PARAM_LOAD_POS_TAGS";

	boolean filterArticles;

	Set<String> articleIds;

	GeniaPOSParser parser;

	GeniaParse parse;

	int progress = 0;

	int tokenIndex = 0;

	boolean loadSentences = true;

	boolean loadTokens = true;

	boolean loadPOSTags = true;

	@Override
	public void initialize() throws ResourceInitializationException {
		articleIds = new HashSet<String>();

		try {
			String articleIdsList = (String) getConfigParameterValue(PARAM_ARTICLE_IDS_LIST);
			if (articleIdsList == null) {
				filterArticles = false;
			}
			else {
				filterArticles = true;
				String[] ids = FileUtil.loadListOfStrings(new File(articleIdsList));
				for (String id : ids) {
					articleIds.add(id);
				}
			}

			String geniaCorpus = (String) getConfigParameterValue(PARAM_GENIA_CORPUS);
			parser = new GeniaPOSParser(new File(geniaCorpus));

			loadSentences = (Boolean) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(), PARAM_LOAD_SENTENCES, true);
			loadTokens = (Boolean) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(), PARAM_LOAD_TOKENS, true);
			loadPOSTags = (Boolean) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(), PARAM_LOAD_POS_TAGS, true);
			loadPOSTags = loadTokens & loadPOSTags;

		}
		catch (IOException ioe) {
			throw new ResourceInitializationException(ioe);
		}
		catch (JDOMException je) {
			throw new ResourceInitializationException(je);
		}
	}

	public void getNext(CAS cas) throws IOException, CollectionException {
		if(!hasNext()) 
			throw new CollectionException("Should not be calling getNext() because hasNext returns false", null);
		try {
			JCas annotationsView = cas.getJCas().getView(ViewNames.DEFAULT);
			String text = parse.getText();
			annotationsView.setDocumentText(text);

			List<GeniaSentence> sentences = parse.getSentences();

			for (GeniaSentence geniaSentence : sentences) {
				if (loadTokens) {
					List<GeniaTag> posTags = geniaSentence.getPosTags();
					for (GeniaTag posTag : posTags) {
						Span tokenSpan = posTag.getSpans().get(0);
						Token token = new Token(annotationsView, tokenSpan.getBegin(), tokenSpan.getEnd());
						if (loadPOSTags) token.setPos(posTag.getLabel());
						token.addToIndexes();
					}
				}
				if (loadSentences) {
					Sentence sentence = new Sentence(annotationsView, geniaSentence.getSpan().getBegin(), geniaSentence.getSpan()
							.getEnd());
					sentence.addToIndexes();
				}
			}

			ViewURIUtil.setURI(cas, parse.getMedline());

			JCas geniaView = cas.getJCas().createView(ViewNames.GENIA_POS);
			geniaView.setDocumentText(parse.getXml());

			parse = null;
		}
		catch (CASException ce) {
			throw new CollectionException(ce);
		}

	}

	public void close() throws IOException {
	}

	public Progress[] getProgress() {
		if (filterArticles) {
			return new Progress[] { new ProgressImpl(progress, articleIds.size(), Progress.ENTITIES) };
		}
		else {
			return new Progress[] { new ProgressImpl(progress, 2000, Progress.ENTITIES) };
		}
	}

	public boolean hasNext() throws IOException, CollectionException {
		if (parse != null) return true;
		while (parser.hasNext()) {
			parse = parser.next();
			if (!filterArticles) {
				progress++;
				return true;
			}
			if (articleIds.contains(parse.getMedline())) {
				progress++;
				return true;
			}
		}
		return false;
	}
	
	public static CollectionReader getDescription(String geniaCorpusFile)
	throws ResourceInitializationException {
		return CollectionReaderFactory.createCollectionReader(
				GeniaPosGoldReader.class, CleartkComponents.TYPE_SYSTEM_DESCRIPTION,
				GeniaPosGoldReader.PARAM_GENIA_CORPUS,geniaCorpusFile);
	}

}
