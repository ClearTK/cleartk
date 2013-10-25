/* 
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
package org.cleartk.examples.chunking.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.ViewUriUtil;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;

/**
 * This class reads MASC (http://www.anc.org/MASC) annotations into the CAS.
 * 
 * For most people browsing through the chunking examples, this is not worth looking at -- it's just
 * necessary so that we can have some human-annotated named entity mentions to train on.
 * 
 * Eventually, this should probably be refined and moved to somewhere else in ClearTK where anyone
 * working from the MASC corpus could use it.
 * 
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class MascGoldAnnotator extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(MascGoldAnnotator.class);
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    File textFile = new File(ViewUriUtil.getURI(jCas));
    String prefix = textFile.getPath().replaceAll("[.]txt$", "");
    File sFile = new File(prefix + "-s.xml");
    File segFile = new File(prefix + "-seg.xml");
    File pennFile = new File(prefix + "-penn.xml");
    File neFile = new File(prefix + "-ne.xml");

    Namespace xmlNS = Namespace.XML_NAMESPACE;
    Namespace grafNS = Namespace.getNamespace("http://www.xces.org/ns/GrAF/1.0/");

    // parse sentences
    Element sRoot = parse(sFile);
    for (Element regionElem : sRoot.getChildren("region", grafNS)) {
      // String id = getAttributeValue(regionElem, "id", xmlNS);
      String anchorsString = getAttributeValue(regionElem, "anchors");

      String[] beginAndEnd = anchorsString.split("\\s+");
      if (beginAndEnd.length != 2) {
        throw new IllegalStateException("expected two anchors, found " + toString(regionElem));
      }

      int begin = Integer.parseInt(beginAndEnd[0]);
      int end = Integer.parseInt(beginAndEnd[1]);
      Sentence sentence = new Sentence(jCas, begin, end);
      sentence.addToIndexes();
    }

    // parse segment begins and ends
    Element segRoot = parse(segFile);
    Map<String, Integer> beginMap = new HashMap<String, Integer>();
    Map<String, Integer> endMap = new HashMap<String, Integer>();
    for (Element regionElem : segRoot.getChildren("region", grafNS)) {
      String id = getAttributeValue(regionElem, "id", xmlNS);
      String anchorsString = getAttributeValue(regionElem, "anchors");

      String[] beginAndEnd = anchorsString.split("\\s+");
      if (beginAndEnd.length != 2) {
        throw new IllegalStateException("expected two anchors, found " + toString(regionElem));
      }

      beginMap.put(id, new Integer(beginAndEnd[0]));
      endMap.put(id, new Integer(beginAndEnd[1]));
    }

    // parse tokens
    Map<String, Token> idTokenMap = new HashMap<String, Token>();
    Element pennRoot = parse(pennFile);
    for (Element nodeElem : pennRoot.getChildren("node", grafNS)) {
      String id = getAttributeValue(nodeElem, "id", xmlNS);
      Element linkElem = getChild(nodeElem, "link", grafNS);
      String targets = getAttributeValue(linkElem, "targets");

      String[] targetsArray = targets.split("\\s+");
      String beginID = targetsArray[0];
      String endID = targetsArray[targetsArray.length - 1];
      Integer begin = beginMap.get(beginID);
      if (begin == null) {
        throw new IllegalStateException("no such segment: " + beginID);
      }
      Integer end = endMap.get(endID);
      if (end == null) {
        throw new IllegalStateException("no such segment: " + endID);
      }

      Token token = new Token(jCas, begin, end);
      // TODO: parse part-of-speech information out of pennRoot
      token.addToIndexes();
      idTokenMap.put(id, token);
    }

    // parse named entities
    Element neRoot = parse(neFile);
    Map<String, NamedEntityMention> idMentionMap = new HashMap<String, NamedEntityMention>();
    for (Element edgeElem : neRoot.getChildren("edge", grafNS)) {
      String mentionID = getAttributeValue(edgeElem, "from");
      NamedEntityMention mention = idMentionMap.get(mentionID);
      if (mention == null) {
        mention = new NamedEntityMention(jCas, jCas.getDocumentText().length(), 0);
        idMentionMap.put(mentionID, mention);
      }

      String tokenID = getAttributeValue(edgeElem, "to");
      Token token = idTokenMap.get(tokenID);
      if (token == null) {
        throw new IllegalStateException("no token with id " + tokenID);
      }

      // simplifying assumption - named entity spans continuously across all tokens
      if (token.getBegin() < mention.getBegin()) {
        mention.setBegin(token.getBegin());
      }
      if (token.getEnd() > mention.getEnd()) {
        mention.setEnd(token.getEnd());
      }
    }
    for (Element aElem : neRoot.getChildren("a", grafNS)) {
      String label = getAttributeValue(aElem, "label");
      String mentionID = getAttributeValue(aElem, "ref");
      NamedEntityMention mention = idMentionMap.get(mentionID);
      if (mention == null) {
        throw new IllegalStateException("no mention for ref in " + toString(aElem));
      }

      // TODO: parse out features like gender
      // Element fsElem = getChild(aElem, "fs", grafNS);
      // for (Element fElem : fsElem.getChildren("f", grafNS)) {
      // String name = getAttributeValue(fElem, "name");
      // String value = getAttributeValue(fElem, "value");
      // }

      mention.setMentionType(label);
    }
    for (Entry<String, NamedEntityMention> entry : idMentionMap.entrySet()) {
      NamedEntityMention mention = entry.getValue();
      if (mention.getMentionType() == null) {
        throw new IllegalStateException("no mention type for mention " + entry.getKey());
      }
      mention.addToIndexes();
    }
  }

  private static Element parse(File file) throws AnalysisEngineProcessException {
    SAXBuilder sax = new SAXBuilder();
    try {
      return sax.build(file).getRootElement();
    } catch (JDOMException e) {
      throw new AnalysisEngineProcessException(e);
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }

  }

  private static Element getChild(Element elem, String name, Namespace namespace) {
    List<Element> elems = elem.getChildren(name, namespace);
    if (elems.size() != 1) {
      throw new IllegalStateException("expected 1 \"" + name + "\" element in " + toString(elem));
    }
    return elems.get(0);
  }

  private static String getAttributeValue(Element elem, String attrName) {
    return getAttributeValue(elem, attrName, null);
  }

  private static String getAttributeValue(Element elem, String attrName, Namespace namespace) {
    String value;
    if (namespace == null) {
      value = elem.getAttributeValue(attrName);
    } else {
      value = elem.getAttributeValue(attrName, namespace);
    }
    if (value == null) {
      throw new IllegalStateException("expected \"" + attrName + "\" attribute in "
          + toString(elem));
    }
    return value;
  }

  private static XMLOutputter xmlOutputter = new XMLOutputter();

  private static String toString(Element elem) {
    return xmlOutputter.outputString(elem);
  }
}
