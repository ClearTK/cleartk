/*
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.timeml.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

import com.google.common.base.Joiner;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TimeWordsExtractor<T extends Annotation> implements NamedFeatureExtractor1<T> {
  
  private String featureName = "TimeType";

  private Map<String, Set<String>> groupedWords;

  public TimeWordsExtractor() {
    this.groupedWords = new HashMap<String, Set<String>>();
    this.groupedWords.put("Now", new HashSet<String>(Arrays.asList("now", "current", "currently")));
    this.groupedWords.put(
        "TimeOfDay",
        new HashSet<String>(Arrays.asList(
            "morning",
            "noon",
            "afternoon",
            "evening",
            "night",
            "midnight")));
    this.groupedWords.put(
        "Day",
        new HashSet<String>(Arrays.asList(
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday",
            "today",
            "tomorrow",
            "yesterday")));
    this.groupedWords.put(
        "Month",
        new HashSet<String>(Arrays.asList(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December")));
    this.groupedWords.put(
        "Season",
        new HashSet<String>(Arrays.asList("spring", "summer", "fall", "autumn", "winter")));
    this.groupedWords.put(
        "TimeDuration",
        new HashSet<String>(
            Arrays.asList("minute", "minutes", "second", "seconds", "hour", "hours")));
    this.groupedWords.put(
        "DateDuration",
        new HashSet<String>(Arrays.asList(
            "day",
            "days",
            "week",
            "weeks",
            "month",
            "months",
            "quarter",
            "quarters",
            "season",
            "seasons",
            "year",
            "years",
            "decade",
            "decades",
            "century",
            "centuries")));
  }
  
  @Override
  public String getFeatureName() {
    return this.featureName;
  }

  @Override
  public List<Feature> extract(JCas view, T focusAnnotation) {
    List<String> types = new ArrayList<String>();
    String[] words = focusAnnotation.getCoveredText().split("\\W+");
    for (String word : words) {
      for (String group : this.groupedWords.keySet()) {
        if (this.groupedWords.get(group).contains(word)) {
          types.add(group);
        }
      }
      if (word.matches("^\\d{4}$")) {
        types.add("Year");
      }
    }
    if (types.isEmpty()) {
      for (String word : words) {
        for (String group : this.groupedWords.keySet()) {
          if (this.groupedWords.get(group).contains(word.toLowerCase())) {
            types.add(group + "Lower");
          }
        }
      }
    }
    if (types.isEmpty()) {
      types.add("None");
    }
    return Arrays.asList(new Feature(this.featureName, Joiner.on('_').join(types)));
  }

}