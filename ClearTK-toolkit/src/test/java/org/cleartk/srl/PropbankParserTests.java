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
package org.cleartk.srl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cleartk.srl.propbank.util.Propbank;
import org.cleartk.srl.propbank.util.PropbankCorefRelation;
import org.cleartk.srl.propbank.util.PropbankFormatException;
import org.cleartk.srl.propbank.util.PropbankNodeRelation;
import org.cleartk.srl.propbank.util.PropbankRelation;
import org.cleartk.srl.propbank.util.PropbankSplitRelation;
import org.cleartk.srl.propbank.util.PropbankTerminalRelation;
import org.cleartk.srl.propbank.util.Proplabel;
import org.junit.Assert;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 * 
 * The numbers of the tests refer to line numbers in prop.txt
 *
 */
public class PropbankParserTests {

	@Test
	public void testInvalid() {
		try {
			Propbank.fromString("");
			Assert.fail("expected exception for empty line");
		} catch (PropbankFormatException e) {}

		try {
			Propbank.fromString("XXX.mrg X 0 gold name.01 -----");
			Assert.fail("expected exception for invalid indices");
		} catch (PropbankFormatException e) {}

		try {
			Propbank.fromString("XXX.mrg X 0 gold XXX -----");
			Assert.fail("expected exception for invalid base form and frameset");
		} catch (PropbankFormatException e) {}

		try {
			Propbank.fromString("XXX.mrg 0 0 gold name.01 ----- 1:0-rel 2:0-ARGX");
			Assert.fail("expected exception for bad label name");
		} catch (PropbankFormatException e) {}

		try {
			Propbank.fromString("XXX.mrg 0 0 gold name.01 ----- 1:0-rel 2:0+ARG0");
			Assert.fail("expected exception for invalid label string");
		} catch (PropbankFormatException e) {}
	}

