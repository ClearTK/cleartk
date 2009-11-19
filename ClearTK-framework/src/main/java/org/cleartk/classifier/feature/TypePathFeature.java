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
package org.cleartk.classifier.feature;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cleartk.classifier.Feature;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philip Ogren
 * 
 */

public class TypePathFeature extends Feature {
	private String typePath;

	Pattern pattern = Pattern.compile("/([^/])?");

	public TypePathFeature(String name, Object value, String typePath) {
		super(value);
		this.typePath = typePath;
		this.name = createName(name);
	}

	public String getTypePath() {
		return typePath;
	}

	private String createName(String name) {
		if (name == null) name = "TypePath";
		String typePathString = typePath == null ? "" : typePath;

		Matcher matcher = pattern.matcher(typePathString);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			if (matcher.group(1) != null) matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
			else matcher.appendReplacement(sb, "");
		}
		matcher.appendTail(sb);

		// may not be > 0 if path is "" or "/"
		if (sb.length() > 0) sb.replace(0, 1, sb.substring(0, 1).toUpperCase());
		
		if (sb.length() > 0) {
			return String.format("%s(%s)", name, sb.toString());
		}
		else {
			return null;
		}
	}

}
