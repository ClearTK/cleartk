package org.cleartk.corpus.ace2005;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.TestsUtil;
import org.junit.Assert;
import org.junit.Test;

public class Ace2005GoldTests {

	private final File rootDir = new File("test/data/corpus/ace2005");

	@Test
	public void testReaderInvalidParameters() throws Exception {
		try {
			TestsUtil.getCollectionReader(
					CollectionReader.class, TestsUtil.getTypeSystem("org.cleartk.TypeSystem"));
			Assert.fail("expected error for invalid corpus directory");
		}
		catch (ResourceInitializationException e) {
		}

		try {
			TestsUtil.getCollectionReader(
					Ace2005GoldReader.class, TestsUtil.getTypeSystem("org.cleartk.TypeSystem"),
					Ace2005GoldReader.PARAM_ACE_CORPUS_DIR, "foo/bar");
			Assert.fail("expected error for invalid corpus directory");
		}
		catch (ResourceInitializationException e) {
		}
	}

	@Test
	public void testAnnotatorDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				"org.cleartk.corpus.ace2005.Ace2005GoldAnnotator");
		engine.collectionProcessComplete();
	}

	@Test
	public void testReaderDescriptor() throws UIMAException, IOException {
		try {
			TestsUtil.getCollectionReader("org.cleartk.corpus.ace2005.Ace2005GoldReader");
			Assert.fail("expected failure for no corpus directory specified");
		} catch (ResourceInitializationException e) {}

		CollectionReader reader = TestsUtil.getCollectionReader(
				"org.cleartk.corpus.ace2005.Ace2005GoldReader",
				Ace2005GoldReader.PARAM_ACE_CORPUS_DIR, this.rootDir.getPath());

		Object corpusDir = reader.getConfigParameterValue(
				Ace2005GoldReader.PARAM_ACE_CORPUS_DIR);
		Assert.assertEquals(this.rootDir.getPath(), corpusDir);

		Object loadNamedEntities = reader.getConfigParameterValue(
				Ace2005GoldReader.PARAM_ACE_FILE_NAMES);
		Assert.assertEquals(null, loadNamedEntities);
	}

}
