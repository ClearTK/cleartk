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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.extractor.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.HeadWordExtractor;
import org.cleartk.classifier.feature.extractor.RelativePositionExtractor;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.SubCategorizationExtractor;
import org.cleartk.classifier.feature.extractor.SyntacticPathExtractor;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
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

public class ArgumentClassificationHandler implements AnnotationHandler<String> {

	public ArgumentClassificationHandler() {
		SimpleFeatureExtractor[] tokenExtractors = {
				new SpannedTextExtractor(),
				new TypePathExtractor(Token.class, "stem"), 
				new TypePathExtractor(Token.class, "pos") 
		};

		SimpleFeatureExtractor[] constituentExtractors = {
				new TypePathExtractor(TreebankNode.class, "nodeType"),
//				new TypePathExtractor(TreebankNode.class, "nodeTags"),
				new HeadWordExtractor(new CombinedExtractor(tokenExtractors))
		};

		predicateTokenExtractor = new CombinedExtractor("PredicateToken", tokenExtractors);
		predicateNodeExtractor = new SubCategorizationExtractor("PredicateNode");

		constituentExtractor = new CombinedExtractor("Constituent", constituentExtractors);
		leftSiblingExtractor = new CombinedExtractor("LeftSibling", constituentExtractors);
		rightSiblingExtractor = new CombinedExtractor("RightSibling", constituentExtractors);
		parentExtractor = new CombinedExtractor("Parent", constituentExtractors);

		pathExtractor = new SyntacticPathExtractor("ConstituentPredicate", new TypePathExtractor(TreebankNode.class, "nodeType"));
		relPosExtractor = new RelativePositionExtractor("ConstituentPredicate");
	}
	
	public void initialize(UimaContext context)
	throws ResourceInitializationException {
	}

	public void process(JCas jCas, InstanceConsumer<String> consumer) throws CleartkException{
		List<Sentence> sentences = AnnotationRetrieval.getAnnotations(jCas, Sentence.class);

		for( Sentence sentence : sentences ) {
			List<TreebankNode> sentenceConstituents = new ArrayList<TreebankNode>(80);
			Iterator<?> constituentIterator = jCas.getJFSIndexRepository().getAnnotationIndex(TreebankNode.type).subiterator(sentence);
			while( constituentIterator.hasNext() )
				sentenceConstituents.add((TreebankNode) constituentIterator.next());
			
			Map<Integer,List<Feature>> constituentFeatures = new HashMap<Integer,List<Feature>>(140);
			for( TreebankNode constituent : sentenceConstituents )
				constituentFeatures.put(buildKey(constituent), extractConstituentFeatures(jCas, constituent));

			
			Iterator<?> predicateIterator = jCas.getJFSIndexRepository().getAnnotationIndex(Predicate.type).subiterator(sentence);
			while( predicateIterator.hasNext() ) {
				Predicate predicate = (Predicate) predicateIterator.next();
				
				TreebankNode predicateNode = AnnotationRetrieval.getMatchingAnnotation(jCas, predicate.getAnnotation(), TreebankNode.class);
				Token predicateToken = AnnotationRetrieval.getMatchingAnnotation(jCas, predicate.getAnnotation(), Token.class);

				List<Feature> predicateFeatures = extractPredicateFeatures(jCas, predicate, predicateNode, predicateToken);

				List<Argument> predicateArguments = UIMAUtil.toList(predicate.getArguments(), Argument.class);
				
				ListIterator<Argument> iterator = predicateArguments.listIterator();
				while( iterator.hasNext() ) {
					SemanticArgument sarg;
					try {
						sarg = (SemanticArgument) iterator.next();
					} catch( ClassCastException e) {
						continue;
					}

					if (sarg.getAnnotation() == null) {
						String message = "annotation property of SemanticArgument must be set";
						throw new IllegalArgumentException(message);
					}
					TreebankNode constituent = AnnotationRetrieval.getMatchingAnnotation(jCas, sarg.getAnnotation(), TreebankNode.class);
				
					Instance<String> instance = new Instance<String>();
					instance.addAll(predicateFeatures);
					instance.addAll(constituentFeatures.get(buildKey(constituent)));
					
					instance.addAll(relPosExtractor.extract(jCas, constituent, predicate.getAnnotation()));
					instance.addAll(pathExtractor.extract(jCas, constituent, predicateNode));

					if (sarg.getFeature() != null ) {
						instance.setOutcome(sarg.getLabel() + "-" + sarg.getFeature());
					} else {
						instance.setOutcome(sarg.getLabel());
					}

					String outcome = consumer.consume(instance);
					
					if( outcome != null ) {
						if( outcome.equals("NULL") ) {
							iterator.remove();
						} else {
							String[] parts = outcome.split("-", 1);
							sarg.setLabel(parts[0]);
							if( parts.length > 1 )
								sarg.setFeature(parts[1]);
							
							sarg.removeFromIndexes();
							sarg.addToIndexes();
						}
					}
					
				}
				
				predicate.setArguments(UIMAUtil.toFSArray(jCas, predicateArguments));
			}
		}
		
	}

	private Integer buildKey(TreebankNode constituent) {
		return constituent.getAddress();
	}
	
	private List<Feature> extractConstituentFeatures(JCas jCas, TreebankNode constituent) {
		List<Feature> features = new ArrayList<Feature>(30);
		features.addAll(this.constituentExtractor.extract(jCas, constituent));

		TreebankNode parent = constituent.getParent();
		if( parent != null ) {
			features.addAll(this.parentExtractor.extract(jCas, parent));
			List<TreebankNode> children = UIMAUtil.toList(parent.getChildren(), TreebankNode.class);
			int index = children.indexOf(constituent);
			if( index > 0 )
				features.addAll(this.leftSiblingExtractor.extract(jCas, children.get(index-1)));						
			if( index < children.size() - 1 )
				features.addAll(this.rightSiblingExtractor.extract(jCas, children.get(index+1)));
		}
		
		return features;	
	}
	
	List<Feature> extractPredicateFeatures(JCas jCas, Predicate predicate, TreebankNode predicateNode, Token predicateToken) {
		List<Feature> features = new ArrayList<Feature>(8);
		features.addAll(predicateTokenExtractor.extract(jCas, predicateToken));
		features.addAll(predicateNodeExtractor.extract(jCas, predicateNode));
		return features;
	}

	private SimpleFeatureExtractor predicateTokenExtractor;
	private SimpleFeatureExtractor predicateNodeExtractor;
	private SyntacticPathExtractor pathExtractor;
	private SimpleFeatureExtractor constituentExtractor;
	private SimpleFeatureExtractor leftSiblingExtractor;
	private SimpleFeatureExtractor rightSiblingExtractor;
	private SimpleFeatureExtractor parentExtractor;
	private RelativePositionExtractor relPosExtractor;

}
