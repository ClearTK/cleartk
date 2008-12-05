

/* First created by JCasGen Fri Dec 05 10:56:21 MST 2008 */
package org.cleartk.corpus.ace2005.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Fri Dec 05 10:56:21 MST 2008
 * XML source: C:/Documents and Settings/Philip/My Documents/CSLR/workspace/ClearTK/desc/TypeSystem.xml
 * @generated */
public class Document extends org.cleartk.type.Document {
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
  //* Feature: aceSource

  /** getter for aceSource - gets 
   * @generated */
  public String getAceSource() {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_aceSource == null)
      jcasType.jcas.throwFeatMissing("aceSource", "org.cleartk.corpus.ace2005.type.Document");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Document_Type)jcasType).casFeatCode_aceSource);}
    
  /** setter for aceSource - sets  
   * @generated */
  public void setAceSource(String v) {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_aceSource == null)
      jcasType.jcas.throwFeatMissing("aceSource", "org.cleartk.corpus.ace2005.type.Document");
    jcasType.ll_cas.ll_setStringValue(addr, ((Document_Type)jcasType).casFeatCode_aceSource, v);}    
   
    
  //*--------------*
  //* Feature: aceUri

  /** getter for aceUri - gets 
   * @generated */
  public String getAceUri() {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_aceUri == null)
      jcasType.jcas.throwFeatMissing("aceUri", "org.cleartk.corpus.ace2005.type.Document");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Document_Type)jcasType).casFeatCode_aceUri);}
    
  /** setter for aceUri - sets  
   * @generated */
  public void setAceUri(String v) {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_aceUri == null)
      jcasType.jcas.throwFeatMissing("aceUri", "org.cleartk.corpus.ace2005.type.Document");
    jcasType.ll_cas.ll_setStringValue(addr, ((Document_Type)jcasType).casFeatCode_aceUri, v);}    
   
    
  //*--------------*
  //* Feature: aceType

  /** getter for aceType - gets 
   * @generated */
  public String getAceType() {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_aceType == null)
      jcasType.jcas.throwFeatMissing("aceType", "org.cleartk.corpus.ace2005.type.Document");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Document_Type)jcasType).casFeatCode_aceType);}
    
  /** setter for aceType - sets  
   * @generated */
  public void setAceType(String v) {
    if (Document_Type.featOkTst && ((Document_Type)jcasType).casFeat_aceType == null)
      jcasType.jcas.throwFeatMissing("aceType", "org.cleartk.corpus.ace2005.type.Document");
    jcasType.ll_cas.ll_setStringValue(addr, ((Document_Type)jcasType).casFeatCode_aceType, v);}    
  }

    