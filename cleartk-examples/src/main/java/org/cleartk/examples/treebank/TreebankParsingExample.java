/** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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

package org.cleartk.examples.treebank;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.corpus.penntreebank.PennTreebankReader;
import org.cleartk.corpus.penntreebank.TreebankGoldAnnotator;
import org.cleartk.eval.EvaluationConstants;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.ae.XmiWriter;
import org.cleartk.util.cr.UriCollectionReader;

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * This class takes in treebank files and parses the the PTB-style format and populates a the CAS
 * with constituent trees and then writes out XMI files. The CAS will contain the raw treebank data
 * in a view You may find it useful to view the generated XMI files in the CAS Visual Debugger.
 * 
 * If you do not have access to PennTreebank data or any other treebank data, then you can run this
 * script with the following arguments:
 * <ul>
 * <li>-td data/pos/treebank</li>
 * <li>-o data/pos/treebank</li>
 * <li>-suf tree</li>
 * </ul>
 * 
 * To view the resulting xmi file in the CAS Visual Debugger you can run the eclipse launch
 * configuration labeled "CVD (cleartk-examples)". When the CVD opens select Menu -> File -> Read
 * XMI CAS File. Now navigate to the single xmi file located in data/pos/treebank. Once the file is
 * open in the CVD be sure to examine the different views by selecting different values from the
 * drop down box labeled "Select View" near the top of the window.
 * 
 * @author Philip Ogren
 * 
 */
public class TreebankParsingExample {

  public interface Options {
    @Option(
        longName = { "td", "treebankDirectory" },
        description = "specify the directory containing treebank files")
    public File getTreebankDirectory();

    @Option(
        shortName = "o",
        longName = "outputDirectory",
        description = "specify the directory to write the XMI files to")
    public String getOutputDirectory();

    @Option(
        longName = { "suf", "treebankFileSuffixes" },
        description = "specify file suffixes of the treebank files in the treebank directory")
    public List<String> getTreebankFileSuffixes();
  }

  public static void main(String[] args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);

    // Loads URIS specified by files into URI view
    String[] suffixes = options.getTreebankFileSuffixes().toArray(
        new String[options.getTreebankFileSuffixes().size()]);
    File treebankDir = options.getTreebankDirectory();
    Collection<File> files = FileUtils.listFiles(treebankDir, suffixes, false);
    CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(files);

    AggregateBuilder builder = new AggregateBuilder();

    // Reads text into TREEBANK_VIEW
    builder.add(UriToDocumentTextAnnotator.getDescriptionForView(PennTreebankReader.TREEBANK_VIEW));

    // Ensures GOLD_VIEW is present
    builder.add(AnalysisEngineFactory.createEngineDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        EvaluationConstants.GOLD_VIEW));

    // Parses treebank text into gold view (instead of default sofa view)
    builder.add(
        TreebankGoldAnnotator.getDescription(),
        CAS.NAME_DEFAULT_SOFA,
        EvaluationConstants.GOLD_VIEW);

    // XMI Writer
    builder.add(XmiWriter.getDescription(new File(options.getOutputDirectory())));

    SimplePipeline.runPipeline(reader, builder.createAggregateDescription());

  }
}
