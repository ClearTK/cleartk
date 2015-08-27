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

import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.test.util.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Lee Becker
 */
public class ClearNlpTest extends CleartkTestBase {
  protected TokenBuilder<Token, Sentence> tokenBuilder;

  protected AnalysisEngineDescription postagger;

  protected AnalysisEngineDescription lemmatizer;

  protected AnalysisEngineDescription depparser;

  protected AnalysisEngineDescription srlabeler;

//  private void initLowMemModels() throws Exception {
//    this.postagger = PosTagger.getDescription();
//    this.lemmatizer = MpAnalyzer.getDescription();
//    this.depparser = DependencyParser.getDescription(
//        "en",
//        "general-en");
//        //ClearNlpTest.class.getResource("/models/sample-en-dep-1.3.0.tgz").toURI());
//
//    this.srlabeler = SemanticRoleLabeler.getDescription(
//        "en",
//        ClearNlpTest.class.getResource("/models/sample-en-pred-1.3.0.tgz").toURI(),
//        ClearNlpTest.class.getResource("/models/sample-en-role-1.3.0.tgz").toURI(),
//        ClearNlpTest.class.getResource("/models/sample-en-srl-1.3.0.tgz").toURI());
//  }

  private void initDefaultModels() throws ResourceInitializationException {
    this.lemmatizer = MpAnalyzer.getDescription();
    this.depparser = DependencyParser.getDescription();
    this.srlabeler = SemanticRoleLabeler.getDescription();
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");
  }

  /**
   * Test description parameters to catch errors like Issue 382.
   */
  @Test
  public void testDescriptionParameters() throws Exception {
    String code = "xq";
    String depParserPath = "foopath";
    String predIdPath = "foo1";
    String rolesetPath = "foo2";
    String srlPath = "foo3";
    AnalysisEngineDescription desc;

    desc = MpAnalyzer.getDescription("xq");
    assertParameterValue(code, desc, MpAnalyzer_ImplBase.PARAM_LANGUAGE_CODE);

    desc = DependencyParser.getDescription(code, depParserPath);
    assertParameterValue(code, desc, DependencyParser_ImplBase.PARAM_LANGUAGE_CODE);
    assertParameterValue(depParserPath, desc, DependencyParser_ImplBase.PARAM_PARSER_MODEL_PATH);

    desc = SemanticRoleLabeler.getDescription(code, predIdPath, rolesetPath, srlPath);
    assertParameterValue(code, desc, SemanticRoleLabeler_ImplBase.PARAM_LANGUAGE_CODE);
    assertParameterValue(srlPath, desc, SemanticRoleLabeler_ImplBase.PARAM_SRL_MODEL_PATH);
  }

  private static void assertParameterValue(
      Object expected,
      AnalysisEngineDescription desc,
      String name) {
    Assert.assertEquals(
        expected,
        desc.getAnalysisEngineMetaData().getConfigurationParameterSettings().getParameterValue(name));
  }

