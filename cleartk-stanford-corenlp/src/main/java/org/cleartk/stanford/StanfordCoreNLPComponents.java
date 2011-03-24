package org.cleartk.stanford;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

public class StanfordCoreNLPComponents {

  public static String[] TYPE_SYSTEM_DESCRIPTOR_NAMES = new String[] {
      "org.cleartk.token.TypeSystem",
      "org.cleartk.ne.TypeSystem",
      "org.cleartk.syntax.TypeSystem",
      "org.cleartk.syntax.dependency.TypeSystem" };

  public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory
      .createTypeSystemDescription(
          "org.cleartk.token.TypeSystem",
          "org.cleartk.ne.TypeSystem",
          "org.cleartk.syntax.TypeSystem",
          "org.cleartk.syntax.dependency.TypeSystem");
}
