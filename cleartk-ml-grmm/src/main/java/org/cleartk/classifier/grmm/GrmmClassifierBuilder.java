/**
 * Copyright (c) 2010, University of Würzburg<br>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Würzburg nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
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
package org.cleartk.classifier.grmm;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;

import org.cleartk.classifier.jar.BuildJar;
import org.cleartk.classifier.jar.ClassifierBuilder;

import edu.umass.cs.mallet.grmm.learning.GenericAcrfTui;

/**
 * <br>
 * Copyright (c) 2010, University of Würzburg<br>
 * All rights reserved.
 * <p>
 * 
 * @author Martin Toepfer
 * 
 */
public class GrmmClassifierBuilder implements ClassifierBuilder<String[]> {

  public static String DEFAULT_MODEL_FILENAME = "acrf.model.ser.gz";

  public static String JAR_ENTRY_MODEL = "model.grmm";

  public static String JAR_ENTRY_OUTCOME_EXAMPLE = "dummy.outcome";

  /**
   * Parameters:<br>
   * <table border="1">
   * <thead>
   * <tr>
   * <th>position</th>
   * <th>type</th>
   * <th>description</td></th>
   * </tr>
   * </thead>
   * <tr>
   * <td>0</td>
   * <td>required</td>
   * <td>path to template file</td>
   * </tr>
   * <tr>
   * <td>1</td>
   * <td>optional</td>
   * <td>inferencer, see GRMM docs for possible values</td>
   * </tr>
   * <tr>
   * <td></td>
   * <td></td>
   * <td>default: LoopyBP</td>
   * </tr>
   * <tr>
   * <td>2</td>
   * <td>optional</td>
   * <td>max-inferencer, see GRMM docs for possible values</td>
   * </tr>
   * <tr>
   * <td></td>
   * <td></td>
   * <td>default: LoopyBP.createForMaxProduct()</td>
   * </tr>
   * </table>
   */
  public void train(File dir, String[] args) throws Exception {
    if (dir == null || !dir.isDirectory()) {
      throw new IllegalArgumentException("invalid directory \"" + dir == null
          ? "null"
          : dir.getPath() + "\"passed.");
    }
    String template;
    if (args == null || args.length == 0) {
      throw new IllegalArgumentException("no template file passed.");
    }
    if (!new File(template = dir + "/" + args[0]).exists()) {
      throw new IllegalArgumentException("template file \"" + template + "\" does not exist!");
    }
    String outputFileName = dir.getAbsolutePath() + "/" + DEFAULT_MODEL_FILENAME;

    String inferencer = args.length < 2 ? "LoopyBP" : args[1];
    String maxInferencer = args.length < 3 ? "LoopyBP.createForMaxProduct()" : args[2];

    // usage of GRMM:
    // (modified version with minor changes in the arguments)
    List<String> argList = Arrays.asList(
        "--output-file",
        outputFileName,
        "--training",
        dir.getAbsolutePath() + "/training-data.grmm",
        "--testing",
        dir.getAbsolutePath() + "/training-data.grmm",
        "--template-file",
        template,
        "--inferencer",
        inferencer,
        "--max-inferencer",
        maxInferencer);
    String[] grmmArgs = new String[args.length + 11];
    argList.toArray(grmmArgs);

    // GenericAcrfTui exists in the mallet library and the GRMM library;
    // use the class from GRMM:
    GenericAcrfTui.main(grmmArgs);
  }

  public void buildJar(File dir, String[] args) throws Exception {
    BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
    File model = new File(dir + "/" + DEFAULT_MODEL_FILENAME);
    File trainingData = new File(dir.getAbsolutePath() + "/training-data.grmm");
    if (!model.exists()) {
      throw new IllegalArgumentException("model file \"" + model.getName() + "\" not found.");
    }
    if (!trainingData.exists()) {
      throw new IllegalArgumentException("training data file \"" + trainingData.getName()
          + "\" not found.");
    }
    // handle an outcome example from the training data over to the
    // classifier
    // through a special jar-file-entry:
    LineNumberReader lnr = new LineNumberReader(new FileReader(trainingData));
    String outcomeExample = lnr.readLine().split("----")[0];
    System.out.println(outcomeExample);
    lnr.close();
    try {
      stream.write(JAR_ENTRY_MODEL, model);
      JarEntry outcomeExampleEntry = new JarEntry(JAR_ENTRY_OUTCOME_EXAMPLE);
      outcomeExampleEntry.setComment(outcomeExample);
      stream.putNextEntry(outcomeExampleEntry);
      stream.flush();
    } finally {
      stream.close();
    }

  }

  public Class<?> getClassifierClass() {
    return GrmmClassifier.class;
  }

}
