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
 * with the cleartk-syntax-berkeley project or visit 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.syntax.berkeley;

import org.cleartk.test.util.ParametersTestUtil;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */
public class ParametersTest {

  @Test
  public void testParameterNames() throws ClassNotFoundException {
    ParametersTestUtil.testParameterDefinitions("src/main/java");
  }
}
