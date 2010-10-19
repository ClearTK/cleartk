package org.cleartk.syntax;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

public class SyntaxComponents {

	public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory
	.createTypeSystemDescription("org.cleartk.syntax.constituent.type.TypeSystem");
}
