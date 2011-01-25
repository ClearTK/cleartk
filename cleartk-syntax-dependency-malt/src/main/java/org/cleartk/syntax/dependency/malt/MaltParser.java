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
package org.cleartk.syntax.dependency.malt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class MaltParser extends JCasAnnotator_ImplBase {

  public static final String ENGMALT_RESOURCE_NAME = "/models/engmalt.linear.mco";

  private static TypeSystemDescription getTypeSystem() {
    return TypeSystemDescriptionFactory.createTypeSystemDescription(
        "org.cleartk.token.TypeSystem",
        "org.cleartk.syntax.dependency.TypeSystem");
  }

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    // get the resource path and strip the ".mco" suffix
    String fileName = MaltParser.class.getResource(ENGMALT_RESOURCE_NAME).getFile();
    String fileBase = fileName.substring(0, fileName.length() - 4);
    return getDescription(fileBase);
  }

  public static AnalysisEngineDescription getDescription(String modelFileName)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        MaltParser.class,
        getTypeSystem(),
        PARAM_MODEL_FILE_NAME,
        modelFileName);
  }

  @ConfigurationParameter(description = "The path to the model file, without the .mco suffix.", mandatory = true)
  private String modelFileName;

  public static final String PARAM_MODEL_FILE_NAME = ConfigurationParameterFactory
      .createConfigurationParameterName(MaltParser.class, "modelFileName");

  private MaltParserService service;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      OptionManager.instance().loadOptionDescriptionFile();
      OptionManager.instance().getOptionDescriptions().generateMaps();
      this.service = new MaltParserService();
      File modelFile = new File(this.modelFileName);
      String fileName = modelFile.getName();
      String workingDirectory = modelFile.getParent();
      String command = String.format("-c %s -m parse -w %s", fileName, workingDirectory);
      this.service.initializeParserModel(command);
    } catch (MaltChainedException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    try {
      this.service.terminateParserModel();
    } catch (MaltChainedException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Sentence sentence : AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
      List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, sentence, Token.class);

      // convert tokens into MaltParser input array
      List<String> inputStrings = new ArrayList<String>();
      int lineNo = -1;
      for (Token token : tokens) {
        lineNo += 1;
        String text = token.getCoveredText();
        String pos = token.getPos();

        // line format is <index>\t<word>\t_\t<pos>\t<pos>\t_
        String lineFormat = "%1$d\t%2$s\t_\t%3$s\t%3$s\t_";
        inputStrings.add(String.format(lineFormat, lineNo + 1, text, pos));
      }

      try {
        // parse with MaltParser
        String[] input = inputStrings.toArray(new String[inputStrings.size()]);
        DependencyStructure graph = this.service.parse(input);

        // convert MaltParser structure into annotations
        Map<Integer, DependencyNode> nodes = new HashMap<Integer, DependencyNode>();
        SortedSet<Integer> tokenIndices = graph.getTokenIndices();
        for (int i : tokenIndices) {
          org.maltparser.core.syntaxgraph.node.DependencyNode maltNode = graph.getTokenNode(i);
          Token token = tokens.get(maltNode.getIndex() - 1);
          DependencyNode node = new DependencyNode(jCas, token.getBegin(), token.getEnd());
          node.addToIndexes();
          nodes.put(i, node);
        }

        // add head links between node annotations
        SymbolTable table = graph.getSymbolTables().getSymbolTable("DEPREL");
        Map<DependencyNode, List<DependencyNode>> nodeChildren;
        nodeChildren = new HashMap<DependencyNode, List<DependencyNode>>();
        for (int i : tokenIndices) {
          org.maltparser.core.syntaxgraph.node.DependencyNode maltNode = graph.getTokenNode(i);
          int headIndex = maltNode.getHead().getIndex();
          if (headIndex != 0) {
            String label = maltNode.getHeadEdge().getLabelSymbol(table);
            DependencyNode node = nodes.get(i);
            DependencyNode head = nodes.get(headIndex);
            node.setHead(head);
            node.setDependencyType(label);

            // collect child information
            if (!nodeChildren.containsKey(head)) {
              nodeChildren.put(head, new ArrayList<DependencyNode>());
            }
            nodeChildren.get(head).add(node);
          }
        }

        // add child links between node annotations
        for (DependencyNode head : nodeChildren.keySet()) {
          head.setChildren(UIMAUtil.toFSArray(jCas, nodeChildren.get(head)));
        }
        for (DependencyNode node : JCasUtil.iterate(jCas, DependencyNode.class)) {
          if (node.getChildren() == null) {
            node.setChildren(new FSArray(jCas, 0));
          }
        }

      } catch (MaltChainedException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }
}
