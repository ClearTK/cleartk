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

package org.cleartk.token.breakit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.BreakIterator;
import java.util.Locale;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class BreakIteratorAnnotator extends JCasAnnotator_ImplBase {

  public static final String PARAM_LOCALE = ConfigurationParameterFactory
          .createConfigurationParameterName(BreakIteratorAnnotator.class, "locale");

  @ConfigurationParameter(description = "provides the name of the locale to be used to instantiate the break iterator")
  private Locale locale;

  public static enum BreakIteratorType {
    WORD, SENTENCE
  }

  public static final String PARAM_BREAK_ITERATOR_TYPE = ConfigurationParameterFactory
          .createConfigurationParameterName(BreakIteratorAnnotator.class, "breakIteratorType");

  @ConfigurationParameter(description = "provides the type of the locale to be used to instantiate the break iterator.  Should be one of  'WORD' or 'SENTENCE'", defaultValue = "SENTENCE")
  private BreakIteratorType breakIteratorType;

  public static final String PARAM_ANNOTATION_TYPE_NAME = ConfigurationParameterFactory
          .createConfigurationParameterName(BreakIteratorAnnotator.class, "annotationTypeName");

  @ConfigurationParameter(description = "class type of the annotations that are created by this annotator.")
  private String annotationTypeName;

  private Class<? extends Annotation> annotationClass;

  private Constructor<? extends Annotation> annotationConstructor;

  private BreakIterator breakIterator;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    try {
      annotationClass = InitializableFactory.getClass(annotationTypeName, Annotation.class);
      annotationConstructor = annotationClass.getConstructor(new Class[] { JCas.class,
          Integer.TYPE, Integer.TYPE });
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

    if (locale == null) {
      locale = Locale.getDefault();
    }
    if (breakIteratorType == BreakIteratorType.WORD) {
      breakIterator = BreakIterator.getWordInstance(locale);
    } else {
      breakIterator = BreakIterator.getSentenceInstance(locale);
    }

  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      processAnnotations(jCas);
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  private void processAnnotations(JCas jCas) throws IllegalArgumentException,
          InstantiationException, IllegalAccessException, InvocationTargetException {
    String text = jCas.getDocumentText();
    breakIterator.setText(text);

    int index = breakIterator.first();
    int endIndex;
    while ((endIndex = breakIterator.next()) != BreakIterator.DONE) {
      String annotationText = text.substring(index, endIndex);
      if (!annotationText.trim().equals("")) {
        annotationConstructor.newInstance(jCas, index, endIndex).addToIndexes();
      }
      index = endIndex;
    }
  }

}
