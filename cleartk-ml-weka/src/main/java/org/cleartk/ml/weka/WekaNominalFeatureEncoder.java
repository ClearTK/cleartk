/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For a complete copy of the license please see the file LICENSE distributed 
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.ml.weka;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cleartk.ml.encoder.CleartkEncoderException;
import org.cleartk.ml.encoder.outcome.OutcomeEncoder;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Majid Laali
 * 
 */
public class WekaNominalFeatureEncoder implements OutcomeEncoder<String, String>{
  private static final long serialVersionUID = -7572202162749261473L;
  
  public static final String NOT_SEAN_VAL = "";
  private String name;
  private Set<String> range = new HashSet<>();
  private List<String> sortedValues = null;
  private Attribute attribute;
  private boolean addNotSeanValue;
  
  public WekaNominalFeatureEncoder(String name, boolean addNotSeanValue) {
    this.name = Utils.quote(name);
    this.addNotSeanValue = addNotSeanValue;
  }

  void save(String value){
    if (attribute != null)
      throw new RuntimeException("The WekaEncoder cannot be modified after the construction of its attribute.");
    range.add(updateIfEqualWithNotSeenConstant(value));
  }
  
  String updateIfEqualWithNotSeenConstant(Object value){
    if (value == null)
      return "null";
    
    String strValue = value.toString();
    if (strValue.equals(NOT_SEAN_VAL) && addNotSeanValue)
      return " " + strValue;
    return strValue;//Utils.quote(value);
  }
  
  public Attribute getAttribute(){
    if (attribute == null){
      attribute = new Attribute(name, getSortedValues());
    }
    return attribute;
  }
  
  public void setAttributeValue(Instance instance, Object value){
    String updatedValue = updateIfEqualWithNotSeenConstant(value);
    int idx = Collections.binarySearch(getSortedValues(), updatedValue);
    if (idx < 0){
      updatedValue = NOT_SEAN_VAL;
    }
    instance.setValue(attribute, updatedValue);
  }
  
  List<String> getSortedValues() {
    if (sortedValues == null){
      sortedValues = new ArrayList<>();
      sortedValues.addAll(range);
      if (addNotSeanValue)
        sortedValues.add(NOT_SEAN_VAL);
      Collections.sort(sortedValues);
    }
    return sortedValues;
  }

  public String decode(String outcome) throws CleartkEncoderException {
    throw new RuntimeException("TODO");
  }

  public void finalizeOutcomeSet(File outputDirectory) throws IOException {
  }

  public String encode(String outcome) throws CleartkEncoderException {
    save(outcome);
    return outcome;
  }
  
  
}
