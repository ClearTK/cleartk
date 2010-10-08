

/* First created by JCasGen Fri Oct 08 11:18:15 MDT 2010 */
package org.cleartk.type.test;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Oct 08 11:18:15 MDT 2010
 * XML source: file:C:/Users/Philip/Documents/Academic/workspace/cleartk-ml-grmm/src/test/resources/org/cleartk/TestTypeSystem.xml
 * @generated */
public class Orthography extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Orthography.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Orthography() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Orthography(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Orthography(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Orthography(JCas jcas, int begin, int end) {
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
     
}

    