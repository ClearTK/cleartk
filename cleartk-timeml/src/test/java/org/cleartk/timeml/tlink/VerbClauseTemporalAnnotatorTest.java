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

import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.ClassifierFactory;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkComponents;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.util.InstanceCollector;
import org.cleartk.syntax.TreebankTestsUtil;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.timeml.TimeMLTestBase;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 */
public class VerbClauseTemporalAnnotatorTest extends TimeMLTestBase {

  public static class AfterNewClassifier implements Classifier<String>, ClassifierFactory<String> {
    public AfterNewClassifier() {
    }

    public String classify(List<Feature> features) throws CleartkException {
      return "AFTER-NEW";
    }

    public List<ScoredOutcome<String>> score(List<Feature> features, int maxResults)
            throws CleartkException {
      return null;
    }

    public Classifier<String> createClassifier() throws IOException, CleartkException {
      return new AfterNewClassifier();
    }
  }

  @Test
  public void test() throws UIMAException, CleartkException {
    AnalysisEngineDescription desc = CleartkComponents.createCleartkAnnotator(
            VerbClauseTemporalAnnotator.class, typeSystemDescription,
            InstanceCollector.StringFactory.class, ".", (List<Class<?>>) null);
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(desc);

    tokenBuilder.buildTokens(jCas, "He said she bought milk.", "He said she bought milk .",
            "PRP VBD PRP VBD NN .", "he say she buy milk .");
    List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, Token.class);

    // create the Event and TemporalLink annotations
    Event source = new Event(jCas, tokens.get(1).getBegin(), tokens.get(1).getEnd());
    Event target = new Event(jCas, tokens.get(3).getBegin(), tokens.get(3).getEnd());
    TemporalLink tlink = new TemporalLink(jCas);
    tlink.setSource(source);
    tlink.setTarget(target);
    tlink.setRelationType("AFTER");
    Annotation[] timemlAnnotations = new Annotation[] { source, target, tlink };
    for (Annotation annotation : timemlAnnotations) {
      annotation.addToIndexes();
    }

    // create the TreebankNode annotations
    TreebankNode root = TreebankTestsUtil.newNode(jCas, "S", TreebankTestsUtil.newNode(jCas, "NP",
            this.newNode(jCas, tokens.get(0))), TreebankTestsUtil.newNode(jCas, "VP", this.newNode(
            jCas, tokens.get(1)), TreebankTestsUtil.newNode(jCas, "SBAR", TreebankTestsUtil
            .newNode(jCas, "NP", this.newNode(jCas, tokens.get(2))), TreebankTestsUtil.newNode(
            jCas, "VP", this.newNode(jCas, tokens.get(3)),
            TreebankTestsUtil.newNode(jCas, "NP", this.newNode(jCas, tokens.get(4)))))));

    Sentence sentence = AnnotationRetrieval.getAnnotations(jCas, Sentence.class).get(0);

    // set the Sentence's constitutentParse feature
    TopTreebankNode tree = new TopTreebankNode(jCas, sentence.getBegin(), sentence.getEnd());
    tree.setNodeType("TOP");
    tree.setChildren(new FSArray(jCas, 1));
    tree.setChildren(0, root);
    tree.addToIndexes();

    // collect the single instance from the annotator
    List<Instance<String>> instances;
    instances = InstanceCollector.StringFactory.collectInstances(engine, jCas);
    Assert.assertEquals(1, instances.size());

    // check the outcome
    Assert.assertEquals("AFTER", instances.get(0).getOutcome());

    // now remove all TimeML annotations
    List<Event> events;
    List<TemporalLink> tlinks;
    for (Annotation annotation : timemlAnnotations) {
      annotation.removeFromIndexes();
    }
    events = AnnotationRetrieval.getAnnotations(jCas, Event.class);
    tlinks = AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class);
    Assert.assertEquals(0, events.size());
    Assert.assertEquals(0, tlinks.size());

    // and run the annotator again, asking it to annotate this time
    desc = AnalysisEngineFactory.createPrimitiveDescription(VerbClauseTemporalAnnotator.class,
            typeSystemDescription, CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
            AfterNewClassifier.class.getName());
    engine = AnalysisEngineFactory.createPrimitive(desc);
    engine.process(jCas);
    engine.collectionProcessComplete();

    // check the resulting TimeML annotations
    events = AnnotationRetrieval.getAnnotations(jCas, Event.class);
    tlinks = AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class);
    Assert.assertEquals(2, events.size());
    Assert.assertEquals(1, tlinks.size());
    Assert.assertEquals("said", events.get(0).getCoveredText());
    Assert.assertEquals("bought", events.get(1).getCoveredText());
    Assert.assertEquals(events.get(0), tlinks.get(0).getSource());
    Assert.assertEquals(events.get(1), tlinks.get(0).getTarget());
    Assert.assertEquals("AFTER-NEW", tlinks.get(0).getRelationType());
  }

  private TreebankNode newNode(JCas jcas, Token token) {
    return TreebankTestsUtil.newNode(jcas, token.getBegin(), token.getEnd(), token.getPos());
  }

  @Test
  public void testModel() throws Exception {
    // fill in text and tokens
    tokenBuilder.buildTokens(jCas, "He said he sold the stocks yesterday.",
            "He said he sold the stocks yesterday .", "PRP VBD PRP VBD DT NNS RB .",
            "he say he sell the stock yesterday .");
    List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, Token.class);

    // fill in tree
    TreebankNode root = TreebankTestsUtil.newNode(jCas, "S", TreebankTestsUtil.newNode(jCas, "NP",
            this.newNode(jCas, tokens.get(0))), TreebankTestsUtil.newNode(jCas, "VP", this.newNode(
            jCas, tokens.get(1)), TreebankTestsUtil.newNode(jCas, "SBAR", TreebankTestsUtil
            .newNode(jCas, "NP", this.newNode(jCas, tokens.get(2))), TreebankTestsUtil.newNode(
            jCas,
            "VP",
            this.newNode(jCas, tokens.get(3)),
            TreebankTestsUtil.newNode(jCas, "NP", this.newNode(jCas, tokens.get(4)),
                    this.newNode(jCas, tokens.get(5))), this.newNode(jCas, tokens.get(6))))), this
            .newNode(jCas, tokens.get(7)));
    Sentence sentence = AnnotationRetrieval.getAnnotations(jCas, Sentence.class).get(0);
    TopTreebankNode tree = new TopTreebankNode(jCas, sentence.getBegin(), sentence.getEnd());
    tree.setNodeType("TOP");
    tree.setChildren(new FSArray(jCas, 1));
    tree.setChildren(0, root);
    tree.addToIndexes();

    // run annotator
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(VerbClauseTemporalAnnotator
            .getAnnotatorDescription());
    engine.process(jCas);

    // check output
    List<TemporalLink> tlinks = AnnotationRetrieval.getAnnotations(jCas, TemporalLink.class);
    Assert.assertEquals(1, tlinks.size());
    Assert.assertEquals("said", tlinks.get(0).getSource().getCoveredText());
    Assert.assertEquals("sold", tlinks.get(0).getTarget().getCoveredText());
    Assert.assertEquals("AFTER", tlinks.get(0).getRelationType());

  }

}
