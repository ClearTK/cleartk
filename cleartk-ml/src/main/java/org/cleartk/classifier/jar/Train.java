/** 
 * Copyright (c) 2007-2011, Regents of the University of Colorado 
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
package org.cleartk.classifier.jar;

import java.io.File;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.SequenceDataWriter;

/**
 * Command line tool for training a classifier from an output directory that has been filled by a
 * {@link DirectoryDataWriter}.
 * 
 * Usage: <code>java org.cleartk.classifier.jar.Train model-dir ...</code>
 * 
 * Some classifiers may accept additional arguments to train, see the documentation for the various
 * {@link JarClassifierBuilder} subclasses for details.
 * 
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class Train {

  /**
   * Trains a classifier in the given directory.
   * 
   * The directory should already contain training data as written by a {@link DataWriter} or
   * {@link SequenceDataWriter}.
   * 
   * @param directory
   *          The directory containing the training data.
   * @param trainingArguments
   *          Additional command-line arguments that should be passed to the classifier.
   */
  public static void main(File directory, String... trainingArguments) throws Exception {
    JarClassifierBuilder.trainAndPackage(directory, trainingArguments);
  }

  public static void main(String... args) throws Exception {
    String programName = Train.class.getName();
    String usage = String.format("usage: java %s DIR\n\n"
        + "The directory DIR should contain the training-data.xxx file as\n"
        + "created by a classifier DataWriter\n", programName);

    // usage message for wrong number of arguments
    if (args.length < 1) {
      System.err.format("error: wrong number of arguments\n%s", usage);
      System.exit(1);
    }

    // parse out the training directory from the arguments
    File dir = new File(args[0]);
    String[] remainingArgs = new String[args.length - 1];
    System.arraycopy(args, 1, remainingArgs, 0, remainingArgs.length);

    // train and package the classifier
    Train.main(dir, remainingArgs);
  }

}
