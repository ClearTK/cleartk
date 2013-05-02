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
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.FSCollectionFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPDecode;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.reader.AbstractReader;

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
public class SemanticRoleLabeler extends JCasAnnotator_ImplBase {


  public static final String DEFAULT_PRED_ID_MODEL_FILE_NAME = "ontonotes-en-pred-1.3.0.tgz";
  public static final String DEFAULT_ROLESET_MODEL_FILE_NAME = "ontonotes-en-role-1.3.0.tgz";
  public static final String DEFAULT_SRL_MODEL_FILE_NAME = "ontonotes-en-srl-1.3.0.tgz";

  public static final String PARAM_SRL_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler.class, 
      "srlModelUri");

  @ConfigurationParameter(
      description = "This parameter provides the URI pointing to the semantic role labeler model.  If none is specified it will use the default ontonotes model.")
  private URI srlModelUri;

  public static final String PARAM_PRED_ID_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler.class,
      "predIdModelUri");
  @ConfigurationParameter(
      description = "This parameter provides the URI pointing to the predicate identifier model.  If none is specified it will use the default ontonotes model.")
  private URI predIdModelUri;
  
  public static final String PARAM_ROLESET_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler.class,
      "rolesetModelUri");

  @ConfigurationParameter(
      description = "This parameter provides the URI pointing to the role set classifier model.  If none is specified it will use the default ontonotes model.")
  private URI rolesetModelUri;

 
  public static final String PARAM_LANGUAGE_CODE = ConfigurationParameterFactory.createConfigurationParameterName(
      Tokenizer.class,
      "languageCode");
  
  @ConfigurationParameter(
      description = "Language code for the semantic role labeler (default value=en).",
      defaultValue= AbstractReader.LANG_EN)
  private String languageCode;


  @Override
  public void initialize(UimaContext aContext)
      throws ResourceInitializationException {
    super.initialize(aContext);

    try {
      URL predIdModelURL = (this.predIdModelUri == null) 
          ? SemanticRoleLabeler.class.getResource(DEFAULT_PRED_ID_MODEL_FILE_NAME).toURI().toURL() 
          : this.predIdModelUri.toURL();
      this.predIdentifier = EngineGetter.getComponent(predIdModelURL.openStream(), languageCode, NLPLib.MODE_PRED);
      
      URL rolesetModelUrl = (this.rolesetModelUri == null)
          ? SemanticRoleLabeler.class.getResource(DEFAULT_ROLESET_MODEL_FILE_NAME).toURI().toURL()
          : this.rolesetModelUri.toURL();
      this.roleSetClassifier = EngineGetter.getComponent(rolesetModelUrl.openStream(), languageCode, NLPLib.MODE_ROLE);

      URL srlModelURL = (this.srlModelUri == null) 
          ? SemanticRoleLabeler.class.getResource(DEFAULT_SRL_MODEL_FILE_NAME).toURI().toURL() 
          : this.srlModelUri.toURL();
      this.srlabeler = EngineGetter.getComponent(srlModelURL.openStream(), languageCode, NLPLib.MODE_SRL);
      
      this.clearNlpDecoder = new NLPDecode();


    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  /**
   * Convenience method for creating Analysis Engine for ClearNLP's dependency parser using default English model files
   */
  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(SemanticRoleLabeler.class);

  }


  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      boolean skipSentence = false;
      List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
      List<String> tokenStrings = JCasUtil.toText(tokens);

      // Build dependency tree from token information
      DEPTree tree = clearNlpDecoder.toDEPTree(tokenStrings);
      //DEPTree tree = new DEPTree();
      for (int i = 1; i < tree.size(); i++) {
        Token token = tokens.get(i-1);
        DEPNode node = tree.get(i);
        node.pos = token.getPos();
        node.lemma = token.getLemma();
        
            //Token token = tokens.get(i);
        //POSNode posNode = new POSNode(token.getCoveredText(), token.getPos(), token.getLemma());
        //DEPNode node = new DEPNode(i+1, posNode);
        //tree.add(node);
      }

      // Build map between CAS dependency node and id for later creation of
      // ClearParser dependency node/tree
      Map<DependencyNode, Integer> depNodeToID = Maps.newHashMap();
      int nodeId = 1;
      for (DependencyNode depNode : JCasUtil.selectCovered(jCas, DependencyNode.class, sentence)) {
        if (depNode instanceof TopDependencyNode) {
          depNodeToID.put(depNode, 0);
        } else {
          depNodeToID.put(depNode, nodeId);
          nodeId++;
        }
      }
      
      // Initialize Dependency Relations for ClearNLP input
      for (int i = 0; i < tokens.size(); i++) {
        Token token = tokens.get(i);

        // Determine node and head
        DependencyNode casDepNode = JCasUtil.selectCovered(jCas, DependencyNode.class, token).get(0);
        if (casDepNode.getHeadRelations().size() == 0) {
          // In cases where the sentence is unparseable we are left with only a root node
          // Thus the Semantic Role Labeler should skip this sentence
          skipSentence = true;
        } else {
          DependencyRelation headRelation = (DependencyRelation) casDepNode.getHeadRelations().get(0);
          DependencyNode head = headRelation.getHead();

          int id = i + 1;
          DEPNode node = tree.get(id);

          int headId = depNodeToID.get(head);
          DEPNode headNode = tree.get(headId);
          node.setHead(headNode, headRelation.getRelation());
        }
      }
      
      // Run the SRL
      if (!skipSentence) {
        this.predIdentifier.process(tree);
        this.roleSetClassifier.process(tree);
        this.srlabeler.process(tree);
        
        // Extract SRL information and create ClearTK CAS types
        this.extractSRLInfo(jCas, tokens, tree);
      }
      

    }
  }


  /**
   * Converts the output from the ClearParser Semantic Role Labeler to the ClearTK Predicate and
   * SemanticArgument Types.
   * 
   * @param jCas
   * @param tokens
   *          - In order list of tokens
   * @param tree
   *          - DepdendencyTree output by ClearParser SRLPredict
   */
  private void extractSRLInfo(JCas jCas, List<Token> tokens, DEPTree tree) {
    Map<Integer, Predicate> headIdToPredicate = Maps.newHashMap();
    Map<Predicate, List<SemanticArgument>> predicateArguments = Maps.newHashMap();

    // Start at node 1, since node 0 is considered the head of the sentence
    for (int i = 1; i < tree.size(); i++) {
      // Every ClearParser parserNode will contain an srlInfo field.
      DEPNode parserNode = tree.get(i);
      Token token = tokens.get(i - 1);
      
      List<DEPArc> semanticHeads = parserNode.getSHeads();
      if (semanticHeads.isEmpty()) { continue; }
      
      // Parse semantic head relations to get SRL triplets
      for (DEPArc shead : semanticHeads) {
        int headId = shead.getNode().id;
        Token headToken = tokens.get(headId - 1);
        Predicate pred;
        List<SemanticArgument> args;
        if (!headIdToPredicate.containsKey(headId)) {
          String rolesetId = shead.getNode().getFeat(DEPLib.FEAT_PB);
          pred = this.createPredicate(jCas, rolesetId, headToken);
          headIdToPredicate.put(headId, pred);
          args = Lists.newArrayList();
          predicateArguments.put(pred, args);
        } else {
          pred = headIdToPredicate.get(headId);
          args = predicateArguments.get(pred);
        }
        args.add(this.createArgument(jCas, shead, token));
      }
    }    
    
    // Store Arguments in Predicate
    for (Map.Entry<Predicate, List<SemanticArgument>> entry : predicateArguments.entrySet()) {
      Predicate predicate = entry.getKey();
      List<SemanticArgument> arguments = entry.getValue();
      predicate.setArguments(new FSArray(jCas, arguments.size()));
      FSCollectionFactory.fillArrayFS(predicate.getArguments(), arguments);
    }
    
      
  }

  private Predicate createPredicate(JCas jCas, String rolesetId, Token token) {
    Predicate pred = new Predicate(jCas, token.getBegin(), token.getEnd());
    pred.setFrameSet(rolesetId);
    pred.addToIndexes();
    return pred;
  }

  private SemanticArgument createArgument(JCas jCas, DEPArc head, Token token) {
    SemanticArgument argument = new SemanticArgument(jCas, token.getBegin(), token.getEnd());
    argument.setLabel(head.getLabel());
    argument.addToIndexes();
    return argument;
  }

  private AbstractComponent predIdentifier;
  private AbstractComponent roleSetClassifier;
  private AbstractComponent srlabeler;
  private NLPDecode clearNlpDecoder;
}
