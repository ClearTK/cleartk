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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.List;

import org.cleartk.eval.util.FCollections;

/**
 * This class provides a way to write an {@link FCollections} to a simple, plain text, human
 * readable, and tab-delimited data file. It also provides a way to read the file back in to an
 * {@link FCollections} object. <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

@Deprecated
public class FCollectionsFile {

  public static NumberFormat percentFormat;

  static {
    percentFormat = NumberFormat.getPercentInstance();
    percentFormat.setMaximumFractionDigits(2);
  }

  /**
   * 
   * This is a simple method for writing an FCollection to a file. Assuming the type of FCollection
   * is String or Integer, then an FCollection written by this method can be read back in using the
   * appropriate readFCollection. The 'countObjectLabel' should be a one or two word label
   * corresponding that names the kind of objects being counted by the provided FCollection. For
   * example, if you are counting part-of-speech tags, then countObjectLabel might be "POS".
   * 
   * @param <T>
   *          the type of the objects being counted by the FCollection
   * @param outputFile
   *          the name of the file to write the FCollection results to
   * @param fc
   *          an FCollection to write to a file
   * @param title
   *          this will be written as the first line of the file. The title should not contain a
   *          newline.
   * @param countObjectLabel
   *          a label that names the objects being counted. Generally, this should be one or two
   *          words to keep the table in the produced file readable.
   */
  public static <T extends Comparable<T>> void writeFCollection(
      File outputFile,
      FCollections<T> fc,
      String title,
      String countObjectLabel) throws IOException {

    PrintStream out = new PrintStream(outputFile);

    out.println(title);
    out.println();
    out.println(countObjectLabel + "\tTP\tFP\tFN\tprecision\trecall\tf-measure");
    List<T> types = fc.getSortedObjects();

    for (T type : types) {
      out.println(type.toString() + "\t" + fc.getTruePositivesCount(type) + "\t"
          + fc.getFalsePositivesCount(type) + "\t" + fc.getFalseNegativesCount(type) + "\t"
          + percentFormat.format(fc.getPrecision(type)) + "\t"
          + percentFormat.format(fc.getRecall(type)) + "\t" + percentFormat.format(fc.getF(type)));
    }
    out.close();
  }

  public static FCollections<String> readStringFCollections(File inputFile) throws IOException {
    return readFCollections(inputFile, new StringConverter());
  }

  public static FCollections<Integer> readIntegerFCollections(File inputFile) throws IOException {
    return readFCollections(inputFile, new IntegerConverter());
  }

  public static <T extends Comparable<T>> FCollections<T> readFCollections(
      File inputFile,
      LabelConverter<T> labelConverter) throws IOException {

    FCollections<T> fCollections = new FCollections<T>();
    BufferedReader input = new BufferedReader(new FileReader(inputFile));
    try {
      input.readLine(); // read title
      input.readLine(); // read blank line
      input.readLine(); // read column headers

      String line;
      while ((line = input.readLine()) != null) {
        String[] columns = line.split("\\t");
        T label = labelConverter.convertLabel(columns[0]);
        fCollections.addTruePositives(label, Integer.parseInt(columns[1]));
        fCollections.addFalsePositives(label, Integer.parseInt(columns[2]));
        fCollections.addFalseNegatives(label, Integer.parseInt(columns[3]));

      }
    } finally {
      input.close();
    }
    return fCollections;
  }

  public static interface LabelConverter<T> {
    public T convertLabel(String label);
  }

  public static class IntegerConverter implements LabelConverter<Integer> {
    @Override
    public Integer convertLabel(String label) {
      return Integer.parseInt(label);
    }
  }

  public static class StringConverter implements LabelConverter<String> {
    @Override
    public String convertLabel(String label) {
      return label;
    }
  }

}
