package org.cleartk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.junit.Test;
import org.uutuc.factory.JCasFactory;

public class DocumentAnnotationTest {

	@Test
	public void testExtents() throws UIMAException {
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		DocumentAnnotation documentAnnotation = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		assertNotNull(documentAnnotation);
		assertEquals(0, documentAnnotation.getBegin());
		assertEquals(0, documentAnnotation.getEnd());

		jCas.setDocumentText("what's all the hullabaloo?");
		
		assertEquals(0, documentAnnotation.getBegin());
		assertEquals(26, documentAnnotation.getEnd());

		JCas otherView = jCas.createView("otherView");
		documentAnnotation = (DocumentAnnotation) otherView.getDocumentAnnotationFs();

		assertNotNull(documentAnnotation);
		assertEquals(0, documentAnnotation.getBegin());
		assertEquals(0, documentAnnotation.getEnd());

		otherView.setDocumentText("I show you hullabaloo!");
		
		assertEquals(0, documentAnnotation.getBegin());
		assertEquals(22, documentAnnotation.getEnd());

	}
}
