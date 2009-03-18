package org.cleartk.example.documentclassification;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.cleartk.ViewNames;

public class GoldAnnotator extends JCasAnnotator_ImplBase {

	public static final String GOLD_VIEW_NAME = "ExampleDocumentClassificationGoldView";
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			JCas uriView = jCas.getView(ViewNames.URI);
			JCas goldView = jCas.createView(GOLD_VIEW_NAME);
			
			URI uri = new URI(uriView.getSofaDataURI());
			File file = new File(uri.getPath());
			
			goldView.setSofaDataString(file.getParentFile().getName(), "text/plain");
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
		catch (URISyntaxException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

}
