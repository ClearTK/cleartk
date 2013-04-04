/*
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.clearnlp;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA/ClearTK wrapper for the ClearNLP dependency parser. A typical
 * pipeline preceding this analysis engine would consist of a tokenizer, sentence segmenter,
 * POS tagger, and lemmatizer (mp analyzer).
 * <p>
 * This parser is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
* 
 * @author Lee Becker
 * 
 */
@TypeCapability(
    inputs = { 
        "org.cleartk.token.type.Sentence", 
        "org.cleartk.token.type.Token:pos", 
        "org.cleartk.token.type.Token:lemma" },
    outputs = { 
        "org.cleartk.syntax.dependency.type.TopDependencyNode",
        "org.cleartk.syntax.dependency.type.DependencyNode", 
        "org.cleartk.syntax.dependency.type.DependencyNode"})
public class DependencyParser extends DependencyParser_ImplBase<Sentence, Token, DependencyNode, TopDependencyNode, DependencyRelation> {
  /**
   * Convenience method for creating Analysis Engine for ClearNLP's dependency parser using default English model files
   */
  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(DependencyParser.class);
  }
  
  public static AnalysisEngineDescription getDescription(String languageCode, URI modelUri) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(DependencyParser.class,
        DependencyParser_ImplBase.PARAM_LANGUAGE_CODE,
        languageCode,
        DependencyParser_ImplBase.PARAM_PARSER_MODEL_URI,
        modelUri);
  }
	
  @Override
  protected Collection<Sentence> selectWindows(JCas jCas) {
    return JCasUtil.select(jCas, Sentence.class);
  }

  @Override
  protected List<Token> selectTokens(JCas jCas, Sentence sentence) {
    return JCasUtil.selectCovered(jCas, Token.class, sentence);
  }

  @Override
  protected String getLemma(JCas jCas, Token token) {
    return token.getLemma();
  }

  @Override
  protected String getPos(JCas jCas, Token token) {
    return token.getPos();
  }

  @Override
  protected TopDependencyNode createTopDependencyNode(JCas jCas, int begin, int end) {
    return new TopDependencyNode(jCas, begin, end);
  }

  @Override
  protected DependencyNode createDependencyNode(JCas jCas, int begin, int end) {
    return new DependencyNode(jCas, begin, end);
  }

  @Override
  protected DependencyRelation createRelation(JCas jCas, DependencyNode head, DependencyNode child, String relation) {
    DependencyRelation rel = new DependencyRelation(jCas);
    rel.setChild(child);
    rel.setHead(head);
    rel.setRelation(relation);
    return rel;
  }

  @Override
  protected void setHeadRelations(
      JCas jCas,
      DependencyNode node,
      List<DependencyRelation> headRelations) {
    node.setHeadRelations(UIMAUtil.toFSArray(jCas, headRelations));
    
  }

  @Override
  protected void setChildRelations(
      JCas jCas,
      DependencyNode node,
      List<DependencyRelation> childRelations) {
    node.setChildRelations(UIMAUtil.toFSArray(jCas, childRelations));
  }
}