	@Test
	public void test6978() {
		String source = "wsj/01/wsj_0124.mrg 0 15 gold name.01 pp--p 15:0-rel 0:2*16:0-ARG1 17:3-ARG2";
		Propbank propbank = Propbank.fromString(source);
		
		assertEquals(source, propbank.toString());
		assertEquals("wsj/01/wsj_0124.mrg", propbank.getFilename());
		assertEquals(0, propbank.getSentenceNumber());
		assertEquals(15, ((PropbankTerminalRelation)propbank.getTerminal()).getTerminalNumber());
		assertEquals("gold", propbank.getTaggerName());
		assertEquals("name", propbank.getBaseForm());
		assertEquals("01", propbank.getFrameSet());
		assertEquals("pp--p", propbank.getInflectionValue());
		
		//15:0-rel
		Proplabel proplabel = propbank.getPropLabels().get(0);
		assertEquals("rel",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		PropbankNodeRelation nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("15:0", nodeRelation.toString());
		assertEquals(15, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());

		//0:2*16:0-ARG1
		proplabel = propbank.getPropLabels().get(1);
		assertEquals("ARG1",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		PropbankCorefRelation corefRelation = (PropbankCorefRelation) proplabel.getRelation();
		assertEquals("0:2*16:0", corefRelation.toString());
		List<PropbankRelation> relations = corefRelation.getCorefRelations();
		nodeRelation = (PropbankNodeRelation) relations.get(0);
		assertEquals(0, nodeRelation.getTerminalNumber());
		assertEquals(2, nodeRelation.getHeight());
		nodeRelation = (PropbankNodeRelation) relations.get(1);
		assertEquals(16, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());

		//17:3-ARG2
		proplabel = propbank.getPropLabels().get(2);
		assertEquals("ARG2",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("17:3", nodeRelation.toString());
		assertEquals(17, nodeRelation.getTerminalNumber());
		assertEquals(3, nodeRelation.getHeight());
	}
	
	@Test 
	public void test6812() {
		String source = "wsj/01/wsj_0125.mrg 2 15 gold price.01 p---p 15:0-rel 17:1-ARG2-at 19:2-ARGM-PNC 5:2,9:1*16:0-ARG1";
		Propbank propbank = Propbank.fromString(source);

		assertEquals(source, propbank.toString());
		assertEquals("wsj/01/wsj_0125.mrg", propbank.getFilename());
		assertEquals(2, propbank.getSentenceNumber());
		assertEquals(15, ((PropbankTerminalRelation)propbank.getTerminal()).getTerminalNumber());
		assertEquals("gold", propbank.getTaggerName());
		assertEquals("price", propbank.getBaseForm());
		assertEquals("01", propbank.getFrameSet());
		assertEquals("p---p", propbank.getInflectionValue());
		
		//15:0-rel
		Proplabel proplabel = propbank.getPropLabels().get(0);
		assertEquals("rel",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		PropbankNodeRelation nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("15:0", nodeRelation.toString());
		assertEquals(15, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());

		//17:1-ARG2-at
		proplabel = propbank.getPropLabels().get(1);
		assertEquals("ARG2",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals("at",  proplabel.getPreposition());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("17:1", nodeRelation.toString());
		assertEquals(17, nodeRelation.getTerminalNumber());
		assertEquals(1, nodeRelation.getHeight());

		//19:2-ARGM-PNC
		proplabel = propbank.getPropLabels().get(2);
		assertEquals("ARGM",  proplabel.getLabel());
		assertEquals("PNC",  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("19:2", nodeRelation.toString());
		assertEquals(19, nodeRelation.getTerminalNumber());
		assertEquals(2, nodeRelation.getHeight());

		//5:2,9:1*16:0-ARG1
		proplabel = propbank.getPropLabels().get(3);
		assertEquals("ARG1",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankCorefRelation.class,  proplabel.getRelation().getClass());
		PropbankCorefRelation corefRelation = (PropbankCorefRelation) proplabel.getRelation();
		List<PropbankRelation> corefRelations = corefRelation.getCorefRelations();
		PropbankSplitRelation splitRelation = (PropbankSplitRelation) corefRelations.get(0);
		List<PropbankRelation> splitRelations = splitRelation.getRelations();
		nodeRelation = (PropbankNodeRelation) splitRelations.get(0);
		assertEquals(5, nodeRelation.getTerminalNumber());
		assertEquals(2, nodeRelation.getHeight());
		nodeRelation = (PropbankNodeRelation) splitRelations.get(1);
		assertEquals(9, nodeRelation.getTerminalNumber());
		assertEquals(1, nodeRelation.getHeight());
		nodeRelation = (PropbankNodeRelation) corefRelations.get(1);
		assertEquals(16, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());
		
	}
	
	@Test 
	public void test30467() {
		String source = "wsj/06/wsj_0656.mrg 4 9 gold acquire.01 i---a 1:1*5:0*7:0-ARG0 9:0-rel 10:2-ARG1";
		Propbank propbank = Propbank.fromString(source);

		assertEquals(source, propbank.toString());
		assertEquals("wsj/06/wsj_0656.mrg", propbank.getFilename());
		assertEquals(4, propbank.getSentenceNumber());
		assertEquals(9, ((PropbankTerminalRelation)propbank.getTerminal()).getTerminalNumber());
		assertEquals("gold", propbank.getTaggerName());
		assertEquals("acquire", propbank.getBaseForm());
		assertEquals("01", propbank.getFrameSet());
		assertEquals("i---a", propbank.getInflectionValue());
		
		//1:1*5:0*7:0-ARG0
		Proplabel proplabel = propbank.getPropLabels().get(0);
		assertEquals("ARG0",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankCorefRelation.class,  proplabel.getRelation().getClass());
		PropbankCorefRelation corefRelation = (PropbankCorefRelation) proplabel.getRelation();
		PropbankNodeRelation nodeRelation = (PropbankNodeRelation) corefRelation.getCorefRelations().get(0);
		assertEquals("1:1", nodeRelation.toString());
		assertEquals(1, nodeRelation.getTerminalNumber());
		assertEquals(1, nodeRelation.getHeight());
		nodeRelation = (PropbankNodeRelation) corefRelation.getCorefRelations().get(1);
		assertEquals("5:0", nodeRelation.toString());
		assertEquals(5, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());
		nodeRelation = (PropbankNodeRelation) corefRelation.getCorefRelations().get(2);
		assertEquals("7:0", nodeRelation.toString());
		assertEquals(7, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());

		
		//9:0-rel 
		proplabel = propbank.getPropLabels().get(1);
		assertEquals("rel",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("9:0", nodeRelation.toString());
		assertEquals(9, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());

		//10:2-ARG1
		proplabel = propbank.getPropLabels().get(2);
		assertEquals("ARG1",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("10:2", nodeRelation.toString());
		assertEquals(10, nodeRelation.getTerminalNumber());
		assertEquals(2, nodeRelation.getHeight());
		
	}
	
	@Test
	public void testNombank() {
		String source = "wsj/00/wsj_0083.mrg 42 14 % XX 6:1*9:1*18:0-ARG1 14:0-rel 15:0-Support";
		Propbank propbank = Propbank.fromString(source);
		
		assertEquals(source, propbank.toString());
		assertEquals("wsj/00/wsj_0083.mrg", propbank.getFilename());
		assertEquals(42, propbank.getSentenceNumber());
		assertEquals(14, ((PropbankTerminalRelation)propbank.getTerminal()).getTerminalNumber());
		assertEquals("%", propbank.getBaseForm());
		assertEquals("XX", propbank.getFrameSet());
		assertEquals(null, propbank.getTaggerName());
		assertEquals(null, propbank.getInflectionValue());
		
		// 6:1*9:1*18:0-ARG1
		Proplabel proplabel = propbank.getPropLabels().get(0);
		assertEquals("ARG1",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankCorefRelation.class,  proplabel.getRelation().getClass());
		PropbankCorefRelation corefRelation = (PropbankCorefRelation) proplabel.getRelation();
		PropbankNodeRelation nodeRelation = (PropbankNodeRelation) corefRelation.getCorefRelations().get(0);
		assertEquals("6:1", nodeRelation.toString());
		assertEquals(6, nodeRelation.getTerminalNumber());
		assertEquals(1, nodeRelation.getHeight());
		nodeRelation = (PropbankNodeRelation) corefRelation.getCorefRelations().get(1);
		assertEquals("9:1", nodeRelation.toString());
		assertEquals(9, nodeRelation.getTerminalNumber());
		assertEquals(1, nodeRelation.getHeight());
		nodeRelation = (PropbankNodeRelation) corefRelation.getCorefRelations().get(2);
		assertEquals("18:0", nodeRelation.toString());
		assertEquals(18, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());

		// 14:0-rel
		proplabel = propbank.getPropLabels().get(1);
		assertEquals("rel",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("14:0", nodeRelation.toString());
		assertEquals(14, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());
		
		// 15:0-Support
		proplabel = propbank.getPropLabels().get(2);
		assertEquals("Support",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("15:0", nodeRelation.toString());
		assertEquals(15, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());
		
	}
	
	@Test
	public void testNombankHyphenTags() {
		String source = "wsj/13/wsj_1363.mrg 2 27 allotment 01 27:0-ARGM-MNR-H0 27:0-rel-H1 28:1-ARG2";
		Propbank propbank = Propbank.fromString(source);

		assertEquals(source, propbank.toString());
		assertEquals("wsj/13/wsj_1363.mrg", propbank.getFilename());
		assertEquals(2, propbank.getSentenceNumber());
		assertEquals(27, ((PropbankTerminalRelation)propbank.getTerminal()).getTerminalNumber());
		assertEquals("allotment", propbank.getBaseForm());
		assertEquals("01", propbank.getFrameSet());
		assertEquals(null, propbank.getTaggerName());
		assertEquals(null, propbank.getInflectionValue());

		// 27:0-ARGM-MNR-H0
		Proplabel proplabel = propbank.getPropLabels().get(0);
		assertEquals("ARGM",  proplabel.getLabel());
		assertEquals("MNR",  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals("H0", proplabel.getHyphenTag());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		PropbankNodeRelation nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("27:0", nodeRelation.toString());
		assertEquals(27, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());

		// 27:0-rel-H1
		proplabel = propbank.getPropLabels().get(1);
		assertEquals("rel",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals("H1", proplabel.getHyphenTag());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("27:0", nodeRelation.toString());
		assertEquals(27, nodeRelation.getTerminalNumber());
		assertEquals(0, nodeRelation.getHeight());

		// 28:1-ARG2
		proplabel = propbank.getPropLabels().get(2);
		assertEquals("ARG2",  proplabel.getLabel());
		assertEquals(null,  proplabel.getFeature());
		assertEquals(null,  proplabel.getPreposition());
		assertEquals(null, proplabel.getHyphenTag());
		assertEquals(PropbankNodeRelation.class,  proplabel.getRelation().getClass());
		nodeRelation = (PropbankNodeRelation) proplabel.getRelation();
		assertEquals("28:1", nodeRelation.toString());
		assertEquals(28, nodeRelation.getTerminalNumber());
		assertEquals(1, nodeRelation.getHeight());

	}
	
}
