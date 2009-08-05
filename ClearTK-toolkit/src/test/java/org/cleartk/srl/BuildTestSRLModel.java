/** 
 * Copyright (c) 2007-2009, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
 * in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote 
 * products derived from this software without specific prior written permission. 
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

package org.cleartk.srl;

import java.util.Arrays;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.svmlight.DefaultSVMlightDataWriterFactory;
import org.cleartk.srl.propbank.PropbankGoldAnnotator;
import org.cleartk.srl.propbank.PropbankGoldReader;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.util.TestsUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.util.JCasIterable;


/**
 * <br>Copyright (c) 2007-2009, Regents of the University of Colorado 
 * <br>All rights reserved.
 *
 * @author Philip Wetzler
 * 
 */

public class BuildTestSRLModel {
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("--------------------");
		System.out.println("Create Training Data");
		System.out.println("--------------------");
		
		TypeSystemDescription typeSystemDescription = TestsUtil.getTypeSystemDescription();
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(PropbankGoldReader.class, typeSystemDescription,
				PropbankGoldReader.PARAM_PROPBANK_FILE, "../ClearTK Data/data/propbank-1.0/prop.txt",
				PropbankGoldReader.PARAM_PENNTREEBANK_DIRECTORY, "../ClearTK Data/data/treebank",
				PropbankGoldReader.PARAM_WSJ_SECTIONS, "02");

		AnalysisEngineDescription tbAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
				TreebankGoldAnnotator.class, typeSystemDescription, (TypePriorities) null);
		
		AnalysisEngineDescription pbAnnotator = AnalysisEngineFactory.createPrimitiveDescription(
				PropbankGoldAnnotator.class, typeSystemDescription, (TypePriorities) null);
		
		
		AnalysisEngineDescription predicateDWA = AnalysisEngineFactory.createPrimitiveDescription(
				DataWriterAnnotator.class, typeSystemDescription, null, 
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, "test/data/srl/predicate",
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultSVMlightDataWriterFactory.class.getName(),
				InstanceConsumer.PARAM_ANNOTATION_HANDLER, PredicateAnnotationHandler.class.getName());
		
		AnalysisEngineDescription argumentDWA = AnalysisEngineFactory.createPrimitiveDescription(
				DataWriterAnnotator.class, typeSystemDescription, null,
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, "test/data/srl/argument",
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMaxentDataWriterFactory.class.getName(),
				InstanceConsumer.PARAM_ANNOTATION_HANDLER, ArgumentAnnotationHandler.class.getName());
		
		AnalysisEngine aggregateAE = AnalysisEngineFactory.createAggregate(
				Arrays.asList(tbAnnotator, pbAnnotator, predicateDWA, argumentDWA), 
				Arrays.asList("tbAnnotator", "pbAnnotator", "predicateDWA", "argumentDWA"),
				typeSystemDescription, (TypePriorities) null, null);

		JCasIterable jCases = new JCasIterable(reader, aggregateAE);
		
		int i=0;
		for(JCas jCas : jCases) {
			assert jCas != null;
			i+=1;
			if( i % 10 == 0 && jCases.hasNext() )
				System.out.format("%d...\n", i);
		}
		System.out.format("%d\n", i);
		
		aggregateAE.collectionProcessComplete();
		
		System.out.println("---------------------");
		System.out.println("Train Predicate Model");
		System.out.println("---------------------");

		org.cleartk.classifier.Train.main("test/data/srl/predicate");

		System.out.println("--------------------");
		System.out.println("Train Argument Model");
		System.out.println("--------------------");

		org.cleartk.classifier.Train.main("test/data/srl/argument");
	}

}
