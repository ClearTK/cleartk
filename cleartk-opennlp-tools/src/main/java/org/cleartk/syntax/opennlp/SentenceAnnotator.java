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
package org.cleartk.syntax.opennlp;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.IOUtil;
import org.cleartk.util.ParamUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.initializable.InitializableFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * @author Lee Becker
 * 
 *         This sentence segmenter is a simple wrapper around the OpenNLP SentenceDetector with
 *         additional sentence detection added that handles multiple newlines (i.e. if multiple
 *         newlines appear (separated only by whitespace) together, then this is treated as a
 *         sentence delimiter.
 * 
 * @see SentenceDetector
 */
@Beta
@TypeCapability(outputs = "org.cleartk.token.type.Sentence")
public class SentenceAnnotator extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        SentenceAnnotator.class,
        PARAM_SENTENCE_MODEL_PATH,
        ParamUtil.getParameterValue(PARAM_SENTENCE_MODEL_PATH, "/models/en-sent.bin"),
        PARAM_WINDOW_CLASS_NAMES,
        ParamUtil.getParameterValue(PARAM_WINDOW_CLASS_NAMES, null));
  }

  public static final String PARAM_SENTENCE_MODEL_PATH = "sentenceModelPath";

  @ConfigurationParameter(
      name = PARAM_SENTENCE_MODEL_PATH,
      mandatory = true,
      description = "provides the path of the OpenNLP sentence segmenter model file")
  private String sentenceModelPath;

  public static final String PARAM_SENTENCE_TYPE_NAME = "sentenceTypeName";

  public static final String PARAM_WINDOW_CLASS_NAMES = "windowClassNames";

  @ConfigurationParameter(
      name = PARAM_WINDOW_CLASS_NAMES,
      mandatory = false,
      description = "provides an array of the annotation types that will be processed by this sentence annotator.  If the parameter is not filled, then SentenceAnnotator will process on the contents of jCas.getDocumentText().  It us up to the caller to ensure annotations do not overlap.")
  private String[] windowClassNames;

  @ConfigurationParameter(
      name = PARAM_SENTENCE_TYPE_NAME,
      description = "class type of the sentences that are created by this annotator. If this parameter is not filled, then sentencesof type org.cleartk.type.Sentence will be created.",
      defaultValue = "org.cleartk.token.type.Sentence")
  private String sentenceTypeName;

  Class<? extends Annotation> sentenceClass;

  protected List<Class<? extends Annotation>> windowClasses;

  Constructor<? extends Annotation> sentenceConstructor;

  public static final String multipleNewlinesRegex = "\\s*\\n\\s*\\n\\s*";

  SentenceDetector sentenceDetector;

  Pattern multipleNewlinesPattern;

  Pattern leadingWhitespacePattern;

  Pattern trailingWhitespacePattern;

  @Override
  public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
    super.initialize(uimaContext);

    try {
      sentenceClass = InitializableFactory.getClass(sentenceTypeName, Annotation.class);
      sentenceConstructor = sentenceClass.getConstructor(new Class[] {
          JCas.class,
          Integer.TYPE,
          Integer.TYPE });

      if (windowClassNames != null && windowClassNames.length > 0) {
        windowClasses = new ArrayList<Class<? extends Annotation>>();
        for (String windowClassName : windowClassNames) {
          windowClasses.add(InitializableFactory.getClass(windowClassName, Annotation.class));
        }
      }

      InputStream modelInputStream = IOUtil.getInputStream(
          SentenceAnnotator.class,
          sentenceModelPath);
      SentenceModel model = new SentenceModel(modelInputStream);
      sentenceDetector = new SentenceDetectorME(model);
      multipleNewlinesPattern = Pattern.compile(multipleNewlinesRegex, Pattern.MULTILINE
          | Pattern.DOTALL);
      leadingWhitespacePattern = Pattern.compile("^\\s+");
      trailingWhitespacePattern = Pattern.compile("\\s+$");
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    if (windowClasses == null) {
      // No window class names are set, operate on the entirety of the CAS' document text
      String text = jCas.getDocumentText();
      processText(jCas, text, 0);
    } else {
      // Window class names are specified, iterate over all annotations of the specified classes
      for (Class<? extends Annotation> windowClass : windowClasses) {
        // make list copy to avoid concurrent modification
        for (Annotation window : new ArrayList<Annotation>(JCasUtil.select(jCas, windowClass))) {
          String text = window.getCoveredText();
          processText(jCas, text, window.getBegin());
        }
      }
    }
  }

  protected void processText(JCas jCas, String text, int textOffset)
      throws AnalysisEngineProcessException {
    List<Integer> sentenceOffsets = getSentenceOffsets(text);

    int begin = 0;
    int end = 0;

    // advance the first sentence to first non-whitespace char
    Matcher matcher;
    matcher = leadingWhitespacePattern.matcher(text);
    if (matcher.find()) {
      begin += matcher.group().length();
    }
    try {
      for (Integer offset : sentenceOffsets) {
        end = offset; // offset is really the beginning of the next sentence so we may adjust this
                      // below.
        String sentenceText = text.substring(begin, end);
        // it is possible that there is a duplicate offset or that getSentenceOffsets returned the
        // first sentence offset
        if (sentenceText.trim().length() > 0) {
          matcher = trailingWhitespacePattern.matcher(sentenceText);
          if (matcher.find()) {
            end -= matcher.group().length();
          }
          sentenceConstructor.newInstance(jCas, textOffset + begin, textOffset + end).addToIndexes();
        }
        begin = offset; // we need to advance begin regardless of whether a sentence was created.
      }
      // take the remaining text if there is any and add it to a sentence.
      // this code will not execute if the text ends with a sentence detected by
      // SentenceDetector because it actually returns an offset corresponding to the end
      // of the last sentence (see note on getSentenceOffsets)
      if (begin < text.length()) {
        String sentenceText = text.substring(begin, text.length());
        end = text.length();
        if (sentenceText.trim().length() > 0) {
          matcher = trailingWhitespacePattern.matcher(sentenceText);
          if (matcher.find()) {
            end -= matcher.group().length();
          }
          sentenceConstructor.newInstance(jCas, textOffset + begin, textOffset + end).addToIndexes();
        }
      }
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  /**
   * returns a list of the beginnings of sentences - except (possibly) the first sentence - from
   * both the OpenNLP sentence detector and the multiple newlines regex. It is possible for this
   * method to return duplicate values. The sentence detector will return an offset corresponding to
   * the end of the text if the last non-whitespace character was classified an end of sentence
   * character.
   */
  private List<Integer> getSentenceOffsets(String text) {
    Matcher matcher = multipleNewlinesPattern.matcher(text);
    List<Integer> offsets = new ArrayList<Integer>();
    while (matcher.find()) {
      offsets.add(matcher.end());
    }

    Span[] sentenceOffsetsML = sentenceDetector.sentPosDetect(text);
    for (int i = 0; i < sentenceOffsetsML.length; i++) {
      offsets.add(sentenceOffsetsML[i].getStart());
    }
    Collections.sort(offsets);
    return offsets;
  }

}
