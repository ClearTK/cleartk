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
package org.cleartk.classifier.svmlight;

import java.io.File;

import org.cleartk.classifier.BuildJar;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.ClassifierBuilder;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class OVASVMlightClassifierBuilder implements ClassifierBuilder<String> {

	public void train(File dir, String[] args) throws Exception {
		for (File file: dir.listFiles()) {
			if (file.getName().matches("training-data-\\d+.svmlight")) {
				SVMlightClassifierBuilder.train(file.getPath(), args);
			}
		}
	}

	public void buildJar(File dir, String[] args) throws Exception {
		BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
		for (File file: dir.listFiles()) {
			if (file.getName().matches("training-data-\\d+.svmlight.model")) {
				String name = file.getName();
				name = name.replaceAll("training-data", "model");
				name = name.replaceAll(".model", "");
				stream.write(name, file);
			}
		}
		stream.close();
	}

	public Class<? extends Classifier<String>> getClassifierClass() {
		return OVASVMlightClassifier.class;
	}

}
