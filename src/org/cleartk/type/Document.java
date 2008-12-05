

/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Dec 05 10:56:21 MST 2008
 * XML source: C:/Documents and Settings/Philip/My Documents/CSLR/workspace/ClearTK/desc/TypeSystem.xml
 * @generated */
public class Document extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Document.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Document() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Document(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Document(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Document(JCas jcas, int begin, int end) {
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
  //* Feature: identifier

  /** getter for identifier - gets 
   * @generated */
  public String getIdentifier() {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_identifier == null)
      jcasType.jcas.throwFeatMissing("identifier", "org.cleartk.type.Document");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Document_Type)jcasType).casFeatCode_identifier);}
    
  /** setter for identifier - sets  
   * @generated */
  public void setIdentifier(String v) {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_identifier == null)
      jcasType.jcas.throwFeatMissing("identifier", "org.cleartk.type.Document");
    jcasType.ll_cas.ll_setStringValue(addr, ((Document_Type)jcasType).casFeatCode_identifier, v);}    
   
    
  //*--------------*
  //* Feature: path

  /** getter for path - gets 
   * @generated */
  public String getPath() {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_path == null)
      jcasType.jcas.throwFeatMissing("path", "org.cleartk.type.Document");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Document_Type)jcasType).casFeatCode_path);}
    
  /** setter for path - sets  
   * @generated */
  public void setPath(String v) {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_path == null)
      jcasType.jcas.throwFeatMissing("path", "org.cleartk.type.Document");
    jcasType.ll_cas.ll_setStringValue(addr, ((Document_Type)jcasType).casFeatCode_path, v);}    
  }

    