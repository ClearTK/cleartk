

/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.srl.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.type.SimpleAnnotation;


/** 
 * Updated by JCasGen Fri Dec 05 10:56:21 MST 2008
 * XML source: C:/Documents and Settings/Philip/My Documents/CSLR/workspace/ClearTK/desc/TypeSystem.xml
 * @generated */
public class SemanticArgument extends Argument {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SemanticArgument.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SemanticArgument() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SemanticArgument(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SemanticArgument(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SemanticArgument(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: label

  /** getter for label - gets 
   * @generated */
  public String getLabel() {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "org.cleartk.srl.type.SemanticArgument");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_label);}
    
  /** setter for label - sets  
   * @generated */
  public void setLabel(String v) {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "org.cleartk.srl.type.SemanticArgument");
    jcasType.ll_cas.ll_setStringValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_label, v);}    
   
    
  //*--------------*
  //* Feature: feature

  /** getter for feature - gets 
   * @generated */
  public String getFeature() {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_feature == null)
      jcasType.jcas.throwFeatMissing("feature", "org.cleartk.srl.type.SemanticArgument");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_feature);}
    
  /** setter for feature - sets  
   * @generated */
  public void setFeature(String v) {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_feature == null)
      jcasType.jcas.throwFeatMissing("feature", "org.cleartk.srl.type.SemanticArgument");
    jcasType.ll_cas.ll_setStringValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_feature, v);}    
   
    
  //*--------------*
  //* Feature: coreferenceAnnotations

  /** getter for coreferenceAnnotations - gets 
   * @generated */
  public FSArray getCoreferenceAnnotations() {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_coreferenceAnnotations == null)
      jcasType.jcas.throwFeatMissing("coreferenceAnnotations", "org.cleartk.srl.type.SemanticArgument");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_coreferenceAnnotations)));}
    
  /** setter for coreferenceAnnotations - sets  
   * @generated */
  public void setCoreferenceAnnotations(FSArray v) {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_coreferenceAnnotations == null)
      jcasType.jcas.throwFeatMissing("coreferenceAnnotations", "org.cleartk.srl.type.SemanticArgument");
    jcasType.ll_cas.ll_setRefValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_coreferenceAnnotations, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for coreferenceAnnotations - gets an indexed value - 
   * @generated */
  public SimpleAnnotation getCoreferenceAnnotations(int i) {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_coreferenceAnnotations == null)
      jcasType.jcas.throwFeatMissing("coreferenceAnnotations", "org.cleartk.srl.type.SemanticArgument");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_coreferenceAnnotations), i);
    return (SimpleAnnotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_coreferenceAnnotations), i)));}

  /** indexed setter for coreferenceAnnotations - sets an indexed value - 
   * @generated */
  public void setCoreferenceAnnotations(int i, SimpleAnnotation v) { 
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_coreferenceAnnotations == null)
      jcasType.jcas.throwFeatMissing("coreferenceAnnotations", "org.cleartk.srl.type.SemanticArgument");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_coreferenceAnnotations), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_coreferenceAnnotations), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: preposition

  /** getter for preposition - gets 
   * @generated */
  public String getPreposition() {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_preposition == null)
      jcasType.jcas.throwFeatMissing("preposition", "org.cleartk.srl.type.SemanticArgument");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_preposition);}
    
  /** setter for preposition - sets  
   * @generated */
  public void setPreposition(String v) {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_preposition == null)
      jcasType.jcas.throwFeatMissing("preposition", "org.cleartk.srl.type.SemanticArgument");
    jcasType.ll_cas.ll_setStringValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_preposition, v);}    
   
    
  //*--------------*
  //* Feature: hyphenTag

  /** getter for hyphenTag - gets 
   * @generated */
  public String getHyphenTag() {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_hyphenTag == null)
      jcasType.jcas.throwFeatMissing("hyphenTag", "org.cleartk.srl.type.SemanticArgument");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_hyphenTag);}
    
  /** setter for hyphenTag - sets  
   * @generated */
  public void setHyphenTag(String v) {
    if (SemanticArgument_Type.featOkTst && ((SemanticArgument_Type)jcasType).casFeat_hyphenTag == null)
      jcasType.jcas.throwFeatMissing("hyphenTag", "org.cleartk.srl.type.SemanticArgument");
    jcasType.ll_cas.ll_setStringValue(addr, ((SemanticArgument_Type)jcasType).casFeatCode_hyphenTag, v);}    
  }

    