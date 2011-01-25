/** 
 * Copyright (c) 2011, Regents of the University of Colorado 
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
package org.cleartk.timeml;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TimeMLAnnotateTest {
  private File tempDir;

  private File inputFile;

  private File outputFile;

  @Before
  public void setUp() throws Exception {
    this.tempDir = File.createTempFile("TimeMLAnnotateTest", "");
    this.tempDir.delete();
    this.tempDir.mkdir();
    this.inputFile = new File(this.tempDir, "input.txt");
    FileUtils.writeStringToFile(this.inputFile, "They met for dinner. He said he bought stocks.");
    this.outputFile = new File(this.tempDir, "input.txt.tml");
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(this.tempDir);
  }

  @Test
  public void test() throws Exception {
    TimeMLAnnotate.main(this.inputFile.getPath(), this.tempDir.getPath());
    String output = FileUtils.readFileToString(this.outputFile);
    output = output.replaceAll("\r\n", "\n");
    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<TimeML>"
        + "They <EVENT eid=\"e1\" tense=\"PAST\">met</EVENT> for dinner. "
        + "He <EVENT eid=\"e2\" class=\"REPORTING\" tense=\"PAST\">said</EVENT> "
        + "he <EVENT eid=\"e3\" tense=\"PAST\">bought</EVENT> stocks."
        + "<TLINK relType=\"AFTER\" eventID=\"e2\" relatedToEvent=\"e3\" />\n" + "</TimeML>\n";
    Assert.assertEquals("TimeML output should match", expected, output);
  }

}
