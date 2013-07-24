/** 
 * Copyright (c) 2007-2012, Regents of the University of Colorado 
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
package org.cleartk.summarization;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractor;
import org.cleartk.classifier.feature.extractor.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.FeatureExtractor1;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.summarization.type.SummarySentence;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;

/**
 * <br>
 * Copyright (c) 2007-2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * 
 * 
 * @author Lee Becker
 */
@Beta
public class SumBasicAnnotator extends CleartkAnnotator<Boolean> {
  public static enum TokenField {
    COVERED_TEXT, STEM, LEMMA
  }

  public static final String PARAM_TOKEN_FIELD = "tokenField";

  @ConfigurationParameter(name = PARAM_TOKEN_FIELD, mandatory = false, description = "token field")
  protected TokenField tokenField = TokenField.COVERED_TEXT;

  public static final String PARAM_STOPWORDS_URI = "stopwordsUri";

  @ConfigurationParameter(
      name = PARAM_STOPWORDS_URI,
      mandatory = false,
      description = "provides a URI pointing to a file containing a whitespace separated list of stopwords")
  protected URI stopwordsUri = null;

  FeatureExtractor1<Sentence> extractor;

  Set<String> stopwords;

  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    try {
      this.stopwords = this.readStopwords();
      this.extractor = this.createTokenCountsExtractor();
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
      Instance<Boolean> instance = new Instance<Boolean>(false, this.extractor.extract(
          jcas,
          sentence));

      if (this.isTraining()) {
        this.dataWriter.write(instance);
      } else {
        Map<Boolean, Double> scoredOutcomes = this.classifier.score(instance.getFeatures());
        Double trueScore = scoredOutcomes.get(true);
        if (trueScore > 0.0) {
          SummarySentence extractedSentence = new SummarySentence(
              jcas,
              sentence.getBegin(),
              sentence.getEnd());
          extractedSentence.setScore(trueScore);
          extractedSentence.addToIndexes();
        }
      }
    }
  }

  private Set<String> readStopwords() throws IOException {
    return Resources.readLines(this.stopwordsUri.toURL(), Charsets.US_ASCII, new ParseWordSet());
  }

  private static class ParseWordSet implements LineProcessor<Set<String>> {
    private Set<String> result;

    public ParseWordSet() {
      this.result = new HashSet<String>();
    }

    @Override
    public boolean processLine(String line) throws IOException {
      this.result.addAll(Arrays.asList(line.split("\\s+")));
      return true;
    }

    @Override
    public Set<String> getResult() {
      return this.result;
    }
  }

  private FeatureExtractor1<Sentence> createTokenCountsExtractor() {
    FeatureExtractor1<Token> tokenFieldExtractor = new CoveredTextExtractor<Token>();
    switch (this.tokenField) {
      case COVERED_TEXT:
        tokenFieldExtractor = new CoveredTextExtractor<Token>();
        break;
      case STEM:
        tokenFieldExtractor = new TypePathExtractor<Token>(Token.class, "stem");
        break;
      case LEMMA:
        tokenFieldExtractor = new TypePathExtractor<Token>(Token.class, "lemma");
        break;
    }

    CleartkExtractor<Sentence, Token> countsExtractor = new CleartkExtractor<Sentence, Token>(
        Token.class,
        new StopwordRemovingExtractor<Token>(this.stopwords, tokenFieldExtractor),
        new CleartkExtractor.Count(new CleartkExtractor.Covered()));

    return countsExtractor;
  }

}
