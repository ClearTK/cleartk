/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
 * with the cleartk-stanford-corenlp project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.stanford;

import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.factory.TypeSystemDescriptionFactory;

/**
 * 
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
@Deprecated
public class StanfordCoreNLPComponents {

  public static String[] TYPE_SYSTEM_DESCRIPTOR_NAMES = new String[] {
      "org.cleartk.token.TypeSystem",
      "org.cleartk.ne.TypeSystem",
      "org.cleartk.syntax.constituent.TypeSystem",
      "org.cleartk.syntax.dependency.TypeSystem" };

  public static TypeSystemDescription TYPE_SYSTEM_DESCRIPTION = TypeSystemDescriptionFactory.createTypeSystemDescription(
      "org.cleartk.token.TypeSystem",
      "org.cleartk.ne.TypeSystem",
      "org.cleartk.syntax.constituent.TypeSystem",
      "org.cleartk.syntax.dependency.TypeSystem");
}
