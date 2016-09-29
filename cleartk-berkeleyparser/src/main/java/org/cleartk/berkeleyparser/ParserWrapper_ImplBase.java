/* 
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

package org.cleartk.berkeleyparser;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 */

public abstract class ParserWrapper_ImplBase<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation, PARSE_TYPE, TOP_NODE_TYPE extends Annotation>
    extends JCasAnnotator_ImplBase {

  public static final String PARAM_INPUT_TYPES_HELPER_CLASS_NAME = "inputTypesHelperClassName";

  @ConfigurationParameter(
	  name = PARAM_INPUT_TYPES_HELPER_CLASS_NAME,
      defaultValue = "org.cleartk.berkeleyparser.DefaultInputTypesHelper",
      mandatory = true)
  protected String inputTypesHelperClassName;
  
  protected InputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE> inputTypesHelper;

  public static final String PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME = "outputTypesHelperClassName";

  @ConfigurationParameter(mandatory = true, name = PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME)
  protected String outputTypesHelperClassName;

  protected OutputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE, PARSE_TYPE, TOP_NODE_TYPE> outputTypesHelper;

  @SuppressWarnings("unchecked")
  @Override
  public void initialize(UimaContext ctx) throws ResourceInitializationException {
    super.initialize(ctx);

    inputTypesHelper = InitializableFactory.create(
        ctx,
        inputTypesHelperClassName,
        InputTypesHelper.class);

    outputTypesHelper = InitializableFactory.create(
        ctx,
        outputTypesHelperClassName,
        OutputTypesHelper.class);

  }
}
