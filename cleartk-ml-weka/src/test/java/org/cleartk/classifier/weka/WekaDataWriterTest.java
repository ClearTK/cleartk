/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.classifier.weka;

import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.test.DefaultTestBase;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philip Ogren
 * 
 */


public class WekaDataWriterTest extends DefaultTestBase {

	public static class Test1Annotator extends CleartkAnnotator<String> {

		public void process(JCas cas) throws AnalysisEngineProcessException {
				List<Feature> features = Arrays.asList(new Feature("A", 1.1), new Feature("B", 3.0), new Feature("C", 1.234));
				Instance<String> instance = new Instance<String>("yes", features);
				this.dataWriter.write(instance);

				features = Arrays.asList(new Feature("A", 2.1), new Feature("B", 2.0), new Feature("C", 2.234));
				instance = new Instance<String>("no", features);
				this.dataWriter.write(instance);

				features = Arrays.asList(new Feature("A", 5.1), new Feature("B", 5.0), new Feature("C", 5.234));
				instance = new Instance<String>("yes", features);
				this.dataWriter.write(instance);

		}
	}

//	@Ignore
	@Test
	public void test1() throws Exception {
		AnalysisEngineDescription dataWriterDescription = CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(Test1Annotator.class, typeSystemDescription, DefaultWekaDataWriterFactory.class, "target/weka-temp");
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(dataWriterDescription, DefaultWekaDataWriterFactory.PARAM_RELATION_TAG, "test1-relation");
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();
	}

	
	
	
	public static class Test2Annotator extends CleartkAnnotator<String> {

		public void process(JCas cas) throws AnalysisEngineProcessException {
				List<Feature> features = Arrays.asList(new Feature("pos", "NN"), new Feature("distance", 3.0), new Feature("precision", 1.234));
				Instance<String> instance = new Instance<String>("A", features);
				this.dataWriter.write(instance);

				features = Arrays.asList(new Feature("name", "2PO"), new Feature("p's", 2));
				instance = new Instance<String>("B", features);
				this.dataWriter.write(instance);

				instance = new Instance<String>("Z");
				this.dataWriter.write(instance);

				features = Arrays.asList(new Feature("A_B", "AB"));
				instance = new Instance<String>("A", features);
				this.dataWriter.write(instance);
		}
	}

	@Test
	public void test2() throws Exception {
		AnalysisEngineDescription dataWriterDescription = CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(Test2Annotator.class, typeSystemDescription, DefaultWekaDataWriterFactory.class, "target/weka-temp2");
		AnalysisEngine dataWriterAnnotator = AnalysisEngineFactory.createPrimitive(dataWriterDescription, DefaultWekaDataWriterFactory.PARAM_RELATION_TAG, "test2-relation");
		dataWriterAnnotator.process(jCas);
		dataWriterAnnotator.collectionProcessComplete();
	}
}
