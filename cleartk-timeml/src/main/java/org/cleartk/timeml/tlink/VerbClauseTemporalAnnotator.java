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
package org.cleartk.timeml.tlink;

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
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkComponents;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.simple.BagExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.opennlp.DefaultMaxentDataWriterFactory;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.type.Anchor;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.timeml.util.PrecedingTokenTextBagExtractor;
import org.cleartk.timeml.util.TargetPathExtractor;
import org.cleartk.timeml.util.TokenPOSExtractor;
import org.cleartk.timeml.util.TokenStemExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.ConfigurationParameterFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * 
 * @author Steven Bethard
 */
@TypeCapability(outputs = {
        "org.cleartk.timeml.type.TemporalLink",
        "org.cleartk.timeml.type.Event"})
public class VerbClauseTemporalAnnotator extends CleartkAnnotator<String> {

  public static final String MODEL_BASE = "/models/timeml/tlink/verb-clause";

  public static final String MODEL_DIR = "src/main/resources" + MODEL_BASE;

  public static final String MODEL_RESOURCE = MODEL_BASE + "/model.jar";

  private static final Map<String, String[]> headMap = new HashMap<String, String[]>();
  static {
    headMap.put("S", "VP S SBAR ADJP".split(" "));
    headMap.put("SBAR", "VP S SBAR ADJP".split(" "));
    headMap.put("VP", ("VP VB VBZ VBP VBG VBN VBD JJ JJR JJS "
            + "NNS NN PRP NNPS NNP ADJP NP S SBAR").split(" "));
    headMap.put("ADJP", "ADJP VB VBZ VBP VBG VBN VBD JJ JJR JJS".split(" "));
    headMap.put("NP", "NP NNS NN PRP NNPS NNP QP ADJP".split(" "));
    headMap.put("QP", "NP NNS NN PRP NNPS NNP QP ADJP".split(" "));
  }

  private static final Set<String> stopWords = new HashSet<String>(
          Arrays.asList("be been is 's am are was were has had have".split(" ")));

  private List<SimpleFeatureExtractor> sourceFeatureExtractors;

  private List<SimpleFeatureExtractor> targetFeatureExtractors;

  private List<SimpleFeatureExtractor> betweenAnchorsFeatureExtractors;

  private TargetPathExtractor pathExtractor;

  private int eventID;
  
  @ConfigurationParameter(defaultValue = "false", description = "Create events for all verbs in "
          + "verb-clause relations (using existing events if present, but adding new ones "
          + "wherever they are not present).")
  private boolean createEvents;

  public static final String PARAM_CREATE_EVENTS = ConfigurationParameterFactory
          .createConfigurationParameterName(VerbClauseTemporalAnnotator.class, "createEvents");

  public static AnalysisEngineDescription getWriterDescription()
          throws ResourceInitializationException {
    return CleartkComponents.createCleartkAnnotator(VerbClauseTemporalAnnotator.class,
            TimeMLComponents.TYPE_SYSTEM_DESCRIPTION, DefaultMaxentDataWriterFactory.class,
            VerbClauseTemporalAnnotator.MODEL_DIR, (List<Class<?>>) null);
  }

  public static AnalysisEngineDescription getAnnotatorDescription()
          throws ResourceInitializationException {
    return CleartkComponents.createCleartkAnnotator(VerbClauseTemporalAnnotator.class,
            TimeMLComponents.TYPE_SYSTEM_DESCRIPTION, VerbClauseTemporalAnnotator.class
                    .getResource(VerbClauseTemporalAnnotator.MODEL_RESOURCE).getFile(),
            (List<Class<?>>) null);
  }

  public static AnalysisEngineDescription getEventCreatingAnnotatorDescription()
          throws ResourceInitializationException {
    return CleartkComponents.createCleartkAnnotator(VerbClauseTemporalAnnotator.class,
            TimeMLComponents.TYPE_SYSTEM_DESCRIPTION, VerbClauseTemporalAnnotator.class
                    .getResource(VerbClauseTemporalAnnotator.MODEL_RESOURCE).getFile(),
            (List<Class<?>>) null, VerbClauseTemporalAnnotator.PARAM_CREATE_EVENTS, true);
  }

