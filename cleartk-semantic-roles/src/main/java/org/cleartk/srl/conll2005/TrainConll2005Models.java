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
import java.util.List;

import org.cleartk.classifier.jar.Train;
import org.cleartk.classifier.libsvm.DefaultBinaryLIBSVMDataWriterFactory;
import org.cleartk.classifier.libsvm.DefaultMultiClassLIBSVMDataWriterFactory;
import org.cleartk.srl.ArgumentClassifier;
import org.cleartk.srl.ArgumentIdentifier;
import org.cleartk.srl.SrlComponents;
import org.cleartk.token.stem.snowball.DefaultSnowballStemmer;
import org.cleartk.util.CleartkComponents;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Steven Bethard, Philipp Wetzler
 */
public class TrainConll2005Models {
	
	private static final File conll2005File = new File("../../ClearTK-data/data/conll2005/trainset-15-18");
//	private static final File predicateIdentificationOutputDirectory = new File("scratch/CoNLL2005/predicateIdentification");
	private static final File argumentIdentificationOutputDirectory = new File("scratch/CoNLL2005/argumentIdentification");
	private static final File argumentClassificationOutputDirectory = new File("scratch/CoNLL2005/argumentClassification");

	public static void main(String[] args) throws Exception {
		argumentIdentificationOutputDirectory.getParentFile().mkdirs();
		argumentClassificationOutputDirectory.getParentFile().mkdirs();
		
		// run the components to write the training data
		SimplePipeline.runPipeline(
				Conll2005GoldReader.getCollectionReader(conll2005File.toString()),
				AnalysisEngineFactory.createPrimitiveDescription(
						Conll2005GoldAnnotator.class, 
						SrlComponents.TYPE_SYSTEM_DESCRIPTION,
						Conll2005GoldAnnotator.PARAM_HAS_VERB_SENSES, true),
				DefaultSnowballStemmer.getDescription("English"),
//				CleartkComponents.createCleartkAnnotator(
//						PredicateAnnotator.class, 
//						DefaultSVMlightDataWriterFactory.class, 
//						predicateIdentificationOutputDirectory.toString()),
				CleartkComponents.createCleartkAnnotator(
						ArgumentIdentifier.class, SrlComponents.TYPE_SYSTEM_DESCRIPTION,
						DefaultBinaryLIBSVMDataWriterFactory.class, 
						argumentIdentificationOutputDirectory.toString(), 
						(List<Class<?>>)null
				),
				CleartkComponents.createCleartkAnnotator(
						ArgumentClassifier.class, SrlComponents.TYPE_SYSTEM_DESCRIPTION,
						DefaultMultiClassLIBSVMDataWriterFactory.class, 
						argumentClassificationOutputDirectory.toString(),
						(List<Class<?>>)null)
		);
		
		// train the model on the training data
//		Train.main(predicateIdentificationOutputDirectory.toString(), "--executable", "svm_perf_learn", "-c", "1", "-l", "1");
		Train.main(argumentIdentificationOutputDirectory.toString(), "--executable", "svm_perf_learn", "-c", "10000");
		Train.main(argumentClassificationOutputDirectory.toString(), "--executable", "svm_perf_learn", "-c", "1");
	}

}
