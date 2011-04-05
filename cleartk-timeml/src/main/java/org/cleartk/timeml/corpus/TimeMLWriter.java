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
package org.cleartk.timeml.corpus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.cleartk.timeml.TimeMLComponents;
import org.cleartk.timeml.util.TimeMLUtil;
import org.cleartk.util.ViewURIUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Steven Bethard
 * 
 */
public class TimeMLWriter extends JCasAnnotator_ImplBase {

  public static final String PARAM_OUTPUT_DIRECTORY_NAME = ConfigurationParameterFactory
      .createConfigurationParameterName(TimeMLWriter.class, "outputDirectoryName");

  @ConfigurationParameter(description = "Provides the path where the TimeML documents should be written.", mandatory = true)
  private String outputDirectoryName;

  public static AnalysisEngineDescription getDescription(String outputDir)
      throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        TimeMLWriter.class,
        TimeMLComponents.TYPE_SYSTEM_DESCRIPTION,
        PARAM_OUTPUT_DIRECTORY_NAME,
        outputDir);
  }

  private File outputDirectory;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    this.outputDirectory = new File(outputDirectoryName);
    if (!this.outputDirectory.exists()) {
      this.outputDirectory.mkdirs();
    }
  }

  public static String toTimeML(JCas jCas) throws AnalysisEngineProcessException {
    try {
      return toXML(jCas.getCas(), new TimeMLAnnotationsToElements());
    } catch (SAXException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    String xmlString = toTimeML(jCas);

    // write the TimeML to the output file
    String filePath = ViewURIUtil.getURI(jCas).getPath();
    String fileName = new File(filePath).getName();
    if (!fileName.endsWith(".tml")) {
      fileName += ".tml";
    }
    File outputFile = new File(this.outputDirectory, fileName);
    try {
      FileWriter writer = new FileWriter(outputFile);
      writer.write(xmlString);
      writer.close();
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  public void setOutputDirectoryName(String outputDirectoryName) {
    this.outputDirectoryName = outputDirectoryName;
  }

  private static interface AnnotationsToElements {
    public void startRootElement(ContentHandler handler) throws SAXException;

    public void endRootElement(ContentHandler handler) throws SAXException;

    public void startAnnotationElement(AnnotationFS annotation, ContentHandler handler)
        throws SAXException;

    public void endAnnotationElement(AnnotationFS annotation, ContentHandler handler)
        throws SAXException;
  }

  private static class TimeMLAnnotationsToElements implements AnnotationsToElements {

    public TimeMLAnnotationsToElements() {
    }

    @Override
    public void startRootElement(ContentHandler handler) throws SAXException {
      handler.startElement("", "TimeML", "TimeML", new AttributesImpl());
    }

    @Override
    public void endRootElement(ContentHandler handler) throws SAXException {
      handler.endElement("", "TimeML", "TimeML");
    }

    @Override
    public void startAnnotationElement(AnnotationFS annotation, ContentHandler handler)
        throws SAXException {
      String name = TimeMLUtil.toTimeMLElementName(annotation);
      if (name != null) {
        handler.startElement("", name, name, TimeMLUtil.toTimeMLAttributes(annotation, name));
      }
    }

    @Override
    public void endAnnotationElement(AnnotationFS annotation, ContentHandler handler)
        throws SAXException {
      String name = TimeMLUtil.toTimeMLElementName(annotation);
      if (name != null) {
        handler.endElement("", name, name);
      }
    }
  }

  /**
   * Copied and modified from {@link org.apache.uima.util.CasToInlineXml}
   */
  private static String toXML(CAS cas, AnnotationsToElements converter) throws SAXException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    XMLSerializer sax2xml = new XMLSerializer(byteArrayOutputStream, false);

    // get document text
    String docText = cas.getDocumentText();
    char[] docCharArray = docText.toCharArray();

    // get iterator over annotations sorted by increasing start position and
    // decreasing end position
    FSIterator<AnnotationFS> iterator = cas.getAnnotationIndex().iterator();

    // This is basically a recursive algorithm that has had the recursion
    // removed through the use of an explicit Stack. We iterate over the
    // annotations, and if an annotation contains other annotations, we
    // push the parent annotation on the stack, process the children, and
    // then come back to the parent later.
    List<AnnotationFS> stack = new ArrayList<AnnotationFS>();
    int pos = 0;

    ContentHandler handler = sax2xml.getContentHandler();
    handler.startDocument();
    // write the start tag
    converter.startRootElement(handler);
    // now use null is a placeholder for this artificial Document annotation
    AnnotationFS curAnnot = null;

    while (iterator.isValid()) {
      AnnotationFS nextAnnot = iterator.get();

      if (curAnnot == null || nextAnnot.getBegin() < curAnnot.getEnd()) {
        // nextAnnot's start point is within the span of curAnnot
        if (curAnnot == null || nextAnnot.getEnd() <= curAnnot.getEnd()) // crossover span check
        {
          // nextAnnot is contained within curAnnot

          // write text between current pos and beginning of nextAnnot
          try {
            handler.characters(docCharArray, pos, nextAnnot.getBegin() - pos);
            pos = nextAnnot.getBegin();
            converter.startAnnotationElement(nextAnnot, handler);

            // push parent annotation on stack
            stack.add(curAnnot);
            // move on to next annotation
            curAnnot = nextAnnot;
          } catch (StringIndexOutOfBoundsException e) {
            System.err.println("Invalid annotation range: " + nextAnnot.getBegin() + ","
                + nextAnnot.getEnd() + " in document of length " + docText.length());
          }
        }
        iterator.moveToNext();
      } else {
        // nextAnnot begins after curAnnot ends
        // write text between current pos and end of curAnnot
        try {
          handler.characters(docCharArray, pos, curAnnot.getEnd() - pos);
          pos = curAnnot.getEnd();
        } catch (StringIndexOutOfBoundsException e) {
          System.err.println("Invalid annotation range: " + curAnnot.getBegin() + ","
              + curAnnot.getEnd() + " in document of length " + docText.length());
        }
        converter.endAnnotationElement(curAnnot, handler);

        // pop next containing annotation off stack
        curAnnot = stack.remove(stack.size() - 1);
      }
    }

    // finished writing all start tags, now finish up
    if (curAnnot != null) {
      try {
        handler.characters(docCharArray, pos, curAnnot.getEnd() - pos);
        pos = curAnnot.getEnd();
      } catch (StringIndexOutOfBoundsException e) {
        System.err.println("Invalid annotation range: " + curAnnot.getBegin() + ","
            + curAnnot.getEnd() + "in document of length " + docText.length());
      }
      converter.endAnnotationElement(curAnnot, handler);

      while (!stack.isEmpty()) {
        curAnnot = stack.remove(stack.size() - 1); // pop
        if (curAnnot == null) {
          break;
        }
        try {
          handler.characters(docCharArray, pos, curAnnot.getEnd() - pos);
          pos = curAnnot.getEnd();
        } catch (StringIndexOutOfBoundsException e) {
          System.err.println("Invalid annotation range: " + curAnnot.getBegin() + ","
              + curAnnot.getEnd() + "in document of length " + docText.length());
        }
        converter.endAnnotationElement(curAnnot, handler);
      }
    }

    if (pos < docCharArray.length) {
      handler.characters(docCharArray, pos, docCharArray.length - pos);
    }
    converter.endRootElement(handler);
    handler.endDocument();

    // return XML string
    return new String(byteArrayOutputStream.toByteArray());
  }
}
