/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */

package org.cleartk.syntax.berkeley;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.constituent.ParserWrapper_ImplBase;
import org.cleartk.util.IOUtil;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.Numberer;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Philip Ogren
 */

public class ParserAnnotator<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation, TOP_NODE_TYPE extends Annotation>
    extends ParserWrapper_ImplBase<TOKEN_TYPE, SENTENCE_TYPE, Tree<String>, TOP_NODE_TYPE> {

  public static final String PARAM_PARSER_MODEL_PATH = ConfigurationParameterFactory
      .createConfigurationParameterName(ParserAnnotator.class, "parserModelPath");

  @ConfigurationParameter
  private String parserModelPath;

  protected CoarseToFineMaxRuleParser parser;

  private int parseFailureCount = 0;

  private int sentenceCount = 0;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    System.out.print("initializing Berkeley Parser: " + parserModelPath + " ... ");

    long start = System.nanoTime();

    InputStream modelInputStream;
    try {
      modelInputStream = IOUtil.getInputStream(ParserAnnotator.class, parserModelPath);

      GZIPInputStream gzis = new GZIPInputStream(modelInputStream); // Compressed
      ObjectInputStream in = new ObjectInputStream(gzis); // Load objects
      ParserData parserData = (ParserData) in.readObject(); // Read the mix of grammars

      Grammar grammar = parserData.getGrammar();
      Lexicon lexicon = parserData.getLexicon();
      Numberer.setNumberers(parserData.getNumbs());
      parser = new CoarseToFineMaxRuleParser(
          grammar,
          lexicon,
          1.0,
          -1,
          false,
          false,
          false,
          false,
          false,
          true,
          true);
      long stop = System.nanoTime();
      float seconds = (float) (stop - start) / 1000000000;
      System.out.println("done.   Loaded in: " + seconds + " seconds");
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }

  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    List<SENTENCE_TYPE> sentenceList = inputTypesHelper.getSentences(jCas);

    for (SENTENCE_TYPE sentence : sentenceList) {
      sentenceCount++;
      List<TOKEN_TYPE> tokens = inputTypesHelper.getTokens(jCas, sentence);
      List<String> words = new ArrayList<String>();
      List<String> tags = new ArrayList<String>();

      for (TOKEN_TYPE token : tokens) {
        words.add(token.getCoveredText());
        String tag = inputTypesHelper.getPosTag(token);
        tags.add(tag);
      }

      Tree<String> tree = parser.getBestConstrainedParse(words, tags, null);
      if (tree.isLeaf()) {
        System.out.println("words: " + words.size() + "  " + words);
        System.out.println("tags: " + tags.size() + "  " + tags);
        System.out.println("unable to parse sentence: " + sentence.getCoveredText());
        parseFailureCount++;
      } else {
        outputTypesHelper.addParse(jCas, tree, sentence, tokens);
      }
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    System.out.println("total number of sentences that were not parsed was: " + parseFailureCount
        + " out of " + sentenceCount);
  }

  public static void main(String[] args) {
//  ParserData parserData = ParserData.Load("data/experiment/berkeley/fold1.gr");
    ParserData parserData = ParserData.Load("src/test/resources/models/11597317.gr");
    Grammar grammar = parserData.getGrammar();
    Lexicon lexicon = parserData.getLexicon();
    Numberer.setNumberers(parserData.getNumbs());

    CoarseToFineMaxRuleParser parser = new CoarseToFineMaxRuleParser(
        grammar,
        lexicon,
        1.0,
        -1,
        false,
        false,
        false,
        false,
        false,
        true,
        true);

    List<String> sentence = Arrays.asList(new String[] {
        "The",
        "striatum",
        "plays",
        "a",
        "pivotal",
        "role",
        "in",
        "modulating",
        "motor",
        "activity",
        "and",
        "higher",
        "cognitive",
        "function",
        "." });
    List<String> posTags = Arrays.asList(new String[] {
        "DT",
        "NN",
        "VBZ",
        "DT",
        "JJ",
        "NN",
        "IN",
        "VBG",
        "NN",
        "NN",
        "CC",
        "JJR",
        "JJ",
        "NN",
        "." });

    System.out.println("sentence size=" + sentence.size());
    System.out.println("posTags size=" + posTags.size());

    Tree<String> parsedTree = parser.getBestConstrainedParse(sentence, posTags, null);
    System.out.println(parsedTree);

  }
}
