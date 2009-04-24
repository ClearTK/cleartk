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
package org.cleartk.token.chunk;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.chunk.DefaultChunkLabeler;
import org.cleartk.util.AnnotationRetrieval;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 */
public class ChunkTokenizerLabeler extends DefaultChunkLabeler {

	/**
	 * In general, the chunks will be annotations of type Token and the labeled
	 * annotations will be annotations of type Subtoken creating labels such as
	 * B-TOKEN or I-TOKEN. I had to override chunk2Labels for the chunker-style
	 * tokenization because there are cases in the GENIA data where the "Token"
	 * has no Subtokens within it because they split a word like "heterodimer"
	 * into two tokens and the Subtokenizer only identifies one Subtoken from
	 * that word. So, when the Token corresponds to "hetero" it will label the
	 * Subtoken that contains it as "B-TOKEN". When the next Token corresponding
	 * to "dimer" is visited the same Subtoken that contains it will be
	 * relabeled as "B-TOKEN".
	 */
	public void chunks2Labels(JCas jCas) throws AnalysisEngineProcessException {
		if (!typesInitialized) initializeTypes(jCas);

		FSIterator chunkAnnotations = jCas.getAnnotationIndex(chunkAnnotationType).iterator();
		while (chunkAnnotations.hasNext()) {
			Annotation chunkAnnotation = (Annotation) chunkAnnotations.next();
			String label = getChunkLabel(jCas, chunkAnnotation);

			List<? extends Annotation> labeledAnnotations = AnnotationRetrieval.getAnnotations(jCas, chunkAnnotation,
					labeledAnnotationClass);

			if (labeledAnnotations.size() == 0) {
				List<Annotation> anns = new ArrayList<Annotation>();
				Annotation labeledAnnotation = AnnotationRetrieval.getContainingAnnotation(jCas, chunkAnnotation,
						labeledAnnotationClass);
				anns.add(labeledAnnotation);
				labeledAnnotations = anns;
			}

			boolean begin = true;
			for (Annotation labelAnnotation : labeledAnnotations) {
				String fullLabel = begin ? BEGIN_PREFIX + SEPARATOR + label : INSIDE_PREFIX + SEPARATOR + label;
				begin = false;
				annotationLabels.put(labelAnnotation, fullLabel);
			}
		}
	}

}
