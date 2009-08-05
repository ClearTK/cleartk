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

package org.cleartk.example.pos;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.CleartkComponents;
import org.cleartk.ViewNames;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.token.snowball.DefaultSnowballStemmer;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.util.FilesCollectionReader;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 * 
 *         For examples of using the ExamplePOSAnnotationHandler using different
 *         classifiers, please see
 *         org.cleartk.example.pos.ExamplePosClassifierTest
 */

public class BuildTestExamplePosModel {

	
	public static void main(String[] args) throws Exception {
		
		TypeSystemDescription typeSystemDescription = CleartkComponents.TYPE_SYSTEM_DESCRIPTION;
		
		SimplePipeline.runPipeline(
				CollectionReaderFactory.createCollectionReader(
						FilesCollectionReader.class, typeSystemDescription,
						FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, "test/data/docs/treebank",
						FilesCollectionReader.PARAM_SUFFIXES,  new String[] { ".tree" },
						FilesCollectionReader.PARAM_VIEW_NAME, ViewNames.TREEBANK),
				CleartkComponents.createPrimitiveDescription(TreebankGoldAnnotator.class, TreebankGoldAnnotator.PARAM_POST_TREES, false),
				CleartkComponents.createPrimitiveDescription(DefaultSnowballStemmer.class, SnowballStemmer.PARAM_STEMMER_NAME, "English"),
				ExamplePOSAnnotationHandler.getWriterDescription(ExamplePOSAnnotationHandler.DEFAULT_OUTPUT_DIRECTORY));
				
		org.cleartk.classifier.Train.main("example/model");

	}
}
