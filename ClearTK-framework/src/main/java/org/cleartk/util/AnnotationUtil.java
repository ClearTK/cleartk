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
package org.cleartk.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;


/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */
public class AnnotationUtil {

	public static boolean contains(Annotation bigAnnotation, Annotation smallAnnotation) {
		if (bigAnnotation == null || smallAnnotation == null) return false;
		if (bigAnnotation.getBegin() <= smallAnnotation.getBegin()
				&& bigAnnotation.getEnd() >= smallAnnotation.getEnd()) return true;
		else return false;
	}

	public static boolean overlaps(Annotation annotation1, Annotation annotation2) {
		Annotation firstAnnotation, secondAnnotation;

		if (annotation1.getBegin() == annotation2.getBegin()) return true;

		if (annotation1.getBegin() < annotation2.getBegin()) {
			firstAnnotation = annotation1;
			secondAnnotation = annotation2;
		}
		else {
			firstAnnotation = annotation2;
			secondAnnotation = annotation1;
		}

		if (firstAnnotation.getEnd() > secondAnnotation.getBegin()) return true;
		return false;

	}

	public static int size(Annotation annotation) {
		try {
			return annotation.getEnd() - annotation.getBegin();
		}
		catch (Exception e) {
			return 0;
		}
	}

	/**
	 * This method provides a way of getting some text before or after an
	 * annotation specified by some "window" of "tokens". For example, you could
	 * retrieve the text corresponding to 5 tokens to the right of a named
	 * entity. This may be useful for e.g. creating a report that shows the
	 * output of an annotator such that surrounding text is included.
	 * 
	 * @param <TOKEN_TYPE>
	 * @param jCas
	 * @param annotation
	 *            an annotation to get surrounding/nearby text from. This can be
	 *            any kind of annotation.
	 * @param tokenClass
	 *            the kind of "tokens" to use. This could be any kind of
	 *            annotation type as well.
	 * @param numberOfTokens
	 *            the number of tokens to consider when creating a span of text
	 * @param before
	 *            determines whether to return text occuring before the
	 *            annotation or after
	 * @return a span of text that occurs before or after the annotation which
	 *         will either end at the beginning of the annotation or begin at
	 *         the end of the annotation. The other edge of the span is
	 *         determined by the start/end location of the "token" found on
	 *         either side of the annotation. If such a "token" does not exist
	 *         then the other edge of the span will be either end of the
	 *         document text.
	 */
	public static <TOKEN_TYPE extends Annotation> String getSurroundingText(JCas jCas, Annotation annotation,
			Class<TOKEN_TYPE> tokenClass, int numberOfTokens, boolean before) {

		if (numberOfTokens < 1) throw new IllegalArgumentException(
				"numberOfTokens must be greater than zero.  Actual values is: " + numberOfTokens);

		String documentText = jCas.getDocumentText();

		int start;
		int end;

		if (before) {
			start = 0;
			end = annotation.getBegin();
			Annotation startToken = AnnotationRetrieval.get(jCas, annotation, tokenClass, -numberOfTokens);
			if (startToken != null) start = startToken.getBegin();
		}
		else {
			start = annotation.getEnd();
			end = documentText.length();
			Annotation endToken = AnnotationRetrieval.get(jCas, annotation, tokenClass, numberOfTokens);
			if (endToken != null) end = endToken.getEnd();
		}

		return documentText.substring(start, end);
	}

	public static <T extends Annotation> void sort(List<T> annotations) {

		Collections.sort(annotations, new Comparator<T>() {

			public int compare(T o1, T o2) {
				if (o1.getBegin() != o2.getBegin()) return o1.getBegin() < o2.getBegin() ? -1 : 1;
				else if (o1.getEnd() != o2.getEnd()) return o1.getEnd() < o2.getEnd() ? -1 : 1;
				return 0;
			}
		});
	}
}
