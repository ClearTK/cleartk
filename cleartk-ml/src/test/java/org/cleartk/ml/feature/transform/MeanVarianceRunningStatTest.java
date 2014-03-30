/** 
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

package org.cleartk.ml.feature.transform;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.cleartk.ml.feature.transform.extractor.ZeroMeanUnitStddevExtractor.MeanVarianceRunningStat;
import org.junit.Test;

import com.google.common.io.Files;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class MeanVarianceRunningStatTest {

  @Test
  public void TestMeanVariance() throws Exception {
    MeanVarianceRunningStat rs = new MeanVarianceRunningStat();

    rs.add(1);
    assertTrue(rs.mean() == 1.0);
    assertTrue(rs.variance() == 0.0);

    rs.add(2);
    assertTrue(rs.mean() == 1.5);
    assertTrue(rs.variance() == 0.25);

    rs.add(3);
    assertTrue(rs.mean() == 2.0);
    assertTrue(rs.variance() == 2.0 / 3.0);

    rs.add(4);
    assertTrue(rs.mean() == 2.5);
    assertTrue(rs.variance() == 1.25);

    rs.add(5);
    assertTrue(rs.mean() == 3);
    assertTrue(rs.variance() == 2);
  }

  @Test
  public void TestSerialization() throws Exception {

    // Compute mean/variance
    MeanVarianceRunningStat stats = new MeanVarianceRunningStat();
    stats.add(1);
    stats.add(2);
    stats.add(3);
    stats.add(4);
    stats.add(5);
    int n = stats.getNumSamples();
    double mean = stats.mean();
    double variance = stats.variance();

    // Write out file
    File tmpDir = Files.createTempDir();
    FileOutputStream fos = new FileOutputStream(new File(tmpDir, "zmus.dat"));
    ObjectOutputStream output = new ObjectOutputStream(fos);
    output.writeObject(stats);
    output.close();

    // Read in file
    FileInputStream fis = new FileInputStream(new File(tmpDir, "zmus.dat"));
    ObjectInputStream input = new ObjectInputStream(fis);
    stats = (MeanVarianceRunningStat) input.readObject();
    input.close();

    // Check values
    assertTrue(n == stats.getNumSamples());
    assertTrue(mean == stats.mean());
    assertTrue(variance == stats.variance());
  }
}
