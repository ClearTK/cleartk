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

package org.cleartk.token.lemma.choi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

import clear.morph.MorphEnAnalyzer;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved. * This class has been deprecated. For a dependency-based Semantic Role
 * Labeler, switch to {@link org.cleartk.clearnlp.MPAnalyzer}
 * <p>
 * LemmaAnnator is a wrapper around and English lemmatization tool provided by the <a
 * href="http://code.google.com/p/clearparser/">clearparser project</a> which is subject to the BSD
 * license and copyrighted by the Regents of the University of Colorado. It is based primarily on
 * the WordNet "Morphy" algorithm described <a
 * href="http://wordnet.princeton.edu/man/morphy.7WN.html">here</a> with some additional logic.
 * MorphEnAnalyzer was written by Jinho Choi.
 * <p>
 * This analysis engine requires part-of-speech tagged tokens to work correctly. The part-of-speech
 * tags should correspond to PennTreebank set of tags.
 * <p>
 * 
 * @author Philip Ogren
 * 
 */

@Deprecated
@TypeCapability(
    inputs = "org.cleartk.token.type.Token:pos",
    outputs = "org.cleartk.token.type.Token:lemma")
public class LemmaAnnotator extends JCasAnnotator_ImplBase {

  @Deprecated
  public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.token.TypeSystem");

  public static final String ENG_LEMMATIZER_DATA_FILE = "wordnet-3.0-lemma-data.jar";

  public static final String PARAM_LEMMATIZER_DATA_FILE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      LemmaAnnotator.class,
      "lemmatizerDataFileName");

  @ConfigurationParameter(
      description = "This parameter provides the file name of the lemmatizer data file required by the constructor of MorphEnAnalyzer.")
  private String lemmatizerDataFileName;

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(LemmaAnnotator.class);
  }

  public static AnalysisEngineDescription getDescription(String lemmatizerDataFileName)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        LemmaAnnotator.class,
        PARAM_LEMMATIZER_DATA_FILE_NAME,
        lemmatizerDataFileName);
  }

  private MorphEnAnalyzer lemmatizer;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    if (this.lemmatizerDataFileName == null) {
      this.lemmatizerDataFileName = this.getClass().getResource(ENG_LEMMATIZER_DATA_FILE).toString();
    }

    try {
      lemmatizer = new MorphEnAnalyzer(new URL(lemmatizerDataFileName));
    } catch (MalformedURLException e) {
      lemmatizer = new MorphEnAnalyzer(lemmatizerDataFileName);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }

  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    for (Token token : JCasUtil.select(jCas, Token.class)) {
      token.setLemma(lemmatizer.getLemma(token.getCoveredText(), token.getPos()));
    }

  }

}
