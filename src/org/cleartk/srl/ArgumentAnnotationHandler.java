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

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.DistanceExtractor;
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
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philipp Wetzler
 */

public class ArgumentAnnotationHandler implements AnnotationHandler<String> {
	
	public ArgumentAnnotationHandler() {
		SimpleFeatureExtractor[] tokenExtractors = {
				new SpannedTextExtractor(),
				new TypePathExtractor(Token.class, "stem"),
				new TypePathExtractor(Token.class, "pos") };

		SimpleFeatureExtractor[] constituentExtractors = {
				new TypePathExtractor(TreebankNode.class, "nodeType"),
				// new TypePathExtractor(TreebankNode.class, "nodeTags"),
				new HeadWordExtractor(new CombinedExtractor(tokenExtractors), true, true) };

		predicateTokenExtractor = new CombinedExtractor("PredicateToken",
				tokenExtractors);
		predicateNodeExtractor = new SubCategorizationExtractor("PredicateNode");

		pathExtractor = new SyntacticPathExtractor("ConstituentPredicate",
				new TypePathExtractor(TreebankNode.class, "nodeType"));
		partialPathExtractor = new SyntacticPathExtractor("ConstituentPredicate",
				new TypePathExtractor(TreebankNode.class, "nodeType"),
				true);
		relPosExtractor = new RelativePositionExtractor("ConstituentPredicate");
		distanceExtractor = new DistanceExtractor("ConstituentPredicate", Token.class);

		constituentExtractor = new CombinedExtractor("Constituent",
				constituentExtractors);
		leftSiblingExtractor = new CombinedExtractor("LeftSibling",
				constituentExtractors);
		rightSiblingExtractor = new CombinedExtractor("RightSibling",
				constituentExtractors);
		parentExtractor = new CombinedExtractor("Parent", constituentExtractors);
		firstWordExtractor = new WindowExtractor("Constituent", Token.class,
				new CombinedExtractor(tokenExtractors),
				WindowFeature.ORIENTATION_MIDDLE, 0, 1);
		lastWordExtractor = new WindowExtractor("Constituent", Token.class,
				new CombinedExtractor(tokenExtractors),
				WindowFeature.ORIENTATION_MIDDLE_REVERSE, 0, 1);
	}

	public void initialize(UimaContext context) throws ResourceInitializationException {
	}

	public void process(JCas jCas, InstanceConsumer<String> consumer) throws CleartkException{
		List<Sentence> sentences = AnnotationRetrieval.getAnnotations(jCas, Sentence.class);
	
		for( Sentence sentence : sentences ) {
			processSentence(jCas, sentence, consumer);
		}		
	}

	public void processSentence(JCas jCas, Sentence sentence, InstanceConsumer<String> consumer) throws CleartkException{

		List<TreebankNode> sentenceConstituents = new ArrayList<TreebankNode>(
				80);
		collectConstituents(sentence.getConstituentParse(),
				sentenceConstituents);

		List<List<Feature>> sentenceConstituentFeatures = new ArrayList<List<Feature>>(
				sentenceConstituents.size());
		for (TreebankNode constituent : sentenceConstituents)
			sentenceConstituentFeatures.add(extractConstituentFeatures(jCas,
					constituent, sentence));

		List<Predicate> predicates = AnnotationRetrieval.getAnnotations(jCas,
				sentence, Predicate.class);
		for (Predicate predicate : predicates) {
			processPredicate(jCas, sentence, predicate, sentenceConstituents,
					sentenceConstituentFeatures, consumer);
		}

	}

