package org.cleartk.util.linewriter;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.type.Token;

public class SillyBlockWriter implements BlockWriter<Token>{

	public void initialize(UimaContext context) throws ResourceInitializationException {
		// TODO Auto-generated method stub
		
	}

	public String writeBlock(JCas cas, Token blockAnnotation) {
		return blockAnnotation.getPos();
	}

}
