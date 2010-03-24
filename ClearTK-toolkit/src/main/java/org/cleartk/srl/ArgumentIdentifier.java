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
import java.util.List;
import java.util.logging.Logger;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.annotationpair.AnnotationPairFeatureExtractor;
import org.cleartk.classifier.feature.extractor.annotationpair.MatchingAnnotationPairExtractor;
import org.cleartk.classifier.feature.extractor.annotationpair.NamingAnnotationPairFeatureExtractor;
import org.cleartk.classifier.feature.extractor.annotationpair.RelativePositionExtractor;
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
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.feature.HeadWordExtractor;
import org.cleartk.syntax.feature.SubCategorizationExtractor;
import org.cleartk.syntax.feature.SyntacticPathExtractor;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.uutuc.util.InitializeUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * <p>
 * ArgumentIdentifier can work in 3 modes: <il>
 * <li> <b>training mode</b>: take in fully annotated Propbank style data and
 * generate training data for detection of arguments </li>
 * <li> <b>filter mode</b>: take in fully annotated Propbank style data and a
 * model, then add annotations for falsely detected arguments, and remove
 * annotations for missed arguments; this is to facilitate training of
 * AnnotationClassifier </li>
 * <li> <b>annotation mode</b>: take in unlabeled Treebank style data and
 * annotate all detected arguments (no labeling is done by this annotator) </li>
 * </il>
 * </p>
 * 
 * @author Philipp Wetzler, Philip Ogren
 */
public class ArgumentIdentifier extends CleartkAnnotator<Boolean> {

	public static AnalysisEngineDescription getWriterDescription(
			Class<? extends DataWriterFactory<Boolean>> dataWriterFactoryClass, File outputDirectory)
	throws ResourceInitializationException {
		return CleartkComponents.createPrimitiveDescription(
				ArgumentIdentifier.class,
				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, dataWriterFactoryClass.getName(),
				JarDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDirectory.toString());
	}

	public static AnalysisEngineDescription getClassifierDescription(File classifierJar)
	throws ResourceInitializationException {
		return CleartkComponents.createPrimitiveDescription(
				ArgumentIdentifier.class,
				JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, classifierJar.toString());
	}

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		InitializeUtil.initialize(this, context);

		SimpleFeatureExtractor defaultTokenExtractorSet = new MatchingAnnotationExtractor(Token.class,
				new SpannedTextExtractor(),
				new StemExtractor(),
				new POSExtractor()
		);

		this.perPredicateExtractor = new NamingExtractor("Predicate",
				new MatchingAnnotationExtractor(Token.class,
						defaultTokenExtractorSet
				),
				new MatchingAnnotationExtractor(TreebankNode.class,
						new SubCategorizationExtractor()
				)
		);

		this.perConstituentExtractor = new NamingExtractor("Constituent",
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
				),
				new NamedEntityExtractor()
		);

		this.perPredicatAndConstituentExtractor = new NamingAnnotationPairFeatureExtractor("PredicateAndConstituent",
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
			nConstituents = 0;
			
			for( Sentence sentence : sentences ) {
				processSentence(jCas, sentence);
			}

			logger.info(String.format("processed %d sentences, %d predicates, ~%d constituents per predicate", nSentences, nPredicates, nPredicates == 0 ? 0 : nConstituents / nPredicates));
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
		List<TreebankNode> sentenceConstituents = new ArrayList<TreebankNode>(
				200);
		collectConstituents(AnnotationRetrieval.getContainingAnnotation(jCas, sentence, TopTreebankNode.class, false),
				sentenceConstituents);

		/*
		 * Compute constituent features for all constituents in sentence
		 */
		List<List<Feature>> sentenceConstituentFeatures = new ArrayList<List<Feature>>(
				sentenceConstituents.size());
		for (TreebankNode constituent : sentenceConstituents) {
			sentenceConstituentFeatures.add(perConstituentExtractor.extract(jCas, constituent));
		}

		/*
		 * Iterate over predicates in sentence
		 */
		List<Predicate> predicates = AnnotationRetrieval.getAnnotations(jCas,
				sentence, Predicate.class);
		for (Predicate predicate : predicates) {
			processPredicate(jCas, predicate, sentenceConstituents,
					sentenceConstituentFeatures);
		}
	}

	public void processPredicate(JCas jCas, Predicate predicate,
			List<TreebankNode> sentenceConstituents,
			List<List<Feature>> sentenceConstituentFeatures) throws CleartkException{
		nPredicates += 1;


		/*
		 * Compute predicate features
		 */
		List<Feature> predicateFeatures = new ArrayList<Feature>(12);
		predicateFeatures.addAll(perPredicateExtractor.extract(jCas, predicate.getAnnotation()));


		/*
		 * Iterate over constituents in sentence
		 */
		for (int i = 0; i < sentenceConstituents.size(); i++) {
			nConstituents += 1;
			TreebankNode constituent = sentenceConstituents.get(i);

			Instance<Boolean> instance = new Instance<Boolean>();

			/*
			 * Compute predicate-constituent features
			 */
			instance.addAll(perPredicatAndConstituentExtractor.extract(jCas, constituent, predicate.getAnnotation()));

			/*
			 * Add constituent features
			 */
			instance.addAll(sentenceConstituentFeatures.get(i));

			/*
			 * Add predicate features
			 */
			instance.addAll(predicateFeatures);

			if( isTraining() ) {
				instance.setOutcome(false);

				for( int j=0; j<predicate.getArguments().size(); j++ ) {
					Argument arg = predicate.getArguments(j);
					if( arg.getAnnotation().equals(constituent) ) {
						instance.setOutcome(true);
						break;
					}
				}
			}


			if (this.isTraining()) {
				this.dataWriter.write(instance);
			} else {
				boolean isArgument = this.classifier.classify(instance.getFeatures());

				if (isArgument) {
					SemanticArgument arg = new SemanticArgument(jCas);
					arg.setAnnotation(constituent);
					arg.setBegin(constituent.getBegin());
					arg.setEnd(constituent.getEnd());
					arg.setLabel("?");
					arg.addToIndexes();
					
					List<Argument> args = UIMAUtil.toList(predicate.getArguments(), Argument.class);
					args.add(arg);
					predicate.setArguments(UIMAUtil.toFSArray(jCas, args));
				}
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


	private SimpleFeatureExtractor perPredicateExtractor;
	private SimpleFeatureExtractor perConstituentExtractor;
	private AnnotationPairFeatureExtractor perPredicatAndConstituentExtractor;
	
	private int nSentences;
	private int nPredicates;
	private int nConstituents;

	private Logger logger = Logger.getLogger(this.getClass().getName());

}
