package org.cleartk.classifier.feature.transform;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.cleartk.classifier.feature.transform.util.MeanVarianceRunningStat;
import org.junit.Test;

import com.google.common.io.Files;

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

    // Read in file
    FileInputStream fis = new FileInputStream(new File(tmpDir, "zmus.dat"));
    ObjectInputStream input = new ObjectInputStream(fis);
    stats = (MeanVarianceRunningStat) input.readObject();

    // Check values
    assertTrue(n == stats.getNumSamples());
    assertTrue(mean == stats.mean());
    assertTrue(variance == stats.variance());
  }
}
