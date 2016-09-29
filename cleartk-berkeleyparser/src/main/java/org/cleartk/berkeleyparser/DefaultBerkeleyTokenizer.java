/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */

package org.cleartk.berkeleyparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.berkeley.nlp.io.LineLexer;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Majid Laali
 */
public class DefaultBerkeleyTokenizer<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation> extends JCasAnnotator_ImplBase {
  public static final String PARAM_INPUT_TYPES_HELPER_CLASS_NAME = "inputTypesHelperClassName";

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException{
    return AnalysisEngineFactory.createEngineDescription(DefaultBerkeleyTokenizer.class, 
        PARAM_INPUT_TYPES_HELPER_CLASS_NAME, DefaultInputTypesHelper.class.getName());
  }

  @ConfigurationParameter(
      name = PARAM_INPUT_TYPES_HELPER_CLASS_NAME,
      defaultValue = "org.cleartk.berkeleyparser.DefaultInputTypesHelper",
      mandatory = true)
  protected String inputTypesHelperClassName;

  private InputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE> inputTypesHelper;
  private LineLexer tokenizer = new LineLexer();
  
  @SuppressWarnings("unchecked")
  @Override
  public void initialize(UimaContext ctx) throws ResourceInitializationException {
    super.initialize(ctx);

    inputTypesHelper = InitializableFactory.create(
        ctx,
        inputTypesHelperClassName,
        InputTypesHelper.class);
  }
  
  public List<TOKEN_TYPE> tokenize(SENTENCE_TYPE sent) {
    try {
      int base = sent.getBegin();
      String strSent = sent.getCoveredText();
      JCas jCas = sent.getCAS().getJCas();
      List<String> strTokens = tokenizer.tokenizeLine(strSent);
      List<TOKEN_TYPE> tokens = new ArrayList<>();
      
      int index = 0;
      for (String strToken: strTokens){
        int nextIndex = strSent.indexOf(strToken, index);
        if (nextIndex == -1){
          System.err.print(String.format("Cannot find token <%s> in the sentence <%s>: \n", strToken, strSent));
          continue;
        }
        index = nextIndex;
        
        int begin = base + index;
        int end = begin + strToken.length();
        TOKEN_TYPE token = inputTypesHelper.buildToken(jCas, begin, end);
        if (token != null){
          token.addToIndexes();
          tokens.add(token);
        }
        index += strToken.length();
      }
      return tokens;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (CASException e) {
      e.printStackTrace();
    }
    return null;
    
  }


  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    List<SENTENCE_TYPE> sentences = inputTypesHelper.getSentences(aJCas);
    for (SENTENCE_TYPE sentence: sentences){
      tokenize(sentence);
    }
  }
}
