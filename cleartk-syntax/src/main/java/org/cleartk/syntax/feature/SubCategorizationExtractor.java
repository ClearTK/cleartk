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
package org.cleartk.syntax.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philipp Wetzler
 */

public class SubCategorizationExtractor implements SimpleFeatureExtractor {
	String name;
	
	public SubCategorizationExtractor(String name) {
		this.name = name;
	}
	
	public SubCategorizationExtractor() {
		this.name = null;
	}
	
	public List<Feature> extract(JCas jCas, Annotation focusAnnotation) throws UnsupportedOperationException {
		if( !(focusAnnotation instanceof TreebankNode) )
			return new ArrayList<Feature>();
		
		TreebankNode parent = ((TreebankNode) focusAnnotation).getParent();
		if( parent == null )
			return new ArrayList<Feature>();
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(parent.getNodeType() + "->");
		boolean first = true;
		for( TreebankNode child : UIMAUtil.toList(parent.getChildren(), TreebankNode.class) ) {
			if( ! first )
				buffer.append("-");
			buffer.append(child.getNodeType());
			first = false;
		}
		
		String featureName = Feature.createName(name, "SubCategorization");
		return Collections.singletonList(new Feature(featureName, buffer));
	}
}
