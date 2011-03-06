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
package org.cleartk.token.pos.genia;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.token.TokenComponents;
import org.cleartk.token.pos.genia.util.GeniaPOSParser;
import org.cleartk.token.pos.genia.util.GeniaParse;
import org.cleartk.token.pos.genia.util.GeniaSentence;
import org.cleartk.token.pos.genia.util.GeniaTag;
import org.cleartk.token.pos.genia.util.Span;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.ViewURIUtil;
import org.jdom.JDOMException;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.SofaCapability;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * @author Philip V. Ogren
 * @see GeniaPOSParser
 */
@SofaCapability(outputSofas = { ViewURIUtil.URI, GeniaPosViewName.GENIA_POS })
public class GeniaPosGoldReader extends JCasCollectionReader_ImplBase {

  public static final String PARAM_GENIA_CORPUS_FILE = ConfigurationParameterFactory
      .createConfigurationParameterName(GeniaPosGoldReader.class, "geniaCorpusFile");

  @ConfigurationParameter(description = "names the file that is the Genia corpus to be loaded. A good value is probably '.../GENIAcorpus3.02.pos.xml'.  Please see README in this directory for edits that you may need to make to this file manually.", mandatory = true)
  private File geniaCorpusFile;

  public static final String PARAM_LOAD_SENTENCES = ConfigurationParameterFactory
      .createConfigurationParameterName(GeniaPosGoldReader.class, "loadSentences");

  @ConfigurationParameter(description = "determines whether sentence annotations will be added from the Genia corpus.", defaultValue = "true")
  private boolean loadSentences = true;

  public static final String PARAM_LOAD_TOKENS = ConfigurationParameterFactory
      .createConfigurationParameterName(GeniaPosGoldReader.class, "loadTokens");

  @ConfigurationParameter(description = "determines whether tokens annotations will be added from the Genia corpus. ", defaultValue = "true")
  private boolean loadTokens = true;

  public static final String PARAM_LOAD_POS_TAGS = ConfigurationParameterFactory
      .createConfigurationParameterName(GeniaPosGoldReader.class, "loadPosTags");

  @ConfigurationParameter(description = "determines whether the part of speech tags assigned to each token in the genia corpus will be loaded. The default value of 'true' is used if this "
      + "parameter is unspecified. If 'loadTokens' is 'false', then 'loadPOSTags' will be treated as 'false' regardless of what is given in the descriptor file.", defaultValue = "true")
  private boolean loadPosTags = true;

  public static final String PARAM_ARTICLE_IDS_LIST_FILE = ConfigurationParameterFactory
      .createConfigurationParameterName(GeniaPosGoldReader.class, "articleIdsListFile");

  @ConfigurationParameter(description = "names the file used to specify the article ids that should be read in")
  File articleIdsListFile;

  private boolean filterArticles;

  private Set<String> articleIds;

  private GeniaPOSParser parser;

  private GeniaParse parse;

  private int progress = 0;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {

    articleIds = new HashSet<String>();

    try {
      if (articleIdsListFile == null) {
        filterArticles = false;
      } else {
        filterArticles = true;
        String[] ids = FileUtil.loadListOfStrings(articleIdsListFile);
        for (String id : ids) {
          articleIds.add(id);
        }
      }

      parser = new GeniaPOSParser(geniaCorpusFile);
      loadPosTags = loadTokens & loadPosTags;
    } catch (IOException ioe) {
      throw new ResourceInitializationException(ioe);
    } catch (JDOMException je) {
      throw new ResourceInitializationException(je);
    }
  }

  public void getNext(JCas jCas) throws IOException, CollectionException {
    if (!hasNext())
      throw new CollectionException(
          "Should not be calling getNext() because hasNext returns false",
          null);
    try {
      JCas annotationsView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      String text = parse.getText();
      annotationsView.setDocumentText(text);

      List<GeniaSentence> sentences = parse.getSentences();

      for (GeniaSentence geniaSentence : sentences) {
        if (loadTokens) {
          List<GeniaTag> posTags = geniaSentence.getPosTags();
          for (GeniaTag posTag : posTags) {
            Span tokenSpan = posTag.getSpans().get(0);
            Token token = new Token(annotationsView, tokenSpan.getBegin(), tokenSpan.getEnd());
            if (loadPosTags)
              token.setPos(posTag.getLabel());
            token.addToIndexes();
          }
        }
        if (loadSentences) {
          Sentence sentence = new Sentence(
              annotationsView,
              geniaSentence.getSpan().getBegin(),
              geniaSentence.getSpan().getEnd());
          sentence.addToIndexes();
        }
      }

      ViewURIUtil.setURI(jCas, new File(parse.getMedline()).toURI());

      JCas geniaView = jCas.createView(GeniaPosViewName.GENIA_POS);
      geniaView.setDocumentText(parse.getXml());

      parse = null;
    } catch (CASException ce) {
      throw new CollectionException(ce);
    }

  }

  public void close() throws IOException {
  }

  public Progress[] getProgress() {
    if (filterArticles) {
      return new Progress[] { new ProgressImpl(progress, articleIds.size(), Progress.ENTITIES) };
    } else {
      return new Progress[] { new ProgressImpl(progress, 2000, Progress.ENTITIES) };
    }
  }

  public boolean hasNext() throws IOException, CollectionException {
    if (parse != null)
      return true;
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
        GeniaPosGoldReader.class,
        TokenComponents.TYPE_SYSTEM_DESCRIPTION,
        GeniaPosGoldReader.PARAM_GENIA_CORPUS_FILE,
        geniaCorpusFile);
  }

  public static String[] TEST_FOLDS = new String[] {
      "resources/genia/article_ids/fold-1-test.txt",
      "resources/genia/article_ids/fold-2-test.txt",
      "resources/genia/article_ids/fold-3-test.txt",
      "resources/genia/article_ids/fold-4-test.txt",
      "resources/genia/article_ids/fold-5-test.txt",
      "resources/genia/article_ids/fold-6-test.txt",
      "resources/genia/article_ids/fold-7-test.txt",
      "resources/genia/article_ids/fold-8-test.txt",
      "resources/genia/article_ids/fold-9-test.txt",
      "resources/genia/article_ids/fold-10-test.txt", };

  public static String[] TRAIN_FOLDS = new String[] {
      "resources/genia/article_ids/fold-1-train.txt",
      "resources/genia/article_ids/fold-2-train.txt",
      "resources/genia/article_ids/fold-3-train.txt",
      "resources/genia/article_ids/fold-4-train.txt",
      "resources/genia/article_ids/fold-5-train.txt",
      "resources/genia/article_ids/fold-6-train.txt",
      "resources/genia/article_ids/fold-7-train.txt",
      "resources/genia/article_ids/fold-8-train.txt",
      "resources/genia/article_ids/fold-9-train.txt",
      "resources/genia/article_ids/fold-10-train.txt", };

}
