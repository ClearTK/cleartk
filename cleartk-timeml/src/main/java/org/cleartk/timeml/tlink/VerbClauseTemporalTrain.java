/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.timeml.tlink;

import java.io.File;
import java.io.IOException;

import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.jar.Train;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.TimeMLViewName;
import org.cleartk.timeml.corpus.PlainTextTLINKGoldAnnotator;
import org.cleartk.timeml.corpus.TimeMLGoldAnnotator;
import org.cleartk.timeml.corpus.TreebankAligningAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.util.cr.FilesCollectionReader;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class VerbClauseTemporalTrain {

  private static void error(String message) throws Exception {
    Logger logger = UimaContextFactory.createUimaContext().getLogger();
    logger.log(Level.SEVERE, String.format("%s\nusage: "
        + "VerbClauseTemporalMain timebank-dir treebank-dir", message));
    System.exit(1);
  }

  public static void main(String[] args) throws Exception {
    // check arguments
    if (args.length != 2) {
      error("wrong number of arguments");
    } else if (!new File(args[0]).exists()) {
      error("TimeBank directory not found: " + args[0]);
    } else if (!new File(args[1]).exists()) {
      error("TreeBank directory not found: " + args[1]);
    }
    String timeBankDir = args[0];
    String treeBankDir = args[1];

    // clean up the mismatches between TimeBank and TreeBank
    File cleanedTimeBankDir = getCleanedTimeBankDir(timeBankDir);
    timeBankDir = cleanedTimeBankDir.getPath();

    // run the components that write out the training data
    SimplePipeline.runPipeline(
        FilesCollectionReader.getCollectionReaderWithPatterns(
            TimeMLComponents.TYPE_SYSTEM_DESCRIPTION,
            timeBankDir,
            TimeMLViewName.TIMEML,
            "wsj_.*[.]tml"),
        TimeMLGoldAnnotator.getDescriptionNoTLINKs(),
        PlainTextTLINKGoldAnnotator.getDescription(),
        TreebankAligningAnnotator.getDescription(treeBankDir),
        DefaultSnowballStemmer.getDescription("English"),
        VerbClauseTemporalAnnotator.getWriterDescription());

    // remove the temporary directory containing the cleaned up TimeBank
    FileUtils.deleteRecursive(cleanedTimeBankDir);

    // train the model
    String trainingDirectory = VerbClauseTemporalAnnotator.MODEL_LOCATOR.getTrainingDirectory();
    Train.main(trainingDirectory);

    // delete the generated files
    for (File file : new File(trainingDirectory).listFiles()) {
      if (!file.isDirectory() && !file.getName().equals("model.jar")) {
        file.delete();
      }
    }
  }

  public static File getCleanedTimeBankDir(String timeBankDir) throws IOException {
    File tempDir = File.createTempFile("TimeBank", "Cleaned");
    tempDir.delete();
    tempDir.mkdir();
    for (File file : new File(timeBankDir).listFiles()) {
      String name = file.getName();
      if (file.isHidden() || name.startsWith(".")) {
        continue;
      }

      // get the file text
      String text = FileUtils.file2String(file);

      // all ampersands are messed up in TimeBank
      text = text.replaceAll("\\bamp\\b", "&amp;");
      text = text.replaceAll("SampP", "S&amp;P");
      text = text.replaceAll("&&amp;;", "&amp;");

      // all "---" missing in TreeBank
      text = text.replaceAll("---", "");

      // fix individual file errors
      text = fixTextByFileName(name, text);

      // write the file to the temp directory
      FileUtils.saveString2File(text, new File(tempDir, file.getName()));
    }
    return tempDir;

  }

  public static String fixTextByFileName(String name, String text) {
    // duplicate "the" in TimeBank
    if (name.equals("wsj_0032.tml")) {
      text = text.replace("the <TIMEX3 tid=\"t18\"", "<TIMEX3 tid=\"t18\"");
    }

    // missing "DD"s in TimeBank
    else if (name.equals("wsj_0159.tml")) {
      text = text.replace(
          "Acquisition has <EVENT eid=\"e11\"",
          "DD Acquisition has <EVENT eid=\"e11\"");
      text = text.replace("Acquisition <EVENT eid=\"e20\"", "DD Acquisition <EVENT eid=\"e20\"");
    }

    // missing "BRUCE R. BENT" in TreeBank
    else if (name.equals("wsj_0266.tml")) {
      text = text.replace("BRUCE R. BENT", "");
    }

    // missing 30. in TreeBank
    else if (name.equals("wsj_0344.tml")) {
      text = text.replace(" 30</TIMEX3>.", "</TIMEX3>");
    }

    // reversed "off roughly" in TimeBank
    else if (name.equals("wsj_0376.tml")) {
      text = text.replace("roughly off", "off roughly");
    }

    // missing @... lines in TreeBank
    else if (name.equals("wsj_0586.tml")) {
      text = text.replaceAll("(?m)@((?!</HL>).)*?$", "");
    }

    // missing @CORPORATES and @EUROBONDS in TreeBank
    else if (name.equals("wsj_0612.tml")) {
      text = text.replace(
          "@ <ENAMEX TYPE=\"ORGANIZATION\">CORPORATES",
          "<ENAMEX TYPE=\"ORGANIZATION\">");
      text = text.replace(
          "@ <ENAMEX TYPE=\"ORGANIZATION\">EUROBONDS",
          "<ENAMEX TYPE=\"ORGANIZATION\">");
    }

    // missing "1988." in TreeBank
    else if (name.equals("wsj_0667.tml")) {
      text = text.replace("1988</TIMEX3>.", "</TIMEX3>");
    }

    // missing "--" in TimeBank and missing "19.29." in TreeBank
    else if (name.equals("wsj_0675.tml")) {
      text = text.replace("Markets</ENAMEX>", "Markets</ENAMEX> --");
      text = text.replace("19.29</CARDINAL>.", "</CARDINAL>");
    }

    // reversed "definitely not" in TimeBank
    else if (name.equals("wsj_0781.tml")) {
      text = text.replace("not definitely", "definitely not");
    }

    // really messed up text in TimeBank
    else if (name.equals("wsj_1003.tml")) {
      text = text.replace("a shhha55 cents a share,   ents a share, but  ssa share", "a share");
      text = text.replace(
          "steel business, <EVENT eid=\"e109\"",
          "Armco, hampered by lower volume in its specialty steel "
              + "business, <EVENT eid=\"e109\"");
    }

    return text;
  }
}
