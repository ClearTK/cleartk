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

package org.cleartk.opennlp.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.opennlp.tools.parser.DefaultOutputTypesHelper;
import org.cleartk.opennlp.tools.parser.InputTypesHelper;
import org.cleartk.opennlp.tools.parser.OutputTypesHelper;
import org.cleartk.opennlp.tools.parser.ParserWrapper_ImplBase;
import org.cleartk.util.IoUtil;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * This class provides a uima wrapper for the OpenNLP name finder that is specific to the
 * ClearTK type system found in the cleartk-syntax project. However, by specifying your own
 * implementations of {@link InputTypesHelper} and {@link OutputTypesHelper} you can use your own
 * types for sentences, tokens, and part-of-speech tags.
 * <p>
 * 
 * @author Majid Laali.
 */
public class NameFinderAnnotator<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation, TOP_NODE_TYPE extends Annotation> 
extends ParserWrapper_ImplBase<TOKEN_TYPE, SENTENCE_TYPE, Parse, TOP_NODE_TYPE> {
  public static final String DEFAULT_DATE_MODEL_PATH = "/models/en-ner-date.bin";
  public static final String DEFAULT_LOCATION_MODEL_PATH = "/models/en-ner-location.bin";
  public static final String DEFAULT_MONEY_MODEL_PATH = "/models/en-ner-money.bin";
  public static final String DEFAULT_ORGANIZATION_MODEL_PATH = "/models/en-ner-organization.bin";
  public static final String DEFAULT_PERCENTAGE_MODEL_PATH = "/models/en-ner-percentage.bin";
  public static final String DEFAULT_PERSON_MODEL_PATH = "/models/en-ner-person.bin";
  public static final String DEFAULT_TIME_MODEL_PATH = "/models/en-ner-time.bin";

  public static final String PARAM_NAME_FINDER_MODEL_PATH = "nameFinderModelPath";

  @ConfigurationParameter(
      name = PARAM_NAME_FINDER_MODEL_PATH,
      description = "provides the path of the OpenNLP name finder model build file, e.g. /models/en-ner-person.bin.")
  private String nameFinderModelPath;

  private NameFinderME nameFinder;

  public static AnalysisEngineDescription getDescription(String nameFinderModelPath) throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(
        NameFinderAnnotator.class,
        PARAM_NAME_FINDER_MODEL_PATH,
        nameFinderModelPath,
        PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME,
        DefaultOutputTypesHelper.class.getName());
  }

  @Override
  public void initialize(UimaContext ctx) throws ResourceInitializationException {
    super.initialize(ctx);

    try {
      InputStream modelIn = IoUtil.getInputStream(ParserAnnotator.class, nameFinderModelPath);
      TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
      nameFinder = new NameFinderME(model);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    List<SENTENCE_TYPE> sentences = inputTypesHelper.getSentences(jCas);
    for (SENTENCE_TYPE sentence: sentences){
      List<String> tokenTexts = new ArrayList<>();
      List<TOKEN_TYPE> tokenAnnotations = inputTypesHelper.getTokens(jCas, sentence);
      for (TOKEN_TYPE token: tokenAnnotations){
        tokenTexts.add(token.getCoveredText());
      }
      
      Span[] opennlpSpans = nameFinder.find(tokenTexts.toArray(new String[tokenTexts.size()]));
      for (Span opennlpSpan: opennlpSpans){
        NamedEntityMention ne = new NamedEntityMention(jCas, 
            tokenAnnotations.get(opennlpSpan.getStart()).getBegin(), 
            tokenAnnotations.get(opennlpSpan.getEnd() - 1).getEnd());
        ne.setMentionType(opennlpSpan.getType());
        ne.addToIndexes();
      }
    }
    
    nameFinder.clearAdaptiveData();
  }

}
