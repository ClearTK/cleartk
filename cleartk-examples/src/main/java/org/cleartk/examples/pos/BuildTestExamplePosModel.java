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

package org.cleartk.examples.pos;

import org.cleartk.examples.ExampleComponents;
import org.cleartk.syntax.constituent.TreebankConstants;
import org.cleartk.syntax.constituent.TreebankGoldAnnotator;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.util.cr.FilesCollectionReader;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 *         For examples of using the ExamplePOSAnnotationHandler using different classifiers, please
 *         see org.cleartk.example.pos.ExamplePosClassifierTest
 */

public class BuildTestExamplePosModel {

  public static void main(String[] args) throws Exception {

    SimplePipeline.runPipeline(FilesCollectionReader.getCollectionReaderWithSuffixes(
        ExampleComponents.TYPE_SYSTEM_DESCRIPTION,
        "src/main/resources/data/pos/treebank",
        TreebankConstants.TREEBANK_VIEW,
        ".tree"), TreebankGoldAnnotator.getDescriptionPOSTagsOnly(), DefaultSnowballStemmer
        .getDescription("English"), ExamplePOSAnnotator
        .getWriterDescription(ExamplePOSAnnotator.DEFAULT_OUTPUT_DIRECTORY));

    System.out.println("training data written to " + ExamplePOSAnnotator.DEFAULT_OUTPUT_DIRECTORY);
    System.out.println("training model...");
    org.cleartk.classifier.jar.Train.main(ExamplePOSAnnotator.DEFAULT_OUTPUT_DIRECTORY);
    System.out.println("model written to " + ExamplePOSAnnotator.DEFAULT_MODEL);

  }
}
