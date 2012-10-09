/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.classifier.feature.selection;

import org.cleartk.classifier.feature.selection.MutualInformationFeatureSelectionExtractor.MutualInformationStats;
import org.cleartk.test.DefaultTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2007-2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Lee Becker
 */
public class MutualInformationTest extends DefaultTestBase {

  @Test
  public void testMutualInformationStats() {
    MutualInformationStats<String> stats = new MutualInformationStats<String>();
    stats.update("export", "poultry", 49);
    stats.update("export", "not_poultry", 141);
    stats.update("not_export", "poultry", 27652);
    stats.update("not_export", "not_poultry", 774106);

    Assert.assertEquals((int) stats.classConditionalCounts.get("export", "poultry"), 49);
    Assert.assertEquals((int) stats.classConditionalCounts.get("export", "not_poultry"), 141);
    Assert.assertEquals((int) stats.classConditionalCounts.get("not_export", "poultry"), 27652);
    Assert.assertEquals((int) stats.classConditionalCounts.get("not_export", "not_poultry"), 774106);

    Assert.assertEquals(stats.classCounts.count("poultry"), 27701);
    Assert.assertEquals(stats.classCounts.count("not_poultry"), 774247);

    Assert.assertEquals(stats.mutualInformation("export", "poultry"), 0.0001105, 0.00000005);
    Assert.assertEquals(stats.mutualInformation("export", "not_poultry"), 0.0001105, 0.00000005);
    Assert.assertEquals(stats.mutualInformation("not_export", "poultry"), 0.0001105, 0.00000005);
    Assert.assertEquals(stats.mutualInformation("not_export", "not_poultry"), 0.0001105, 0.00000005);
  }

}
