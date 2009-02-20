package org.cleartk.corpus.ace2005;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;

public class Ace2005GoldTests {

	private final File rootDir = new File("test/data/corpus/ace2005");

	@Test
	public void testReaderInvalidParameters() throws Exception {
		try {
			CollectionReaderFactory.createCollectionReader(
					CollectionReader.class, TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"));
			Assert.fail("expected error for invalid corpus directory");
		}
		catch (ResourceInitializationException e) {
		}

		try {
			CollectionReaderFactory.createCollectionReader(
					Ace2005GoldReader.class, TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TypeSystem"),
					Ace2005GoldReader.PARAM_ACE_CORPUS_DIR, "foo/bar");
			Assert.fail("expected error for invalid corpus directory");
		}
		catch (ResourceInitializationException e) {
		}
	}

	@Test
	public void testAnnotatorDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(
				"org.cleartk.corpus.ace2005.Ace2005GoldAnnotator");
		engine.collectionProcessComplete();
	}

	@Test
	public void testReaderDescriptor() throws UIMAException, IOException {
		try {
			CollectionReaderFactory.createCollectionReader("org.cleartk.corpus.ace2005.Ace2005GoldReader");
			Assert.fail("expected failure for no corpus directory specified");
		} catch (ResourceInitializationException e) {}

		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
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
