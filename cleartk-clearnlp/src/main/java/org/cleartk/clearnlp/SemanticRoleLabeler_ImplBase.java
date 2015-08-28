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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.srl.SRLConfiguration;
import edu.emory.clir.clearnlp.component.utils.GlobalLexica;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPLib;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.util.arc.SRLArc;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA/ClearTK wrapper for the ClearNLP semantic role labeler. A typical
 * pipeline preceding this analysis engine would consist of a tokenizer, sentence segmenter, POS
 * tagger, lemmatizer (mp analyzer), and dependency parser.
 * <p>
 * The ClearNLP labeler is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
 * 
 * @author Lee Becker
 * 
 */
@Beta
public abstract class SemanticRoleLabeler_ImplBase<WINDOW_TYPE extends Annotation, TOKEN_TYPE extends Annotation, DEPENDENCY_NODE_TYPE extends TOP, DEPENDENCY_ROOT_NODE_TYPE extends DEPENDENCY_NODE_TYPE, DEPENDENCY_RELATION_TYPE extends TOP, ARGUMENT_TYPE extends TOP, PREDICATE_TYPE extends TOP>
    extends JCasAnnotator_ImplBase {

  public static final String DEFAULT_SRL_MODEL_PATH = "general-en-srl.xz";

  public static final String PARAM_SRL_MODEL_PATH = "srlModelPath";
  @ConfigurationParameter(
      name = PARAM_SRL_MODEL_PATH,
      mandatory = false,
      description = "This parameter provides the path pointing to the semantic role labeler model.  If none is specified it will use the default ontonotes model.",
      defaultValue=DEFAULT_SRL_MODEL_PATH)
  private String srlModelPath;

  public static final String PARAM_LANGUAGE_CODE = "languageCode";

  @ConfigurationParameter(
      name = PARAM_LANGUAGE_CODE,
      mandatory = false,
      description = "Language code for the semantic role labeler (default value=en).",
      defaultValue = "ENGLISH")
  private TLanguage languageCode;

  public static final String PARAM_WINDOW_CLASS = "windowClass";

  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "By default, the tokenizer will tokenize a document sentence by sentence.  If you do not want to precede tokenization with"
      + "sentence segmentation, then a reasonable value for this parameter is 'org.apache.uima.jcas.tcas.DocumentAnnotation'";

  @ConfigurationParameter(
      name = PARAM_WINDOW_CLASS,
      mandatory = false,
      description = WINDOW_TYPE_DESCRIPTION,
      defaultValue = "org.cleartk.token.type.Sentence")
  private Class<? extends WINDOW_TYPE> windowClass;

  private TokenOps<TOKEN_TYPE> tokenOps;

  private DependencyOps<DEPENDENCY_NODE_TYPE, TOKEN_TYPE, DEPENDENCY_ROOT_NODE_TYPE, WINDOW_TYPE, DEPENDENCY_RELATION_TYPE> dependencyOps;

  private SemanticRoleOps<ARGUMENT_TYPE, TOKEN_TYPE, PREDICATE_TYPE, TOKEN_TYPE> srlOps;

  public SemanticRoleLabeler_ImplBase(
      TokenOps<TOKEN_TYPE> tokenOps,
      DependencyOps<DEPENDENCY_NODE_TYPE, TOKEN_TYPE, DEPENDENCY_ROOT_NODE_TYPE, WINDOW_TYPE, DEPENDENCY_RELATION_TYPE> dependencyOps,
      SemanticRoleOps<ARGUMENT_TYPE, TOKEN_TYPE, PREDICATE_TYPE, TOKEN_TYPE> srlOps) {
    this.tokenOps = tokenOps;
    this.dependencyOps = dependencyOps;
    this.srlOps = srlOps;
  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    
    // initialize global lexica
    // FIXME: This should probably be put in a shared resource for multiple analysis engines
    List<String> paths = new ArrayList<>();
    paths.add("brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz");
    GlobalLexica.initDistributionalSemanticsWords(paths);
    

    try {
      /*
      this.predIdentifier = NLPGetter.getComponent(
          this.predIdModelPath,
          languageCode,
          NLPMode.MODE_PRED);

      this.roleSetClassifier = NLPGetter.getComponent(
          this.rolesetModelPath,
          languageCode,
          NLPMode.MODE_ROLE);
          */

      this.srlabeler = NLPUtils.getSRLabeler(
          languageCode,
          this.srlModelPath,
          new SRLConfiguration(4, 3));

    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  /**
   * Convenience method for creating Analysis Engine for ClearNLP's dependency parser using default
   * English model files
   */
  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(SemanticRoleLabeler_ImplBase.class);

  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    for (WINDOW_TYPE window : JCasUtil.select(jCas, this.windowClass)) {
      boolean skipSentence = false;
      List<TOKEN_TYPE> tokens = this.tokenOps.selectTokens(jCas, window);
      List<String> tokenStrings = JCasUtil.toText(tokens);
      DEPENDENCY_ROOT_NODE_TYPE dependencyRoot = this.dependencyOps.selectRootNode(jCas, window);
      List<DEPENDENCY_NODE_TYPE> dependencyNodes = this.dependencyOps.selectNodes(jCas, window);
      if (dependencyNodes.size() != tokens.size()) {
        throw new IllegalArgumentException(String.format(
            "Expected one dependency node per token; found %d tokens and %d dependency nodes",
            tokens.size(),
            dependencyNodes.size()));
      }

      // Build dependency tree from token information
      DEPTree tree = new DEPTree(tokenStrings);
      // DEPTree tree = new DEPTree();
      for (int i = 1; i < tree.size(); i++) {
        TOKEN_TYPE token = tokens.get(i - 1);
        DEPNode node = tree.get(i);
        node.setPOSTag(this.tokenOps.getPos(jCas, token));

        node.setLemma(this.tokenOps.getLemma(jCas, token));
      }

      // Build map between CAS dependency node and id for later creation of
      // ClearParser dependency node/tree
      Map<DEPENDENCY_NODE_TYPE, Integer> depNodeToID = Maps.newHashMap();
      depNodeToID.put(dependencyRoot, 0);
      int nodeId = 1;
      for (DEPENDENCY_NODE_TYPE depNode : dependencyNodes) {
        depNodeToID.put(depNode, nodeId);
        nodeId++;
      }

      // Initialize Dependency Relations for ClearNLP input
      for (DEPENDENCY_NODE_TYPE casDepNode : dependencyNodes) {
        List<DEPENDENCY_RELATION_TYPE> relations = this.dependencyOps.getHeadRelations(
            jCas,
            casDepNode);
        if (relations.size() == 0) {
          // In cases where the sentence is unparseable we are left with only a root node
          // Thus the Semantic Role Labeler should skip this sentence
          skipSentence = true;
        } else if (relations.size() != 1) {
          throw new IllegalArgumentException("Expected 1 head, found " + relations.size());
        } else {
          for (DEPENDENCY_RELATION_TYPE relation : relations) {
            DEPENDENCY_NODE_TYPE head = this.dependencyOps.getHead(jCas, relation);
            String label = this.dependencyOps.getLabel(jCas, relation);
            DEPNode node = tree.get(depNodeToID.get(casDepNode));
            DEPNode headNode = tree.get(depNodeToID.get(head));
            node.setHead(headNode, label);
          }
        }
      }

      // Run the SRL
      if (!skipSentence) {
        //this.predIdentifier.process(tree);
        //this.roleSetClassifier.process(tree);
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
  private void extractSRLInfo(JCas jCas, List<TOKEN_TYPE> tokens, DEPTree tree) {
    Map<Integer, PREDICATE_TYPE> headIdToPredicate = Maps.newHashMap();
    Map<PREDICATE_TYPE, List<ARGUMENT_TYPE>> predicateArguments = Maps.newHashMap();

    // Start at node 1, since node 0 is considered the head of the sentence
    for (int i = 1; i < tree.size(); i++) {
      // Every ClearParser parserNode will contain an srlInfo field.
      DEPNode parserNode = tree.get(i);
      TOKEN_TYPE token = tokens.get(i - 1);

      List<SRLArc> semanticHeads = parserNode.getSemanticHeadArcList();
      if (semanticHeads.isEmpty()) {
        continue;
      }

      // Parse semantic head relations to get SRL triplets
      for (SRLArc shead : semanticHeads) {
        int headId = shead.getNode().getID();
        TOKEN_TYPE headToken = tokens.get(headId - 1);
        PREDICATE_TYPE pred;
        List<ARGUMENT_TYPE> args;
        if (!headIdToPredicate.containsKey(headId)) {
          String rolesetId = shead.getNode().getFeat(DEPLib.FEAT_PB);
          pred = this.srlOps.createPredicate(jCas, headToken, rolesetId);
          headIdToPredicate.put(headId, pred);
          args = Lists.newArrayList();
          predicateArguments.put(pred, args);
        } else {
          pred = headIdToPredicate.get(headId);
          args = predicateArguments.get(pred);
        }
        args.add(this.srlOps.createArgument(jCas, token, shead.getLabel()));
      }
    }

    // Store Arguments in Predicate
    for (Map.Entry<PREDICATE_TYPE, List<ARGUMENT_TYPE>> entry : predicateArguments.entrySet()) {
      PREDICATE_TYPE predicate = entry.getKey();
      List<ARGUMENT_TYPE> arguments = entry.getValue();
      this.srlOps.setPredicateArguments(jCas, predicate, arguments);
    }

  }

  private AbstractComponent srlabeler;
}
