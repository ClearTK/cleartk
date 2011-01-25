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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotatorDescriptionFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.util.InstanceCollector;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.TreebankTestsUtil;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */
public class PredicateArgumentHandlerTest extends SrlTestBase {

  @Test
  public void testArgumentAnnotationNoPredicate() throws UIMAException, CleartkException {
    // create the document
    AnalysisEngine engine = AnalysisEngineFactory
        .createPrimitive(CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(
            ArgumentAnnotator.class,
            typeSystemDescription,
            InstanceCollector.StringFactory.class,
            "."));
    this.setTokens(jCas);
    this.setTrees(jCas);

    // make sure the handler produces no instances
    List<Instance<String>> instances = InstanceCollector.StringFactory.collectInstances(
        engine,
        jCas);
    Assert.assertEquals(0, instances.size());
  }

  @Test
  public void testArgumentIdentificationNoPredicate() throws UIMAException, CleartkException {
    // create the document
    AnalysisEngine engine = AnalysisEngineFactory
        .createPrimitive(CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(
            ArgumentIdentifier.class,
            typeSystemDescription,
            InstanceCollector.BooleanFactory.class,
            "."));
    this.setTokens(jCas);
    this.setTrees(jCas);

    // make sure the handler produces no instances
    List<Instance<Boolean>> instances = InstanceCollector.BooleanFactory.collectInstances(
        engine,
        jCas);
    Assert.assertEquals(0, instances.size());
  }

  @Test
  public void testArgumentClassificationNoPredicate() throws UIMAException, CleartkException {
    // create the document
    AnalysisEngine engine = AnalysisEngineFactory
        .createPrimitive(CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(
            ArgumentClassifier.class,
            typeSystemDescription,
            InstanceCollector.StringFactory.class,
            "."));
    this.setTokens(jCas);
    this.setTrees(jCas);

    // make sure the handler produces no instances
    List<Instance<String>> instances = InstanceCollector.StringFactory.collectInstances(
        engine,
        jCas);
    Assert.assertEquals(0, instances.size());
  }

  @Test
  public void testArgumentAnnotationNoTree() throws UIMAException, CleartkException {
    // create the document
    AnalysisEngine engine = AnalysisEngineFactory
        .createPrimitive(CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(
            ArgumentAnnotator.class,
            typeSystemDescription,
            InstanceCollector.StringFactory.class,
            "."));
    this.setTokens(jCas);
    this.setPredicates(jCas);

    // make sure the handler produces an exception
    HideLogging hider = new HideLogging();
    try {
      InstanceCollector.StringFactory.collectInstances(engine, jCas);
      Assert.fail("expected exception for missing TopTreebankNode");
    } catch (AnalysisEngineProcessException e) {
    } finally {
      hider.restoreLogging();
    }
  }

  @Test
  public void testArgumentIdentificationNoTree() throws UIMAException, CleartkException {
    // create the document
    AnalysisEngine engine = AnalysisEngineFactory
        .createPrimitive(CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(
            ArgumentIdentifier.class,
            typeSystemDescription,
            InstanceCollector.BooleanFactory.class,
            "."));
    this.setTokens(jCas);
    this.setPredicates(jCas);

    // make sure the handler produces an exception
    HideLogging hider = new HideLogging();
    try {
      InstanceCollector.BooleanFactory.collectInstances(engine, jCas);
      Assert.fail("expected exception for missing TopTreebankNode");
    } catch (AnalysisEngineProcessException e) {
    } finally {
      hider.restoreLogging();
    }
  }

  @Test
  public void testArgumentClassificationNoTree() throws UIMAException, CleartkException {
    // create the document
    AnalysisEngine engine = AnalysisEngineFactory
        .createPrimitive(CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(
            ArgumentClassifier.class,
            typeSystemDescription,
            InstanceCollector.StringFactory.class,
            "."));
    this.setTokens(jCas);
    this.setPredicates(jCas);

    // make sure the handler produces an exception
    HideLogging hider = new HideLogging();
    try {
      InstanceCollector.StringFactory.collectInstances(engine, jCas);
      Assert.fail("expected exception for missing TopTreebankNode");
    } catch (AnalysisEngineProcessException e) {
    } finally {
      hider.restoreLogging();
    }
  }

