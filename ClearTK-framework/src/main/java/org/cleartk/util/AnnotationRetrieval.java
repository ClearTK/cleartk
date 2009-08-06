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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.uima.cas.ConstraintFactory;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIntConstraint;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.cas.FSTypeConstraint;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philip Ogren
 * @author Philipp Wetzler
 * @author Steven Bethard
 */
public class AnnotationRetrieval {
	/**
	 * This method exists simply as a convenience method for unit testing. It is
	 * not very efficient and should not, in general be used outside the context
	 * of unit testing.
	 */
	public static <T extends Annotation> T get(JCas jCas, Class<T> cls, int index) {
		int type;
		try {
			type = (Integer) cls.getField("type").get(null);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException();
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException();
		}

		// TODO we should probably iterate from the end rather than
		// iterating forward from the begining.
		FSIndex fsIndex = jCas.getAnnotationIndex(type);
		if (index < 0) index = fsIndex.size() + index;

		if (index < 0 || index >= fsIndex.size()) return null;
		FSIterator iterator = fsIndex.iterator();
		Object returnValue = iterator.next();
		for (int i = 0; i < index; i++) {
			returnValue = iterator.next();
		}
		return cls.cast(returnValue);
	}

	public static <T extends Annotation> T get(JCas jCas, T annotation, int relativePosition) {
		return get(jCas, annotation, relativePosition, null);
	}

	public static <T extends Annotation> T get(JCas jCas, T annotation, int relativePosition,
			Annotation windowAnnotation) {
		FSIterator cursor = jCas.getAnnotationIndex(annotation.getType()).iterator();
		cursor.moveTo(annotation);

		if (relativePosition > 0) {
			for (int i = 0; i < relativePosition && cursor.isValid(); i++)
				cursor.moveToNext();
		}
		else {
			for (int i = 0; i < -relativePosition && cursor.isValid(); i++)
				cursor.moveToPrevious();
		}
		if (cursor.isValid()) {
			Annotation relativeAnnotation = (Annotation) cursor.get();

			T returnValue = ReflectionUtil.<Class<T>>uncheckedCast(annotation.getClass()).cast(relativeAnnotation);

			if (windowAnnotation != null) {
				if (AnnotationUtil.contains(windowAnnotation, relativeAnnotation)) return returnValue;
				else return null;
			}
			else return returnValue;
		}
		else return null;
	}

	public static <RETURN_TYPE extends Annotation> RETURN_TYPE get(JCas jCas, Annotation annotation, Class<RETURN_TYPE> returnCls, int relativePosition) {
		if(relativePosition == 0) {
			return getFirstAnnotation(jCas, annotation, returnCls);
		}
		else if(relativePosition < 0) {
			RETURN_TYPE returnValue = getAdjacentAnnotation(jCas, annotation, returnCls, true);
			if(returnValue == null)
				return null;
			if(relativePosition == -1)
				return returnValue;
			return get(jCas, returnValue, relativePosition + 1);
		} else {
			RETURN_TYPE returnValue = getAdjacentAnnotation(jCas, annotation, returnCls, false);
			if(returnValue == null)
				return null;
			if(relativePosition == 1)
				return returnValue;
			return get(jCas, returnValue, relativePosition - 1);
		} 
	}

	/**
	 * We initially used the AnnotationIndex.subiterator() but ran into issues
	 * that we wanted to work around concerning the use of type priorities when
	 * the window and windowed annotations have the same begin and end offsets.
	 * By using our own iterator we can back up to the first annotation that has
	 * the same beginning as the window annotation and avoid having to set type
	 * priorities.
	 * 
	 * @param jCas
	 * @param windowAnnotation
	 * @return an FSIterator that is at the correct position
	 */
	private static FSIterator initializeWindowCursor(JCas jCas, Annotation windowAnnotation) {

		FSIterator cursor = jCas.getAnnotationIndex().iterator();
		
		cursor.moveTo(windowAnnotation);
		
		// if cursor is invalid now we're past the end of the index
		if( !cursor.isValid() )
			return cursor;

		while (cursor.isValid() && ((Annotation) cursor.get()).getBegin() >= windowAnnotation.getBegin()) {
			cursor.moveToPrevious();
		}

		if (cursor.isValid()) {
			cursor.moveToNext();
		}
		else {
			cursor.moveToFirst();
		}

		return cursor;
	}