  public VerbClauseTemporalAnnotator() {
    this.eventID = 1;

    PrecedingTokenTextBagExtractor precedingAuxiliaries;
    precedingAuxiliaries = new PrecedingTokenTextBagExtractor(3, "MD", "TO", "IN", "VB", "RB");

    this.sourceFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
    this.sourceFeatureExtractors.add(new NamingExtractor("Source", new SpannedTextExtractor()));
    this.sourceFeatureExtractors.add(new NamingExtractor("Source", new TokenPOSExtractor()));
    this.sourceFeatureExtractors.add(new NamingExtractor("Source", new TokenStemExtractor()));
    this.sourceFeatureExtractors.add(new NamingExtractor("Source", precedingAuxiliaries));

    this.targetFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
    this.targetFeatureExtractors.add(new NamingExtractor("Target", new SpannedTextExtractor()));
    this.targetFeatureExtractors.add(new NamingExtractor("Target", new TokenPOSExtractor()));
    this.targetFeatureExtractors.add(new NamingExtractor("Target", new TokenStemExtractor()));
    this.targetFeatureExtractors.add(new NamingExtractor("Target", precedingAuxiliaries));

    this.betweenAnchorsFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
    this.betweenAnchorsFeatureExtractors.add(new NamingExtractor("WordsBetween", new BagExtractor(
            Token.class, new SpannedTextExtractor())));
    this.pathExtractor = new TargetPathExtractor();
  }

  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      this.processSimple(jCas);
    } catch (CleartkException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  public void processSimple(JCas jCas) throws AnalysisEngineProcessException, CleartkException {
    int docEnd = jCas.getDocumentText().length();

    // collect TLINKs if necessary
    Map<String, TemporalLink> tlinks = null;
    if (this.isTraining()) {
      tlinks = this.getTemporalLinks(jCas);
    }

    // look for verb-clause pairs in each sentence in the document
    for (Sentence sentence : AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
      TopTreebankNode tree = AnnotationRetrieval.getContainingAnnotation(jCas, sentence,
              TopTreebankNode.class);
      if (tree == null) {
        String fmt = "missing syntactic parse for sentence: %s";
        String msg = String.format(fmt, sentence.getCoveredText());
        this.getContext().getLogger().log(Level.WARNING, msg);
        continue;
      }

      // iterate over all verb-clause pairs
      List<TreebankNodeLink> links = new ArrayList<TreebankNodeLink>();
      this.collectVerbClausePairs(tree, links);
      for (TreebankNodeLink link : links) {

        Token sourceToken = AnnotationRetrieval.getAnnotations(jCas, link.source, Token.class).get(
                0);
        Token targetToken = AnnotationRetrieval.getAnnotations(jCas, link.target, Token.class).get(
                0);
        int firstEnd = Math.min(sourceToken.getEnd(), targetToken.getEnd());
        int lastBegin = Math.max(sourceToken.getBegin(), targetToken.getBegin());

        // create an instance and populate it with features
        Instance<String> instance = new Instance<String>();
        for (SimpleFeatureExtractor extractor : this.sourceFeatureExtractors) {
          instance.addAll(extractor.extract(jCas, sourceToken));
        }
        for (SimpleFeatureExtractor extractor : this.targetFeatureExtractors) {
          instance.addAll(extractor.extract(jCas, targetToken));
        }
        Annotation windowAnnotation = new Annotation(jCas, firstEnd, lastBegin);
        for (SimpleFeatureExtractor extractor : this.betweenAnchorsFeatureExtractors) {
          instance.addAll(extractor.extract(jCas, windowAnnotation));
        }
        instance.addAll(this.pathExtractor.extract(jCas, link.source, link.target));

        // find source and target anchors if they're available
        Anchor source = AnnotationRetrieval
                .getContainingAnnotation(jCas, link.source, Anchor.class);
        Anchor target = AnnotationRetrieval
                .getContainingAnnotation(jCas, link.target, Anchor.class);

        // if we're building training data, get the relation type from a
        // TLINK
        if (this.isTraining()) {
          if (source != null && target != null) {
            String key = String.format("%s:%s", source.getId(), target.getId());
            TemporalLink tlink = tlinks.remove(key);
            if (tlink != null) {
              instance.setOutcome(tlink.getRelationType());
              this.dataWriter.write(instance);
            }
          }
        }

        // if we're classifying create new TLINKs from the
        // classification outcomes
        else {
          source = this.getOrCreateEvent(jCas, source, link.source);
          target = this.getOrCreateEvent(jCas, target, link.target);
          // only create TLINKs for events that exist (or were created, if requested)
          if (source != null && target != null) {
            String relationType = this.classifier.classify(instance.getFeatures());
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
    }
  }

  private Event getOrCreateEvent(JCas jCas, Anchor anchor, TreebankNode node) {
    if (anchor != null && anchor instanceof Event) {
      return (Event) anchor;
    } else if (this.createEvents) {
      Event event = new Event(jCas, node.getBegin(), node.getEnd());
      event.setId("e" + this.eventID);
      this.eventID++;
      event.addToIndexes();
      return event;
    } else {
      return null;
    }
  }

  private Map<String, TemporalLink> getTemporalLinks(JCas jCas) {
    Map<String, TemporalLink> tlinks = new HashMap<String, TemporalLink>();
    for (TemporalLink tlink : AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class)) {
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
          for (TreebankNode source : sources) {
            for (TreebankNode target : targets) {

              // skip pairs where the head of the VP is inside the
              // clause
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
    String[] headTypes = VerbClauseTemporalAnnotator.headMap.get(node.getNodeType());
    if (headTypes != null) {
      for (String headType : headTypes) {
        boolean foundChildWithHeadType = false;
        for (int i = 0; i < node.getChildren().size(); i++) {
          TreebankNode child = node.getChildren(i);
          if (child.getNodeType().equals(headType)) {
            String text = child.getCoveredText();
            if (!VerbClauseTemporalAnnotator.stopWords.contains(text)) {
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
    for (int i = 0; i < node.getChildren().size(); i++) {
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
