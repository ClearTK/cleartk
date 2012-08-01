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

package org.cleartk.srl.dependency.clear;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.dependency.clear.ClearParserUtil;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.dep.srl.SRLHead;
import clear.parse.AbstractSRLParser;
import clear.reader.AbstractReader;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA wrapper for the CLEAR parser Semantic Role Labeler. This parser is
 * available here.
 * <p>
 * http://code.google.com/p/clearparser/
 * <p>
 * Before using this AnalysisEngine, you should run a Tokenizer, POS-tagger, Lemmatizer, and the
 * CLEAR parser dependency parser.
 * <p>
 * Please see /cleartk-clearparser/src/main/resources/org/cleartk/srl/dependency/clear/README for
 * important information pertaining to the models provided for this parser.
 * <p>
 * 
 * @author Lee Becker
 * 
 */
@TypeCapability(inputs = {
    "org.cleartk.token.type.Token:pos",
    "org.cleartk.token.type.Token:lemma",
    "org.cleartk.syntax.dependency.type.DependencyNode" })
public class ClearParserSRL extends JCasAnnotator_ImplBase {

  @Deprecated
  public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem");

  // TODO: change this
  public static final String DEFAULT_MODEL_FILE_NAME = "en_srl_ontonotes.jar";

  public static final String PARAM_PARSER_MODEL_FILE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      ClearParserSRL.class,
      "parserModelFileName");

  @ConfigurationParameter(
      description = "This parameter provides the file name of the semantic role labeler model required by the factory method provided by ClearParserUtil.")
  private String parserModelFileName;

  private AbstractSRLParser parser;

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ClearParserSRL.class,
        TYPE_SYSTEM_DESCRIPTION);
  }

  public static AnalysisEngineDescription getDescription(String modelFileName)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ClearParserSRL.class,
        TYPE_SYSTEM_DESCRIPTION,
        PARAM_PARSER_MODEL_FILE_NAME,
        modelFileName);
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      URL parserModelURL = this.parserModelFileName == null
          ? ClearParserSRL.class.getResource(DEFAULT_MODEL_FILE_NAME)
          : new File(this.parserModelFileName).toURI().toURL();
      parser = ClearParserUtil.createSRLParser(parserModelURL.openStream());

    } catch (MalformedURLException e) {
      throw new ResourceInitializationException(e);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
      DepTree tree = new DepTree();

      // Build map between CAS dependency node and id for later creation of
      // ClearParser dependency node/tree
      Map<DependencyNode, Integer> depNodeToID = new HashMap<DependencyNode, Integer>();
      int nodeId = 1;
      for (DependencyNode depNode : JCasUtil.selectCovered(jCas, DependencyNode.class, sentence)) {
        if (depNode instanceof TopDependencyNode) {
          depNodeToID.put(depNode, 0);
        } else {
          depNodeToID.put(depNode, nodeId);
          nodeId++;
        }
      }

      // Initialize Token / Sentence info for the ClearParser Semantic Role Labeler
      for (int i = 0; i < tokens.size(); i++) {
        Token token = tokens.get(i);

        // Determine HeadId
        DepNode node = new DepNode();
        DependencyNode casDepNode = JCasUtil.selectCovered(jCas, DependencyNode.class, token).get(0);
        DependencyRelation headRelation = (DependencyRelation) casDepNode.getHeadRelations().get(0);
        DependencyNode head = headRelation.getHead();
        int headId = depNodeToID.get(head);

        // Populate Dependency Node / Tree information
        node.id = i + 1;
        node.form = token.getCoveredText();
        node.pos = token.getPos();
        node.lemma = token.getLemma();
        node.setHead(headId, headRelation.getRelation(), 0);
        tree.add(node);
      }
      tree.setPredicates(AbstractReader.LANG_EN);

      // Run the SRL
      parser.parse(tree);

      // Convert ClearParser SRL output to CAS types
      extractSRLInfo(jCas, tokens, tree);
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
  private void extractSRLInfo(JCas jCas, List<Token> tokens, DepTree tree) {
    Map<Integer, Predicate> headIdToPredicate = new HashMap<Integer, Predicate>();
    Map<Predicate, List<SemanticArgument>> predicateArguments = new HashMap<Predicate, List<SemanticArgument>>();

    // Start at node 1, since node 0 is considered the head of the sentence
    for (int i = 1; i < tree.size(); i++) {
      // Every ClearParser parserNode will contain an srlInfo field.
      DepNode parserNode = tree.get(i);
      Token token = tokens.get(i - 1);
      if (parserNode.srlInfo == null) {
        continue;
      }

      if (parserNode.srlInfo.isPredicate()) {
        int headId = i;
        if (!headIdToPredicate.containsKey(headId)) {
          // We have not encountered this predicate yet, so create it
          Predicate pred = this.createPredicate(jCas, parserNode.srlInfo.rolesetId, token);
          headIdToPredicate.put(headId, pred);
        }
      } else {
        for (SRLHead head : parserNode.srlInfo.heads) {
          Predicate predicate;

          // Determine which predicate this argument belongs to
          if (!headIdToPredicate.containsKey(head.headId)) {
            // The predicate hasn't been encountered, so create it
            Token headToken = tokens.get(head.headId - 1);
            predicate = this.createPredicate(jCas, parserNode.srlInfo.rolesetId, headToken);
            headIdToPredicate.put(head.headId, predicate);
          } else {
            predicate = headIdToPredicate.get(head.headId);
          }

          // Append this argument to the predicate's list of arguments
          if (!predicateArguments.containsKey(predicate)) {
            predicateArguments.put(predicate, new ArrayList<SemanticArgument>());
          }
          List<SemanticArgument> argumentList = predicateArguments.get(predicate);

          // Create the semantic argument and store for later link creation
          SemanticArgument argument = createArgument(jCas, head, token);
          argumentList.add(argument);
        }
      }
    }

    // Store Arguments in Predicate
    for (Map.Entry<Predicate, List<SemanticArgument>> entry : predicateArguments.entrySet()) {
      Predicate predicate = entry.getKey();
      List<SemanticArgument> arguments = entry.getValue();
      predicate.setArguments(UIMAUtil.toFSArray(jCas, arguments));
    }
  }

  private Predicate createPredicate(JCas jCas, String rolesetId, Token token) {
    Predicate pred = new Predicate(jCas, token.getBegin(), token.getEnd());
    pred.setFrameSet(rolesetId);
    pred.addToIndexes();
    return pred;
  }

  private SemanticArgument createArgument(JCas jCas, SRLHead head, Token token) {
    SemanticArgument argument = new SemanticArgument(jCas, token.getBegin(), token.getEnd());
    argument.setLabel(head.label);
    argument.addToIndexes();
    return argument;
  }

}
