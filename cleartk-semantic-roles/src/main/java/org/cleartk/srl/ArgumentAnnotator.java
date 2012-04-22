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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.DataWriterFactory;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.ContextExtractor;
import org.cleartk.classifier.feature.extractor.ContextExtractor.FirstCovered;
import org.cleartk.classifier.feature.extractor.ContextExtractor.LastCovered;
import org.cleartk.classifier.feature.extractor.annotationpair.DistanceExtractor;
import org.cleartk.classifier.feature.extractor.annotationpair.RelativePositionExtractor;
import org.cleartk.classifier.feature.extractor.simple.CombinedExtractor;
import org.cleartk.classifier.feature.extractor.simple.CoveredTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.NamingExtractor;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.GenericJarClassifierFactory;
import org.cleartk.srl.type.Argument;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil;
import org.cleartk.syntax.feature.HeadWordExtractor;
import org.cleartk.syntax.feature.SubCategorizationExtractor;
import org.cleartk.syntax.feature.SyntacticPathExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationUtil;
import org.cleartk.util.UIMAUtil;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 */

public class ArgumentAnnotator extends CleartkAnnotator<String> {

  public static AnalysisEngineDescription getWriterDescription(
      Class<? extends DataWriterFactory<String>> dataWriterFactoryClass,
      File outputDirectory) throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ArgumentAnnotator.class,
        SrlComponents.TYPE_SYSTEM_DESCRIPTION,
        CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
        dataWriterFactoryClass.getName(),
        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
        outputDirectory.toString());
  }

  public static AnalysisEngineDescription getClassifierDescription(File classifierJar)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        ArgumentAnnotator.class,
        SrlComponents.TYPE_SYSTEM_DESCRIPTION,
        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
        classifierJar.toString());
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    SimpleFeatureExtractor[] tokenExtractors = {
        new CoveredTextExtractor(),
        new TypePathExtractor(Token.class, "stem"),
        new TypePathExtractor(Token.class, "pos") };

    SimpleFeatureExtractor[] constituentExtractors = {
        new TypePathExtractor(TreebankNode.class, "nodeType"),
        // new TypePathExtractor(TreebankNode.class, "nodeTags"),
        new HeadWordExtractor(new CombinedExtractor(tokenExtractors), true) };

    predicateTokenExtractor = new CombinedExtractor(tokenExtractors);
    predicateNodeExtractor = new SubCategorizationExtractor("PredicateNode");

    pathExtractor = new SyntacticPathExtractor(
        new TypePathExtractor(TreebankNode.class, "nodeType"));
    partialPathExtractor = new SyntacticPathExtractor(new TypePathExtractor(
        TreebankNode.class,
        "nodeType"), true);
    relPosExtractor = new RelativePositionExtractor();
    distanceExtractor = new DistanceExtractor("ConstituentPredicate", Token.class);

    constituentExtractor = new CombinedExtractor(constituentExtractors);
    leftSiblingExtractor = new CombinedExtractor(constituentExtractors);
    rightSiblingExtractor = new CombinedExtractor(constituentExtractors);
    parentExtractor = new CombinedExtractor(constituentExtractors);
    firstAndLastWordExtractor = new ContextExtractor<Token>(Token.class, new NamingExtractor(
        "Constituent",
        new CombinedExtractor(tokenExtractors)), new FirstCovered(1), new LastCovered(1));
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
      processSentence(jCas, sentence);
    }
  }

  private void processSentence(JCas jCas, Sentence sentence) throws AnalysisEngineProcessException {

    List<TreebankNode> sentenceConstituents = new ArrayList<TreebankNode>(80);
    TopTreebankNode top = AnnotationUtil.selectFirstMatching(jCas, TopTreebankNode.class, sentence);
    if (top == null) {
      throw CleartkExtractorException.noAnnotationInWindow(TopTreebankNode.class, sentence);
    }
    collectConstituents(top, sentenceConstituents);

    List<List<Feature>> sentenceConstituentFeatures = new ArrayList<List<Feature>>(
        sentenceConstituents.size());
    for (TreebankNode constituent : sentenceConstituents)
      sentenceConstituentFeatures.add(extractConstituentFeatures(jCas, constituent, sentence));

    List<Predicate> predicates = JCasUtil.selectCovered(jCas, Predicate.class, sentence);
    for (Predicate predicate : predicates) {
      processPredicate(jCas, sentence, predicate, sentenceConstituents, sentenceConstituentFeatures);
    }

  }

  private void processPredicate(
      JCas jCas,
      Sentence sentence,
      Predicate predicate,
      List<TreebankNode> constituents,
      List<List<Feature>> constituentFeatures) throws AnalysisEngineProcessException {
    TreebankNode predicateNode = TreebankNodeUtil.selectMatchingLeaf(
        jCas,
        predicate.getAnnotation());
    List<Token> predicateTokens = JCasUtil.selectCovered(
        jCas,
        Token.class,
        predicate.getAnnotation());
    Token predicateToken = predicateTokens.get(0);

    List<Feature> predicateFeatures = new ArrayList<Feature>(20);
    predicateFeatures.addAll(predicateTokenExtractor.extract(jCas, predicateToken));
    predicateFeatures.addAll(predicateNodeExtractor.extract(jCas, predicateNode));

    for (int i = 0; i < constituents.size(); i++) {
      TreebankNode constituent = constituents.get(i);

      Instance<String> instance = new Instance<String>();

      instance.addAll(predicateFeatures);
      instance.addAll(constituentFeatures.get(i));
      instance.addAll(relPosExtractor.extract(jCas, constituent, predicate.getAnnotation()));
      instance.addAll(pathExtractor.extract(jCas, constituent, predicateNode));
      instance.addAll(partialPathExtractor.extract(jCas, constituent, predicateNode));
      instance.addAll(distanceExtractor.extract(jCas, constituent, predicateNode));

      instance.setOutcome("NULL");

      for (Argument arg : UIMAUtil.toList(predicate.getArguments(), Argument.class)) {
        if (!(arg instanceof SemanticArgument))
          continue;

        SemanticArgument sarg = (SemanticArgument) arg;

        if (sarg.getAnnotation() == constituent && !sarg.getLabel().equals("rel")) {
          if (sarg.getFeature() != null) {
            instance.setOutcome(sarg.getLabel() + "-" + sarg.getFeature());
          } else {
            instance.setOutcome(sarg.getLabel());
          }
          break;
        }
      }

      if (this.isTraining()) {
        this.dataWriter.write(instance);
      } else {
        String outcome = this.classifier.classify(instance.getFeatures());
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
            args = UIMAUtil.toList(predicate.getArguments(), Argument.class);
          else
            args = new ArrayList<Argument>(1);
          args.add(arg);
          predicate.setArguments(UIMAUtil.toFSArray(jCas, args));
        }
      }
    }

  }

  private List<Feature> extractConstituentFeatures(
      JCas jCas,
      TreebankNode constituent,
      Sentence sentence) throws CleartkExtractorException {
    List<Feature> features = new ArrayList<Feature>(20);
    features.addAll(constituentExtractor.extract(jCas, constituent));
    features.addAll(firstAndLastWordExtractor.extractWithin(jCas, constituent, sentence));

    TreebankNode parent = constituent.getParent();
    if (parent != null) {
      features.addAll(parentExtractor.extract(jCas, parent));

      int constituentPosition = 0;
      while (parent.getChildren(constituentPosition) != constituent)
        constituentPosition += 1;

      if (constituentPosition > 0)
        features.addAll(leftSiblingExtractor.extract(
            jCas,
            parent.getChildren(constituentPosition - 1)));

      if (constituentPosition < parent.getChildren().size() - 1)
        features.addAll(rightSiblingExtractor.extract(
            jCas,
            parent.getChildren(constituentPosition + 1)));
    }

    return features;
  }

  private void collectConstituents(TreebankNode top, List<TreebankNode> constituents) {
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

  private ContextExtractor<Token> firstAndLastWordExtractor;

  private SimpleFeatureExtractor leftSiblingExtractor;

  private SimpleFeatureExtractor rightSiblingExtractor;

  private SimpleFeatureExtractor parentExtractor;

}
