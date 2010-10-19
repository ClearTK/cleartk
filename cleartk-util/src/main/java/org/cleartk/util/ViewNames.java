/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
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
package org.cleartk.util;

import org.apache.uima.cas.CAS;

/**
 * Contains the view name constants used by ClearTK.
 * 
 * @author Steven Bethard
 * 
 * <br>
 *         Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 *         All rights reserved.
 */
public class ViewNames {

	/**
	 * The view where the document Uniform Resource Identifier is placed.
	 */
	public static final String URI = "UriView";

	/**
	 * The view where annotations are placed by default. This is equivalent to
	 * the default view named "_InitialView" and is repeated here only for
	 * convenience.  For more information on the "_InitialView" please see:
	 * http://incubator.apache.org/uima/downloads/releaseDocs/2.2.2-incubating/docs/html/tools/tools.html#ugr.tools.cde.capabilities.sofa_name_mapping
	 * 
	 * @see CAS#NAME_DEFAULT_SOFA
	 */
	public static final String DEFAULT = CAS.NAME_DEFAULT_SOFA;

	/**
	 * The view containing the XML text of an ACE APF file.
	 */
	public static final String ACE_APF_URI = "ApfUriView";

	/**
	 * The view containing the XML text of a TimeML file.
	 */
	public static final String TIMEML = "TimeMLView";

	/**
	 * The view containing CoNLL 2005 formatted text.
	 */
	public static final String CONLL_2005 = "CoNLL2005View";

	/**
	 * The view containing the text of a PropBank "prop.txt" file.
	 */
	public static final String PROPBANK = "PropbankView";

	/**
	 * The view containing the parenthesized text of a TreeBank .mrg file.
	 */
	public static final String TREEBANK = "TreebankView";

	/**
	 * The view containing Genia part of speech formatted text.
	 */
	public static final String GENIA_POS = "GeniaPOSView";

	public static final String GOLD_VIEW = "GoldView";

	public static final String SYSTEM_VIEW = "SystemView";

	/**
	 * Private constructor to enforce un-instantiability.
	 */
	private ViewNames() {
	}

}
