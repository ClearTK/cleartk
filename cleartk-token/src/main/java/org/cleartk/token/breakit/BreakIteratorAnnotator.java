package org.cleartk.token.breakit;

import java.lang.reflect.Constructor;
import java.text.BreakIterator;
import java.util.Locale;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.initializable.InitializableFactory;

public class BreakIteratorAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_LOCALE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
			BreakIteratorAnnotator.class, "localeName"); 
	
	@ConfigurationParameter(description = "provides the name of the locale to be used to instantiate the break iterator")
	private Locale locale;

	public static enum BreakIteratorType {
		WORD, SENTENCE, CHARACTER, LINE
	}

	public static final String PARAM_BREAK_ITERATOR_TYPE = ConfigurationParameterFactory.createConfigurationParameterName(
			BreakIteratorAnnotator.class, "breakIteratorType"); 
	
	@ConfigurationParameter(description = "provides the type of the locale to be used to instantiate the break iterator.  Should be one of 'CHARACTER', 'WORD', 'SENTENCE', or 'LINE'")
	private BreakIteratorType breakIteratorType;
	
	public static final String PARAM_ANNOTATION_TYPE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(BreakIteratorAnnotator.class, "tokenTypeName");

	@ConfigurationParameter (description = "class type of the annotations that are created by this annotator.")
	private String annotationTypeName;

	private Class<? extends Annotation> annotationClass;

	private Constructor<? extends Annotation> annotationConstructor;

	private BreakIterator breakIterator;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		try {
			annotationClass = InitializableFactory.getClass(annotationTypeName, Annotation.class);
			annotationConstructor = annotationClass.getConstructor(new Class[] { JCas.class, Integer.TYPE, Integer.TYPE });
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}



	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

	}

}
