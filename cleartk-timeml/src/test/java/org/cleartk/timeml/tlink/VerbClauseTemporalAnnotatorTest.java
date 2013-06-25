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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Classifier;
import org.cleartk.classifier.ClassifierFactory;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.util.PublicFieldDataWriter;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.timeml.TimeMLTestBase;
import org.cleartk.timeml.type.Event;
import org.cleartk.timeml.type.TemporalLink;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

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

    public String classify(List<Feature> features) {
      return "AFTER-NEW";
    }

    public List<ScoredOutcome<String>> score(List<Feature> features, int maxResults) {
      return null;
    }

    public Classifier<String> createClassifier() {
      return new AfterNewClassifier();
    }
  }

  @Test
  public void test() throws UIMAException {
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        VerbClauseTemporalAnnotator.class,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        PublicFieldDataWriter.StringFactory.class.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        ".");

    tokenBuilder.buildTokens(
        jCas,
        "He said she bought milk.",
        "He said she bought milk .",
        "PRP VBD PRP VBD NN .",
        "he say she buy milk .");
    List<Token> tokens = new ArrayList<Token>(JCasUtil.select(jCas, Token.class));

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
    TreebankNode root = TreebankNodeUtil.newNode(jCas, "S", TreebankNodeUtil.newNode(
        jCas,
        "NP",
        this.newNode(jCas, tokens.get(0))), TreebankNodeUtil.newNode(
        jCas,
        "VP",
        this.newNode(jCas, tokens.get(1)),
        TreebankNodeUtil.newNode(jCas, "SBAR", TreebankNodeUtil.newNode(
            jCas,
            "NP",
            this.newNode(jCas, tokens.get(2))), TreebankNodeUtil.newNode(
            jCas,
            "VP",
            this.newNode(jCas, tokens.get(3)),
            TreebankNodeUtil.newNode(jCas, "NP", this.newNode(jCas, tokens.get(4)))))));

    Sentence sentence = JCasUtil.selectSingle(jCas, Sentence.class);

    // set the Sentence's constitutentParse feature
    TopTreebankNode tree = new TopTreebankNode(jCas, sentence.getBegin(), sentence.getEnd());
    tree.setNodeType("TOP");
    tree.setChildren(new FSArray(jCas, 1));
    tree.setChildren(0, root);
    tree.addToIndexes();

    // collect the single instance from the annotator
    List<Instance<String>> instances;
    instances = PublicFieldDataWriter.StringFactory.collectInstances(engine, jCas);
    Assert.assertEquals(1, instances.size());

    // check the outcome
    Assert.assertEquals("AFTER", instances.get(0).getOutcome());

    // now remove all TimeML annotations
    Collection<Event> events;
    Collection<TemporalLink> tlinks;
    for (Annotation annotation : timemlAnnotations) {
      annotation.removeFromIndexes();
    }
    events = JCasUtil.select(jCas, Event.class);
    tlinks = JCasUtil.select(jCas, TemporalLink.class);
    Assert.assertEquals(0, events.size());
    Assert.assertEquals(0, tlinks.size());

    // and run the annotator again, asking it to annotate this time
    // but don't let it add any events
    engine = AnalysisEngineFactory.createPrimitive(
        VerbClauseTemporalAnnotator.class,
        CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        AfterNewClassifier.class.getName());
    engine.process(jCas);
    engine.collectionProcessComplete();

    // check that no TimeML annotations were created
    events = JCasUtil.select(jCas, Event.class);
    tlinks = JCasUtil.select(jCas, TemporalLink.class);
    Assert.assertEquals(0, events.size());
    Assert.assertEquals(0, tlinks.size());

    // run the annotator again, but let it add events this time
    engine = AnalysisEngineFactory.createPrimitive(
        VerbClauseTemporalAnnotator.class,
        CleartkAnnotator.PARAM_CLASSIFIER_FACTORY_CLASS_NAME,
        AfterNewClassifier.class.getName(),
        VerbClauseTemporalAnnotator.PARAM_CREATE_EVENTS,
        true);
    engine.process(jCas);
    engine.collectionProcessComplete();

    // check the resulting TimeML annotations
    events = JCasUtil.select(jCas, Event.class);
    tlinks = JCasUtil.select(jCas, TemporalLink.class);
    Iterator<Event> eventsIter = events.iterator();
    Event event0 = eventsIter.next();
    Event event1 = eventsIter.next();
    TemporalLink tlink0 = tlinks.iterator().next();
    Assert.assertEquals(2, events.size());
    Assert.assertEquals(1, tlinks.size());
    Assert.assertEquals("said", event0.getCoveredText());
    Assert.assertEquals("bought", event1.getCoveredText());
    Assert.assertEquals(event0, tlink0.getSource());
    Assert.assertEquals(event1, tlink0.getTarget());
    Assert.assertEquals("AFTER-NEW", tlink0.getRelationType());
  }

  private TreebankNode newNode(JCas jcas, Token token) {
    return TreebankNodeUtil.newNode(jcas, token.getBegin(), token.getEnd(), token.getPos());
  }

  @Test
  public void testModel() throws Exception {
    // fill in text and tokens
    tokenBuilder.buildTokens(
        jCas,
        "He said he sold the stocks yesterday.",
        "He said he sold the stocks yesterday .",
        "PRP VBD PRP VBD DT NNS RB .",
        "he say he sell the stock yesterday .");
    Iterator<Token> tokensIter = JCasUtil.select(jCas, Token.class).iterator();

    // fill in tree
    TreebankNode root = TreebankNodeUtil.newNode(jCas, "S", TreebankNodeUtil.newNode(
        jCas,
        "NP",
        this.newNode(jCas, tokensIter.next())), TreebankNodeUtil.newNode(
        jCas,
        "VP",
        this.newNode(jCas, tokensIter.next()),
        TreebankNodeUtil.newNode(jCas, "SBAR", TreebankNodeUtil.newNode(
            jCas,
            "NP",
            this.newNode(jCas, tokensIter.next())), TreebankNodeUtil.newNode(
            jCas,
            "VP",
            this.newNode(jCas, tokensIter.next()),
            TreebankNodeUtil.newNode(
                jCas,
                "NP",
                this.newNode(jCas, tokensIter.next()),
                this.newNode(jCas, tokensIter.next())),
            this.newNode(jCas, tokensIter.next())))), this.newNode(jCas, tokensIter.next()));
    Sentence sentence = JCasUtil.selectSingle(jCas, Sentence.class);
    TopTreebankNode tree = new TopTreebankNode(jCas, sentence.getBegin(), sentence.getEnd());
    tree.setNodeType("TOP");
    tree.setChildren(new FSArray(jCas, 1));
    tree.setChildren(0, root);
    tree.addToIndexes();

    // run annotator
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        VerbClauseTemporalAnnotator.FACTORY.getAnnotatorDescription(),
        VerbClauseTemporalAnnotator.PARAM_CREATE_EVENTS,
        true);
    engine.process(jCas);

    // check output
    Collection<TemporalLink> tlinks = JCasUtil.select(jCas, TemporalLink.class);
    TemporalLink tlink0 = tlinks.iterator().next();
    Assert.assertEquals(1, tlinks.size());
    Assert.assertEquals("said", tlink0.getSource().getCoveredText());
    Assert.assertEquals("sold", tlink0.getTarget().getCoveredText());
    Assert.assertEquals("AFTER", tlink0.getRelationType());

  }

}
