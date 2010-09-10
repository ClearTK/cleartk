

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
public class Header extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Header.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Header() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Header(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Header(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Header(JCas jcas, int begin, int end) {
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
  //* Feature: authors

  /** getter for authors - gets 
   * @generated */
  public FSArray getAuthors() {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_authors == null)
      jcasType.jcas.throwFeatMissing("authors", "org.cleartk.type.test.Header");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_authors)));}
    
  /** setter for authors - sets  
   * @generated */
  public void setAuthors(FSArray v) {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_authors == null)
      jcasType.jcas.throwFeatMissing("authors", "org.cleartk.type.test.Header");
    jcasType.ll_cas.ll_setRefValue(addr, ((Header_Type)jcasType).casFeatCode_authors, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for authors - gets an indexed value - 
   * @generated */
  public AuthorInfo getAuthors(int i) {
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_authors == null)
      jcasType.jcas.throwFeatMissing("authors", "org.cleartk.type.test.Header");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_authors), i);
    return (AuthorInfo)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_authors), i)));}

  /** indexed setter for authors - sets an indexed value - 
   * @generated */
  public void setAuthors(int i, AuthorInfo v) { 
    if (Header_Type.featOkTst && ((Header_Type)jcasType).casFeat_authors == null)
      jcasType.jcas.throwFeatMissing("authors", "org.cleartk.type.test.Header");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_authors), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Header_Type)jcasType).casFeatCode_authors), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    