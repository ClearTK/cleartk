/* 
 Copyright 2010 University of Minnesota  
 All rights reserved. 

 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 

 http://www.apache.org/licenses/LICENSE-2.0 

 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */

package org.cleartk.evaluation.factory.corpus;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.evaluation.factory.CorpusFactory;
import org.uimafit.component.JCasAnnotatorAdapter;
import org.uimafit.factory.AnalysisEngineFactory;

/**
 * 
 * @author Philip Ogren
 * 
 */

public abstract class CorpusFactory_ImplBase implements CorpusFactory {

  protected TypeSystemDescription typeSystemDescription;

  public CorpusFactory_ImplBase(TypeSystemDescription typeSystemDescription) {
    this.typeSystemDescription = typeSystemDescription;
  }

  public AnalysisEngineDescription createPreprocessor() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(
        JCasAnnotatorAdapter.class,
        typeSystemDescription);
  }

  protected void verifyFoldValue(int fold) {
    if (fold > 0 && fold <= numberOfFolds()) {
      return;
    }
    throw new RuntimeException("fold number must be greater than 0 and less than or equal to "
        + numberOfFolds());
  }

}
