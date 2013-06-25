/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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

package org.cleartk.util.ae.parenthetical;

import java.lang.reflect.Constructor;
import java.util.Stack;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.CleartkInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class ParentheticalAnnotator extends JCasAnnotator_ImplBase {

  public static final String PARAM_WINDOW_TYPE_NAME = "windowTypeName";

  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "If no value is given, then the entire document will be tokenized at once. ";

  // do not set the default value to 'org.cleartk.token.type.Sentence'. If you do, then unit tests
  // will break. The symptom will be a tokenizer that doesn't generate any tokens (because there
  // are no sentences to iterate over.
  @ConfigurationParameter(
      name = PARAM_WINDOW_TYPE_NAME,
      description = WINDOW_TYPE_DESCRIPTION)
  private String windowTypeName;

  public static final String PARAM_PARENTHETICAL_TYPE_NAME = "parentheticalTypeName";

  @ConfigurationParameter(
      name = PARAM_PARENTHETICAL_TYPE_NAME,
      description = "class name of the annotations that are created by this annotator.",
      defaultValue = "org.cleartk.util.type.Parenthetical",
      mandatory = true)
  private String parentheticalTypeName;

  public static final String PARAM_LEFT_PARENTHESIS = "leftParenthesis";

  @ConfigurationParameter(
      name = PARAM_LEFT_PARENTHESIS,
      defaultValue = "(", mandatory = true)
  private String leftParenthesis;

  private char leftParen;

  public static final String PARAM_RIGHT_PARENTHESIS = "rightParenthesis";

  @ConfigurationParameter(name = PARAM_RIGHT_PARENTHESIS, 
      defaultValue = ")", mandatory = true)
  private String rightParenthesis;

  private char rightParen;

  private Class<? extends Annotation> windowClass;

  private Constructor<? extends Annotation> parentheticalConstructor;

  public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
    super.initialize(uimaContext);
    if (windowTypeName != null)
      windowClass = InitializableFactory.getClass(windowTypeName, Annotation.class);

    if (leftParenthesis.length() != 1) {
      throw CleartkInitializationException.notSingleCharacter(
          PARAM_LEFT_PARENTHESIS,
          leftParenthesis);
    }
    leftParen = leftParenthesis.charAt(0);

    if (rightParenthesis.length() != 1) {
      throw CleartkInitializationException.notSingleCharacter(
          PARAM_RIGHT_PARENTHESIS,
          rightParenthesis);
    }
    rightParen = rightParenthesis.charAt(0);

    Class<? extends Annotation> parentheticalClass = InitializableFactory.getClass(
        parentheticalTypeName,
        Annotation.class);

    try {
      parentheticalConstructor = parentheticalClass.getConstructor(new Class[] {
          JCas.class,
          Integer.TYPE,
          Integer.TYPE });
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    if (windowClass != null) {
      for (Annotation window : JCasUtil.select(jCas, windowClass)) {
        String text = window.getCoveredText();
        createParentheticals(jCas, text, window.getBegin());
      }
    } else {
      String text = jCas.getDocumentText();
      createParentheticals(jCas, text, 0);
    }
  }

  private void createParentheticals(JCas jCas, String text, int offset)
      throws AnalysisEngineProcessException {
    Stack<Integer> leftRoundedParens = new Stack<Integer>();
    leftRoundedParens.clear();
    for (int ci = 0; ci < text.length(); ci++) {
      char c = text.charAt(ci);
      if (c == leftParen) {
        leftRoundedParens.push(ci);
      }
      if (c == rightParen && !leftRoundedParens.isEmpty()) {
        int leftOffset = leftRoundedParens.pop();
        Annotation ann;
        try {
          ann = parentheticalConstructor.newInstance(jCas, offset + leftOffset, offset + ci + 1);
        } catch (Exception e) {
          throw new AnalysisEngineProcessException(e);
        }
        ann.addToIndexes();
      }
    }
  }


  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return getDescription(null, '(', ')');
  }

  public static AnalysisEngineDescription getDescription(Class<? extends Annotation> windowClass)
      throws ResourceInitializationException {
    return getDescription(windowClass, '(', ')');
  }

  public static AnalysisEngineDescription getDescription(
      Class<? extends Annotation> windowClass,
      char leftParen,
      char rightParen) throws ResourceInitializationException {
    AnalysisEngineDescription aed = AnalysisEngineFactory.createPrimitiveDescription(
        ParentheticalAnnotator.class,
        PARAM_LEFT_PARENTHESIS,
        "" + leftParen,
        PARAM_RIGHT_PARENTHESIS,
        "" + rightParen);

    if (windowClass != null) {
      ConfigurationParameterFactory.addConfigurationParameters(
          aed,
          PARAM_WINDOW_TYPE_NAME,
          windowClass.getName());
    }

    return aed;
  }
}
