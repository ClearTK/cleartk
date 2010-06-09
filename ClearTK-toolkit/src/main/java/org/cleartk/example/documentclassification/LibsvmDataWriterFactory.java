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
package org.cleartk.example.documentclassification;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.FeatureVectorFeaturesEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.features.TFIDFEncoder;
import org.cleartk.classifier.encoder.features.normalizer.EuclidianNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.encoder.outcome.StringToIntegerOutcomeEncoder;
import org.cleartk.classifier.libsvm.MultiClassLIBSVMDataWriter;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.InitializeUtil;
import org.uimafit.util.initialize.Initializable;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Ogren
 *
 */

public class LibsvmDataWriterFactory implements org.cleartk.classifier.DataWriterFactory<String>, Initializable {

	public static final String PARAM_OUTPUT_DIRECTORY = 
			ConfigurationParameterFactory.createConfigurationParameterName(LibsvmDataWriterFactory.class, "outputDirectory");
	@ConfigurationParameter(mandatory = true, description = "provides the name of the directory where the training data will be written.")
	protected File outputDirectory;

	public static final String PARAM_IDFMAP_FILE_NAME = 
			ConfigurationParameterFactory.createConfigurationParameterName(LibsvmDataWriterFactory.class, "idfmapFileName");
	@ConfigurationParameter(mandatory = true, description = "provides the file name of the IDF Map")
	public String idfmapFileName;
	
	public static final String PARAM_CUTOFF = 
			ConfigurationParameterFactory.createConfigurationParameterName(LibsvmDataWriterFactory.class, "cutoff");
	@ConfigurationParameter(
			defaultValue = "5",
			description = "features that occur less than this number of times over the whole training set will not be encoded during testing")
	protected int cutoff = 5;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		InitializeUtil.initialize(this, context);
	}

	
	public DataWriter<String> createDataWriter() throws IOException {
		MultiClassLIBSVMDataWriter dataWriter = new MultiClassLIBSVMDataWriter(outputDirectory);

			NameNumberNormalizer normalizer = new EuclidianNormalizer();
			FeatureVectorFeaturesEncoder featuresEncoder = new FeatureVectorFeaturesEncoder(cutoff, normalizer);
			featuresEncoder.addEncoder(new TFIDFEncoder(new File(idfmapFileName)));
			featuresEncoder.addEncoder(new NumberEncoder());
			featuresEncoder.addEncoder(new BooleanEncoder());
			featuresEncoder.addEncoder(new StringEncoder());
			dataWriter.setFeaturesEncoder(featuresEncoder);

			dataWriter.setOutcomeEncoder(new StringToIntegerOutcomeEncoder());

		return dataWriter;
	}

}
