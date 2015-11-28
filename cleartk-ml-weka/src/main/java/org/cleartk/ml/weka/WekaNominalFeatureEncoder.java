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
  
  String updateIfEqualWithNotSeenConstant(String value){
    if (value.equals(NOT_SEAN_VAL) && addNotSeanValue)
      return value = " " + value;
    return value;//Utils.quote(value);
  }
  
  public Attribute getAttribute(){
    if (attribute == null){
      attribute = new Attribute(name, getSortedValues());
    }
    return attribute;
  }
  
  public void setAttributeValue(Instance instance, String value){
    String updatedValue = updateIfEqualWithNotSeenConstant(value);
    int idx = Collections.binarySearch(getSortedValues(), updatedValue);
    if (idx < 0){
      value = NOT_SEAN_VAL;
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
