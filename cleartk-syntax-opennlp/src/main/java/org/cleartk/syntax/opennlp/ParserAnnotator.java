/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.syntax.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.util.Span;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.SyntaxComponents;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.opennlp.parser.CasPosTagger;
import org.cleartk.syntax.opennlp.parser.InputTypesHelper;
import org.cleartk.syntax.opennlp.parser.Parser;
import org.cleartk.util.IOUtil;
import org.cleartk.util.ParamUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * This class provides a uima wrapper for the OpenNLP chunking parser that is specific to the
 * ClearTK type system found in the cleartk-syntax project. However, by specifying your own
 * implementations of {@link SentenceHelper} and {@link InputTypesHelper} you can use your own input
 * types for sentences, tokens, and part-of-speech tags. Regardless, the resulting syntactic trees
 * will use the ClearTK type system.
 * <p>
 * The default behavior of the OpenNLP chunking parser is to perform part-of-speech tagging in
 * addition to syntactic parsing. This may not be desirable in some situations where you have
 * already populated the CAS with part-of-speech tags. In such cases you can set the configuration
 * parameter with the name {@link #PARAM_USE_TAGS_FROM_CAS} to "true". This will bypass the
 * OpenNLP's part-of-speech tagger and instead use tags from the CAS as defined by the
 * implementation of {@link InputTypesHelper} you are using.
 * 
 * @author Philipp Wetzler, Philip Ogren.
 */
@TypeCapability(outputs = { "org.cleartk.syntax.constituent.type.TreebankNode",
    "org.cleartk.syntax.constituent.type.TerminalTreebankNode",
    "org.cleartk.syntax.constituent.type.TopTreebankNode" })
public class ParserAnnotator<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation> extends JCasAnnotator_ImplBase {

  public static final String DEFAULT_PARSER_MODEL_PATH = "/models/en-parser-chunking.bin";

  public static final int DEFAULT_BEAM_SIZE = Parser.defaultBeamSize;

  public static final float DEFAULT_ADVANCE_PERCENTAGE = (float) Parser.defaultAdvancePercentage;

  public static final String PARAM_PARSER_MODEL_PATH = ConfigurationParameterFactory
          .createConfigurationParameterName(ParserAnnotator.class, "parserModelPath");

  @ConfigurationParameter(defaultValue = DEFAULT_PARSER_MODEL_PATH, description = "provides the path of the OpenNLP parser model build file, e.g. /models/en-parser-chunking.bin.  See javadoc for opennlp.tools.parser.chunking.Parser.")
  private String parserModelPath;

  public static final String PARAM_BEAM_SIZE = ConfigurationParameterFactory
          .createConfigurationParameterName(ParserAnnotator.class, "beamSize");

  @ConfigurationParameter(defaultValue = "" + DEFAULT_BEAM_SIZE, description = "indicates the beam size that should be used in the parser's search.  See javadoc for opennlp.tools.parser.chunking.Parser.")
  private int beamSize;

  public static final String PARAM_ADVANCE_PERCENTAGE = ConfigurationParameterFactory
          .createConfigurationParameterName(ParserAnnotator.class, "advancePercentage");

  @ConfigurationParameter(defaultValue = "" + Parser.defaultAdvancePercentage, description = "indicates \"the amount of probability mass required of advanced outcomes\".  See javadoc for opennlp.tools.parser.chunking.Parser.")
  private float advancePercentage;

  public static final String PARAM_INPUT_TYPES_HELPER_CLASS_NAME = ConfigurationParameterFactory
          .createConfigurationParameterName(ParserAnnotator.class, "inputTypesHelperClassName");

  @ConfigurationParameter(defaultValue = "org.cleartk.syntax.opennlp.parser.DefaultInputTypesHelper", description = "provides the class name of the class used to read sentences from the document")
  private String inputTypesHelperClassName;

  protected InputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE> inputTypesHelper;

  public static final String PARAM_USE_TAGS_FROM_CAS = ConfigurationParameterFactory
          .createConfigurationParameterName(ParserAnnotator.class, "useTagsFromCas");

  @ConfigurationParameter(defaultValue = "false", description = "determines whether or not part-of-speech tags that are already in the CAS will be used or not.")
  private boolean useTagsFromCas;

  protected Parser parser;

  protected CasPosTagger<TOKEN_TYPE, SENTENCE_TYPE> casTagger;

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(UimaContext ctx) throws ResourceInitializationException {
    super.initialize(ctx);

    inputTypesHelper = InitializableFactory.create(ctx, inputTypesHelperClassName,
            InputTypesHelper.class);

    try {
      InputStream modelInputStream = IOUtil
              .getInputStream(SentenceAnnotator.class, parserModelPath);
      ParserModel parserModel = new ParserModel(modelInputStream);
      if (useTagsFromCas) {
        this.casTagger = new CasPosTagger<TOKEN_TYPE, SENTENCE_TYPE>(inputTypesHelper);
        this.parser = new Parser(parserModel, beamSize, advancePercentage, casTagger);
      } else {
        this.parser = new Parser(parserModel, beamSize, advancePercentage);
      }
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    String text = jCas.getDocumentText();

    List<SENTENCE_TYPE> sentenceList = inputTypesHelper.getSentences(jCas);

    for (SENTENCE_TYPE sentence : sentenceList) {

      Parse parse = new Parse(text, new Span(sentence.getBegin(), sentence.getEnd()),
              Parser.INC_NODE, 1, null);

      List<TOKEN_TYPE> tokenList = inputTypesHelper.getTokens(jCas, sentence);

      for (Annotation token : tokenList) {
        parse.insert(new Parse(text, new Span(token.getBegin(), token.getEnd()), Parser.TOK_NODE,
                0, 0));
      }

      if (useTagsFromCas) {
        this.casTagger.setTokens(tokenList);
      }

      parse = this.parser.parse(parse);

      // if the sentence was successfully parsed, add the tree to the
      // sentence
      if (parse.getType() == Parser.TOP_NODE) {
        TopTreebankNode topNode = (TopTreebankNode) buildAnnotation(parse, jCas);
        topNode.addToIndexes();
      }

      // add the POS tags to the tokens
      if (!useTagsFromCas) {
        setPOSTags(parse, tokenList.iterator(), jCas);
      }
    }
  }

  protected TreebankNode buildAnnotation(Parse p, JCas jCas) {
    TreebankNode myNode;
    if (p.getType() == Parser.TOP_NODE) {
      TopTreebankNode topNode = new TopTreebankNode(jCas);
      topNode.setParent(null);

      StringBuffer sb = new StringBuffer();
      p.show(sb);
      topNode.setTreebankParse(sb.toString());

      myNode = topNode;
    } else {
      myNode = new TreebankNode(jCas);
    }

    myNode.setNodeType(p.getType());
    myNode.setBegin(p.getSpan().getStart());
    myNode.setEnd(p.getSpan().getEnd());

    if (p.getChildCount() == 1 && p.getChildren()[0].getType() == Parser.TOK_NODE) {
      myNode.setLeaf(true);
      myNode.setNodeValue(p.getChildren()[0].toString());
      myNode.setChildren(new FSArray(jCas, 0));
    } else {
      myNode.setNodeValue(null);
      myNode.setLeaf(false);

      List<FeatureStructure> cArray = new ArrayList<FeatureStructure>(p.getChildCount());

      for (Parse cp : p.getChildren()) {
        TreebankNode cNode = buildAnnotation(cp, jCas);
        cNode.setParent(myNode);
        cNode.addToIndexes();
        cArray.add(cNode);
      }

      FSArray cFSArray = new FSArray(jCas, cArray.size());
      cFSArray.copyFromArray(cArray.toArray(new FeatureStructure[cArray.size()]), 0, 0,
              cArray.size());
      myNode.setChildren(cFSArray);
    }

    if (p.getType() == Parser.TOP_NODE) {
      List<TreebankNode> tList = getTerminals(myNode);
      FSArray tfsa = new FSArray(jCas, tList.size());
      tfsa.copyFromArray(tList.toArray(new FeatureStructure[tList.size()]), 0, 0, tList.size());
      ((TopTreebankNode) myNode).setTerminals(tfsa);
    }
    return myNode;
  }

  protected void setPOSTags(Parse p, Iterator<? extends Annotation> tokenIterator, JCas view) {
    if (p.isPosTag()) {
      Annotation t = tokenIterator.next();
      inputTypesHelper.setTag(t, p.getType());
    } else {
      for (Parse child : p.getChildren()) {
        setPOSTags(child, tokenIterator, view);
      }
    }
  }

  protected List<TreebankNode> getTerminals(TreebankNode node) {
    List<TreebankNode> tList = new ArrayList<TreebankNode>();

    if (node.getChildren().size() == 0) {
      tList.add(node);
      return tList;
    }

    TreebankNode[] children = Arrays.asList(node.getChildren().toArray()).toArray(
            new TreebankNode[node.getChildren().size()]);

    for (TreebankNode child : children) {
      tList.addAll(getTerminals(child));
    }
    return tList;
  }

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(ParserAnnotator.class,
            SyntaxComponents.TYPE_SYSTEM_DESCRIPTION, PARAM_PARSER_MODEL_PATH,
            ParamUtil.getParameterValue(PARAM_PARSER_MODEL_PATH, "/models/en-parser-chunking.bin"));
  }
}
