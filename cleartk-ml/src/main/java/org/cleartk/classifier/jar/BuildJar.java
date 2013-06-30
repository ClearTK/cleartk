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


/**
 * Command line tool for building a model jar file from a directory where a model has been trained,
 * e.g. by {@link Train}.
 * 
 * Usage: <code>java org.cleartk.classifier.jar.BuildJar model-dir</code>
 * 
 * <br>
 * Copyright (c) 2007-2011, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class BuildJar {

  public static void main(String... args) throws Exception {
    String programName = BuildJar.class.getName();
    String usage = String.format("usage: java %s DIR\n\n"
        + "The directory DIR should contain the MANIFEST.MF file as created by\n"
        + "a classifier DataWriter, and all files created by training a model\n"
        + "from the corresponding training-data.xxx file\n", programName);

    // usage message for wrong number of arguments
    if (args.length != 1) {
      System.err.format("error: wrong number of arguments\n%s", usage);
      System.exit(1);
    }
    File dir = new File(args[0]);

    // get the classifier builder from the training directory
    JarClassifierBuilder<?> classifierBuilder = JarClassifierBuilder.fromTrainingDirectory(dir);
    classifierBuilder.packageClassifier(dir);
  }
}
