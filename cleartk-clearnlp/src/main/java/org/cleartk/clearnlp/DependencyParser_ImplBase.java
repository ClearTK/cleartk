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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.annotations.Beta;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.utils.GlobalLexica;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPFeat;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.util.lang.TLanguage;
/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides the base implementation class for the UIMA/ClearTK wrapper for the ClearNLP
 * dependency parser. Subclasses should override methods for creating and setting properties on
 * dependency annotations
 * 
 * <p>
 * This parser is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
 * 
 * @author Lee Becker
 * 
 */
@Beta
public abstract class DependencyParser_ImplBase<WINDOW_TYPE extends Annotation, TOKEN_TYPE extends Annotation, DEPENDENCY_NODE_TYPE extends TOP, DEPENDENCY_ROOT_NODE_TYPE extends DEPENDENCY_NODE_TYPE, DEPENDENCY_RELATION_TYPE extends TOP>
    extends JCasAnnotator_ImplBase {

  public static final String DEFAULT_MODEL_PATH = "general-en-dep.xz";

  public static final String PARAM_PARSER_MODEL_PATH = "parserModelPath";

  @ConfigurationParameter(
      name = PARAM_PARSER_MODEL_PATH,
      mandatory = false,
      description = "This parameter provides the file name of the dependency parser model required by the factory method provided by ClearParserUtil.",
      defaultValue=DEFAULT_MODEL_PATH)
  private String parserModelPath;


  public static final String PARAM_LANGUAGE_CODE = "languageCode";

  @ConfigurationParameter(
      name = PARAM_LANGUAGE_CODE,
      mandatory = false,
      description = "Language code for the dependency parser (default value=en).",
      defaultValue = "ENGLISH")
  private String languageCode;

  public static final String PARAM_WINDOW_CLASS = "windowClass";

  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "By default, the tokenizer will tokenize a document sentence by sentence.  If you do not want to precede tokenization with"
      + "sentence segmentation, then a reasonable value for this parameter is 'org.apache.uima.jcas.tcas.DocumentAnnotation'";

  @ConfigurationParameter(
      name = PARAM_WINDOW_CLASS,
      mandatory = false,
      description = WINDOW_TYPE_DESCRIPTION,
      defaultValue = "org.cleartk.token.type.Sentence")
  private Class<WINDOW_TYPE> windowClass;

  private TokenOps<TOKEN_TYPE> tokenOps;

  private DependencyOps<DEPENDENCY_NODE_TYPE, TOKEN_TYPE, DEPENDENCY_ROOT_NODE_TYPE, WINDOW_TYPE, DEPENDENCY_RELATION_TYPE> dependencyOps;

  public DependencyParser_ImplBase(
      TokenOps<TOKEN_TYPE> tokenOps,
      DependencyOps<DEPENDENCY_NODE_TYPE, TOKEN_TYPE, DEPENDENCY_ROOT_NODE_TYPE, WINDOW_TYPE, DEPENDENCY_RELATION_TYPE> dependencyOps) {
    this.tokenOps = tokenOps;
    this.dependencyOps = dependencyOps;
  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    
    // FIXME: Is this possible to put into a shared resource so it stays in memory for all annotators?
    // initialize global lexica
    List<String> paths = new ArrayList<>();
    paths.add("brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz");
    GlobalLexica.initDistributionalSemanticsWords(paths);

    this.parser = NLPUtils.getDEPParser(TLanguage.getType(languageCode), this.parserModelPath, new DEPConfiguration("root"));
    
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    for (WINDOW_TYPE window : JCasUtil.select(jCas, this.windowClass)) {
      List<TOKEN_TYPE> tokens = this.tokenOps.selectTokens(jCas, window);

      // Extract data from CAS and stuff it into ClearNLP data structures
      DEPTree tree = new DEPTree(tokens.size());
      for (int i = 0; i < tokens.size(); i++) {
        TOKEN_TYPE token = tokens.get(i);
        String lemma = this.tokenOps.getLemma(jCas, token);
        String pos = this.tokenOps.getPos(jCas, token);
        DEPNode node = new DEPNode(i + 1, token.getCoveredText(), lemma, pos, new DEPFeat());
        tree.add(node);
      }

      // Run the parser
      this.parser.process(tree);

      // convert ClearNLP output back into CAS type system annotation
      this.addTreeToCas(jCas, tree, window, tokens);
    }
  }

  /**
   * Takes parsed tree from ClearNLP and converts it into dependency type system.
   * 
   * @param jCas
   * @param tree
   * @param window
   * @param tokens
   */
  private void addTreeToCas(JCas jCas, DEPTree tree, WINDOW_TYPE window, List<TOKEN_TYPE> tokens) {

    ArrayList<DEPENDENCY_NODE_TYPE> nodes = new ArrayList<DEPENDENCY_NODE_TYPE>(tree.size());
    DEPENDENCY_ROOT_NODE_TYPE rootNode = this.dependencyOps.createRootNode(jCas, window);
    nodes.add(rootNode);

    for (int i = 0; i < tokens.size(); i++) {
      TOKEN_TYPE token = tokens.get(i);
      nodes.add(this.dependencyOps.createNode(jCas, token));
    }

    Multimap<DEPENDENCY_NODE_TYPE, DEPENDENCY_RELATION_TYPE> headRelations = HashMultimap.create();
    Multimap<DEPENDENCY_NODE_TYPE, DEPENDENCY_RELATION_TYPE> childRelations = HashMultimap.create();
    // extract relation arcs from ClearNLP parse tree
    for (int i = 0; i < tree.size(); i++) {
      DEPNode parserNode = tree.get(i);
      if (parserNode.hasHead()) {
        int headIndex = parserNode.getHead().getID();
        DEPENDENCY_NODE_TYPE node = nodes.get(i);
        DEPENDENCY_NODE_TYPE headNode = nodes.get(headIndex);
        DEPENDENCY_RELATION_TYPE rel = this.dependencyOps.createRelation(
            jCas,
            headNode,
            node,
            parserNode.getLabel());

        headRelations.put(node, rel);
        childRelations.put(headNode, rel);
      }
    }

    // finalize nodes: add links between nodes and relations
    for (DEPENDENCY_NODE_TYPE node : nodes) {
      this.dependencyOps.setHeadRelations(jCas, node, Lists.newArrayList(headRelations.get(node)));
      this.dependencyOps.setChildRelations(jCas, node, Lists.newArrayList(childRelations.get(node)));
      node.addToIndexes();
    }
  }

  private AbstractComponent parser;
}