  //@Test
//  public void srlLowMemTest() throws Exception {
//
//    this.initLowMemModels();
//    this.jCas.reset();
//    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");
//
//    tokenBuilder.buildTokens(
//        jCas,
//        "John still drives the car Mary gave him in 1982 .",
//        "John still drives the car Mary gave him in 1982 .");
//        //"NNP  RB    VBZ    DT  NN  NNP  VBD  PRP IN CD .");
//    SimplePipeline.runPipeline(jCas, postagger, depparser, srlabeler);
//
//    // Check dependency relations
//    Set<String> expectedDep = Sets.newHashSet(
//        "root(TOP, drives)",
//        "nsubj(drives, John)",
//        "advmod(drives, still)",
//        "nsubj(gave, Mary)",
//        "pobj(in, 1982)",
//        "punct(drives, .)");
//    Set<String> actualDep = Sets.newHashSet();
//    for (DependencyNode depnode : JCasUtil.select(jCas, DependencyNode.class)) {
//      for (DependencyRelation deprel : JCasUtil.select(
//          depnode.getHeadRelations(),
//          DependencyRelation.class)) {
//        DependencyNode head = deprel.getHead();
//        if (head instanceof TopDependencyNode) {
//          actualDep.add(String.format("%s(TOP, %s)", deprel.getRelation(), depnode.getCoveredText()));
//        } else {
//          actualDep.add(String.format(
//              "%s(%s, %s)",
//              deprel.getRelation(),
//              deprel.getHead().getCoveredText(),
//              depnode.getCoveredText()));
//        }
//      }
//    }
//    // take the subset of actualDep that's being tested (the parser may get some of the others
//    // wrong)
//    actualDep.retainAll(expectedDep);
//    Assert.assertEquals(expectedDep, actualDep);
//
//    // Check SRL relations
//    List<String> expectedSrl = Arrays.asList(
//    // "A0(drives, John)", "AM-ADV(drives, still)", "A1(drives, gave)", "A1(drives, him)",
//    // "AM-LOC(drives, in)", "A0(gave, Mary)", "A1(gave, him)", "AM-LOC(gave, in)");
//        "A0(drives, John)",
//        "AM-ADV(drives, still)",
//        "A1(drives, gave)",
//        "A0(gave, Mary)");
//    List<String> actualSrl = Lists.newArrayList();
//    for (Predicate pred : JCasUtil.select(jCas, Predicate.class)) {
//      pred.getArguments();
//      for (SemanticArgument arg : JCasUtil.select(pred.getArguments(), SemanticArgument.class)) {
//        actualSrl.add(String.format(
//            "%s(%s, %s)",
//            arg.getLabel(),
//            pred.getCoveredText(),
//            arg.getCoveredText()));
//      }
//    }
//
//    Assert.assertEquals(expectedSrl, actualSrl);
//  }

  @Test
  public void srlTest() throws Exception {
    this.assumeBigMemoryTestsEnabled();
    this.assumeLongTestsEnabled();
    //this.logger.info(BIG_MEMORY_TEST_MESSAGE);
    //this.logger.info(LONG_TEST_MESSAGE);
    this.initDefaultModels();

    this.jCas.reset();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");

    tokenBuilder.buildTokens(
        jCas,
        "John still drives the car Mary gave him in 1982 .",
        "John still drives the car Mary gave him in 1982 .",
        "NNP  RB    VBZ    DT  NN  NNP  VBD  PRP IN CD .");
    SimplePipeline.runPipeline(jCas, lemmatizer, depparser, srlabeler);

    // Check dependency relations
    List<String> expectedDep = Arrays.asList(
        "nsubj(drives, John)",
        "advmod(drives, still)",
        "root(TOP, drives)",
        "det(car, the)",
        "dobj(drives, car)",
        "nsubj(gave, Mary)",
        "relcl(car, gave)",
        "dobj(gave, him)",
        "prep(gave, in)",
        "pobj(in, 1982)",
        "punct(drives, .)");

    List<String> actualDep = Lists.newArrayList();
    for (DependencyNode depnode : JCasUtil.select(jCas, DependencyNode.class)) {
      for (DependencyRelation deprel : JCasUtil.select(
          depnode.getHeadRelations(),
          DependencyRelation.class)) {
        DependencyNode head = deprel.getHead();
        if (head instanceof TopDependencyNode) {
          actualDep.add(String.format("%s(TOP, %s)", deprel.getRelation(), depnode.getCoveredText()));
        } else {
          actualDep.add(String.format(
              "%s(%s, %s)",
              deprel.getRelation(),
              deprel.getHead().getCoveredText(),
              depnode.getCoveredText()));
        }

      }
    }
    Assert.assertEquals(expectedDep, actualDep);

    // Check SRL relations
    List<String> expectedSrl = Arrays.asList(
        "A0(drives, John)",
        "TMP(drives, still)",
        "A1(drives, car)",
        "A1(gave, car)",
        "A0(gave, Mary)",
        "GOL(gave, him)",
        "TMP(gave, in)");

    List<String> actualSrl = Lists.newArrayList();
    for (Predicate pred : JCasUtil.select(jCas, Predicate.class)) {
      pred.getArguments();
      for (SemanticArgument arg : JCasUtil.select(pred.getArguments(), SemanticArgument.class)) {
        actualSrl.add(String.format(
            "%s(%s, %s)",
            arg.getLabel(),
            pred.getCoveredText(),
            arg.getCoveredText()));
      }
    }

    Assert.assertEquals(expectedSrl, actualSrl);
  }

}
