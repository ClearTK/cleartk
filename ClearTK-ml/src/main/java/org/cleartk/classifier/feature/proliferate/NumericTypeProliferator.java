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
package org.cleartk.classifier.feature.proliferate;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.util.NumericTypeUtil;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 */

public class NumericTypeProliferator extends FeatureProliferator{

	public static final String DEFAULT_NAME = "NumericType";

	/**
	 * all characters are digits.
	 */
	public static final String DIGITS = "DIGITS";
	/**
	 * all characters are digits and it looks like a year - i.e. string is
	 * 4 digits starting with 1 or 20 or 21.
	 */
	public static final String YEAR_DIGITS = "YEAR_DIGITS";
	/**
	 * All characters are either letters or digits
	 */
	public static final String ALPHANUMERIC = "ALPHANUMERIC";
	/**
	 * Contains some digits and some non-letters - but can still contain letters
	 */
	public static final String SOME_DIGITS = "SOME_DIGITS";
	/**
	 * matches a regular expression for Roman numerals.   
	 */
	public static final String ROMAN_NUMERAL = "ROMAN_NUMERAL";
	
	static Pattern yearDigitsPattern = Pattern.compile("(?:1[0-9]{3,3})|(?:2[0|1][0-9]{2,2})");
	static Pattern alphanumericPattern = Pattern.compile("[a-zA-Z0-9-]+");
	static Pattern someLetters = Pattern.compile("[a-zA-Z]");
	static Pattern romanNumeralPattern = Pattern.compile("^M?M?M?(CM|CD|D?C?C?C?)(XC|XL|L?X?X?X?)(IX|IV|V?I?I?I?)$");
	
	public NumericTypeProliferator() {
		super(NumericTypeProliferator.DEFAULT_NAME);
	}
	public NumericTypeProliferator(String featureName) {
		super(featureName);
	}
	
	/**
	 * If the value of the feature is a StringValue and is determined to be one of 
	 * DIGITS, YEAR_DIGITS, ALPHANUMERIC, SOME_DIGITS, or ROMAN_NUMERAL, then
	 * a feature containing one of those five values is returned.  If
	 * the value of the feature cannot be characterized by one of these
	 * five values, then an empty list is returned (e.g. the value is an empty string, contains only
	 * white space, or contains only letters, etc.)
	 * 
	 * <p>
	 * This method draws heavily from NumericTypeTagger.py written by Steven Bethard.  That code credits
	 * <a href="http://diveintopython.org/unit_testing/stage_5.html">Dive Into Python</a> for the regular
	 * expression for matching roman numerals.
	 * 
	 * @return a feature that has a value that is
	 * one of DIGITS, YEAR_DIGITS, ALPHANUMERIC, SOME_DIGITS, or ROMAN_NUMERAL.  Otherwise an empty list is 
	 * returned.  
	 */
	
	@Override
	public List<Feature> proliferate(Feature feature) {
		String featureName = Feature.createName(this.getFeatureName(), feature.getName());
		Object featureValue = feature.getValue();
		if(featureValue == null) return Collections.emptyList();
		else if(featureValue instanceof String) {
			String value = featureValue.toString();
			if(value == null || value.length() == 0) return Collections.emptyList();

			if(NumericTypeUtil.isDigits(value)) {
				if(yearDigitsPattern.matcher(value).matches()) {
					return Collections.singletonList(new Feature(featureName, YEAR_DIGITS));
				}
				else return Collections.singletonList(new Feature(featureName, DIGITS));
			}
			else if(NumericTypeUtil.containsDigits(value)) {
				if(alphanumericPattern.matcher(value).matches() && someLetters.matcher(value).find()) {
					return Collections.singletonList(new Feature(featureName, ALPHANUMERIC)); 
				}
				else return Collections.singletonList(new Feature(featureName, SOME_DIGITS));
			}
			else if(romanNumeralPattern.matcher(value).matches()) {
				return Collections.singletonList(new Feature(featureName, ROMAN_NUMERAL));
			}
		}
		return Collections.emptyList();
	}
}
