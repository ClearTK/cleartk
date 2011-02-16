package org.cleartk.syntax.constituent;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.constituent.types.InputTypesHelper;
import org.cleartk.syntax.constituent.types.OutputTypesHelper;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;

public abstract class ParserWrapper_ImplBase<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation, PARSE_TYPE, TOP_NODE_TYPE extends Annotation> extends JCasAnnotator_ImplBase{

  public static final String PARAM_INPUT_TYPES_HELPER_CLASS_NAME = ConfigurationParameterFactory
  .createConfigurationParameterName(ParserWrapper_ImplBase.class, "inputTypesHelperClassName");

  @ConfigurationParameter(defaultValue = "org.cleartk.syntax.constituent.types.DefaultInputTypesHelper", mandatory = true)
  protected String inputTypesHelperClassName;

  protected InputTypesHelper<TOKEN_TYPE, SENTENCE_TYPE> inputTypesHelper;

  public static final String PARAM_OUTPUT_TYPES_HELPER_CLASS_NAME = ConfigurationParameterFactory
  .createConfigurationParameterName(ParserWrapper_ImplBase.class, "outputTypesHelperClassName");

  @ConfigurationParameter(mandatory=true)
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
