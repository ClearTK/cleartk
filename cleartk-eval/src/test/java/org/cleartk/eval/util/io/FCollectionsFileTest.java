/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.eval.util.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cleartk.eval.EvaluationTestBase;
import org.cleartk.eval.util.FCollections;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

@SuppressWarnings("deprecation")
public class FCollectionsFileTest extends EvaluationTestBase {

  @Test
  public void testStringFCollections() throws Exception {
    FCollections<String> fc = new FCollections<String>();
    fc.addTruePositive("blue");
    fc.addFalsePositive("blue");
    fc.addFalseNegative("blue");

    fc.addTruePositives("green", 100);
    fc.addFalsePositives("green", 10);
    fc.addFalseNegatives("green", 5);

    File outputFile = new File(outputDirectory, "bluegreen.txt");
    FCollectionsFile.writeFCollection(outputFile, fc, "comparison of blue and green", "color");

    List<String> lines = FileUtils.readLines(outputFile);
    assertEquals("comparison of blue and green", lines.get(0));
    assertEquals("", lines.get(1));
    assertEquals("color\tTP\tFP\tFN\tprecision\trecall\tf-measure", lines.get(2));
    assertEquals("blue\t1\t1\t1\t50%\t50%\t50%", lines.get(3));
    assertEquals("green\t100\t10\t5\t90.91%\t95.24%\t93.02%", lines.get(4));

    fc = FCollectionsFile.readStringFCollections(outputFile);
    assertEquals(1, fc.getTruePositivesCount("blue"));
    assertEquals(1, fc.getFalsePositivesCount("blue"));
    assertEquals(1, fc.getFalseNegativesCount("blue"));
    assertEquals(100, fc.getTruePositivesCount("green"));
    assertEquals(10, fc.getFalsePositivesCount("green"));
    assertEquals(5, fc.getFalseNegativesCount("green"));
    assertEquals(101, fc.getTruePositivesCount());
    assertEquals(11, fc.getFalsePositivesCount());
    assertEquals(6, fc.getFalseNegativesCount());
  }

  @Test
  public void testIntegerFCollections() throws Exception {
    FCollections<Integer> fc = new FCollections<Integer>();
    fc.addTruePositive(1);
    fc.addFalsePositive(1);
    fc.addFalseNegative(1);

    fc.addTruePositives(2, 100);
    fc.addFalsePositives(2, 10);
    fc.addFalseNegatives(2, 5);

    fc.addTruePositive(3);

    File outputFile = new File(outputDirectory, "123.txt");
    FCollectionsFile.writeFCollection(outputFile, fc, "comparison of 1, 2, and 3", "count");

    List<String> lines = FileUtils.readLines(outputFile);
    assertEquals("comparison of 1, 2, and 3", lines.get(0));
    assertEquals("", lines.get(1));
    assertEquals("count\tTP\tFP\tFN\tprecision\trecall\tf-measure", lines.get(2));
    assertEquals("1\t1\t1\t1\t50%\t50%\t50%", lines.get(3));
    assertEquals("2\t100\t10\t5\t90.91%\t95.24%\t93.02%", lines.get(4));
    assertEquals("3\t1\t0\t0\t100%\t100%\t100%", lines.get(5));

    fc = FCollectionsFile.readIntegerFCollections(outputFile);
    assertEquals(1, fc.getTruePositivesCount(1));
    assertEquals(1, fc.getFalsePositivesCount(1));
    assertEquals(1, fc.getFalseNegativesCount(1));
    assertEquals(100, fc.getTruePositivesCount(2));
    assertEquals(10, fc.getFalsePositivesCount(2));
    assertEquals(5, fc.getFalseNegativesCount(2));
    assertEquals(1, fc.getTruePositivesCount(3));
    assertEquals(0, fc.getFalsePositivesCount(3));
    assertEquals(0, fc.getFalseNegativesCount(3));
    assertEquals(102, fc.getTruePositivesCount());
    assertEquals(11, fc.getFalsePositivesCount());
    assertEquals(6, fc.getFalseNegativesCount());
  }
}
