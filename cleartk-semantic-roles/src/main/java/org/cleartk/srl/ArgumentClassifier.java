/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.srl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.annotationpair.AnnotationPairFeatureExtractor;
import org.cleartk.classifier.feature.extractor.annotationpair.MatchingAnnotationPairExtractor;
import org.cleartk.classifier.feature.extractor.annotationpair.NamingAnnotationPairFeatureExtractor;
import org.cleartk.classifier.feature.extractor.annotationpair.RelativePositionExtractor;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.FirstInstanceExtractor;
import org.cleartk.classifier.feature.extractor.simple.LastInstanceExtractor;
import org.cleartk.classifier.feature.extractor.simple.MatchingAnnotationExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.classifier.jar.JarDataWriterFactory;
import org.cleartk.srl.feature.NamedEntityExtractor;
import org.cleartk.srl.feature.NodeTypeExtractor;
import org.cleartk.srl.feature.POSExtractor;
import org.cleartk.srl.feature.StemExtractor;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.feature.HeadWordExtractor;
import org.cleartk.syntax.feature.ParentExtractor;
import org.cleartk.syntax.feature.SiblingExtractor;
import org.cleartk.syntax.feature.SubCategorizationExtractor;
import org.cleartk.syntax.feature.SyntacticPathExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.uimafit.factory.AnalysisEngineFactory;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philipp Wetzler
 */

public class ArgumentClassifier extends CleartkAnnotator<String> {

