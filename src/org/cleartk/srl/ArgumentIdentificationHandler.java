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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.HeadWordExtractor;
import org.cleartk.classifier.feature.extractor.RelativePositionExtractor;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.SubCategorizationExtractor;
import org.cleartk.classifier.feature.extractor.SyntacticPathExtractor;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;


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
public class ArgumentIdentificationHandler implements AnnotationHandler<Boolean> {

	public ArgumentIdentificationHandler() {
		SimpleFeatureExtractor[] tokenExtractors = {
				new SpannedTextExtractor(),
				new TypePathExtractor(Token.class, "stem"),
				new TypePathExtractor(Token.class, "pos") };

		SimpleFeatureExtractor[] constituentExtractors = {
				new TypePathExtractor(TreebankNode.class, "nodeType"),
				// new TypePathExtractor(TreebankNode.class, "nodeTags"),
				new HeadWordExtractor(new CombinedExtractor(tokenExtractors)) };

		predicateTokenExtractor = new CombinedExtractor("PredicateToken",
				tokenExtractors);
		predicateNodeExtractor = new SubCategorizationExtractor("PredicateNode");

		constituentExtractor = new CombinedExtractor("Constituent",
				constituentExtractors);
		// leftSiblingExtractor = new CombinedExtractor("LeftSibling",
		// constituentExtractors);
		// rightSiblingExtractor = new CombinedExtractor("RightSibling",
		// constituentExtractors);
		// parentExtractor = new CombinedExtractor("Parent",
		// constituentExtractors);

		firstWordExtractor = new WindowExtractor("Constituent", Token.class,
				new CombinedExtractor(tokenExtractors),
				WindowFeature.ORIENTATION_MIDDLE, 0, 1);

		lastWordExtractor = new WindowExtractor("Constituent", Token.class,
				new CombinedExtractor(tokenExtractors),
				WindowFeature.ORIENTATION_MIDDLE_REVERSE, 0, 1);

		pathExtractor = new SyntacticPathExtractor("ConstituentPredicate",
				new TypePathExtractor(TreebankNode.class, "nodeType"));
		relPosExtractor = new RelativePositionExtractor("ConstituentPredicate");
	}

	public void initialize(UimaContext context)
	throws ResourceInitializationException {
		Boolean value = (Boolean) context.getConfigParameterValue("FilterMode");
		if (value != null && value)
			this.filterMode = true;
	}

	public void process(JCas jCas, InstanceConsumer<Boolean> consumer) throws CleartkException{
		/*
		 * Iterate over sentences in document
		 */
		List<Sentence> sentences = AnnotationRetrieval.getAnnotations(jCas, Sentence.class);

		for( Sentence sentence : sentences ) {
			processSentence(jCas, sentence, consumer);
		}
	}

	void processSentence(JCas jCas, Sentence sentence, InstanceConsumer<Boolean> consumer) throws CleartkException{

		/*
		 * Pre-compute sentence level data: sentenceConstituents: list of all
		 * constituents in sentence
		 */
		List<TreebankNode> sentenceConstituents = new ArrayList<TreebankNode>(
				200);
		collectConstituents(sentence.getConstituentParse(),
				sentenceConstituents);

		/*
		 * Compute constituent features for all constituents in sentence
		 */
		List<List<Feature>> sentenceConstituentFeatures = new ArrayList<List<Feature>>(
				sentenceConstituents.size());
		for (TreebankNode constituent : sentenceConstituents) {
			sentenceConstituentFeatures.add(extractConstituentFeatures(jCas, constituent, sentence));
		}

		/*
		 * Iterate over predicates in sentence
		 */
		List<Predicate> predicates = AnnotationRetrieval.getAnnotations(jCas,
				sentence, Predicate.class);
		for (Predicate predicate : predicates) {
			processPredicate(jCas, predicate, sentenceConstituents,
					sentenceConstituentFeatures, consumer);
		}
	}

