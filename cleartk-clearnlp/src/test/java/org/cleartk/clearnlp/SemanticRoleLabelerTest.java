package org.cleartk.clearnlp;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;


import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.cleartk.srl.type.Predicate;
import org.cleartk.srl.type.SemanticArgument;
import org.cleartk.test.CleartkTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;

public class SemanticRoleLabelerTest extends CleartkTestBase {
	protected TokenBuilder<Token, Sentence> tokenBuilder;
	protected static AnalysisEngineDescription lemmatizer;
	protected static AnalysisEngineDescription parser;
	protected static AnalysisEngineDescription srlabeler;

	static {
		try {
		  lemmatizer = MPAnalyzer.getDescription();
		  parser = AnalysisEngineFactory.createPrimitiveDescription(DependencyParser.class, 
		      DependencyParser.PARAM_PARSER_MODEL_URI,
			    new File("src/test/resources/models/sample-dep.jar").toURI());
		      
		  srlabeler = AnalysisEngineFactory.createPrimitiveDescription(SemanticRoleLabeler.class, 
			    SemanticRoleLabeler.PARAM_PRED_ID_MODEL_URI,
			    new File("src/test/resources/models/sample-pred.jar").toURI(),
			    SemanticRoleLabeler.PARAM_SRL_MODEL_URI,
			    new File("src/test/resources/models/sample-srl.jar").toURI());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	


	@Test
	public void srlTest() throws Exception {
		this.jCas.reset();
		//AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(PosTaggerAndMPAnalyzer.getDescription());
		tokenBuilder = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class, "pos", "stem");

		this.tokenBuilder.buildTokens(
				this.jCas,
				"Mary gave the car to John in 1986.",
				"Mary gave the car to John in 1986.",
				"NNP VBD DT NN IN NNP IN CD .");
		SimplePipeline.runPipeline(jCas, lemmatizer, parser, srlabeler);
		
		
		// Values are wrong, but this is using dummy models.
		List<String> expected = Arrays.asList("A0(gave, Mary)", "A1(gave, car)", "AM-LOC(gave, in)");
		List<String> actual = Lists.newArrayList();
		
    for (Predicate pred : JCasUtil.select(jCas, Predicate.class)) {
      pred.getArguments();
      for (SemanticArgument arg : JCasUtil.select(pred.getArguments(), SemanticArgument.class)) {
        actual.add(String.format("%s(%s, %s)", arg.getLabel(), pred.getCoveredText(), arg.getCoveredText()));
      }
    }
    Assert.assertEquals(expected, actual);
	}
	
}
