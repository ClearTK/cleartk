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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philipp G. Wetzler
 *
 */
public class EvaluationAnnotator extends JCasAnnotator_ImplBase {

	int totalClassifications = 0;
	int totalCorrect = 0;
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		try {
			JCas goldView = jCas.getView(GoldAnnotator.GOLD_VIEW_NAME);
			JCas predictionView = jCas.getView(DocumentClassificationAnnotator.PREDICTION_VIEW_NAME);

			String gold = goldView.getSofaDataString();
			String prediction = predictionView.getSofaDataString();

			classes.add(gold);
			classes.add(prediction);

			int value = get(gold, prediction);
			set(gold, prediction, value+1);
			
			totalClassifications++;
			if(gold.equals(prediction)) {
				totalCorrect++;
			}
		}
		catch (CASException e) {
			throw new AnalysisEngineProcessException();
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		float accuracy = (float) totalCorrect / totalClassifications;
		System.out.println("overall accuracy: "+totalCorrect+"/"+totalClassifications+" = "+accuracy);
		try {
			super.collectionProcessComplete();

			System.out.print("        ");
			for( String cp : classes ) {
				System.out.format("%7.7s\t", cp);
			}
			System.out.println();

			for( String cg : classes ) {
				System.out.format("%7.7s\t", cg);
				for( String cp : classes ) {
					System.out.format("%7d\t", get(cg, cp));
				}
				System.out.println();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private int get(String gold, String prediction) {
		if( confusionMatrix.containsKey(gold) ) {
			Map<String, Integer> m = confusionMatrix.get(gold);
			if( m.containsKey(prediction) )
				return m.get(prediction);
			else
				return 0;
		} else {
			return 0;
		}
	}

	private void set(String gold, String prediction, int value) {
		if( confusionMatrix.containsKey(gold) ) {
			Map<String, Integer> m = confusionMatrix.get(gold);
			m.put(prediction, value);
		} else {
			Map<String, Integer> m = new HashMap<String, Integer>();
			m.put(prediction, value);
			confusionMatrix.put(gold, m);
		}
	}

	private Set<String> classes = new TreeSet<String>();
	private Map<String, Map<String, Integer>> confusionMatrix = new HashMap<String, Map<String, Integer>>();

}
