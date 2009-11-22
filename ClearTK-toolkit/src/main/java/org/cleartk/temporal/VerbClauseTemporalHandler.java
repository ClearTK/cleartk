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
package org.cleartk.temporal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.CleartkComponents;
import org.cleartk.CleartkException;
import org.cleartk.classifier.AnnotationHandler;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.InstanceConsumer;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.svmlight.DefaultOVASVMlightDataWriterFactory;
import org.cleartk.corpus.timeml.type.Anchor;
import org.cleartk.corpus.timeml.type.Event;
import org.cleartk.corpus.timeml.type.TemporalLink;
import org.cleartk.syntax.feature.SyntacticPathExtractor;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.ViewURIUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 *
 * @author Steven Bethard
 */
public class VerbClauseTemporalHandler implements AnnotationHandler<String> {
	
	private static final Map<String, String[]> headMap = new HashMap<String, String[]>();
	static {
		headMap.put("S",    "VP S SBAR ADJP".split(" "));
		headMap.put("SBAR", "VP S SBAR ADJP".split(" "));
		headMap.put("VP", ("VP VB VBZ VBP VBG VBN VBD JJ JJR JJS " +
				           "NNS NN PRP NNPS NNP ADJP NP S SBAR").split(" "));
		headMap.put("ADJP", "ADJP VB VBZ VBP VBG VBN VBD JJ JJR JJS".split(" "));
		headMap.put("NP", "NP NNS NN PRP NNPS NNP QP ADJP".split(" "));
		headMap.put("QP", "NP NNS NN PRP NNPS NNP QP ADJP".split(" "));
	}
	
	private static final Set<String> stopWords = new HashSet<String>(Arrays.asList(
			"be been is 's am are was were has had have".split(" ")));
	
	private List<SimpleFeatureExtractor> tokenFeatureExtractors;
	private SyntacticPathExtractor pathExtractor;
	private int eventID;

	
	public static AnalysisEngineDescription getWriterDescription(String outputDir)
	throws ResourceInitializationException {
		return CleartkComponents.createDataWriterAnnotator(
				VerbClauseTemporalHandler.class,
				DefaultOVASVMlightDataWriterFactory.class,
				outputDir, null);
	}

	public static AnalysisEngineDescription getAnnotatorDescription()
	throws ResourceInitializationException {
		return CleartkComponents.createClassifierAnnotator(
				VerbClauseTemporalHandler.class,
				"resources/models/verb-clause-temporal-model.jar");
	}

	public VerbClauseTemporalHandler() {
		this.eventID = 1;
		this.tokenFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
		this.tokenFeatureExtractors.add(new SpannedTextExtractor());
		this.tokenFeatureExtractors.add(new TypePathExtractor(Token.class, "pos"));
		this.tokenFeatureExtractors.add(new TypePathExtractor(Token.class, "stem"));
		this.pathExtractor = new SyntacticPathExtractor(
				new TypePathExtractor(TreebankNode.class, "nodeType"));
	}

	public void process(JCas jCas, InstanceConsumer<String> consumer)
			throws AnalysisEngineProcessException, CleartkException {
		int docEnd = jCas.getDocumentText().length();

		// collect TLINKs if necessary
		Map<String, TemporalLink> tlinks = null;
		if (consumer.expectsOutcomes()) {
			tlinks = this.getTemporalLinks(jCas);
		}

		// look for verb-clause pairs in each sentence in the document
		for (Sentence sentence: AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
			TopTreebankNode tree = AnnotationRetrieval.getContainingAnnotation(jCas, sentence, TopTreebankNode.class, false);
			if (tree == null) {
				throw new AnalysisEngineProcessException(new Exception(String.format(
						"%s: missing syntactic parses", ViewURIUtil.getURI(jCas))));
			}

			// iterate over all verb-clause pairs
			List<TreebankNodeLink> links = new ArrayList<TreebankNodeLink>();
			this.collectVerbClausePairs(tree, links);
			for (TreebankNodeLink link: links) {
				
				Token sourceToken = AnnotationRetrieval.getAnnotations(
						jCas, link.source, Token.class).get(0);
				Token targetToken = AnnotationRetrieval.getAnnotations(
						jCas, link.target, Token.class).get(0);
				
				// create an instance and populate it with features
				Instance<String> instance = new Instance<String>();
				for (SimpleFeatureExtractor extractor: this.tokenFeatureExtractors) {
					instance.addAll(extractor.extract(jCas, sourceToken));
					instance.addAll(extractor.extract(jCas, targetToken));
				}
				instance.addAll(this.pathExtractor.extract(
						jCas, link.source, link.target));
				
				// find source and target anchors if they're available
				Anchor source = AnnotationRetrieval.getContainingAnnotation(
						jCas, link.source, Anchor.class);
				Anchor target = AnnotationRetrieval.getContainingAnnotation(
						jCas, link.target, Anchor.class);

				// if the consumer expects outcomes, get the relation
				// type from a TLINK annotation
				if (consumer.expectsOutcomes()) {
					if (source != null && target != null) {
						String key = String.format("%s:%s", source.getId(), target.getId());
						TemporalLink tlink = tlinks.remove(key);
						if (tlink != null) {
							instance.setOutcome(tlink.getRelationType());
							consumer.consume(instance);
						}
					}
				}
				
				// if the consumer doesn't expect outcomes, send all instances
				// to the consumer, and create a new TLINK with the result
				else {
					String relationType = consumer.consume(instance);
					source = this.getOrCreateEvent(jCas, source, link.source);
					target = this.getOrCreateEvent(jCas, target, link.target);
					TemporalLink tlink = new TemporalLink(jCas, docEnd, docEnd);
					tlink.setSource(source);
					tlink.setTarget(target);
					tlink.setRelationType(relationType);
					tlink.setEventID(source.getId());
					tlink.setRelatedToEvent(target.getId());
					tlink.addToIndexes();
				}
			}			
		}
//		if (!tlinks.isEmpty()) {
//			System.err.println();
//			System.err.println(DocumentUtil.getIdentifier(jCas));
//			System.err.println();
//			System.err.println("TLINKs not used:");
//			System.err.println(tlinks.keySet());
//			for (String key: tlinks.keySet()) {
//				TemporalLink tlink = tlinks.get(key); 
//				Event source = (Event)tlink.getSource();
//				Event target = (Event)tlink.getTarget();
//				System.err.format("%s  %s (%s) -> %s (%s) \n", key,
//						source.getCoveredText(), source.getEventInstanceID(),
//						target.getCoveredText(), target.getEventInstanceID());
//				Sentence sentence = AnnotationRetrieval.getContainingAnnotation(jCas, source, Sentence.class);
//				System.err.println(sentence.getCoveredText());
//				System.err.println(sentence.getConstituentParse().getTreebankParse());
//			}
//			throw new AnalysisEngineProcessException();
//		}
	}
	
