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
package org.cleartk.srl.conll2005;

import java.io.File;

import org.cleartk.CleartkComponents;
import org.cleartk.classifier.svmlight.DefaultSVMlightDataWriterFactory;
import org.cleartk.srl.ArgumentIdentificationHandler;
import org.cleartk.token.snowball.DefaultSnowballStemmer;
import org.cleartk.token.snowball.SnowballStemmer;
import org.uutuc.util.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Steven Bethard, Philipp Wetzler
 */
public class TrainConll2005Models {
	
	private static final File conll2005File = new File("../../ClearTK-data/data/conll2005/train-set-15-18");
	private static final File argumentIdentificationOutputDirectory = new File("scratch/CoNLL2005/argumentIdentification");
	private static final File argumentClassificationOutputDirectory = new File("scratch/CoNLL2005/argumentClassification");

	public static void main(String[] args) throws Exception {
		// run the components to write the training data
		SimplePipeline.runPipeline(
				CleartkComponents.createConll2005GoldReader(conll2005File.toString()),
				CleartkComponents.createConll2005GoldAnnotator(),
				CleartkComponents.createPrimitiveDescription(DefaultSnowballStemmer.class, SnowballStemmer.PARAM_STEMMER_NAME, "English"),
				CleartkComponents.createDataWriterAnnotator(
						ArgumentIdentificationHandler.class,
						DefaultSVMlightDataWriterFactory.class,
						argumentIdentificationOutputDirectory.toString(),
						null)//,
//				CleartkComponents.createDataWriterAnnotator(
//						ArgumentClassificationHandler.class,
//						DefaultMultiClassLIBSVMDataWriterFactory.class,
//						argumentClassificationOutputDirectory.toString())
		);
		
		// train the model on the training data
//		Train.main(outputDirectory.toString());
	}

}
