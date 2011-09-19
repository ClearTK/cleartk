/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.eval.provider;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.eval.EvaluationTestBase;
import org.cleartk.type.test.Token;
import org.cleartk.util.ViewURIUtil;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class AnnotationEvaluatorTest extends EvaluationTestBase {

  @Test
  public void testSpans() throws Exception {
    AnalysisEngineDescription desc = AnnotationEvaluator.getSpanDescription(
        Token.class,
        "Gold",
        "System");
    // necessary because test type system descriptors are not in their own files
    desc.getAnalysisEngineMetaData().setTypeSystem(
        TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.type.test.TestTypeSystem"));
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(desc);
    JCas base = engine.newJCas();
    ViewURIUtil.setURI(base, new File("testSpans").toURI());
    JCas gold = base.createView("Gold");
    JCas system = base.createView("System");
    for (JCas jCas : Arrays.asList(gold, system)) {
      jCas.setDocumentText("She sells seashells by the seashore.");
    }
    addToken(gold, 0, 3);
    addToken(gold, 4, 9);
    addToken(gold, 10, 19);
    addToken(gold, 20, 22);
    addToken(gold, 23, 26);
    addToken(gold, 27, 35);
    addToken(system, 4, 9);
    addToken(system, 10, 19);
    addToken(system, 20, 26);
    addToken(system, 27, 35);
    addToken(system, 35, 36);

    // save old logging levels
    Map<Handler, Level> consoleHandlers = new HashMap<Handler, Level>();
    for (Handler handler : Logger.getLogger("").getHandlers()) {
      if (handler instanceof ConsoleHandler) {
        consoleHandlers.put(handler, handler.getLevel());
      }
    }

    // handlers to collect warning and info messages
    StringHandler warningHandler = new StringHandler();
    warningHandler.setLevel(Level.WARNING);
    StringHandler infoHandler = new StringHandler();
    infoHandler.setLevel(Level.INFO);

    Logger logger = Logger.getLogger(AnnotationEvaluator.class.getName());
    try {
      // turn off other logging and add new handlers
      logger.addHandler(warningHandler);
      logger.addHandler(infoHandler);
      for (Handler consoleHandler : consoleHandlers.keySet()) {
        consoleHandler.setLevel(Level.OFF);
      }

      // process document
      engine.process(base);

      // complete a batch
      engine.batchProcessComplete();

      // check the warnings
      String warnings = warningHandler.logBuilder.toString();
      assertProperty("gold", "6", warnings);
      assertProperty("system", "5", warnings);
      assertProperty("matching", "3", warnings);
      assertProperty("precision", "0.600", warnings);
      assertProperty("recall", "0.500", warnings);
      assertProperty("f1", "0.545", warnings);
      Assert.assertFalse(warnings.matches(".*(WRONG|DROPPED|ADDED).*"));

      // check the info
      String info = infoHandler.logBuilder.toString();
      assertSomeLineMatches(".*testSpans.*", info);
      assertSomeLineMatches("DROPPED.*\\[She\\].*", info);
      assertSomeLineMatches("DROPPED.*\\[by\\].*", info);
      assertSomeLineMatches("DROPPED.*\\[the\\].*", info);
      assertSomeLineMatches("ADDED.*\\[by the\\].*", info);
      assertSomeLineMatches("ADDED.*\\[\\.\\].*", info);
      assertProperty("gold", "6", warnings);
      assertProperty("system", "5", warnings);
      assertProperty("matching", "3", warnings);
      assertProperty("precision", "0.600", warnings);
      assertProperty("recall", "0.500", warnings);
      assertProperty("f1", "0.545", warnings);

      // empty logs
      warningHandler.logBuilder.delete(0, warningHandler.logBuilder.length());
      infoHandler.logBuilder.delete(0, infoHandler.logBuilder.length());

      // process document
      engine.process(base);

      // complete the collection
      engine.collectionProcessComplete();

      // check the warnings
      warnings = warningHandler.logBuilder.toString();
      assertProperty("gold", "12", warnings);
      assertProperty("system", "10", warnings);
      assertProperty("matching", "6", warnings);
      assertProperty("precision", "0.600", warnings);
      assertProperty("recall", "0.500", warnings);
      assertProperty("f1", "0.545", warnings);
      Assert.assertFalse(warnings.matches(".*(WRONG|DROPPED|ADDED).*"));

    } finally {
      // restore original logging configuration
      logger.removeHandler(warningHandler);
      logger.removeHandler(infoHandler);
      for (Handler consoleHandler : consoleHandlers.keySet()) {
        consoleHandler.setLevel(consoleHandlers.get(consoleHandler));
      }
    }
  }

  @Test
  public void testAttributes() throws Exception {
    AnalysisEngineDescription desc = AnnotationEvaluator.getAttributeDescription(
        Token.class,
        "pos",
        "Gold",
        "System");
    // necessary because test type system descriptors are not in their own files
    desc.getAnalysisEngineMetaData().setTypeSystem(
        TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.type.test.TestTypeSystem"));
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        desc,
        AnnotationEvaluator.PARAM_IGNORE_SYSTEM_SPANS_NOT_IN_GOLD,
        true);
    JCas base = engine.newJCas();
    ViewURIUtil.setURI(base, new File("testSpans").toURI());
    JCas gold = base.createView("Gold");
    JCas system = base.createView("System");
    for (JCas jCas : Arrays.asList(gold, system)) {
      jCas.setDocumentText("She sells seashells by the seashore.");
    }
    addToken(gold, 0, 3, "PRP");
    addToken(gold, 4, 9, "VBZ");
    addToken(gold, 10, 19, "NNS");
    addToken(gold, 20, 22, "IN");
    addToken(gold, 23, 26, "DT");
    addToken(gold, 27, 35, "NN");
    addToken(system, 0, 3, "PRP");
    addToken(system, 4, 9, "VBZ");
    addToken(system, 10, 19, "NN");
    addToken(system, 20, 22, "IN");
    addToken(system, 27, 35, "NN");
    addToken(system, 35, 36, ".");

    // save old logging levels
    Map<Handler, Level> consoleHandlers = new HashMap<Handler, Level>();
    for (Handler handler : Logger.getLogger("").getHandlers()) {
      if (handler instanceof ConsoleHandler) {
        consoleHandlers.put(handler, handler.getLevel());
      }
    }

    // handlers to collect warning and info messages
    StringHandler warningHandler = new StringHandler();
    warningHandler.setLevel(Level.WARNING);
    StringHandler infoHandler = new StringHandler();
    infoHandler.setLevel(Level.INFO);

    Logger logger = Logger.getLogger(AnnotationEvaluator.class.getName());
    try {
      // turn off other logging and add new handlers
      logger.addHandler(warningHandler);
      logger.addHandler(infoHandler);
      for (Handler consoleHandler : consoleHandlers.keySet()) {
        consoleHandler.setLevel(Level.OFF);
      }

      // process document
      engine.process(base);

      // complete a batch
      engine.batchProcessComplete();

      // check the warnings
      String warnings = warningHandler.logBuilder.toString();
      assertProperty("gold", "6", warnings);
      assertProperty("system", "5", warnings);
      assertProperty("matching", "4", warnings);
      assertProperty("precision", "0.800", warnings);
      assertProperty("recall", "0.667", warnings);
      assertProperty("f1", "0.727", warnings);
      Assert.assertFalse(warnings.matches(".*(WRONG|DROPPED|ADDED).*"));

      // check the info
      String info = infoHandler.logBuilder.toString();
      assertSomeLineMatches(".*testSpans.*", info);
      assertSomeLineMatches("WRONG.*NN.*NNS.*\\[seashells\\].*", info);
      assertSomeLineMatches("DROPPED.*DT.*\\[the\\].*", info);
      Assert.assertFalse(warnings.matches(".*ADDED.*"));
      assertProperty("gold", "6", warnings);
      assertProperty("system", "5", warnings);
      assertProperty("matching", "4", warnings);
      assertProperty("precision", "0.800", warnings);
      assertProperty("recall", "0.667", warnings);
      assertProperty("f1", "0.727", warnings);

      // empty logs
      warningHandler.logBuilder.delete(0, warningHandler.logBuilder.length());
      infoHandler.logBuilder.delete(0, infoHandler.logBuilder.length());

      // process document
      engine.process(base);

      // complete the collection
      engine.collectionProcessComplete();

      // check the warnings
      warnings = warningHandler.logBuilder.toString();
      assertProperty("gold", "12", warnings);
      assertProperty("system", "10", warnings);
      assertProperty("matching", "8", warnings);
      assertProperty("precision", "0.800", warnings);
      assertProperty("recall", "0.667", warnings);
      assertProperty("f1", "0.727", warnings);
      Assert.assertFalse(warnings.matches(".*(WRONG|DROPPED|ADDED).*"));

    } finally {
      // restore original logging configuration
      logger.removeHandler(warningHandler);
      logger.removeHandler(infoHandler);
      for (Handler consoleHandler : consoleHandlers.keySet()) {
        consoleHandler.setLevel(consoleHandlers.get(consoleHandler));
      }
    }
  }

  public static class POSGrouper implements AnnotationEvaluator.AnnotationGrouper {

    @Override
    public List<String> getGroups(Annotation annotation) {
      return Arrays.asList(((Token) annotation).getPos());
    }

  }

  @Test
  public void testGroups() throws Exception {
    AnalysisEngineDescription desc = AnnotationEvaluator.getAttributeDescription(
        Token.class,
        "pos",
        "Gold",
        "System");
    // necessary because test type system descriptors are not in their own files
    desc.getAnalysisEngineMetaData().setTypeSystem(
        TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.type.test.TestTypeSystem"));
    AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
        desc,
        AnnotationEvaluator.PARAM_ANNOTATION_GROUPER_CLASS_NAME,
        POSGrouper.class.getName());
    JCas base = engine.newJCas();
    ViewURIUtil.setURI(base, new File("testSpans").toURI());
    JCas gold = base.createView("Gold");
    JCas system = base.createView("System");
    for (JCas jCas : Arrays.asList(gold, system)) {
      jCas.setDocumentText("Cats like dogs.");
    }
    addToken(gold, 0, 4, "NNS");
    addToken(gold, 5, 9, "VB");
    addToken(gold, 10, 14, "NNS");
    addToken(gold, 14, 15, ".");
    addToken(system, 0, 5, "NNS"); // wrong span
    addToken(system, 5, 9, "VB");
    addToken(system, 10, 14, "NNS");
    // missing period

    // save old logging levels
    Map<Handler, Level> consoleHandlers = new HashMap<Handler, Level>();
    for (Handler handler : Logger.getLogger("").getHandlers()) {
      if (handler instanceof ConsoleHandler) {
        consoleHandlers.put(handler, handler.getLevel());
      }
    }

    // handlers to collect warning and info messages
    StringHandler warningHandler = new StringHandler();
    warningHandler.setLevel(Level.WARNING);
    StringHandler infoHandler = new StringHandler();
    infoHandler.setLevel(Level.INFO);

    Logger logger = Logger.getLogger(AnnotationEvaluator.class.getName());
    try {
      // turn off other logging and add new handlers
      logger.addHandler(warningHandler);
      logger.addHandler(infoHandler);
      for (Handler consoleHandler : consoleHandlers.keySet()) {
        consoleHandler.setLevel(Level.OFF);
      }

      // process document
      engine.process(base);

      // complete a batch
      engine.batchProcessComplete();

      // check the warnings
      String warnings = warningHandler.logBuilder.toString();
      assertSomeLineMatches(".*Token:pos Batch 0 \\.", warnings);
      // @formatter:off
      Assert.assertTrue(warnings.matches(
          "(?s).*" +
          "Token:pos Batch 0 \\.\n" +
          "gold      = 1\n" + 
          "system    = 0\n" +
          "matching  = 0\n" +
          "precision = 1.000\n" +
          "recall    = 0.000\n" +
          "f1        = 0.000\n" +
          "Token:pos Batch 0 NNS\n" +
          "gold      = 2\n" +
          "system    = 2\n" +
          "matching  = 1\n" +
          "precision = 0.500\n" +
          "recall    = 0.500\n" +
          "f1        = 0.500\n" +
          "Token:pos Batch 0 VB\n" +
          "gold      = 1\n" +
          "system    = 1\n" +
          "matching  = 1\n" +
          "precision = 1.000\n" +
          "recall    = 1.000\n" +
          "f1        = 1.000\n" +
          ".*"));
      // @formatter:on
      Assert.assertFalse(warnings.matches(".*(WRONG|DROPPED|ADDED).*"));

      // check the info
      String info = infoHandler.logBuilder.toString();
      assertSomeLineMatches(".*testSpans.*", info);
      assertSomeLineMatches("DROPPED.*\\[Cats\\].*", info);
      assertSomeLineMatches("ADDED.*\\[Cats \\].*", info);
      assertSomeLineMatches("DROPPED.*\\[.\\].*", info);

    } finally {
      // restore original logging configuration
      logger.removeHandler(warningHandler);
      logger.removeHandler(infoHandler);
      for (Handler consoleHandler : consoleHandlers.keySet()) {
        consoleHandler.setLevel(consoleHandlers.get(consoleHandler));
      }
    }
  }

  private static void assertProperty(String key, String value, String text) {
    for (String line : text.split("\n")) {
      String[] keyValue = line.split("\\s*=\\s*");
      if (keyValue.length == 2 && keyValue[0].trim().equals(key)) {
        Assert.assertEquals(value, keyValue[1].trim());
      }
    }
  }

  private static void assertSomeLineMatches(String regex, String text) {
    for (String line : text.split("\n")) {
      if (line.matches(regex)) {
        return;
      }
    }
    Assert.fail("No line matched " + regex);
  }

  private static void addToken(JCas jCas, int begin, int end) {
    Token token = new Token(jCas, begin, end);
    token.addToIndexes();
  }

  private static void addToken(JCas jCas, int begin, int end, String pos) {
    Token token = new Token(jCas, begin, end);
    token.setPos(pos);
    token.addToIndexes();
  }

  private static class StringHandler extends Handler {
    public StringBuilder logBuilder;

    public StringHandler() {
      this.logBuilder = new StringBuilder();
    }

    @Override
    public void publish(LogRecord record) {
      this.logBuilder.append(record.getMessage()).append('\n');
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
  }
}
