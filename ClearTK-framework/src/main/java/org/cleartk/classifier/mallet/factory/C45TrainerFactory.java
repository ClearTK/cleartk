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
package org.cleartk.classifier.mallet.factory;

import cc.mallet.classify.C45;
import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.ClassifierTrainer;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Philip Ogren
 */

public class C45TrainerFactory implements ClassifierTrainerFactory<C45> {

	public static final String NAME = "C45";
	
	public ClassifierTrainer<C45> createTrainer(String... args) {
		C45Trainer trainer = new C45Trainer();
		if(args != null) {
			if(args.length % 2 != 0) {
				throw new IllegalArgumentException("each argument must be supplied with a value:  "+getUsageMessage());
			}
			for(int i=0; i<args.length; i+=2) {
				String optionName = args[i];
				String optionValue = args[i+1];
				if(optionName.equals("--depthLimited"))
					trainer.setDepthLimited(Boolean.parseBoolean(optionValue));
				else if(optionName.equals("--doPruning"))
					trainer.setDoPruning(Boolean.parseBoolean(optionValue));
				else if(optionName.equals("--maxDepth"))
					trainer.setMaxDepth(Integer.parseInt(optionValue));
				else if(optionName.equals("--minNumInsts"))
					trainer.setMinNumInsts(Integer.parseInt(optionValue));
				else
					throw new IllegalArgumentException(String.format("the argument %1$s is invalid.  ", optionName) + getUsageMessage());
			}
		}
		return trainer;
	}

	public String getUsageMessage() {
		return "The arguments for C45TrainerFactory.createTrainer(String...args) should be either empty or include any of the following:" +
		"\n--depthLimited boolean" +
		"\n--doPruning boolean" +
		"\n--maxDepth integer" +
		"\n--minNumInsts integer";
	}

}
