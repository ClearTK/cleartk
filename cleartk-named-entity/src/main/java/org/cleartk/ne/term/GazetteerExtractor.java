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
package org.cleartk.ne.term;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.ne.type.GazetteerNamedEntityMention;
import org.cleartk.util.AnnotationUtil;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philip Ogren
 * 
 */

public class GazetteerExtractor {
  private Set<String> gazetteerNames;

  public GazetteerExtractor(List<String> gazetteerNames) {
    this.gazetteerNames = new HashSet<String>();
    for (String gazetteerName : gazetteerNames) {
      this.gazetteerNames.add(gazetteerName);
    }
  }

  public List<Feature> extract(JCas jCas, Annotation focusAnnotation, Annotation windowAnnotation)
      throws UnsupportedOperationException {
    List<Feature> returnValues = new ArrayList<Feature>();
    List<GazetteerNamedEntityMention> gnems = JCasUtil.selectCovered(
        jCas,
        GazetteerNamedEntityMention.class,
        windowAnnotation);
    for (GazetteerNamedEntityMention gnem : gnems) {
      if (gazetteerNames.contains(gnem.getMentionedEntity().getEntityType())
          && AnnotationUtil.contains(gnem, focusAnnotation)) {
        String entityType = gnem.getMentionedEntity().getEntityType();
        Feature feature = new Feature("Gazetteer", entityType);
        returnValues.add(feature);
      }
    }
    return returnValues;
  }

}
