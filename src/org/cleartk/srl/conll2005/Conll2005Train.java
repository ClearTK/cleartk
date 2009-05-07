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

import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.ClearTKComponents;
import org.cleartk.classifier.Train;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.srl.ArgumentAnnotationHandler;
import org.cleartk.util.UIMAUtil;
import org.uutuc.factory.UimaContextFactory;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Steven Bethard
 */
public class Conll2005Train {

	private static void error(String message) throws Exception {
		Logger logger = UimaContextFactory.createUimaContext().getLogger();
		logger.log(Level.SEVERE, String.format("%s\nusage: " +
				"%s train-set-file output-dir",
				message, Conll2005Train.class.getSimpleName()));
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		// check arguments
		if (args.length != 2) {
			error("wrong number of arguments");
		} else if (!new File(args[0]).exists()) {
			error("CoNLL 2005 train-set file not found: " + args[0]);
		}
		String conll2005File = args[0];
		String outputDir = args[1];
		
		// run the components to write the training data
		UIMAUtil.runUIMAPipeline(
				ClearTKComponents.createConll2005GoldReader(conll2005File),
				ClearTKComponents.createConll2005GoldAnnotator(),
				ClearTKComponents.createSnowballStemmer("English"),
				ClearTKComponents.createDataWriterAnnotator(
						ArgumentAnnotationHandler.class,
						DefaultMaxentDataWriterFactory.class,
						outputDir));
		
		// train the model on the training data
		Train.main(outputDir);
	}

}
