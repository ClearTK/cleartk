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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public class GeniaPOSParserTest {

  @Test
  public void testArticleA() throws Exception {
    File xmlFile = new File("src/test/resources/token/genia/GENIAcorpus3.02.articleA.pos.xml");
    GeniaPOSParser gp = new GeniaPOSParser(xmlFile);
    GeniaParse parse = gp.next();

    assertEquals("95369245", parse.getMedline());
    assertTrue(parse.getText().startsWith(
        "IL-2 gene expression and NF-kappa B activation through CD28 requires reactive oxygen production by 5-lipoxygenase."));
    assertEquals(144, parse.getPosTags().size());
    GeniaTag posTag = parse.getPosTags().get(0);
    assertEquals("NN", posTag.getLabel());
    assertEquals(
        "IL-2",
        parse.getText().substring(posTag.spans.get(0).begin, posTag.spans.get(0).end));
    assertEquals(1, posTag.spans.size());
    assertEquals("0|4", posTag.spans.get(0).toString());

    posTag = parse.getPosTags().get(1);
    assertEquals("NN", posTag.getLabel());
    assertEquals(
        "gene",
        parse.getText().substring(posTag.spans.get(0).begin, posTag.spans.get(0).end));
    assertEquals("5|9", posTag.spans.get(0).toString());

    posTag = parse.getPosTags().get(29);
    assertEquals("NN", posTag.getLabel());
    assertEquals(
        "cell",
        parse.getText().substring(posTag.spans.get(0).begin, posTag.spans.get(0).end));

    posTag = parse.getPosTags().get(70);
    assertEquals("JJ", posTag.getLabel());
    assertEquals(
        "CD28-mediated",
        parse.getText().substring(posTag.spans.get(0).begin, posTag.spans.get(0).end));

    posTag = parse.getPosTags().get(75);
    assertEquals("JJ", posTag.getLabel());
    assertEquals(
        "B/CD28-responsive",
        parse.getText().substring(posTag.spans.get(0).begin, posTag.spans.get(0).end));

    posTag = parse.getPosTags().get(142);
    assertEquals("NN", posTag.getLabel());
    assertEquals(
        "pathway",
        parse.getText().substring(posTag.spans.get(0).begin, posTag.spans.get(0).end));

    posTag = parse.getPosTags().get(143);
    assertEquals(".", posTag.getLabel());
    assertEquals(".", parse.getText().substring(posTag.spans.get(0).begin, posTag.spans.get(0).end));

  }

}
