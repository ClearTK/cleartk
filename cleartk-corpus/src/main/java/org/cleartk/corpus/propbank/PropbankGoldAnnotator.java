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
package org.cleartk.corpus.propbank;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.propbank.util.Propbank;
import org.cleartk.syntax.constituent.type.TopTreebankNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.util.AnnotationUtil;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.fit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * 
 * 
 * <p>
 * The PropbankParser AnalysisEngine annotates the "_InitialView" SOFA from the "PropbankView" SOFA.
 * Treebank annotations must already exist.
 * </p>
 * 
 * @author Philipp Wetzler, Philip Ogren
 */

@SofaCapability(inputSofas = { PropbankConstants.PROPBANK_VIEW, CAS.NAME_DEFAULT_SOFA })
public class PropbankGoldAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      JCas pbView = jCas.getView(PropbankConstants.PROPBANK_VIEW);
      JCas docView = jCas.getView(CAS.NAME_DEFAULT_SOFA);
      List<Sentence> sentenceList = new ArrayList<Sentence>(
          JCasUtil.select(docView, Sentence.class));

      for (String propbankDatum : pbView.getDocumentText().split("\n")) {
        if (propbankDatum.length() == 0)
          continue;
        Propbank propbank = Propbank.fromString(propbankDatum);
        Sentence sentence = sentenceList.get(propbank.getSentenceNumber());
        TopTreebankNode top = AnnotationUtil.selectFirstMatching(
            docView,
            TopTreebankNode.class,
            sentence);
        if (top == null) {
          throw new IllegalArgumentException(String.format(
              "%s missing for %s", TopTreebankNode.class.getName(), sentence));
        }
        propbank.convert(docView, top, sentence);
      }
    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

}