	/**
	 * @param <T>
	 *            determines the return type of the method
	 * @param jCas
	 *            the current jCas or view
	 * @param windowAnnotation
	 *            an annotation that defines a window
	 * @param cls
	 *            determines the return type of the method
	 * @return the last annotation of type cls that is "inside" the window
	 * @see #getAnnotations(JCas, Annotation, Class)
	 */
	public static <T extends Annotation> T getLastAnnotation(JCas jCas, Annotation windowAnnotation, Class<T> cls) {
		FSIterator cursor = initializeWindowCursor(jCas, windowAnnotation);

		T currentBestGuess = null;
		while (cursor.isValid() && ((Annotation) cursor.get()).getBegin() <= windowAnnotation.getEnd()) {
			Annotation annotation = (Annotation) cursor.get();

			if (cls.isInstance(annotation) && annotation.getEnd() <= windowAnnotation.getEnd()) currentBestGuess = cls
			.cast(annotation);
			cursor.moveToNext();
		}
		return currentBestGuess;
	}

	/**
	 * @param <T>
	 *            determines the return type of the method
	 * @param jCas
	 *            the current jCas or view
	 * @param windowAnnotation
	 *            an annotation that defines a window
	 * @param cls
	 *            determines the return type of the method
	 * @return the first annotation of type cls that is "inside" the window
	 * @see #getAnnotations(JCas, Annotation, Class)
	 */
	public static <T extends Annotation> T getFirstAnnotation(JCas jCas, Annotation windowAnnotation, Class<T> cls) {
		FSIterator cursor = initializeWindowCursor(jCas, windowAnnotation);

		// I left in the while loop because the first annotation we see might
		// not be the right class
		while (cursor.isValid() && ((Annotation) cursor.get()).getBegin() <= windowAnnotation.getEnd()) {
			Annotation annotation = (Annotation) cursor.get();
			if (cls.isInstance(annotation) && annotation.getEnd() <= windowAnnotation.getEnd()) return cls
			.cast(annotation);
			cursor.moveToNext();
		}
		return null;
	}
	
	/**
	 * @param <T>
	 *            determines the return type of the method
	 * @param view
	 *            the jCAS view
	 * @param cls
	 *            determines the return type of the method
	 * @return the first annotation of type cls in this view
	 */
	public static <T extends Annotation> T getFirstAnnotation(JCas view, Class<T> cls) {
		FSIterator cursor = view.getAnnotationIndex().iterator();
		cursor.moveToFirst();

		while( cursor.isValid() ) {
			Annotation annotation = (Annotation) cursor.get();
			if( cls.isInstance(annotation) ) return cls.cast(annotation);
			cursor.moveToNext();
		}
		return null;
	}

	/*
	 * Find an annotation of a different type that covers the same span.
	 * 
	 * @param <T> is the type of annotation we're looking for @param jCas is the
	 * current CAS view @param windowAnnotation determines the span of the
	 * annotation @return the first annotation in the index that has the same
	 * span as windowAnnotation
	 */
	public static <T extends Annotation> T getMatchingAnnotation(JCas jCas, Annotation windowAnnotation, Class<T> cls) {
		if (cls.isInstance(windowAnnotation)) return cls.cast(windowAnnotation);

		FSIterator cursor = jCas.getAnnotationIndex().iterator();

		cursor.moveTo(windowAnnotation);
		Annotation cursorAnnotation = (Annotation) cursor.get();
		if (cursorAnnotation.getBegin() != windowAnnotation.getBegin()
				|| cursorAnnotation.getEnd() != windowAnnotation.getEnd()) return null;

		while (cursor.isValid() && cursorAnnotation.getBegin() == windowAnnotation.getBegin()
				&& cursorAnnotation.getEnd() == windowAnnotation.getEnd()) {
			cursor.moveToPrevious();
			if (cursor.isValid()) cursorAnnotation = (Annotation) cursor.get();
			else cursorAnnotation = null;
		}

		if (cursor.isValid()) {
			cursor.moveToNext();
			cursorAnnotation = (Annotation) cursor.get();
		}
		else {
			cursor.moveToFirst();
			cursorAnnotation = (Annotation) cursor.get();
		}

		while (cursor.isValid() && cursorAnnotation.getBegin() == windowAnnotation.getBegin()
				&& cursorAnnotation.getEnd() == windowAnnotation.getEnd()) {
			if (cls.isInstance(cursorAnnotation)) return cls.cast(cursorAnnotation);
			cursor.moveToNext();
			if (cursor.isValid()) cursorAnnotation = (Annotation) cursor.get();
			else cursorAnnotation = null;
		}

		return null;
	}

