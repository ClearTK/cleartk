/** 
 * Copyright (c) 2007-2015, Regents of the University of Colorado 
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
package org.cleartk.ml.tksvmlight;

import java.util.HashMap;
import java.util.Map;

import org.cleartk.ml.tksvmlight.kernel.SubsetTreeKernel;
import org.cleartk.ml.tksvmlight.kernel.SyntacticSemanticTreeKernel;
import org.cleartk.ml.tksvmlight.model.ContinuousCosineLexicalSimilarity;
import org.cleartk.ml.tksvmlight.model.IdentityLexicalSimilarity;
import org.cleartk.ml.tksvmlight.model.LexicalFunctionModel;
import org.cleartk.test.util.DefaultTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2015, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Tim Miller
 */

public class LexicalSimilarityTest extends DefaultTestBase {
  
  public static final double EPSILON = 0.01;

  Map<String,double[]> wordMap = new HashMap<>();
  
  LexicalFunctionModel cont = null;
  LexicalFunctionModel ident = null;
  
  /*
   * (non-Javadoc)
   * @see org.cleartk.test.util.DefaultTestBase#setUp()
   * 
   * Just for ease of making up examples, I'm using 5D vector
   * with dimensions "Is Determiner", "Is definite", "Is animal", "Poops indoors", "Has windows"
   */
  @Before
  @Override
  public void setUp(){
    
    wordMap.put("the",   new double[] {0.5, 0.5, 0.0, 0.0, 0.0});
    wordMap.put("a",     new double[] {1.0, 0.0, 0.0, 0.0, 0.0});
    wordMap.put("cat",   new double[] {0.0, 0.0, 1.0, 0.0, 0.0});
    wordMap.put("dog",   new double[] {0.0, 0.0, 0.5, 0.5, 0.0});
    wordMap.put("house", new double[] {0.0, 0.0, 0.0, 0.0, 1.0});

    cont = new ContinuousCosineLexicalSimilarity(wordMap);
    ident = new IdentityLexicalSimilarity();
  }
  
  @Test
  public void testLexicalSimilarity(){
    
    Assert.assertTrue(cont.getLexicalSimilarity("the", "the") > cont.getLexicalSimilarity("the", "a"));
    Assert.assertEquals(1.0, ident.getLexicalSimilarity("the", "the"), EPSILON);
    
    Assert.assertTrue(cont.getLexicalSimilarity("cat", "dog") > 0.0);
    Assert.assertEquals(0.0, ident.getLexicalSimilarity("cat", "dog"), EPSILON);
    
    Assert.assertEquals(1.0, cont.getLexicalSimilarity("cat", "cat"), EPSILON);
    
    Assert.assertEquals(0.707, cont.getLexicalSimilarity("cat", "dog"), EPSILON);
    Assert.assertEquals(0.707, cont.getLexicalSimilarity("the", "a"), EPSILON);
    Assert.assertEquals(0.0, cont.getLexicalSimilarity("cat", "house"), EPSILON);
  }
  
  @Test
  public void testLexSimKernels(){
    SyntacticSemanticTreeKernel sstk = new SyntacticSemanticTreeKernel(cont, 1.0, false);
    SubsetTreeKernel sst = new SubsetTreeKernel(1.0, false);
    
    String tree1 = "(NP (DT the) (NN dog))";
    String tree2 = "(NP (DT a) (NN cat))";
    String tree3 = "(NP (DT the) (NN house))";
    
    // validate on similar words
    TreeFeature tf1 = new TreeFeature("TF1", tree1, sstk);
    TreeFeature tf2 = new TreeFeature("TF2", tree2, sstk);
    double sim = sstk.evaluate(tf1, tf2);
    Assert.assertEquals(4.327, sim, EPSILON);
    
    // reminder that the subset tree kernel is not as high a value 
    tf1 = new TreeFeature("TF1", tree1, sst);
    tf2 = new TreeFeature("TF2", tree2, sst);
    Assert.assertEquals(1.0, sst.evaluate(tf1, tf2), EPSILON);
    
    // try with non-matching word "the house" vs. "a cat"
    tf1 = new TreeFeature("TF1", tree3, sstk);
    tf2 = new TreeFeature("TF2", tree2, sstk);
    Assert.assertEquals(2.414, sstk.evaluate(tf1, tf2), EPSILON);
  }
}