	void processPredicate(JCas jCas, Sentence sentence, Predicate predicate,
			List<TreebankNode> constituents,
			List<List<Feature>> constituentFeatures, InstanceConsumer<String> consumer) throws CleartkException{
		TreebankNode predicateNode = AnnotationRetrieval.getMatchingAnnotation(
				jCas, predicate.getAnnotation(), TreebankNode.class);
		Token predicateToken = AnnotationRetrieval.getMatchingAnnotation(jCas,
				predicate.getAnnotation(), Token.class);

		List<Feature> predicateFeatures = new ArrayList<Feature>(20);
		predicateFeatures.addAll(predicateTokenExtractor.extract(
				jCas, predicateToken));
		predicateFeatures.addAll(predicateNodeExtractor.extract(
				jCas, predicateNode));

		for( int i=0; i<constituents.size(); i++ ) {
			TreebankNode constituent = constituents.get(i);
			
			Instance<String> instance = new Instance<String>();

			instance.addAll(predicateFeatures);
			instance.addAll(constituentFeatures.get(i));
			instance.addAll(relPosExtractor.extract(jCas,
					constituent, predicate.getAnnotation()));
			instance.addAll(pathExtractor.extract(jCas,
					constituent, predicateNode));
			instance.addAll(partialPathExtractor.extract(jCas, constituent, predicateNode));
			instance.addAll(distanceExtractor.extract(jCas, constituent, predicateNode));
			

			instance.setOutcome("NULL");
			
			for (Argument arg : UIMAUtil.toList(predicate
					.getArguments(), Argument.class)) {
				if( !(arg instanceof SemanticArgument) )
					continue;
				
				SemanticArgument sarg = (SemanticArgument) arg;
				
				if (sarg.getAnnotation() == constituent
						&& !sarg.getLabel().equals("rel")) {
					if (sarg.getFeature() != null) {
						instance.setOutcome(sarg.getLabel() + "-" + sarg.getFeature());
					} else {
						instance.setOutcome(sarg.getLabel());
					}
					break;
				}
			}

			String outcome = consumer.consume(instance);
			

			if( outcome != null ) {
				if (!outcome.equals("NULL")) {
					SemanticArgument arg = new SemanticArgument(jCas);
					arg.setAnnotation(constituent);
					arg.setBegin(constituent.getBegin());
					arg.setEnd(constituent.getEnd());

					String[] parts = outcome.split("-", 1);
					arg.setLabel(parts[0]);
					if (parts.length > 1)
						arg.setFeature(parts[1]);

					arg.addToIndexes();

					List<Argument> args;
					if (predicate.getArguments() != null)
						args = UIMAUtil.toList(predicate.getArguments(),
								Argument.class);
					else
						args = new ArrayList<Argument>(1);
					args.add(arg);
					predicate.setArguments(UIMAUtil.toFSArray(jCas, args));
				}
			}
		}

	}

	List<Feature> extractConstituentFeatures(JCas jCas,
			TreebankNode constituent, Sentence sentence) {
		List<Feature> features = new ArrayList<Feature>(20);
		features.addAll(constituentExtractor.extract(jCas, constituent));
		features
				.addAll(firstWordExtractor.extract(jCas, constituent, sentence));
		features.addAll(lastWordExtractor.extract(jCas, constituent, sentence));

		TreebankNode parent = constituent.getParent();
		if (parent != null) {
			features.addAll(parentExtractor.extract(jCas, parent));

			int constituentPosition = 0;
			while (parent.getChildren(constituentPosition) != constituent)
				constituentPosition += 1;

			if (constituentPosition > 0)
				features.addAll(leftSiblingExtractor.extract(jCas, parent
						.getChildren(constituentPosition - 1)));

			if (constituentPosition < parent.getChildren().size() - 1)
				features.addAll(rightSiblingExtractor.extract(jCas, parent
						.getChildren(constituentPosition + 1)));
		}

		return features;
	}

	void collectConstituents(TreebankNode top, List<TreebankNode> constituents) {
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


	private SimpleFeatureExtractor predicateTokenExtractor;

	private SimpleFeatureExtractor predicateNodeExtractor;

	private SyntacticPathExtractor pathExtractor;
	
	private SyntacticPathExtractor partialPathExtractor;

	private RelativePositionExtractor relPosExtractor;
	
	private DistanceExtractor distanceExtractor;

	private SimpleFeatureExtractor constituentExtractor;

	private WindowExtractor firstWordExtractor;

	private WindowExtractor lastWordExtractor;

	private SimpleFeatureExtractor leftSiblingExtractor;

	private SimpleFeatureExtractor rightSiblingExtractor;

	private SimpleFeatureExtractor parentExtractor;

}
