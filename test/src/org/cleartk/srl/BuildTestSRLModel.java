package org.cleartk.srl;

import java.util.Arrays;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.metadata.SofaMapping;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.ViewNames;
import org.cleartk.classifier.DataWriterAnnotator;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.classifier.svmlight.DefaultSVMlightDataWriterFactory;
import org.cleartk.srl.propbank.PropbankGoldAnnotator;
import org.cleartk.srl.propbank.PropbankGoldReader;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.util.TestsUtil;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.SofaMappingFactory;
import org.uutuc.util.JCasIterable;

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

		AnalysisEngineDescription tbAnnotator = AnalysisEngineFactory.createPrimitiveAnalysisEngineDescription(
				TreebankGoldAnnotator.class, typeSystemDescription, (TypePriorities) null);
		
		AnalysisEngineDescription pbAnnotator = AnalysisEngineFactory.createPrimitiveAnalysisEngineDescription(
				PropbankGoldAnnotator.class, typeSystemDescription, (TypePriorities) null);
		
		
		AnalysisEngineDescription predicateDWA = AnalysisEngineFactory.createPrimitiveAnalysisEngineDescription(
				DataWriterAnnotator.class, typeSystemDescription, null, 
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, "test/data/srl/predicate",
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultSVMlightDataWriterFactory.class.getName(),
				DataWriterAnnotator.PARAM_ANNOTATION_HANDLER, PredicateAnnotationHandler.class.getName());
		
		AnalysisEngineDescription argumentDWA = AnalysisEngineFactory.createPrimitiveAnalysisEngineDescription(
				DataWriterAnnotator.class, typeSystemDescription, null,
				DataWriterAnnotator.PARAM_OUTPUT_DIRECTORY, "test/data/srl/argument",
				DataWriterAnnotator.PARAM_DATAWRITER_FACTORY_CLASS, DefaultMaxentDataWriterFactory.class.getName(),
				DataWriterAnnotator.PARAM_ANNOTATION_HANDLER, ArgumentAnnotationHandler.class.getName());
		
		AnalysisEngine aggregateAE = AnalysisEngineFactory.createAggregateAnalysisEngine(
				Arrays.asList(tbAnnotator, pbAnnotator, predicateDWA, argumentDWA), 
				Arrays.asList("tbAnnotator", "pbAnnotator", "predicateDWA", "argumentDWA"),
				typeSystemDescription, (TypePriorities) null, 
				new SofaMapping[] {
					SofaMappingFactory.createSofaMapping(ViewNames.TREEBANK_ANNOTATIONS, "predicateDWA", ViewNames.DEFAULT),
					SofaMappingFactory.createSofaMapping(ViewNames.TREEBANK_ANNOTATIONS, "argumentDWA", ViewNames.DEFAULT),
				});

		JCasIterable jCases = new JCasIterable(reader, aggregateAE);
		
		int i=0;
		for(@SuppressWarnings("unused") JCas jCas : jCases) { 
			i+=1;
			if( i % 10 == 0 && jCases.hasNext() )
				System.out.format("%d...\n", i);
		}
		System.out.format("%d\n", i);
		
		aggregateAE.collectionProcessComplete();
		
		System.out.println("---------------------");
		System.out.println("Train Predicate Model");
		System.out.println("---------------------");

		org.cleartk.classifier.Train.main(new String[] {"test/data/srl/predicate"});

		System.out.println("--------------------");
		System.out.println("Train Argument Model");
		System.out.println("--------------------");

		org.cleartk.classifier.Train.main(new String[] {"test/data/srl/argument"});
	}

}
