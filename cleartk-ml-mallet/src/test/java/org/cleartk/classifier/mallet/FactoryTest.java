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
package org.cleartk.classifier.mallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cleartk.classifier.mallet.factory.C45TrainerFactory;
import org.cleartk.classifier.mallet.factory.ClassifierTrainerFactory;
import org.cleartk.classifier.mallet.factory.McMaxEntTrainerFactory;
import org.cleartk.classifier.mallet.factory.MaxEntTrainerFactory;
import org.cleartk.classifier.mallet.factory.NaiveBayesTrainerFactory;
import org.junit.Test;

import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.NaiveBayesTrainer;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 */

public class FactoryTest {

  @Test
  public void testC45TrainerFactory() {
    C45TrainerFactory factory = new C45TrainerFactory();
    C45Trainer trainer = (C45Trainer) factory.createTrainer(new String[] {
        "--depthLimited",
        "true",
        "--doPruning",
        "false",
        "--maxDepth",
        "6",
        "--minNumInsts",
        "3" });

    assertTrue(trainer.getDepthLimited());
    assertFalse(trainer.getDoPruning());
    assertEquals(6, trainer.getMaxDepth());
    assertEquals(3, trainer.getMinNumInsts());

    testBadConfig(factory, new String[] {
        "--depthLimited",
        "--doPruning",
        "false",
        "--maxDepth",
        "6",
        "--minNumInsts",
        "3" });

    testBadConfig(factory, new String[] { "--depthimited", "true" });
    testBadConfig(factory, new String[] { "--numIterations" });

    assertTrue(factory.getClass().getName().endsWith("TrainerFactory"));
  }

  @Test
  public void testMaxentTrainerFactory() {
    MaxEntTrainerFactory factory = new MaxEntTrainerFactory();
    factory.createTrainer(new String[] {
        "--numIterations",
        "200",
        "--gaussianPriorVariance",
        "0.552" });

    testBadConfig(factory, new String[] { "--depthimited", "true" });
    testBadConfig(factory, new String[] { "--numIterations", "true" });
    testBadConfig(factory, new String[] { "--numIterations", "-5" });
    testBadConfig(factory, new String[] { "--numIterations" });

    assertTrue(factory.getClass().getName().endsWith("TrainerFactory"));
  }

  @Test
  public void testMCMaxentTrainerFactory() {
    McMaxEntTrainerFactory factory = new McMaxEntTrainerFactory();
    factory.createTrainer(new String[] {
        "--useHyperbolicPrior",
        "true",
        "--gaussianPriorVariance",
        "0.552",
        "--hyperbolicPriorSlope",
        "0.3",
        "--hyperbolicPriorSharpness",
        "15.0",
        "--numIterations",
        "20" });

    testBadConfig(factory, new String[] { "--hyperbolicPriorSlope", "false" });
    testBadConfig(factory, new String[] { "--numIterations" });
    testBadConfig(factory, new String[] { "--numItertions", "true" });

    assertTrue(factory.getClass().getName().endsWith("TrainerFactory"));
  }

  @Test
  public void testNaiveBayesTrainerFactory() {
    NaiveBayesTrainerFactory factory = new NaiveBayesTrainerFactory();
    NaiveBayesTrainer trainer = (NaiveBayesTrainer) factory.createTrainer(new String[] {
        "--docLengthNormalization",
        "15.0" });

    assertEquals(15.0, trainer.getDocLengthNormalization(), 0.001);

    testBadConfig(factory, new String[] { "--hyperbolicPriorSlope", "false" });
    testBadConfig(factory, new String[] { "--docLengthNormalization" });
    testBadConfig(factory, new String[] {
        "--docLengthNormalization",
        "15.0",
        "--useHyperbolicPrior",
        "true" });

    assertTrue(factory.getClass().getName().endsWith("TrainerFactory"));
  }

  private void testBadConfig(ClassifierTrainerFactory<?> factory, String... args) {
    IllegalArgumentException exception = null;
    try {
      factory.createTrainer(args);
    } catch (IllegalArgumentException iae) {
      exception = iae;
    }
    assertNotNull(exception);

  }

}
