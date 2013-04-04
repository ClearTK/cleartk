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

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import com.googlecode.clearnlp.dependency.DEPArc;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA/ClearTK wrapper for the ClearNLP semantic role labeler. A typical
 * pipeline preceding this analysis engine would consist of a tokenizer, sentence segmenter,
 * POS tagger, lemmatizer (mp analyzer), and dependency parser.
 * <p>
 * The ClearNLP labeler is available here:
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
        "org.cleartk.token.type.Token:lemma",
        "org.cleartk.syntax.dependency.type.TopDependencyNode",
        "org.cleartk.syntax.dependency.type.DependencyNode", 
        "org.cleartk.syntax.dependency.type.DependencyNode"},
    outputs = {
        "org.cleartk.srl.type.Predicate",
        "org.cleartk.srl.type.SemanticArgument"} )
  
public class SemanticRoleLabeler extends SemanticRoleLabeler_ImplBase<Sentence, Token, DependencyNode, SemanticArgument, Predicate> {
  
  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(SemanticRoleLabeler.class);
  }
  
  public static AnalysisEngineDescription getDescription(String languageCode, URI predIdModelUri, URI rolesetModelUri, URI srlModelUri) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(SemanticRoleLabeler.class, 
      SemanticRoleLabeler_ImplBase.PARAM_LANGUAGE_CODE,
      languageCode,
      SemanticRoleLabeler_ImplBase.PARAM_PRED_ID_MODEL_URI,
      new File("src/test/resources/models/sample-en-pred-1.3.0.tgz").toURI(),
      SemanticRoleLabeler_ImplBase.PARAM_ROLESET_MODEL_URI,
      new File("src/test/resources/models/sample-en-role-1.3.0.tgz").toURI(),
      SemanticRoleLabeler_ImplBase.PARAM_SRL_MODEL_URI,
      new File("src/test/resources/models/sample-en-srl-1.3.0.tgz").toURI());
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
  protected boolean isTopNode(JCas jCas, DependencyNode depNode) {
    return depNode instanceof TopDependencyNode;
  }

  @Override
  protected boolean hasHeadRelation(JCas jCas, DependencyNode node) {
    return node.getHeadRelations().size() != 0;
    
  }

  @Override
  protected List<DependencyNode> selectDependencyNodes(JCas jCas, Sentence sentence) {
    return JCasUtil.selectCovered(jCas, DependencyNode.class, sentence);
  }

  @Override
  protected DependencyNode getDependencyNode(JCas jCas, Token token) {
    return JCasUtil.selectCovered(jCas, DependencyNode.class, token).get(0);
  }

  @Override
  protected String getHeadRelation(JCas jCas, DependencyNode node) {
    return node.getHeadRelations(0).getRelation();
  }

  @Override
  protected DependencyNode getHead(JCas jCas, DependencyNode node) {
    return node.getHeadRelations(0).getHead();
  }

  @Override
  protected SemanticArgument createArgument(JCas jCas, DEPArc head, Token token) {
    SemanticArgument argument = new SemanticArgument(jCas, token.getBegin(), token.getEnd());
    argument.setLabel(head.getLabel());
    argument.addToIndexes();
    return argument;
  }

  @Override
  protected Predicate createPredicate(JCas jCas, String rolesetId, Token token) {
    Predicate pred = new Predicate(jCas, token.getBegin(), token.getEnd());
    pred.setFrameSet(rolesetId);
    pred.addToIndexes();
    return pred;
  }

  @Override
  protected void setPredicateArguments(JCas jCas, Predicate predicate, List<SemanticArgument> arguments) {
    predicate.setArguments(UIMAUtil.toFSArray(jCas, arguments));
  }
}
