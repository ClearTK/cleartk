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

package org.cleartk.classifier.mallet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.cleartk.Initializable;
import org.cleartk.classifier.SequentialDataWriter;
import org.cleartk.classifier.SequentialDataWriterFactory_ImplBase;
import org.cleartk.classifier.encoder.factory.NameNumberEncoderFactory;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */

public class DefaultMalletCRFDataWriterFactory extends SequentialDataWriterFactory_ImplBase implements Initializable{

	@Override
	public void initialize(UimaContext context) {
		NameNumberEncoderFactory nnef = new NameNumberEncoderFactory();
		featuresEncoder = nnef.createFeaturesEncoder(context);
		outcomeEncoder = nnef.createOutcomeEncoder(context);
	}
	
	@Override
	public SequentialDataWriter<?> createSequentialDataWriter(File outputDirectory) throws IOException {
		MalletCRFDataWriter mdw = new MalletCRFDataWriter(outputDirectory);
		mdw.setFeaturesEncoder((FeaturesEncoder<List<NameNumber>>)getFeaturesEncoder());
		mdw.setOutcomeEncoder((OutcomeEncoder<String, String>)getOutcomeEncoder());
		return mdw;
	}
}