	public void processPredicate(JCas jCas, Predicate predicate,
			List<TreebankNode> sentenceConstituents,
			List<List<Feature>> sentenceConstituentFeatures,
			InstanceConsumer<Boolean> consumer) throws CleartkException{
		/*
		 * Pre-compute predicate level data: predicateArguments: all semantic
		 * arguments of the predicate predicateNode: the constituent of the
		 * predicate predicateToken: the token of the predicate (first if there
		 * are multiple)
		 */
		Map<Integer, SemanticArgument> predicateArguments = new TreeMap<Integer, SemanticArgument>();
		int numberOfArgs = predicate.getArguments().size();
		for (int i = 0; i < numberOfArgs; i++) {
			Argument arg = predicate.getArguments(i);
			if (arg instanceof SemanticArgument
					&& arg.getAnnotation() instanceof TreebankNode) {
				predicateArguments.put(buildKey(arg.getAnnotation()),
						(SemanticArgument) arg);
			}
		}
		boolean modifiedPredicateArguments = false;


		TreebankNode predicateNode = AnnotationRetrieval.getMatchingAnnotation(
				jCas, predicate.getAnnotation(), TreebankNode.class);
		Token predicateToken = AnnotationRetrieval.getMatchingAnnotation(jCas,
				predicate.getAnnotation(), Token.class);


		/*
		 * Compute predicate features: predicateTokenFeatures
		 * predicateNodeFeatures
		 */
		List<Feature> predicateFeatures = new ArrayList<Feature>(12);
		predicateFeatures.addAll(predicateTokenExtractor.extract(jCas,
				predicateToken));
		predicateFeatures.addAll(predicateNodeExtractor.extract(jCas,
				predicateNode));


		/*
		 * Iterate over constituents in sentence
		 */
		for (int i = 0; i < sentenceConstituents.size(); i++) {
			TreebankNode constituent = sentenceConstituents.get(i);

			Instance<Boolean> instance = new Instance<Boolean>();

			/*
			 * Compute predicate-constituent features: relPosFeatures
			 * pathFeatures
			 */
			instance.addAll(relPosExtractor.extract(jCas, constituent,
					predicate.getAnnotation()));
			instance.addAll(pathExtractor.extract(jCas, constituent,
					predicateNode));

			/*
			 * Add constituent features
			 */
			instance.addAll(sentenceConstituentFeatures.get(i));

			/*
			 * Add predicate features
			 */
			instance.addAll(predicateFeatures);

			instance.setOutcome(predicateArguments.containsKey(buildKey(constituent)));


			Boolean outcome = consumer.consume(instance);

			if( outcome != null ) {
				if (this.filterMode) {
					boolean isIdentified = outcome;
					boolean isArgument = predicateArguments
					.containsKey(buildKey(constituent));

					if (!isIdentified && isArgument) {
						predicateArguments.get(buildKey(constituent))
						.removeFromIndexes();

						predicateArguments.remove(buildKey(constituent));
						modifiedPredicateArguments = true;
					} else if (isIdentified && !isArgument) {
						SemanticArgument arg = new SemanticArgument(jCas);
						arg.setAnnotation(constituent);
						arg.setBegin(constituent.getBegin());
						arg.setEnd(constituent.getEnd());
						arg.setLabel("NULL");
						arg.addToIndexes();

						predicateArguments.put(buildKey(constituent), arg);
						modifiedPredicateArguments = true;
					}
				} else {
					boolean isArgument = outcome;

					if (isArgument) {
						SemanticArgument arg = new SemanticArgument(jCas);
						arg.setAnnotation(constituent);
						arg.setBegin(constituent.getBegin());
						arg.setEnd(constituent.getEnd());
						arg.setLabel("?");
						arg.addToIndexes();

						predicateArguments.put(buildKey(constituent), arg);
					}
				}
			}

			/*
			 * Update predicates list of arguments in the CAS, if it was modified
			 */
			if (modifiedPredicateArguments) {
				FSArray args = new FSArray(jCas, predicateArguments.size());
				int index = 0;
				for (SemanticArgument arg : predicateArguments.values()) {
					args.set(index, arg);
					index += 1;
				}
				predicate.setArguments(args);
			}
		}
	}

	Integer buildKey(Annotation annotation) {
		return annotation.getAddress();
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

	protected List<Feature> extractConstituentFeatures(JCas jCas, TreebankNode constituent, Sentence sentence) {
		List<Feature> features = new ArrayList<Feature>(40);
		features.addAll(this.constituentExtractor
				.extract(jCas, constituent));
		features.addAll(this.firstWordExtractor.extract(jCas, constituent, sentence));
		features.addAll(this.lastWordExtractor.extract(jCas, constituent, sentence));

		// TreebankNode parent = constituent.getParent();
		// if (parent != null) {
		// features.addAll(this.parentExtractor.extract(jCas, parent));
		//
		// int constituentPosition = 0;
		// while (parent.getChildren(constituentPosition) !=
		// constituent)
		// constituentPosition += 1;
		//
		// if (constituentPosition > 0)
		// features.addAll(this.leftSiblingExtractor.extract(jCas,
		// parent.getChildren(constituentPosition - 1)));
		// if (constituentPosition < parent.getChildren().size() - 1)
		// features.addAll(this.rightSiblingExtractor.extract(
		// jCas, parent
		// .getChildren(constituentPosition + 1)));
		// }

		return features;
	}


	private SimpleFeatureExtractor predicateTokenExtractor;

	private SimpleFeatureExtractor predicateNodeExtractor;

	private SyntacticPathExtractor pathExtractor;

	private SimpleFeatureExtractor constituentExtractor;

	private WindowExtractor firstWordExtractor;

	private WindowExtractor lastWordExtractor;

	// private SimpleFeatureExtractor leftSiblingExtractor;
	//
	// private SimpleFeatureExtractor rightSiblingExtractor;
	//
	// private SimpleFeatureExtractor parentExtractor;

	private RelativePositionExtractor relPosExtractor;

	private boolean filterMode = false;


}
