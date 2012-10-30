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


import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;


import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.syntax.dependency.type.DependencyRelation;
import org.cleartk.syntax.dependency.type.TopDependencyNode;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * @author Lee Becker
 */
public class ClearNLPTest extends CleartkTestBase {
	protected TokenBuilder<Token, Sentence> tokenBuilder;
	protected AnalysisEngineDescription lemmatizer;
	protected AnalysisEngineDescription depparser;
	protected AnalysisEngineDescription srlabeler;

	private void initLowMemModels() throws ResourceInitializationException {
	  this.lemmatizer = MPAnalyzer.getDescription();
	  this.depparser = AnalysisEngineFactory.createPrimitiveDescription(DependencyParser.class, 
	      DependencyParser.PARAM_PARSER_MODEL_URI,
	      new File("src/test/resources/models/sample-dep.jar").toURI());

	  this.srlabeler = AnalysisEngineFactory.createPrimitiveDescription(SemanticRoleLabeler.class, 
	      SemanticRoleLabeler.PARAM_PRED_ID_MODEL_URI,
	      new File("src/test/resources/models/sample-pred.jar").toURI(),
	      SemanticRoleLabeler.PARAM_SRL_MODEL_URI,
	      new File("src/test/resources/models/sample-srl.jar").toURI());
	}

	private void initDefaultModels() throws ResourceInitializationException {
	  this.lemmatizer = MPAnalyzer.getDescription();
	  this.depparser = DependencyParser.getDescription();
	  this.srlabeler = SemanticRoleLabeler.getDescription();
	}
	
  @Before
  public void setUp() throws Exception {
    super.setUp();
    tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");
  }
  
  

	@Test
	public void srlLowMemTest() throws Exception {
	  this.initLowMemModels();
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
		    "nsubj(drives, John)", "advmod(drives, still)", "root(TOP, drives)", "det(gave, the)", "nn(Mary, car)", "nsubj(gave, Mary)", 
		    "punct(drives, gave)", "punct(drives, him)", "prep(drives, in)", "pobj(in, 1982)", "punct(drives, .)");
		List<String> actualDep = Lists.newArrayList();
		for (DependencyNode depnode : JCasUtil.select(jCas, DependencyNode.class)) {
      for (DependencyRelation deprel : JCasUtil.select(depnode.getHeadRelations(), DependencyRelation.class)) {
        DependencyNode head = deprel.getHead();
        if (head instanceof TopDependencyNode) {
          actualDep.add(String.format("%s(TOP, %s)", deprel.getRelation(), depnode.getCoveredText()));
        } else {
          actualDep.add(String.format("%s(%s, %s)", deprel.getRelation(), deprel.getHead().getCoveredText(), depnode.getCoveredText()));
        }
            
      }
		}
    Assert.assertEquals(expectedDep, actualDep);
    
    // Check SRL relations
		List<String> expectedSrl = Arrays.asList(
		    "A0(drives, John)", "AM-ADV(drives, still)", "A1(drives, gave)", "A1(drives, him)", 
		    "AM-LOC(drives, in)", "A0(gave, Mary)", "A1(gave, him)", "AM-LOC(gave, in)");
		List<String> actualSrl = Lists.newArrayList();
    for (Predicate pred : JCasUtil.select(jCas, Predicate.class)) {
      pred.getArguments();
      for (SemanticArgument arg : JCasUtil.select(pred.getArguments(), SemanticArgument.class)) {
        actualSrl.add(String.format("%s(%s, %s)", arg.getLabel(), pred.getCoveredText(), arg.getCoveredText()));
      }
    }
    
    Assert.assertEquals(expectedSrl, actualSrl);
	}
	
	@Test
	public void srlTest() throws Exception {
    this.assumeBigMemoryTestsEnabled();
    this.logger.info(BIG_MEMORY_TEST_MESSAGE);
    
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
		    "nsubj(drives, John)", "advmod(drives, still)", "root(TOP, drives)", "det(gave, the)", "nn(Mary, car)", "nsubj(gave, Mary)", 
		    "punct(drives, gave)", "punct(drives, him)", "prep(drives, in)", "pobj(in, 1982)", "punct(drives, .)");
		List<String> actualDep = Lists.newArrayList();
		for (DependencyNode depnode : JCasUtil.select(jCas, DependencyNode.class)) {
      for (DependencyRelation deprel : JCasUtil.select(depnode.getHeadRelations(), DependencyRelation.class)) {
        DependencyNode head = deprel.getHead();
        if (head instanceof TopDependencyNode) {
          actualDep.add(String.format("%s(TOP, %s)", deprel.getRelation(), depnode.getCoveredText()));
        } else {
          actualDep.add(String.format("%s(%s, %s)", deprel.getRelation(), deprel.getHead().getCoveredText(), depnode.getCoveredText()));
        }
            
      }
		}
    Assert.assertEquals(expectedDep, actualDep);
    
		
    // Check SRL relations
		List<String> expectedSrl = Arrays.asList(
		    "A0(drives, John)", "AM-ADV(drives, still)", "A1(drives, gave)", "A1(drives, him)", 
		    "AM-LOC(drives, in)", "A0(gave, Mary)", "A1(gave, him)", "AM-LOC(gave, in)");
		List<String> actualSrl = Lists.newArrayList();
    for (Predicate pred : JCasUtil.select(jCas, Predicate.class)) {
      pred.getArguments();
      for (SemanticArgument arg : JCasUtil.select(pred.getArguments(), SemanticArgument.class)) {
        actualSrl.add(String.format("%s(%s, %s)", arg.getLabel(), pred.getCoveredText(), arg.getCoveredText()));
      }
    }
    
    Assert.assertEquals(expectedSrl, actualSrl);
	}
	
	
    
	
}
