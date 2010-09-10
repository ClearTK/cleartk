

/* First created by JCasGen Fri Sep 10 16:27:54 MDT 2010 */
package org.cleartk.type.test;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Sep 10 16:27:54 MDT 2010
 * XML source: file:C:/Users/Philip/Documents/Academic/workspace/ClearTK-test-util/src/main/resources/org/cleartk/type/test/TestTypeSystem.xml
 * @generated */
public class Token extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Token.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Token() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Token(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Token(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Token(JCas jcas, int begin, int end) {
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
  //* Feature: pos

  /** getter for pos - gets 
   * @generated */
  public String getPos() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "org.cleartk.type.test.Token");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_pos);}
    
  /** setter for pos - sets  
   * @generated */
  public void setPos(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "org.cleartk.type.test.Token");
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_pos, v);}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated */
  public Lemma getLemma() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "org.cleartk.type.test.Token");
    return (Lemma)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma)));}
    
  /** setter for lemma - sets  
   * @generated */
  public void setLemma(Lemma v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "org.cleartk.type.test.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: posTag

  /** getter for posTag - gets 
   * @generated */
  public FSArray getPosTag() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "org.cleartk.type.test.Token");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag)));}
    
  /** setter for posTag - sets  
   * @generated */
  public void setPosTag(FSArray v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "org.cleartk.type.test.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for posTag - gets an indexed value - 
   * @generated */
  public POSTag getPosTag(int i) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "org.cleartk.type.test.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag), i);
    return (POSTag)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag), i)));}

  /** indexed setter for posTag - sets an indexed value - 
   * @generated */
  public void setPosTag(int i, POSTag v) { 
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_posTag == null)
      jcasType.jcas.throwFeatMissing("posTag", "org.cleartk.type.test.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_posTag), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: depRel

  /** getter for depRel - gets 
   * @generated */
  public FSArray getDepRel() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_depRel == null)
      jcasType.jcas.throwFeatMissing("depRel", "org.cleartk.type.test.Token");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel)));}
    
  /** setter for depRel - sets  
   * @generated */
  public void setDepRel(FSArray v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_depRel == null)
      jcasType.jcas.throwFeatMissing("depRel", "org.cleartk.type.test.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for depRel - gets an indexed value - 
   * @generated */
  public DependencyRelation getDepRel(int i) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_depRel == null)
      jcasType.jcas.throwFeatMissing("depRel", "org.cleartk.type.test.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel), i);
    return (DependencyRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel), i)));}

  /** indexed setter for depRel - sets an indexed value - 
   * @generated */
  public void setDepRel(int i, DependencyRelation v) { 
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_depRel == null)
      jcasType.jcas.throwFeatMissing("depRel", "org.cleartk.type.test.Token");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_depRel), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: orthogr

  /** getter for orthogr - gets 
   * @generated */
  public Orthography getOrthogr() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_orthogr == null)
      jcasType.jcas.throwFeatMissing("orthogr", "org.cleartk.type.test.Token");
    return (Orthography)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_orthogr)));}
    
  /** setter for orthogr - sets  
   * @generated */
  public void setOrthogr(Orthography v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_orthogr == null)
      jcasType.jcas.throwFeatMissing("orthogr", "org.cleartk.type.test.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_orthogr, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    