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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class MaltParserTest extends CleartkTestBase {

  private static final Logger LOGGER = Logger.getLogger(MaltParserTest.class.getName());

  @Rule
  public MethodRule memoryRule = new MemoryRule(2000000000L);

  public static class MemoryRule implements MethodRule {

    private long minMemory;

    public MemoryRule(long minMemory) {
      this.minMemory = minMemory;
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod method, Object object) {
      long maxMemory = Runtime.getRuntime().maxMemory();
      boolean enoughMemory = maxMemory >= this.minMemory;
      if (!enoughMemory) {
        String message = String.format(
            "Skipping %s: expected %s bytes memory, found %s",
            method.getName(),
            this.minMemory,
            maxMemory);
        System.err.println(message);
        throw new AssumptionViolatedException(message);
      }
      return statement;
    }
  }

  @Override
  public String[] getTypeSystemDescriptorNames() {
    return new String[] {
        "org.cleartk.token.TypeSystem",
        "org.cleartk.syntax.dependency.TypeSystem" };
  }

  @Test
  public void test() throws UIMAException {
    
    if (!RUN_BIGMEM_TESTS) {
      LOGGER.info(BIGMEM_TEST_MESSAGE);
    }
    
    assumeTrue(RUN_BIGMEM_TESTS);
    
    
    
    TokenBuilder<Token, Sentence> tokenBuilder = new TokenBuilder<Token, Sentence>(
        Token.class,
        Sentence.class,
        "pos",
        "stem");
    tokenBuilder.buildTokens(
        this.jCas,
        "The dog chased the fox down the road.",
        "The dog chased the fox down the road .",
        "DT NN VBD DT NN IN DT NN .");

    AnalysisEngineDescription desc = MaltParser.getDescription();
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(desc);
    engine.process(this.jCas);
    engine.collectionProcessComplete();

    // The -det-> dog
    // dog -nsubj-> chased
    // chased -> ROOT
    // the -det-> fox
    // fox -dobj-> chased
    // down -advmod-> chased
    // the -det-> road
    // road -pobj-> down
    // . -punct-> chased
    List<DependencyNode> nodes;
    nodes = AnnotationRetrieval.getAnnotations(jCas, DependencyNode.class);

    // check node spans
    String[] texts = "The dog chased the fox down the road .".split(" ");
    assertEquals(texts.length, nodes.size());
    for (int i = 0; i < texts.length; ++i) {
      assertEquals(texts[i], nodes.get(i).getCoveredText());
    }

    // node aliases
    DependencyNode the1 = nodes.get(0);
    DependencyNode dog = nodes.get(1);
    DependencyNode chased = nodes.get(2);
    DependencyNode the2 = nodes.get(3);
    DependencyNode fox = nodes.get(4);
    DependencyNode down = nodes.get(5);
    DependencyNode the3 = nodes.get(6);
    DependencyNode road = nodes.get(7);
    DependencyNode period = nodes.get(8);

    // check node heads and dependency types
    assertEquals(dog, the1.getHead());
    assertEquals("det", the1.getDependencyType());
    assertEquals(chased, dog.getHead());
    assertEquals("nsubj", dog.getDependencyType());
    assertNull(chased.getHead());
    assertNull(chased.getDependencyType());
    assertEquals(fox, the2.getHead());
    assertEquals("det", the2.getDependencyType());
    assertEquals(chased, fox.getHead());
    assertEquals("dobj", fox.getDependencyType());
    assertEquals(chased, down.getHead());
    assertEquals("advmod", down.getDependencyType());
    assertEquals(road, the3.getHead());
    assertEquals("det", the3.getDependencyType());
    assertEquals(down, road.getHead());
    assertEquals("pobj", road.getDependencyType());
    assertEquals(chased, period.getHead());
    assertEquals("punct", period.getDependencyType());

    // check node children
    List<DependencyNode> emptyList = Collections.emptyList();
    assertEquals(emptyList, getChildren(the1));
    assertEquals(Arrays.asList(the1), getChildren(dog));
    assertEquals(Arrays.asList(dog, fox, down, period), getChildren(chased));
    assertEquals(emptyList, getChildren(the2));
    assertEquals(Arrays.asList(the2), getChildren(fox));
    assertEquals(Arrays.asList(road), getChildren(down));
    assertEquals(emptyList, getChildren(the3));
    assertEquals(Arrays.asList(the3), getChildren(road));
    assertEquals(emptyList, getChildren(period));
  }

  private static List<DependencyNode> getChildren(DependencyNode node) {
    return UIMAUtil.toList(node.getChildren(), DependencyNode.class);
  }
}
