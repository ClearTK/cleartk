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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.eval.EvaluationConstants;
import org.cleartk.syntax.constituent.TreebankConstants;
import org.cleartk.syntax.constituent.TreebankGoldAnnotator;
import org.cleartk.util.Options_ImplBase;
import org.cleartk.util.ViewURIFileNamer;
import org.cleartk.util.cr.FilesCollectionReader;
import org.kohsuke.args4j.Option;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;

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
 * <li>-td src/main/resources/data/pos/treebank</li>
 * <li>-o src/main/resources/data/pos/treebank</li>
 * <li>-suf .tree</li>
 * </ul>
 * 
 * To view the resulting xmi file in the CAS Visual Debugger you can run the eclipse launch
 * configuration labeled "CVD (cleartk-examples)". When the CVD opens select Menu -> File -> Read
 * XMI CAS File. Now navigate to the single xmi file located in
 * src/main/resources/data/pos/treebank. Once the file is open in the CVD be sure to examine the
 * different views by selecting different values from the drop down box labeled "Select View" near
 * the top of the window.
 * 
 * @author Philip Ogren
 * 
 */
public class TreebankParsingExample {

  public static class Options extends Options_ImplBase {
    @Option(
        name = "-td",
        aliases = "--treebankDirectory",
        usage = "specify the directory containing treebank files",
        required = true)
    public String treebankDirectory;

    @Option(
        name = "-o",
        aliases = "--outputDirectory",
        usage = "specify the directory to write the XMI files to",
        required = true)
    public String outputDirectory;

    @Option(
        name = "-suf",
        aliases = "--treebankFileSuffixes",
        usage = "specify file suffixes of the treebank files in the treebank directory",
        multiValued = true)
    public List<String> treebankFileSuffixes = new ArrayList<String>();
  }

  public static void main(String[] args) throws UIMAException, IOException {
    Options options = new Options();
    options.parseOptions(args);

    String[] suffixes = options.treebankFileSuffixes.toArray(new String[options.treebankFileSuffixes.size()]);

    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        FilesCollectionReader.class,
        FilesCollectionReader.PARAM_ROOT_FILE,
        options.treebankDirectory,
        FilesCollectionReader.PARAM_VIEW_NAME,
        TreebankConstants.TREEBANK_VIEW,
        FilesCollectionReader.PARAM_SUFFIXES,
        suffixes);

    AnalysisEngine viewCreator = AnalysisEngineFactory.createPrimitive(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        EvaluationConstants.GOLD_VIEW);

    AnalysisEngineDescription treebankParserDescription = AnalysisEngineFactory.createPrimitiveDescription(TreebankGoldAnnotator.class);
    AnalysisEngine treebankParser = AnalysisEngineFactory.createAnalysisEngine(
        treebankParserDescription,
        EvaluationConstants.GOLD_VIEW);

    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
        XWriter.class,
        XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        options.outputDirectory,
        XWriter.PARAM_FILE_NAMER_CLASS_NAME,
        ViewURIFileNamer.class.getName());

    SimplePipeline.runPipeline(reader, viewCreator, treebankParser, xWriter);

  }

}
