package org.cleartk.util;

import java.util.List;

import opennlp.tools.util.Span;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.type.ContiguousAnnotation;
import org.cleartk.type.SplitAnnotation;

public class AnnotationUtil2 {

	public static Span getAnnotationsExtent(List<? extends Annotation> annotations) {
		int start = Integer.MAX_VALUE;
		int end = 0;

		for (Annotation annotation : annotations) {
			if (annotation.getBegin() < start) start = annotation.getBegin();
			if (annotation.getEnd() > end) end = annotation.getEnd();
		}

		return new Span(start, end);
	}


	/**
	 * Determines whether the big annotation contains the small annotation
	 * w.r.t. to character offsets (begin and end).
	 * 
	 */
	public static boolean contains(Annotation bigAnnotation, Annotation smallAnnotation) {
		if (bigAnnotation instanceof SplitAnnotation) {
			FSArray splits = ((SplitAnnotation) bigAnnotation).getAnnotations();

			for (int i = 0; i < splits.size(); i++) {
				ContiguousAnnotation split = (ContiguousAnnotation) splits.get(i);
				if (contains(split, smallAnnotation)) return true;
			}
			return false;
		}

		if (bigAnnotation == null || smallAnnotation == null) return false;
		if (bigAnnotation.getBegin() <= smallAnnotation.getBegin()
				&& bigAnnotation.getEnd() >= smallAnnotation.getEnd()) return true;
		else return false;
	}

}
