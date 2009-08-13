/** 
  * Copyright (c) 2007-2009, Regents of the University of Colorado 
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

import java.util.List;

import opennlp.tools.util.Span;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * <br>
 * Copyright (c) 2007-2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 */

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

		if (bigAnnotation == null || smallAnnotation == null) return false;
		if (bigAnnotation.getBegin() <= smallAnnotation.getBegin()
				&& bigAnnotation.getEnd() >= smallAnnotation.getEnd()) return true;
		else return false;
	}

}
