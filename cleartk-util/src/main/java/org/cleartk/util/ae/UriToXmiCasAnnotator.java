package org.cleartk.util.ae;

import java.io.InputStream;
import java.net.URI;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

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

  public static enum XmlScheme {
    XMI, XCAS
  }

  public static final String PARAM_XML_SCHEME = ConfigurationParameterFactory.createConfigurationParameterName(
      UriToXmiCasAnnotator.class,
      "xmlScheme");

  @ConfigurationParameter(
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
