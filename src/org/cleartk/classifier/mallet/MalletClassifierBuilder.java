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
package org.cleartk.classifier.mallet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.cleartk.classifier.BuildJar;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.ClassifierBuilder;
import org.cleartk.classifier.mallet.factory.ClassifierTrainerFactory;
import org.cleartk.util.ReflectionUtil;

import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.types.InstanceList;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philip Ogren
 */

public class MalletClassifierBuilder implements ClassifierBuilder<String> {

	public void train(File dir, String[] args) throws Exception {

		InstanceListCreator instanceListCreator = new InstanceListCreator();
		InstanceList instanceList = instanceListCreator.createInstanceList(new File(dir, "training-data.mallet"));
		instanceList.save(new File(dir, "training-data.ser"));
		
		String factoryName = args[0];
		Class<ClassifierTrainerFactory<?>> factoryClass = createTrainerFactory(factoryName);
		if(factoryClass == null) {
			String factoryName2 = "org.cleartk.classifier.mallet.factory."+factoryName+"TrainerFactory";
			factoryClass = createTrainerFactory(factoryName2);
		}
		if(factoryClass == null) {
			throw new IllegalArgumentException(String.format("name for classifier trainer factory is not valid: name given ='%s'.  Valid classifier names include: %s, %s, %s, and %s", factoryName, ClassifierTrainerFactory.NAMES[0], ClassifierTrainerFactory.NAMES[1], ClassifierTrainerFactory.NAMES[2], ClassifierTrainerFactory.NAMES[3]));
		}
		
		String[] factoryArgs = new String[args.length - 1];
		System.arraycopy(args, 1, factoryArgs, 0, factoryArgs.length);

		ClassifierTrainerFactory<?> factory = factoryClass.newInstance();
		ClassifierTrainer<?> trainer = null;
		try {
			trainer = factory.createTrainer(factoryArgs);
		} catch(Throwable t) {
			throw new IllegalArgumentException("Unable to create trainer.  Usage for "+factoryClass.getCanonicalName()+": "+factory.getUsageMessage(),t);
		}
		
		cc.mallet.classify.Classifier classifier = trainer.train(instanceList);

		ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (new File(dir, "model.mallet")));
		oos.writeObject (classifier);
		oos.close();

	}

	private Class<ClassifierTrainerFactory<?>> createTrainerFactory(String className){
		try {
			return ReflectionUtil.uncheckedCast(Class.forName(className));
		} catch(ClassNotFoundException cnfe) {
			return null;
		}
	}
	
	public void buildJar(File dir, String[] args) throws Exception {
		BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
		stream.write("model.mallet", new File(dir, "model.mallet"));
		stream.close();
	}

	public Class<? extends Classifier<String>> getClassifierClass() {
		return MalletClassifier.class;
	}

}









//
//String[] malletArgs = new String[6];
//malletArgs[0] = "--input";
//malletArgs[1] = new File(dir, "training-data.ser").getPath();
//malletArgs[2] = "--output-classifier";
//malletArgs[3] = new File(dir, "model.mallet").getPath();
//malletArgs[4] = "--trainer";
//malletArgs[5] = args[0];
//
//for (String string : malletArgs) {
//	System.out.println(string);
//}
//Vectors2Classify.main(malletArgs);
