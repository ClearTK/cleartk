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
package org.cleartk.token.pos.genia.util;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * 
 * This class parses the file GENIAcorpus3.02.pos.xml which provides sentence, word, and
 * part-of-speech data. This parser maintains the whitespace found in the xml file so that the text
 * added to the CAS does not come out as:
 * <p>
 * <code>"... of anti- Ro(SSA) antibodies . A pair of restriction "</code>
 * <p>
 * but instead comes out as:
 * <p>
 * <code>"... of anti-Ro(SSA) antibodies.  A pair of restriction "</code>
 * <p>
 * 
 * There is no white space provided between sentences provided by the genia corpus. So, this parser
 * simply adds in two spaces between each sentence. It also adds two newlines between the title and
 * the body of the abstract.
 * <p>
 * The parses returned by this parser will not have any named entities - i.e. there will be now
 * values returned from GeniaParse.getSemTags().
 * 
 * <p>
 * About 4000 word (w) tags have a part-of-speech assignment "*" which I refer to as the wildcard
 * part-of-speech tag. An example is:
 * 
 * <pre>
 * 	&lt;w c=&quot;*&quot;&gt;Ras&lt;/w&gt;&lt;w c=&quot;NN&quot;&gt;/protein&lt;/w&gt;
 * </pre>
 * 
 * The above tags are parsed as a single token Ras/protein with the tag "NN".
 * 
 * @author Philip V. Ogren
 */

public class GeniaPOSParser implements Iterator<GeniaParse> {

  Element root;

  Iterator<?> articles;

  Set<String> posLabels;

  XMLOutputter outputter;

  public GeniaPOSParser(File xmlFile) throws IOException, JDOMException {
    this();
    SAXBuilder builder = new SAXBuilder();
    builder.setDTDHandler(null);
    root = builder.build(xmlFile).getRootElement();
    articles = root.getChildren("article").iterator();
    outputter = new XMLOutputter();
  }

  public GeniaPOSParser() {
    posLabels = new HashSet<String>();
  }

  public boolean hasNext() {
    return articles.hasNext();
  }

  public GeniaParse next() {
    return parse((Element) articles.next());
  }

  public void remove() {
  }

  public GeniaParse parse(Element articleElement) {
    GeniaParse parse = new GeniaParse();

    try {
      StringWriter stringWriter = new StringWriter();
      new XMLOutputter().output(articleElement, stringWriter);
      parse.setXml(stringWriter.toString());
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    String medline = articleElement.getChild("articleinfo").getChild("bibliomisc").getText();
    medline = medline.split(":")[1];
    parse.setMedline(medline);

    StringBuffer text = new StringBuffer();
    int offset = 0;

    Element titleElement = articleElement.getChild("title");
    Element abstractElement = articleElement.getChild("abstract");

    if (titleElement != null) {
      offset = parse(titleElement, parse, text, offset);
      if (abstractElement != null) {
        text.append("\n\n");
        offset += 2;
      }
    }

    if (abstractElement != null) {
      offset = parse(abstractElement, parse, text, offset);
    }

    parse.setText(text.toString());
    return parse;
  }

  private int parse(Element abstractElement, GeniaParse parse, StringBuffer text, int offset) {
    List<GeniaTag> posTags = new ArrayList<GeniaTag>();
    List<GeniaTag> sentencePosTags = new ArrayList<GeniaTag>();

    List<GeniaTag> wildcardTags = new ArrayList<GeniaTag>();
    Iterator<?> sentences = abstractElement.getChildren("sentence").iterator();
    while (sentences.hasNext()) {
      sentencePosTags.clear();
      wildcardTags.clear();
      Element sentence = (Element) sentences.next();
      int beginSentence = offset;
      Iterator<?> contents = sentence.getContent().iterator();
      while (contents.hasNext()) {
        Object content = contents.next();
        if (content instanceof Text) {
          Text contentText = (Text) content;
          text.append(contentText.getText());
          offset += contentText.getText().length();

        } else if (content instanceof Element) {
          Element wordElement = (Element) content;
          if (!wordElement.getName().equals("w"))
            throw new RuntimeException("non-word element in sentence: " + wordElement);
          String wordText = wordElement.getText();
          text.append(wordText);
          String pos = wordElement.getAttributeValue("c");
          if (pos.indexOf('|') != -1)
            pos = pos.substring(0, pos.indexOf('|'));
          GeniaTag posTag = new GeniaTag(pos, new Span(offset, offset + wordText.length()));
          if (pos.equals("*")) {
            wildcardTags.add(posTag);
          } else {
            if (wildcardTags.size() > 0) {
              int start = wildcardTags.get(0).getSpans().get(0).getBegin();
              posTag = new GeniaTag(pos, new Span(start, offset + wordText.length()));
              wildcardTags.clear();
            }
            posTags.add(posTag);
            sentencePosTags.add(posTag);
          }
          offset += wordText.length();
        }
      }

      int endSentence = offset;
      Span sentenceSpan = new Span(beginSentence, endSentence);
      GeniaSentence geniaSentence = new GeniaSentence();
      geniaSentence.setSpan(sentenceSpan);
      geniaSentence.addPosTags(sentencePosTags);
      parse.addSentence(geniaSentence);

      text.append("  ");
      offset += 2;
    }
    parse.addPosTags(posTags);
    return offset;
  }

  public static void main(String[] args) {
    try {
      System.out.print("loading GENIA...");
      String xmlFileName = args[0];
      GeniaPOSParser parser = new GeniaPOSParser(new File(xmlFileName));
      System.out.println("done.");
      Set<String> tags = new HashSet<String>();
      while (parser.hasNext()) {
        GeniaParse parse = parser.next();
        for (GeniaTag posTag : parse.getPosTags()) {
          tags.add(posTag.getLabel());
        }
      }
      List<String> sortedTags = new ArrayList<String>(tags);
      Collections.sort(sortedTags);
      System.out.println("number of tags=" + sortedTags.size());
      for (String tag : sortedTags) {
        System.out.println(tag);
      }

      // if (parser.hasNext()) {
      // GeniaParse parse = parser.next();
      // String text = parse.getText();
      // System.out.println("\n\n\n\ntext = " + text);
      // for (GeniaTag posTag : parse.getPosTags()) {
      // System.out.println(posTag.getLabel());
      // Span span = posTag.getSpans().get(0);
      // System.out.println(text.substring(span.getBegin(),
      // span.getEnd()));
      // }
      // for (GeniaSentence sentence : parse.getSentences()) {
      // Span span = sentence.getSpan();
      // System.out.println(text.substring(span.getBegin(),
      // span.getEnd()));
      // }
      // }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
