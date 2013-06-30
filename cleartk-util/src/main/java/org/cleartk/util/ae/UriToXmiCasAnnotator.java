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

package org.cleartk.util.ae;

import java.io.InputStream;
import java.net.URI;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.component.ViewCreatorAnnotator;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * Reads contents of URI in URIView and deserializes it into the CAS.
 * 
 * @see XmiCasSerializer
 * @see XCASSerializer
 * 
 * @author Lee Becker
 * 
 */
public class UriToXmiCasAnnotator extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(UriToXmiCasAnnotator.class);
  }

  /**
   * Use this aggregate description if UriToDocumentTextAnnotator will be writing to a mapped view.
   */
  public static AnalysisEngineDescription getCreateViewAggregateDescription(String targetViewName)
      throws ResourceInitializationException {
    AggregateBuilder builder = new AggregateBuilder();
    builder.add(AnalysisEngineFactory.createPrimitiveDescription(
        ViewCreatorAnnotator.class,
        ViewCreatorAnnotator.PARAM_VIEW_NAME,
        targetViewName));
    builder.add(UriToXmiCasAnnotator.getDescription(), CAS.NAME_DEFAULT_SOFA, targetViewName);
    return builder.createAggregateDescription();
  }

  public static enum XmlScheme {
    XMI, XCAS
  }

  public static final String PARAM_XML_SCHEME = "xmlScheme";

  @ConfigurationParameter(
      name = PARAM_XML_SCHEME,
      defaultValue = "XMI",
      description = "specifies the UIMA XML serialization scheme that should be used. Valid values for this parameter are 'XMI' and 'XCAS'")
  private XmlScheme xmlScheme;

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    URI uri = ViewURIUtil.getURI(jCas);

    InputStream inputStream = null;
    try {
      inputStream = uri.toURL().openStream();
      switch (this.xmlScheme) {
        case XMI:
          XmiCasDeserializer.deserialize(inputStream, jCas.getCas());
          break;
        case XCAS:
          XCASDeserializer.deserialize(inputStream, jCas.getCas());
          break;
      }
      inputStream.close();
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

  }

}
