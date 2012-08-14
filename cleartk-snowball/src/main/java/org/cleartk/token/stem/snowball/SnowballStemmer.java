/** 
 * Copyright (c) 2007-2009, Regents of the University of Colorado 
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
package org.cleartk.token.stem.snowball;

import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.UIMAUtil;
import org.tartarus.snowball.SnowballProgram;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * This class borrows from {@link SnowballFilter}
 * 
 * <br>
 * Copyright (c) 2007-2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */
public abstract class SnowballStemmer<TOKEN_TYPE extends Annotation> extends JCasAnnotator_ImplBase {

  public static final String PARAM_STEMMER_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      SnowballStemmer.class,
      "stemmerName");

  private static final String STEMMER_NAME_DESCRIPTION = "specifies which snowball stemmer to use. Possible values are: "
      + "Danish, Dutch, English, Finnish, French, German2, German, Italian, Kp, Lovins, Norwegian, Porter, Portuguese, Russian, Spanish, Swedish";

  @ConfigurationParameter(description = STEMMER_NAME_DESCRIPTION, mandatory = true)
  public String stemmerName;

  private SnowballProgram stemmer;

  private Class<? extends Annotation> tokenClass;

  private Type tokenType = null;

  private boolean typesInitialized = false;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    String className = String.format("org.tartarus.snowball.ext.%sStemmer", stemmerName);
    this.stemmer = InitializableFactory.create(null, className, SnowballProgram.class);

    this.tokenClass = ReflectionUtil.<Class<? extends Annotation>> uncheckedCast(ReflectionUtil.getTypeArgument(
        SnowballStemmer.class,
        "TOKEN_TYPE",
        this));
  }

  private void initializeTypes(JCas jCas) {
    if (tokenClass != null) {
      tokenType = UIMAUtil.getCasType(jCas, tokenClass);
    }
    typesInitialized = true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    if (!typesInitialized)
      initializeTypes(jCas);

    FSIterator<Annotation> tokens = jCas.getAnnotationIndex(tokenType).iterator();
    while (tokens.hasNext()) {
      TOKEN_TYPE token = (TOKEN_TYPE) tokens.next();
      stemmer.setCurrent(token.getCoveredText().toLowerCase());
      stemmer.stem();
      String stem = stemmer.getCurrent();
      setStem(token, stem);
    }
  }

  public abstract void setStem(TOKEN_TYPE token, String stem);

  public void setStemmerName(String stemmerName) {
    this.stemmerName = stemmerName;
  }

}
