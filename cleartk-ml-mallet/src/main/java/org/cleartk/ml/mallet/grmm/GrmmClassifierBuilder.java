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
package org.cleartk.ml.mallet.grmm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPInputStream;

import org.cleartk.ml.encoder.features.NameNumber;
import org.cleartk.ml.jar.JarStreams;
import org.cleartk.ml.jar.SequenceClassifierBuilder_ImplBase;

import cc.mallet.grmm.learning.ACRF;
import cc.mallet.grmm.learning.GenericAcrfTui;

import com.google.common.io.Files;

/**
 * <br>
 * Copyright (c) 2010, University of Würzburg<br>
 * All rights reserved.
 * <p>
 * 
 * @author Martin Toepfer
 * 
 */
public class GrmmClassifierBuilder extends
    SequenceClassifierBuilder_ImplBase<GrmmClassifier, List<NameNumber>, String[], String[]> {

  private static String DEFAULT_MODEL_FILENAME = "acrf.model.ser.gz";

  private static String JAR_ENTRY_MODEL = "model.grmm";

  private static String JAR_ENTRY_OUTCOME_EXAMPLE = "dummy.outcome";

  @Override
  public File getTrainingDataFile(File dir) {
    return new File(dir, "training-data.grmm");
  }

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
  public void trainClassifier(File dir, String... args) throws Exception {
    if (dir == null || !dir.isDirectory()) {
      throw new IllegalArgumentException(String.format("invalid directory \"%s\"", dir));
    }
    if (args == null || args.length == 0 || args[0] == null) {
      throw new IllegalArgumentException("missing template file in \"args\"");
    }
    File template = new File(dir, args[0]);
    if (!template.exists()) {
      String msg = "template file \"%s\" does not exist!";
      throw new IllegalArgumentException(String.format(msg, template));
    }
    File outputFile = new File(dir, DEFAULT_MODEL_FILENAME);

    String inferencer = args.length < 2 ? "LoopyBP" : args[1];
    String maxInferencer = args.length < 3 ? "LoopyBP.createForMaxProduct()" : args[2];

    // usage of GRMM:
    String[] grmmArgs = new String[] {
        "--training",
        new File(dir, "training-data.grmm").getAbsolutePath(),
        "--testing",
        new File(dir, "training-data.grmm").getAbsolutePath(),
        "--model-file",
        template.getAbsolutePath(),
        "--inferencer",
        inferencer,
        "--max-inferencer",
        maxInferencer };

    // GenericAcrfTui saves in the current directory; move to the appropriate directory
    GenericAcrfTui.main(grmmArgs);
    Files.move(new File("acrf.ser.gz"), outputFile.getAbsoluteFile());
  }

  @Override
  protected void packageClassifier(File dir, JarOutputStream modelStream) throws IOException {
    super.packageClassifier(dir, modelStream);

    File model = new File(dir, DEFAULT_MODEL_FILENAME);
    File trainingData = new File(dir, "training-data.grmm");
    if (!model.exists()) {
      String msg = "model file \"%s\" not found";
      throw new IllegalArgumentException(String.format(msg, model));
    }
    if (!trainingData.exists()) {
      String msg = "training data file \"%s\" not found";
      throw new IllegalArgumentException(String.format(msg, trainingData));
    }
    // handle an outcome example from the training data over to the
    // classifier
    // through a special jar-file-entry:
    LineNumberReader lnr = new LineNumberReader(new FileReader(trainingData));
    this.outcomeExample = lnr.readLine().split("----")[0];
    lnr.close();
    JarStreams.putNextJarEntry(modelStream, JAR_ENTRY_MODEL, model);
    modelStream.putNextEntry(new JarEntry(JAR_ENTRY_OUTCOME_EXAMPLE));
    new ObjectOutputStream(modelStream).writeObject(outcomeExample);
  }

  protected ACRF acrf;

  protected String outcomeExample;

  @Override
  protected void unpackageClassifier(JarInputStream modelStream) throws IOException {
    super.unpackageClassifier(modelStream);
    try {
      JarStreams.getNextJarEntry(modelStream, JAR_ENTRY_MODEL);
      this.acrf = (ACRF) new ObjectInputStream(new GZIPInputStream(modelStream)).readObject();
      JarStreams.getNextJarEntry(modelStream, JAR_ENTRY_OUTCOME_EXAMPLE);
      this.outcomeExample = (String) new ObjectInputStream(modelStream).readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  @Override
  protected GrmmClassifier newClassifier() {
    return new GrmmClassifier(
        this.featuresEncoder,
        this.outcomeEncoder,
        this.acrf,
        this.outcomeExample);
  }

}
