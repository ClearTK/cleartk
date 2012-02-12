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

package org.cleartk.examples.documentclassification;

import java.util.Arrays;
import java.util.List;

import org.cleartk.classifier.jar.Train;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 */
@Deprecated
public class Step3TrainModel {

  public static class Args {
    @Option(
        name = "-t",
        aliases = "--trainingDataDirectoryName",
        usage = "specify the directory that contains the training data")
    public String trainingDataDirectoryName = "example/documentclassification/libsvm";

    @Option(
        name = "-ta",
        aliases = "--trainingArgument",
        usage = "specify training arguments to be passed to the learner.  For multiple values specify -ta for each - e.g. '-ta -t -ta 0'")
    public List<String> trainingArguments = Arrays.asList("-t", "0");

    public static Args parseArguments(String[] stringArgs) {
      Args args = new Args();
      CmdLineParser parser = new CmdLineParser(args);
      try {
        parser.parseArgument(stringArgs);
      } catch (CmdLineException e) {
        e.printStackTrace();
        parser.printUsage(System.err);
        System.exit(1);
      }
      return args;
    }
  }

  public static void main(String[] stringArgs) throws Exception {

    Args args = Args.parseArguments(stringArgs);

    String trainingDataDirectory = args.trainingDataDirectoryName;
    List<String> trainingArgs = args.trainingArguments;

    String[] modelArgs = new String[trainingArgs.size() + 1];
    modelArgs[0] = trainingDataDirectory;
    System.arraycopy(trainingArgs.toArray(), 0, modelArgs, 1, trainingArgs.size());

    System.out.println("Training the model with the following arguments: "
        + Arrays.asList(modelArgs));
    Train.main(modelArgs);

  }
}
