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
package org.cleartk.srl.propbank;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.cleartk.srl.SrlComponents;
import org.cleartk.syntax.constituent.TreebankGoldAnnotator;
import org.cleartk.util.ViewURIFileNamer;
import org.uimafit.component.xwriter.XWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class ParseProbankExample {

  public static void main(String[] args) throws UIMAException, IOException {
    String propbankFileName = args[0];
    String penntreebankDirectoryName = args[1];
    String wsjSections = args[2];
    String outputDirectory = args[3];

    CollectionReader reader = CollectionReaderFactory.createCollectionReader(
        PropbankGoldReader.class,
        TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.srl.TypeSystem"),
        PropbankGoldReader.PARAM_PROPBANK_FILE_NAME,
        propbankFileName,
        PropbankGoldReader.PARAM_PENNTREEBANK_DIRECTORY_NAME,
        penntreebankDirectoryName,
        PropbankGoldReader.PARAM_WSJ_SECTIONS,
        wsjSections);
    AnalysisEngine treebankEngine = AnalysisEngineFactory.createPrimitive(
        TreebankGoldAnnotator.class,
        SrlComponents.TYPE_SYSTEM_DESCRIPTION);
    AnalysisEngine propbankEngine = AnalysisEngineFactory.createPrimitive(
        PropbankGoldAnnotator.class,
        SrlComponents.TYPE_SYSTEM_DESCRIPTION);
    AnalysisEngine xWriter = AnalysisEngineFactory.createPrimitive(
        XWriter.class,
        SrlComponents.TYPE_SYSTEM_DESCRIPTION,
        XWriter.PARAM_OUTPUT_DIRECTORY_NAME,
        outputDirectory,
        XWriter.PARAM_FILE_NAMER_CLASS_NAME,
        ViewURIFileNamer.class.getName());

    SimplePipeline.runPipeline(reader, treebankEngine, propbankEngine, xWriter);
  }
}
