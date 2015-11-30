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


import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

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
  
public class SemanticRoleLabeler extends SemanticRoleLabeler_ImplBase<Sentence, Token, DependencyNode, TopDependencyNode, DependencyRelation, SemanticArgument, Predicate> {
  
  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(SemanticRoleLabeler.class);
  }
  
  public static AnalysisEngineDescription getDescription(String languageCode, String predIdModelPath, String rolesetModelPath, String srlModelPath) throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(SemanticRoleLabeler.class, 
      SemanticRoleLabeler_ImplBase.PARAM_LANGUAGE_CODE,
      languageCode,
      SemanticRoleLabeler_ImplBase.PARAM_SRL_MODEL_PATH,
      srlModelPath);
  }

   public SemanticRoleLabeler() {
    super(new CleartkTokenOps(), new CleartkDependencyOps(), new CleartkSemanticRoleOps());
  }
}
