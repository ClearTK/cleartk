/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.classifier.viterbi;

import java.io.File;

import org.apache.uima.pear.util.FileUtil;
import org.cleartk.classifier.jar.BuildJar;
import org.cleartk.classifier.jar.ClassifierBuilder;
import org.cleartk.classifier.jar.Train;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */

public class ViterbiClassifierBuilder<OUTCOME_TYPE> implements ClassifierBuilder<OUTCOME_TYPE> {

  public static final String DELEGATED_MODEL_FILE_NAME = "delegated-model.jar";

  public void buildJar(File dir, String[] args) throws Exception {
    File delegatedOutputDirectory = new File(dir, ViterbiDataWriter.DELEGATED_MODEL_DIRECTORY_NAME);
    FileUtil.copyFile(new File(delegatedOutputDirectory, BuildJar.MODEL_FILE_NAME), new File(dir,
            DELEGATED_MODEL_FILE_NAME));

    BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
    stream.write(DELEGATED_MODEL_FILE_NAME, new File(dir, DELEGATED_MODEL_FILE_NAME));
    stream.write(ViterbiDataWriter.OUTCOME_FEATURE_EXTRACTOR_FILE_NAME, new File(dir,
            ViterbiDataWriter.OUTCOME_FEATURE_EXTRACTOR_FILE_NAME));
    stream.close();

  }

  public Class<?> getClassifierClass() {
    return ViterbiClassifier.class;
  }

  public void train(File dir, String[] args) throws Exception {
    File delegatedOutputDirectory = new File(dir, ViterbiDataWriter.DELEGATED_MODEL_DIRECTORY_NAME);
    String[] delegatedArgs = new String[args.length + 1];
    System.arraycopy(args, 0, delegatedArgs, 1, args.length);
    delegatedArgs[0] = delegatedOutputDirectory.getPath();
    Train.main(delegatedArgs);
  }

}
