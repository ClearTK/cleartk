/* 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */

package org.cleartk.opennlp.tools.parser;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;

import com.google.common.annotations.Beta;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 */
@Beta
public abstract class ParserWrapper_ImplBase<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation, PARSE_TYPE, TOP_NODE_TYPE extends Annotation>
    extends JCasAnnotator_ImplBase {

  public static final String PARAM_INPUT_TYPES_HELPER_CLASS_NAME = "inputTypesHelperClassName";

  @ConfigurationParameter(
      name = PARAM_INPUT_TYPES_HELPER_CLASS_NAME,
      defaultValue = "org.cleartk.opennlp.tools.parser.DefaultInputTypesHelper",
      mandatory = true)
  protected String inputTypesHelperClassName;

  protected InputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE> inputTypesHelper;

  public static final String PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME = "outputTypesHelperClassName";

  @ConfigurationParameter(
		  name = PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME,
		  mandatory = true)
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
