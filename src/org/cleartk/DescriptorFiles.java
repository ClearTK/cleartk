package org.cleartk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.TokenAnnotator;
import org.xml.sax.SAXException;

public class DescriptorFiles {

	public static void main(String[] args) throws SAXException, IOException, ResourceInitializationException {
		String outputDirectoryName = args[0];
		File outputDirectory = new File(outputDirectoryName);
		if(!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		AnalysisEngineDescription description = TokenAnnotator.getDescription();
		description.toXML(new FileWriter(new File(outputDirectory, TokenAnnotator.class.getSimpleName()+".xml")));

		description = ClearTKComponents.createSentencesAndTokens();
		description.toXML(new FileWriter(new File(outputDirectory, "SentencesAndTokens.xml")));
	}
}
