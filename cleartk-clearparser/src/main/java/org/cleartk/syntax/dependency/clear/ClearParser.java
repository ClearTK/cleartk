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

package org.cleartk.syntax.dependency.clear;

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
import clear.parse.AbstractDepParser;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA wrapper for the CLEAR parser. This parser is available here:
 * <p>
 * http://code.google.com/p/clearparser/
 * <p>
 * Please see
 * /cleartk-syntax-dependency-clear/src/main/resources/org/cleartk/syntax/dependency/clear/README
 * for important information pertaining to the models provided for this parser. In particular, note
 * that the output of the CLEAR parser is different than that of the Malt parser and so these two
 * parsers may not be interchangeable (without some effort) for most use cases.
 * <p>
 * 
 * @author Philip Ogren
 * 
 */
@TypeCapability(
    inputs = { "org.cleartk.token.type.Token:pos", "org.cleartk.token.type.Token:lemma" })
public class ClearParser extends JCasAnnotator_ImplBase {

  @Deprecated
  public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem");

  public static final String DEFAULT_MODEL_FILE_NAME = "conll-2009-dev-shift-pop.jar";

  public static final String DEFAULT_PARSER_ALGORITHM_NAME = AbstractDepParser.ALG_SHIFT_POP;

  public static final String PARAM_PARSER_MODEL_FILE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      ClearParser.class,
      "parserModelFileName");

  @ConfigurationParameter(
      description = "This parameter provides the file name of the dependency parser model required by the factory method provided by ClearParserUtil.")
  private String parserModelFileName;

  public static final String PARAM_PARSER_ALGORITHM_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
      ClearParser.class,
      "parserAlgorithmName");

  @ConfigurationParameter(
      defaultValue = DEFAULT_PARSER_ALGORITHM_NAME,
      mandatory = true,
      description = "This parameter provides the algorithm name used by the dependency parser that is required by the factory method provided by ClearParserUtil.  If in doubt, do not change from the default value.")
  private String parserAlgorithmName;

  private AbstractDepParser parser;

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ClearParser.class,
        TYPE_SYSTEM_DESCRIPTION);
  }

  public static AnalysisEngineDescription getDescription(String modelFileName)
      throws ResourceInitializationException {
    String algorithmName = DEFAULT_PARSER_ALGORITHM_NAME;
    return getDescription(modelFileName, algorithmName);
  }

  public static AnalysisEngineDescription getDescription(
      String modelFileName,
      String parserAlgorithmName) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ClearParser.class,
        TYPE_SYSTEM_DESCRIPTION,
        PARAM_PARSER_MODEL_FILE_NAME,
        modelFileName,
        PARAM_PARSER_ALGORITHM_NAME,
        parserAlgorithmName);
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      URL parserModelURL = this.parserModelFileName == null
          ? ClearParser.class.getResource(DEFAULT_MODEL_FILE_NAME)
          : new File(this.parserModelFileName).toURI().toURL();
      parser = ClearParserUtil.createParser(parserModelURL.openStream(), parserAlgorithmName);
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

      for (int i = 0; i < tokens.size(); i++) {
        Token token = tokens.get(i);
        DepNode node = new DepNode();
        node.id = i + 1;
        node.form = token.getCoveredText();
        node.pos = token.getPos();
        node.lemma = token.getLemma();
        tree.add(node);
      }
      parser.parse(tree);

      addTree(jCas, sentence, tokens, tree);
    }
  }

  private void addTree(JCas jCas, Sentence sentence, List<Token> tokens, DepTree tree) {
    DependencyNode[] nodes = new DependencyNode[tree.size()];

    DependencyNode rootNode = new TopDependencyNode(jCas, sentence.getBegin(), sentence.getEnd());
    rootNode.addToIndexes();
    nodes[0] = rootNode;

    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      nodes[i + 1] = new DependencyNode(jCas, token.getBegin(), token.getEnd());
    }

    Map<DependencyNode, List<DependencyRelation>> headRelations;
    headRelations = new HashMap<DependencyNode, List<DependencyRelation>>();
    Map<DependencyNode, List<DependencyRelation>> childRelations;
    childRelations = new HashMap<DependencyNode, List<DependencyRelation>>();
    for (int i = 0; i < tree.size(); i++) {
      DepNode parserNode = tree.get(i);
      if (parserNode.hasHead) {
        int headIndex = parserNode.headId;

        DependencyNode node = nodes[i];
        DependencyNode headNode = nodes[headIndex];
        DependencyRelation rel = new DependencyRelation(jCas);
        rel.setChild(node);
        rel.setHead(headNode);
        rel.setRelation(parserNode.deprel);

        if (!headRelations.containsKey(node)) {
          headRelations.put(node, new ArrayList<DependencyRelation>());
        }
        headRelations.get(node).add(rel);
        if (!childRelations.containsKey(headNode)) {
          childRelations.put(headNode, new ArrayList<DependencyRelation>());
        }
        childRelations.get(headNode).add(rel);
      }
    }

    // finalize nodes: add links between nodes and relations
    for (DependencyNode node : nodes) {
      node.setHeadRelations(UIMAUtil.toFSArray(jCas, headRelations.get(node)));
      node.setChildRelations(UIMAUtil.toFSArray(jCas, childRelations.get(node)));
      node.addToIndexes();
    }
  }

}
