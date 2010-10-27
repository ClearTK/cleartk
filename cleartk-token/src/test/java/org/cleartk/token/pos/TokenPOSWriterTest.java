package org.cleartk.token.pos;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.util.FileUtils;
import org.cleartk.token.TokenTestBase;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.ae.linewriter.LineWriter;
import org.cleartk.util.ae.linewriter.block.BlankLineBlockWriter;
import org.cleartk.util.ae.linewriter.block.DocumentIdBlockWriter;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

public class TokenPOSWriterTest extends TokenTestBase {

	static String newline = System.getProperty("line.separator");

	@Test
	public void testTokenPosWriter() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
					LineWriter.class,
					typeSystemDescription,
					LineWriter.PARAM_OUTPUT_FILE_NAME, new File(outputDirectory, "output.txt").getPath(), 
					LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, Token.class.getName(),
					LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, TokenPOSWriter.class.getName(),
					LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, Sentence.class.getName(),
					LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME, BlankLineBlockWriter.class.getName());
		
		String text = "Me and all my friends are non-conformists.  I will subjugate my freedom oppressor.";
		tokenBuilder.buildTokens(jCas, text,
				"Me and all my friends are non-conformists . \n I will subjugate my freedom oppressor . ",
				"1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		String expectedText = newline +  
			"Me\t1"+newline +
			"and\t2"+newline +
			"all\t3"+newline +
			"my\t4"+newline +
			"friends\t5"+newline +
			"are\t6"+newline +
			"non-conformists\t7"+newline +
			".\t8"+newline +
			newline+
			"I\t9"+newline +
			"will\t10"+newline +
			"subjugate\t11"+newline +
			"my\t12"+newline +
			"freedom\t13"+newline +
			"oppressor\t14"+newline +
			".\t15"+newline;
			
		
		File outputFile = new File(this.outputDirectory, "output.txt");
		assertTrue(outputFile.exists());
		String actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(expectedText, actualText);
	}

	@Test
	public void testTokenPosWriter2() throws Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createPrimitive(
					LineWriter.class,
					typeSystemDescription,
					LineWriter.PARAM_OUTPUT_DIRECTORY_NAME, outputDirectory.getPath(), 
					LineWriter.PARAM_OUTPUT_ANNOTATION_CLASS_NAME, Token.class.getName(),
					LineWriter.PARAM_FILE_SUFFIX, "txt",
					LineWriter.PARAM_ANNOTATION_WRITER_CLASS_NAME, TokenPOSWriter.class.getName(),
					LineWriter.PARAM_BLOCK_ANNOTATION_CLASS_NAME, DocumentAnnotation.class.getName(),
					LineWriter.PARAM_BLOCK_WRITER_CLASS_NAME, DocumentIdBlockWriter.class.getName());
		
		String text = "Me and all my friends are non-conformists.  I will subjugate my freedom oppressor.";
		tokenBuilder.buildTokens(jCas, text,
				"Me and all my friends are non-conformists . \n I will subjugate my freedom oppressor . ",
				"1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
		ViewURIUtil.setURI(jCas, "1234");
		engine.process(jCas);
		engine.collectionProcessComplete();
		
		String expectedText =
			"1234"+newline + 
			"Me\t1"+newline +
			"and\t2"+newline +
			"all\t3"+newline +
			"my\t4"+newline +
			"friends\t5"+newline +
			"are\t6"+newline +
			"non-conformists\t7"+newline +
			".\t8"+newline +
			"I\t9"+newline +
			"will\t10"+newline +
			"subjugate\t11"+newline +
			"my\t12"+newline +
			"freedom\t13"+newline +
			"oppressor\t14"+newline +
			".\t15"+newline;
			
		
		File outputFile = new File(this.outputDirectory, "1234.txt");
		assertTrue(outputFile.exists());
		String actualText = FileUtils.file2String(outputFile);
		Assert.assertEquals(expectedText, actualText);
	}

}
