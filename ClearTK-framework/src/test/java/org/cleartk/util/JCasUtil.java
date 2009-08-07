package org.cleartk.util;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.JCasAnnotatorAdapter;

public class JCasUtil {

	public static ThreadLocal<JCas> JCAS = new ThreadLocal<JCas>();
	public static ThreadLocal<TypeSystemDescription> TYPE_SYSTEM = new ThreadLocal<TypeSystemDescription>();
	public static ThreadLocal<AnalysisEngine> AE_ADAPTER = new ThreadLocal<AnalysisEngine>();

	static {
		try {
			TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescription("org.cleartk.TestTypeSystem");
			TYPE_SYSTEM.set(tsd);

			AnalysisEngine aeAdapter = AnalysisEngineFactory.createPrimitive(
					JCasAnnotatorAdapter.class,
					tsd);
			AE_ADAPTER.set(aeAdapter);
			
			JCas jCas = aeAdapter.newJCas();
			JCAS.set(jCas);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	public static JCas getJCas() {
		JCas jCas = JCAS.get();
		jCas.reset();
		return jCas;
	}

	public static TypeSystemDescription getTypeSystemDescription() {
		return TYPE_SYSTEM.get();
	}

}
