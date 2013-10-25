/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.examples.documentclassification.advanced;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.cleartk.examples.type.UsenetDocument;
import org.cleartk.util.ViewUriUtil;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * This class will assign the gold-standard document categories for the documents in this example by
 * looking at their parent directory.
 * 
 * @author Lee Becker
 * 
 */
public class GoldDocumentCategoryAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      JCas uriView = jCas.getView(ViewUriUtil.URI);
      URI uri = new URI(uriView.getSofaDataURI());
      File file = new File(uri.getPath());

      // Create UsenetDocument annotation
      // The document category will come from the document directory structure
      UsenetDocument document = new UsenetDocument(jCas, 0, jCas.getDocumentText().length());
      document.setCategory(file.getParentFile().getName());
      document.addToIndexes();

    } catch (CASException e) {
      throw new AnalysisEngineProcessException(e);
    } catch (URISyntaxException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

}