	/**
	 * Returns a List of all annotations of a given type.
	 * 
	 * @param <T>
	 *            The type of Annotation being collected.
	 * @param jCas
	 *            The JCas from which annotations should be collected.
	 * @param annotationClass
	 *            The Annotation class.
	 * @return A List of all Annotations of the given type.
	 */
	public static <T extends Annotation> List<T> getAnnotations(JCas jCas, Class<T> annotationClass) {
		// get the integer type from the annotation class
		// not that T.type doesn't work because that gives us Annotation.type
		// (why?)
		int type;
		try {
			type = annotationClass.getField("type").getInt(null);
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

		// collect all annotations of the given type into a list
		AnnotationIndex index = jCas.getAnnotationIndex(type);
		List<T> annotations = new ArrayList<T>();
		for (FSIterator iter = index.iterator(); iter.hasNext();) {
			Object next = iter.next();
			annotations.add(annotationClass.cast(next));
		}
		return annotations;
	}

	/**
	 * This method returns all annotations (in order) that are inside a "window"
	 * annotation of a particular kind. The functionality provided is similar to
	 * using AnnotationIndex.subiterator(), however we found the use of type
	 * priorities which is documented in detail in their javadocs to be
	 * distasteful. This method does not require that type priorities be set in
	 * order to work as expected for the condition where the window annotation
	 * and the "windowed" annotation have the same size and location.
	 * 
	 * @param <T>
	 *            determines the return type of the method
	 * @param jCas
	 *            the current jCas or view
	 * @param windowAnnotation
	 *            an annotation that defines a window
	 * @param cls
	 *            determines the return type of the method
	 * @return a list of annotations of type cls that are "inside" the window
	 * @see AnnotationIndex#subiterator(org.apache.uima.cas.text.AnnotationFS)
	 */
	public static <T extends Annotation> List<T> getAnnotations(JCas jCas, Annotation windowAnnotation, Class<T> cls) {
//		if (windowAnnotation instanceof SplitAnnotation) return getAnnotations(jCas,
//				(SplitAnnotation) windowAnnotation, cls);

		FSIterator cursor = initializeWindowCursor(jCas, windowAnnotation);

		List<T> annotations = new ArrayList<T>();
		while (cursor.isValid() && ((Annotation) cursor.get()).getBegin() <= windowAnnotation.getEnd()) {
			Annotation annotation = (Annotation) cursor.get();

			if (cls.isInstance(annotation) && annotation.getEnd() <= windowAnnotation.getEnd()) annotations.add(cls
					.cast(annotation));

			cursor.moveToNext();
		}
		return annotations;
	}

	public static <T extends Annotation> List<T> getAnnotations(JCas jCas, Annotation windowAnnotation, Class<T> cls,
			boolean exactSpan) {
//		if (windowAnnotation instanceof SplitAnnotation) return getAnnotations(jCas,
//				(SplitAnnotation) windowAnnotation, cls);

		if (!exactSpan) return getAnnotations(jCas, windowAnnotation, cls);
		else {
			FSIterator cursor = initializeWindowCursor(jCas, windowAnnotation);

			List<T> annotations = new ArrayList<T>();
			while (cursor.isValid() && ((Annotation) cursor.get()).getBegin() <= windowAnnotation.getEnd()) {
				Annotation annotation = (Annotation) cursor.get();

				if (cls.isInstance(annotation) && annotation.getBegin() == windowAnnotation.getBegin()
						&& annotation.getEnd() == windowAnnotation.getEnd()) annotations.add(cls.cast(annotation));

				cursor.moveToNext();
			}
			return annotations;
		}
	}

	public static <T extends Annotation> List<T> getAnnotations(JCas jCas, int begin, int end, Class<T> cls,
			boolean exactSpan) {
		if (begin > end) return null;
		if (!exactSpan) {
			return getAnnotations(jCas, begin, end, cls);
		}
		else {
			return getAnnotations(jCas, new Annotation(jCas, begin, end), cls, exactSpan);
		}

	}

//	public static <T extends Annotation> List<T> getAnnotations(JCas jCas, SplitAnnotation splitAnnotation, Class<T> cls) {
//		List<T> returnValues = new ArrayList<T>();
//
//		for (FeatureStructure subAnnotation : splitAnnotation.getAnnotations().toArray()) {
//			returnValues.addAll(getAnnotations(jCas, (Annotation) subAnnotation, cls));
//		}
//
//		return returnValues;
//	}

	public static <T extends Annotation> List<T> getAnnotations(JCas jCas, int begin, int end, Class<T> cls) {
		if(begin > end)
			return null;
		Annotation annotation = new Annotation(jCas, begin, end);
		return getAnnotations(jCas, annotation, cls);
	}

	/**
	 * This method provides a way to have multiple annotations define the window
	 * that is used to constrain the annotations in the returned list. The
	 * intersection of the windows is determined and used. This method will
	 * treat any split annotations in the list as a contiguous annotation.
	 * 
	 * @see #getAnnotations(JCas, Annotation, Class)
	 * @see #getAnnotations(JCas, SplitAnnotation, Class)
	 */
	public static <T extends Annotation> List<T> getAnnotations(JCas jCas, List<Annotation> windowAnnotations,
			Class<T> cls) {
		if (windowAnnotations == null || windowAnnotations.size() == 0) return null;

		int windowBegin = Integer.MIN_VALUE;
		int windowEnd = Integer.MAX_VALUE;
		for (Annotation windowAnnotation : windowAnnotations) {
			if (windowAnnotation.getBegin() > windowBegin) windowBegin = windowAnnotation.getBegin();
			if (windowAnnotation.getEnd() < windowEnd) windowEnd = windowAnnotation.getEnd();
		}
		return getAnnotations(jCas, windowBegin, windowEnd, cls);
	}

	// TODO think about how to make this method faster by avoiding using a
	// constrained iterator. See TODO note for
	// get adjacent annotation.
	/**
	 * returns getContainingAnnotation(jCas, focusAnnotation, cls, false)
	 */
	public static <T extends Annotation> T getContainingAnnotation(JCas jCas, Annotation focusAnnotation, Class<T> cls) {
		return getContainingAnnotation(jCas, focusAnnotation, cls, false);
	}

	/**
	 * This method finds the smallest annotation of type cls that contains the
	 * focus annotation.
	 * 
	 * @param jCas
	 * @param focusAnnotation
	 *            an annotation that will be contained by the returned value
	 * @param cls
	 *            determines the type of the returned annotation
	 * @param exclusiveContain
	 *            if true, then the method will only return an annotation if it
	 *            is larger than the focus annotation. If false, then an
	 *            annotation of the same size can be returned if one exists of
	 *            the type specified by the cls param. This may be undesirable
	 *            if the focus annotation is the same type as the cls param
	 *            because it will return the focus annotation when you might
	 *            actually want the next biggest annotation that contains the
	 *            focus annotation.
	 * @return an annotation of type cls that contains the focus annotation or
	 *         null it one does not exist.
	 */

	public static <T extends Annotation> T getContainingAnnotation(JCas jCas, Annotation focusAnnotation, Class<T> cls,
			boolean exclusiveContain) {

		if(focusAnnotation == null)
			throw new IllegalArgumentException("focus annotation may not be null");


		FSIterator cursor = jCas.getAnnotationIndex().iterator();

		//the goal is to move the cursor to the last annotation that begins at the same index as the focus annotation 
		cursor.moveTo(focusAnnotation);
		while (cursor.isValid()) {
			cursor.moveToNext();

			//if we have moved past the last element, then move cursor to last element and be done
			if(!cursor.isValid()) {
				cursor.moveToLast();
				break;
			}

			//if we have moved to an annotation that does not be begin at the same index as the focus annotation,
			//then move to previous.  
			Annotation nextAnnotation = (Annotation) cursor.get();
			if(nextAnnotation.getBegin() != focusAnnotation.getBegin()) {
				cursor.moveToPrevious();
				break;
			}
		}


		while (cursor.isValid()) {
			Annotation annotation = (Annotation) cursor.get();
			if (exclusiveContain) {
				if (cls.isInstance(annotation) && annotation.getBegin() <= focusAnnotation.getBegin()
						&& annotation.getEnd() >= focusAnnotation.getEnd()
						&& AnnotationUtil.size(annotation) > AnnotationUtil.size(focusAnnotation)) {
					return cls.cast(annotation);
				}
			}
			else {
				if (cls.isInstance(annotation) && annotation.getBegin() <= focusAnnotation.getBegin()
						&& annotation.getEnd() >= focusAnnotation.getEnd()) {
					return cls.cast(annotation);
				}
			}
			cursor.moveToPrevious();
		}
		return null;

	}

	public static <T extends Annotation> List<T> getOverlappingAnnotations(JCas jCas, Annotation focusAnnotation, Class<T> cls, boolean before) {
		List<T> annotations = new ArrayList<T>();

		ConstraintFactory constraintFactory = jCas.getConstraintFactory();

		FSTypeConstraint typeConstraint = constraintFactory.createTypeConstraint();
		Type type = UIMAUtil.getCasType(jCas, cls);
		typeConstraint.add(type);
		
		
		FSMatchConstraint overlapConstraint; 
		if(before)
			overlapConstraint = getOverlapBeforeConstraint(jCas, focusAnnotation, constraintFactory);
		else
			overlapConstraint = getOverlapAfterConstraint(jCas, focusAnnotation, constraintFactory);

		FSIterator iterator = jCas.createFilteredIterator(jCas.getAnnotationIndex(type).iterator(), overlapConstraint);
		while(iterator.hasNext()) {
			T annotation = cls.cast(iterator.next()); 
			annotations.add(annotation);
		}
		
		return annotations;
	}

	private static FSMatchConstraint getOverlapBeforeConstraint(JCas jCas, Annotation focusAnnotation, ConstraintFactory constraintFactory) {

		FSIntConstraint beginIntConstraint = constraintFactory.createIntConstraint();
		beginIntConstraint.lt(focusAnnotation.getBegin());
		FeaturePath beginPath = jCas.createFeaturePath();
		Feature beginFeature = jCas.getTypeSystem().getFeatureByFullName("uima.tcas.Annotation:begin");
		beginPath.addFeature(beginFeature);
		FSMatchConstraint beginConstraint = constraintFactory.embedConstraint(beginPath, beginIntConstraint);
		
		FSIntConstraint endIntConstraint = constraintFactory.createIntConstraint();
		endIntConstraint.gt(focusAnnotation.getBegin());
		endIntConstraint.leq(focusAnnotation.getEnd());
		FeaturePath endPath = jCas.createFeaturePath();
		Feature endFeature = jCas.getTypeSystem().getFeatureByFullName("uima.tcas.Annotation:end");
		endPath.addFeature(endFeature);
		FSMatchConstraint endConstraint = constraintFactory.embedConstraint(endPath, endIntConstraint);

		return constraintFactory.and(beginConstraint, endConstraint);
	}

	private static FSMatchConstraint getOverlapAfterConstraint(JCas jCas, Annotation focusAnnotation, ConstraintFactory constraintFactory) {

		FSIntConstraint beginIntConstraint = constraintFactory.createIntConstraint();
		beginIntConstraint.geq(focusAnnotation.getBegin());
		beginIntConstraint.lt(focusAnnotation.getEnd());
		
		FeaturePath beginPath = jCas.createFeaturePath();
		Feature beginFeature = jCas.getTypeSystem().getFeatureByFullName("uima.tcas.Annotation:begin");
		beginPath.addFeature(beginFeature);
		FSMatchConstraint beginConstraint = constraintFactory.embedConstraint(beginPath, beginIntConstraint);
		
		FSIntConstraint endIntConstraint = constraintFactory.createIntConstraint();
		endIntConstraint.gt(focusAnnotation.getEnd());
		FeaturePath endPath = jCas.createFeaturePath();
		Feature endFeature = jCas.getTypeSystem().getFeatureByFullName("uima.tcas.Annotation:end");
		endPath.addFeature(endFeature);
		FSMatchConstraint endConstraint = constraintFactory.embedConstraint(endPath, endIntConstraint);

		return constraintFactory.and(beginConstraint, endConstraint);
	}

	/**
	 * Finds and returns the annotation of the provided type that is adjacent to
	 * the focus annotation in either to the left or right. Adjacent simply
	 * means the last annotation of the passed in type that ends before the
	 * start of the focus annotation (for the left side) or the first annotation
	 * that starts after the end of the focus annotation (for the right side).
	 * Thus, adacent refers to order of annotations at the annotation level
	 * (e.g. give me the first annotation of type x to the left of the focus
	 * annotation) and does not mean that the annotations are adjacenct at the
	 * character offset level.
	 * 
	 * <br>
	 * <b>note:</b> This method runs <b>much</b> faster if the type of the
	 * passed in annotation and the passed in type are the same.
	 * 
	 * @param jCas
	 * @param focusAnnotation
	 *            an annotation that you want to find an annotation adjacent to
	 *            this.
	 * @param adjacentClass
	 *            the type of annotation that you want to find
	 * @param adjacentBefore
	 *            if true then returns an annotation to the left of the passed
	 *            in annotation, otherwise an annotation to the right will be
	 *            returned.
	 * @return an annotation of type adjacentType or null
	 */
	public static <T extends Annotation> T getAdjacentAnnotation(JCas jCas, Annotation focusAnnotation,
			Class<T> adjacentClass, boolean adjacentBefore) {
		try {
			Type adjacentType = UIMAUtil.getCasType(jCas, adjacentClass);
			if (focusAnnotation.getType().equals(adjacentType)) {
				FSIterator iterator = jCas.getAnnotationIndex(adjacentType).iterator();
				iterator.moveTo(focusAnnotation);
				if (adjacentBefore) iterator.moveToPrevious();
				else iterator.moveToNext();
				return adjacentClass.cast(iterator.get());
			}
			else {
				FSIterator cursor = jCas.getAnnotationIndex().iterator();
				cursor.moveTo(focusAnnotation);
				if (adjacentBefore) {
					while (cursor.isValid()) {
						cursor.moveToPrevious();
						Annotation annotation = (Annotation) cursor.get();
						if (adjacentClass.isInstance(annotation) && annotation.getEnd() <= focusAnnotation.getBegin()) return adjacentClass
						.cast(annotation);
					}
				}
				else {
					while (cursor.isValid()) {
						cursor.moveToNext();
						Annotation annotation = (Annotation) cursor.get();
						if (adjacentClass.isInstance(annotation) && annotation.getBegin() >= focusAnnotation.getEnd()) return adjacentClass
						.cast(annotation);
					}
				}
			}
		}
		catch (NoSuchElementException nsee) {
			return null;
		}
		return null;
	}

	public static <T extends Annotation> AnnotationIndex getAnnotationIndex(JCas jCas, Class<T> cls) {
		return jCas.getAnnotationIndex(UIMAUtil.getCasType(jCas, cls));
	}

	/**
	 * Get the DocumentAnnotation for this JCas. 
	 * @param jCas The JCas for the document.
	 * @return     The DocumentAnnotation.
	 */
	public static DocumentAnnotation getDocument(JCas jCas)
	{
		return (DocumentAnnotation)jCas.getDocumentAnnotationFs();
	}

}

