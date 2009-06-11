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

package org.cleartk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.example.pos.ExamplePOSAnnotationHandler;
import org.cleartk.example.pos.ExamplePOSPlainTextWriter;
import org.cleartk.token.TokenAnnotator;
import org.xml.sax.SAXException;

public class DescriptorFiles {

	/**
	 * <br>
	 * Copyright (c) 2009, Regents of the University of Colorado <br>
	 * All rights reserved.
	 * @author Philip Ogren
	 */

	public static void main(String[] args) throws SAXException, IOException, ResourceInitializationException {
		String outputDirectoryName = "src/org/cleartk/descriptor";
		File outputDirectory = new File(outputDirectoryName);
		if(!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		AnalysisEngineDescription description = TokenAnnotator.getDescription();
		description.toXML(new FileWriter(new File(outputDirectory, TokenAnnotator.class.getSimpleName()+".xml")));

		description = ClearTKComponents.createSentencesAndTokens();
		description.toXML(new FileWriter(new File(outputDirectory, "SentencesAndTokens.xml")));

		description = ExamplePOSAnnotationHandler.getClassifierDescription(ExamplePOSAnnotationHandler.DEFAULT_MODEL);
		description.toXML(new FileWriter(new File(outputDirectory, "ExamplePOSAnnotator.xml")));

		description = ExamplePOSAnnotationHandler.getWriterDescription(ExamplePOSAnnotationHandler.DEFAULT_OUTPUT_DIRECTORY);
		description.toXML(new FileWriter(new File(outputDirectory, "ExamplePOSDataWriter.xml")));

		description = ExamplePOSPlainTextWriter.getDescription(ExamplePOSPlainTextWriter.DEFAULT_OUTPUT_DIRECTORY);
		description.toXML(new FileWriter(new File(outputDirectory, "ExamplePOSPlainTextWriter.xml")));
}
}
