package org.cleartk.syntax.dependency;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

public class DependencyComponents {

  public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory
      .createTypeSystemDescription(
          "org.cleartk.token.TypeSystem",
          "org.cleartk.syntax.dependency.TypeSystem");

}