  @Test
  public void testPredicateAnnotation() throws UIMAException, CleartkException {
    // create the document
    AnalysisEngine engine = AnalysisEngineFactory
        .createPrimitive(CleartkAnnotatorDescriptionFactory.createCleartkAnnotator(
            PredicateAnnotator.class,
            typeSystemDescription,
            InstanceCollector.BooleanFactory.class,
            "."));
    this.setTokens(jCas);
    this.setTrees(jCas);
    this.setPredicates(jCas);

    // get the instances produced by the handler
    List<Instance<Boolean>> instances = InstanceCollector.BooleanFactory.collectInstances(
        engine,
        jCas);
    Assert.assertEquals(5, instances.size());
    Object[] featureValues;

    // check "broke"
    Instance<Boolean> brokeInstance = instances.get(1);
    featureValues = new Object[] {
        "broke",
        "break",
        "VBD",
        "John",
        "John",
        "NNP",
        null,
        "the",
        "the",
        "DT",
        "lamp",
        "lamp",
        "NN" };
    Assert.assertEquals(Arrays.asList(featureValues), this.getFeatureValues(brokeInstance));
    Assert.assertEquals(true, brokeInstance.getOutcome());

    // check "lamp"
    Instance<Boolean> lampInstance = instances.get(3);
    featureValues = new Object[] {
        "lamp",
        "lamp",
        "NN",
        "the",
        "the",
        "DT",
        "broke",
        "break",
        "VBD",
        ".",
        ".",
        ".",
        null };
    Assert.assertEquals(Arrays.asList(featureValues), this.getFeatureValues(lampInstance));
    Assert.assertEquals(false, lampInstance.getOutcome());
  }

  /*
   * removed a few tests: makes no sense to test that produced features match an exact expectation
   * -- that's not part of the specification
   */

  private void setTokens(JCas jCas) throws UIMAException {
    tokenBuilder.buildTokens(
        jCas,
        "John broke the lamp.",
        "John broke the lamp .",
        "NNP VBD DT NN .",
        "John break the lamp .");
  }

  private void setTrees(JCas jCas) {
    TreebankNode sNode = TreebankTestsUtil.newNode(jCas, "S", TreebankTestsUtil.newNode(
        jCas,
        "NP",
        TreebankTestsUtil.newNode(jCas, 0, 4, "NNP")), TreebankTestsUtil.newNode(
        jCas,
        "VP",
        TreebankTestsUtil.newNode(jCas, 5, 10, "VBD"),
        TreebankTestsUtil.newNode(
            jCas,
            "NP",
            TreebankTestsUtil.newNode(jCas, 11, 14, "DT"),
            TreebankTestsUtil.newNode(jCas, 15, 19, "NN"))), TreebankTestsUtil.newNode(
        jCas,
        19,
        20,
        "."));

    TopTreebankNode topNode = new TopTreebankNode(jCas, sNode.getBegin(), sNode.getEnd());
    topNode.setNodeType("TOP");
    topNode.setChildren(UIMAUtil.toFSArray(jCas, Collections.singletonList(sNode)));
    topNode.addToIndexes();
  }

  private void setPredicates(JCas jCas) {
    List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, Token.class);
    Token predToken = tokens.get(1);
    Predicate predicate = new Predicate(jCas, predToken.getBegin(), predToken.getEnd());
    predicate.setAnnotation(predToken);
    predicate.addToIndexes();

    List<TreebankNode> nodes = AnnotationRetrieval.getAnnotations(jCas, TreebankNode.class);
    SemanticArgument arg0 = new SemanticArgument(jCas, 0, 4);
    arg0.setLabel("ARG0");
    arg0.setFeature("XXX");
    arg0.addToIndexes();
    SemanticArgument arg1 = new SemanticArgument(jCas, 11, 19);
    arg1.setLabel("ARG1");
    arg1.addToIndexes();
    for (TreebankNode node : nodes) {
      if (node.getNodeType().equals("NP") && node.getCoveredText().equals("John")) {
        arg0.setAnnotation(node);
      }
      if (node.getCoveredText().equals("the lamp")) {
        arg1.setAnnotation(node);
      }
    }
    predicate.setArguments(new FSArray(jCas, 2));
    predicate.setArguments(0, arg0);
    predicate.setArguments(1, arg1);
  }

  private List<String> getFeatureValues(Instance<?> instance) {
    List<String> values = new ArrayList<String>();
    for (Feature feature : instance.getFeatures()) {
      Object value = feature == null ? null : feature.getValue();
      values.add(value == null ? null : value.toString());
    }
    return values;
  }

  private static class HideLogging {
    private Logger root;

    private Handler[] handlers;

    public HideLogging() {
      this.root = Logger.getLogger("");
      this.handlers = root.getHandlers();
      for (Handler handler : this.handlers) {
        root.removeHandler(handler);
      }
    }

    public void restoreLogging() {
      for (Handler handler : this.handlers) {
        this.root.addHandler(handler);
      }
    }
  }

}