	private Event getOrCreateEvent(JCas jCas, Anchor anchor, TreebankNode node) {
		if (anchor != null && anchor instanceof Event) {
			return (Event)anchor;
		}
		Event event = new Event(jCas, node.getBegin(), node.getEnd());
		event.setId("e" + this.eventID);
		this.eventID++;
		event.addToIndexes();
		return event;
	}
	
	private Map<String, TemporalLink> getTemporalLinks(JCas jCas) {
		Map<String, TemporalLink> tlinks = new HashMap<String, TemporalLink>();
		for (TemporalLink tlink: AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class)) {
			String sourceID = tlink.getSource().getId();
			String targetID = tlink.getTarget().getId();
			String key = String.format("%s:%s", sourceID, targetID);
			tlinks.put(key, tlink);
		}
		return tlinks;
	}
	
	private void collectVerbClausePairs(TreebankNode node, List<TreebankNodeLink> links) {
		if (this.isVerbPhrase(node)) {
			List<TreebankNode> sources = new ArrayList<TreebankNode>();
			List<TreebankNode> targets = new ArrayList<TreebankNode>();
			this.collectHeads(node, sources);
			
			// look for clauses in descendants
			for (int i = 0; i < node.getChildren().size(); i++) {
				TreebankNode child = node.getChildren(i);
				if (this.isClause(child)) {
					
					// pair the verb phrase heads with the clause heads
					targets.clear();
					this.collectHeads(child, targets);
					for (TreebankNode source: sources) {
						for (TreebankNode target: targets) {
							
							// skip pairs where the head of the VP is inside the clause
							if (!this.contains(child, source)) {
								links.add(new TreebankNodeLink(source, target));
							}
						}
					}
				}
			}
		}
		// look for verb phrases in descendants
		for (int i = 0; i < node.getChildren().size(); i++) {
			TreebankNode child = node.getChildren(i);
			this.collectVerbClausePairs(child, links);
		}
	}
	
	private void collectHeads(TreebankNode node, List<TreebankNode> heads) {
		if (node.getLeaf()) {
			heads.add(node);
		}
		String[] headTypes = VerbClauseTemporalHandler.headMap.get(node.getNodeType());
		if (headTypes != null) {
			for (String headType: headTypes) {
				boolean foundChildWithHeadType = false;
				for (int i = 0; i < node.getChildren().size(); i++) {
					TreebankNode child = node.getChildren(i);
					if (child.getNodeType().equals(headType)) {
						String text = child.getCoveredText();
						if (!VerbClauseTemporalHandler.stopWords.contains(text)) {
							this.collectHeads(child, heads);
							foundChildWithHeadType = true;
						}
					}
				}
				if (foundChildWithHeadType) {
					break;
				}
			}
		}
	}
	
	private boolean contains(TreebankNode node, TreebankNode descendant) {
		if (node == descendant) {
			return true;
		}
		for (int i = 0; i < node.getChildren().size(); i ++) {
			boolean result = this.contains(node.getChildren(i), descendant);
			if (result) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isVerbPhrase(TreebankNode node) {
		return node.getNodeType().startsWith("VP");
	}
	
	private boolean isClause(TreebankNode node) {
		return node.getNodeType().startsWith("S");
	}
	
	private class TreebankNodeLink {
		public TreebankNode source;
		public TreebankNode target;
		public TreebankNodeLink(TreebankNode source, TreebankNode target) {
			this.source = source;
			this.target = target;
		}
	}

}