	public static AnalysisEngineDescription getWriterDescription(
			Class<? extends DataWriterFactory<String>> dataWriterFactoryClass, File outputDirectory)
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				ArgumentClassifier.class,
				SrlComponents.TYPE_SYSTEM_DESCRIPTION,
				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, dataWriterFactoryClass.getName(),
				JarDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDirectory.toString());
	}

	public static AnalysisEngineDescription getClassifierDescription(File classifierJar)
	throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
				ArgumentClassifier.class,
				SrlComponents.TYPE_SYSTEM_DESCRIPTION,
				JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, classifierJar.toString());
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		SimpleFeatureExtractor defaultTokenExtractorSet = new MatchingAnnotationExtractor(Token.class,
				new SpannedTextExtractor(),
				new StemExtractor(),
				new POSExtractor()
		);

		SimpleFeatureExtractor defaultConstituentExtractorSet = new CombinedExtractor(
				new NodeTypeExtractor(),
				// new TypePathExtractor(TreebankNode.class, "nodeTags"),
				new HeadWordExtractor(
						defaultTokenExtractorSet
				),
				new FirstInstanceExtractor(Token.class,
						defaultTokenExtractorSet
				),
				new LastInstanceExtractor(Token.class,
						defaultTokenExtractorSet
				)
		);

		this.predicateExtractor = new NamingExtractor("Predicate",
				defaultTokenExtractorSet,
				new MatchingAnnotationExtractor(TreebankNode.class,
						new SubCategorizationExtractor()
				)
		);

		this.constituentExtractor = new NamingExtractor("Constituent",
				new MatchingAnnotationExtractor(TreebankNode.class,
						defaultConstituentExtractorSet,
						new ParentExtractor(defaultConstituentExtractorSet),
						new SiblingExtractor(-1, defaultConstituentExtractorSet),
						new SiblingExtractor(1, defaultConstituentExtractorSet),
						new NamedEntityExtractor()
				)
		);

		this.predicateAndConstituentExtractor = new NamingAnnotationPairFeatureExtractor("PredicateAndConstituent",
				new MatchingAnnotationPairExtractor(TreebankNode.class, TreebankNode.class,
						new SyntacticPathExtractor(
								new NodeTypeExtractor()
						),
						new RelativePositionExtractor()
				)
		);

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		/*
		 * Iterate over sentences in document
		 */
		List<Sentence> sentences = AnnotationRetrieval.getAnnotations(jCas, Sentence.class);

		try {
			nSentences = 0;
			nPredicates = 0;
			nArguments = 0;
			
			for( Sentence sentence : sentences ) {
				processSentence(jCas, sentence);
			}

			logger.info(String.format("processed %d sentences, %d predicates, ~%d arguments per predicate", nSentences, nPredicates, nPredicates == 0 ? 0 : nArguments / nPredicates));
		} catch (CleartkException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	void processSentence(JCas jCas, Sentence sentence) throws CleartkException{
		nSentences += 1;
		
		if( sentence.getCoveredText().length() > 40 )
			logger.fine(String.format("process sentence \"%s ...\"", sentence.getCoveredText().substring(0, 39)));
		else
			logger.fine(String.format("process sentence \"%s\"", sentence.getCoveredText()));

		/*
		 * Pre-compute sentence level data: sentenceConstituents: list of all
		 * constituents in sentence
		 */
		List<TreebankNode> constituents = new ArrayList<TreebankNode>(
				200);
		collectConstituents(AnnotationRetrieval.getContainingAnnotation(jCas, sentence, TopTreebankNode.class, false),
				constituents);

		/*
		 * Compute constituent features for all constituents in sentence
		 */
		Map<TreebankNode,List<Feature>> constituentFeatures = new HashMap<TreebankNode,List<Feature>>();
		for( TreebankNode c : constituents ) {
			List<Feature> features = constituentExtractor.extract(jCas, c);
			constituentFeatures.put(c, features);
		}

		/*
		 * Iterate over predicates in sentence
		 */
		List<Predicate> predicates = AnnotationRetrieval.getAnnotations(jCas,
				sentence, Predicate.class);
		for (Predicate predicate : predicates) {
			processPredicate(jCas, predicate,
					constituentFeatures);
		}
	}

	public void processPredicate(JCas jCas, Predicate predicate,
			Map<TreebankNode,List<Feature>> sentenceConstituentFeatures) throws CleartkException{
		nPredicates += 1;

		/*
		 * Compute predicate features
		 */
		List<Feature> predicateFeatures = new ArrayList<Feature>(12);
		predicateFeatures.addAll(predicateExtractor.extract(jCas, predicate));

		List<SemanticArgument> arguments = UIMAUtil.toList(predicate.getArguments(), SemanticArgument.class);

		/*
		 * Iterate over arguments
		 */
		for( SemanticArgument arg : arguments ) {
			if( ! (arg.getAnnotation() instanceof TreebankNode) ) {
				logger.warning(String.format("skipping argument of \"%s\", because it doesn't align with the parse tree", predicate.getCoveredText()));
				continue;
			}
			
			nArguments += 1;

			TreebankNode constituent = (TreebankNode) arg.getAnnotation();

			Instance<String> instance = new Instance<String>();

			/*
			 * Compute predicate-constituent features
			 */
			instance.addAll(predicateAndConstituentExtractor.extract(jCas, predicate, constituent));

			/*
			 * Add constituent features
			 */
			instance.addAll(sentenceConstituentFeatures.get(constituent));

			/*
			 * Add predicate features
			 */
			instance.addAll(predicateFeatures);

			if( isTraining() ) {
				instance.setOutcome(arg.getLabel());
				this.dataWriter.write(instance);
			} else {
				arg.setLabel(this.classifier.classify(instance.getFeatures()));
			}

		}
	}

	/**
	 * Recursively build a list of constituents under a TreebankNode.
	 * 
	 * @param top
	 *            the root of the parse tree to operate on; <b>top</b> itself
	 *            will also be added, unless it is of type TopTrebankNode
	 * @param constituents
	 *            list of nodes to add to
	 */
	protected void collectConstituents(TreebankNode top,
			List<TreebankNode> constituents) {
		if (top == null)
			throw new IllegalArgumentException();

		if (!(top instanceof TopTreebankNode))
			constituents.add(top);

		if (top.getChildren() == null)
			return;

		int numberOfChildren = top.getChildren().size();
		for (int i = 0; i < numberOfChildren; i++) {
			collectConstituents(top.getChildren(i), constituents);
		}
	}

	/////////

	//	public void process(JCas jCas, InstanceConsumer<String> consumer) throws CleartkException{
	//		List<Sentence> sentences = AnnotationRetrieval.getAnnotations(jCas, Sentence.class);
	//
	//		for( Sentence sentence : sentences ) {
	//			
	//			logger.fine(String.format("process sentence \"%s\"", sentence.getCoveredText()));
	//			
	//			List<TreebankNode> sentenceConstituents = new ArrayList<TreebankNode>(80);
	//			Iterator<?> constituentIterator = jCas.getJFSIndexRepository().getAnnotationIndex(TreebankNode.type).subiterator(sentence);
	//			while( constituentIterator.hasNext() )
	//				sentenceConstituents.add((TreebankNode) constituentIterator.next());
	//			
	//			Map<Integer,List<Feature>> constituentFeatures = new HashMap<Integer,List<Feature>>(140);
	//			for( TreebankNode constituent : sentenceConstituents )
	//				constituentFeatures.put(buildKey(constituent), extractConstituentFeatures(jCas, constituent));
	//
	//			
	//			Iterator<?> predicateIterator = jCas.getJFSIndexRepository().getAnnotationIndex(Predicate.type).subiterator(sentence);
	//			while( predicateIterator.hasNext() ) {
	//				Predicate predicate = (Predicate) predicateIterator.next();
	//				
	//				TreebankNode predicateNode = AnnotationRetrieval.getMatchingAnnotation(jCas, predicate.getAnnotation(), TreebankNode.class);
	//				Token predicateToken = AnnotationRetrieval.getMatchingAnnotation(jCas, predicate.getAnnotation(), Token.class);
	//
	//				List<Feature> predicateFeatures = extractPredicateFeatures(jCas, predicate, predicateNode, predicateToken);
	//
	//				List<Argument> predicateArguments = UIMAUtil.toList(predicate.getArguments(), Argument.class);
	//				
	//				ListIterator<Argument> iterator = predicateArguments.listIterator();
	//				while( iterator.hasNext() ) {
	//					SemanticArgument sarg;
	//					try {
	//						sarg = (SemanticArgument) iterator.next();
	//					} catch( ClassCastException e) {
	//						continue;
	//					}
	//
	//					if (sarg.getAnnotation() == null) {
	//						String message = "annotation property of SemanticArgument must be set";
	//						throw new IllegalArgumentException(message);
	//					}
	//					TreebankNode constituent = AnnotationRetrieval.getMatchingAnnotation(jCas, sarg.getAnnotation(), TreebankNode.class);
	//				
	//					Instance<String> instance = new Instance<String>();
	//					instance.addAll(predicateFeatures);
	//					instance.addAll(constituentFeatures.get(buildKey(constituent)));
	//					
	//					instance.addAll(relPosExtractor.extract(jCas, constituent, predicate.getAnnotation()));
	//					instance.addAll(pathExtractor.extract(jCas, constituent, predicateNode));
	//
	//					if (sarg.getFeature() != null ) {
	//						instance.setOutcome(sarg.getLabel() + "-" + sarg.getFeature());
	//					} else {
	//						instance.setOutcome(sarg.getLabel());
	//					}
	//
	//					String outcome = consumer.consume(instance);
	//					
	//					if( outcome != null ) {
	//						if( outcome.equals("NULL") ) {
	//							iterator.remove();
	//						} else {
	//							String[] parts = outcome.split("-", 1);
	//							sarg.setLabel(parts[0]);
	//							if( parts.length > 1 )
	//								sarg.setFeature(parts[1]);
	//							
	//							sarg.removeFromIndexes();
	//							sarg.addToIndexes();
	//						}
	//					}
	//					
	//				}
	//				
	//				predicate.setArguments(UIMAUtil.toFSArray(jCas, predicateArguments));
	//			}
	//		}
	//		
	//	}
	//
	//	private Integer buildKey(TreebankNode constituent) {
	//		return constituent.getAddress();
	//	}
	//	
	//	private List<Feature> extractConstituentFeatures(JCas jCas, TreebankNode constituent) {
	//		List<Feature> features = new ArrayList<Feature>(30);
	//		features.addAll(this.constituentExtractor.extract(jCas, constituent));
	//
	//		TreebankNode parent = constituent.getParent();
	//		if( parent != null ) {
	//			features.addAll(this.parentExtractor.extract(jCas, parent));
	//			List<TreebankNode> children = UIMAUtil.toList(parent.getChildren(), TreebankNode.class);
	//			int index = children.indexOf(constituent);
	//			if( index > 0 )
	//				features.addAll(this.leftSiblingExtractor.extract(jCas, children.get(index-1)));						
	//			if( index < children.size() - 1 )
	//				features.addAll(this.rightSiblingExtractor.extract(jCas, children.get(index+1)));
	//		}
	//		
	//		return features;	
	//	}
	//	
	//	List<Feature> extractPredicateFeatures(JCas jCas, Predicate predicate, TreebankNode predicateNode, Token predicateToken) {
	//		List<Feature> features = new ArrayList<Feature>(8);
	//		features.addAll(predicateTokenExtractor.extract(jCas, predicateToken));
	//		features.addAll(predicateNodeExtractor.extract(jCas, predicateNode));
	//		return features;
	//	}

	private SimpleFeatureExtractor predicateExtractor;
	private SimpleFeatureExtractor constituentExtractor;
	private AnnotationPairFeatureExtractor predicateAndConstituentExtractor;

	private int nSentences;
	private int nPredicates;
	private int nArguments;

	private Logger logger = Logger.getLogger(this.getClass().getName());

}
