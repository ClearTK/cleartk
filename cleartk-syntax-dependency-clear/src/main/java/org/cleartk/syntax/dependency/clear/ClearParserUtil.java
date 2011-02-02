/*
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

package org.cleartk.syntax.dependency.clear;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import clear.decode.AbstractMultiDecoder;
import clear.decode.OneVsAllDecoder;
import clear.ftr.map.DepFtrMap;
import clear.ftr.xml.DepFtrXml;
import clear.parse.AbstractDepParser;
import clear.parse.ShiftEagerParser;
import clear.parse.ShiftPopParser;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * This class was mostly written by Jinho Choi and will hopefully be folded back into the Clear
 * Parser code. See the following issue on the Clear Parser project page:
 * 
 * http://code.google.com/p/clearparser/issues/detail?id=2
 * 
 */

public class ClearParserUtil {

  public static AbstractDepParser createParser(String modelFileName, String algorithmName)
      throws IOException {
    ZipInputStream zin = new ZipInputStream(new FileInputStream(modelFileName));
    ZipEntry zEntry;

    DepFtrXml xml = null;
    DepFtrMap map = null;
    AbstractMultiDecoder decoder = null;

    final String ENTRY_LEXICA = "lexica";
    final String ENTRY_MODEL = "model";
    final String ENTRY_FEATURE = "feature";

    while ((zEntry = zin.getNextEntry()) != null) {
      if (zEntry.getName().equals(ENTRY_FEATURE)) {
        System.out.println("- loading feature template");

        BufferedReader reader = new BufferedReader(new InputStreamReader(zin));
        StringBuilder build = new StringBuilder();
        String string;

        while ((string = reader.readLine()) != null) {
          build.append(string);
          build.append("\n");
        }

        xml = new DepFtrXml(new ByteArrayInputStream(build.toString().getBytes()));
      }

      if (zEntry.getName().equals(ENTRY_LEXICA)) {
        System.out.println("- loading lexica");
        map = new DepFtrMap(xml, new BufferedReader(new InputStreamReader(zin)));
      } else if (zEntry.getName().equals(ENTRY_MODEL)) {
        System.out.println("- loading model");
        decoder = new OneVsAllDecoder(new BufferedReader(new InputStreamReader(zin)));
      }
    }

    if (algorithmName.equals(AbstractDepParser.ALG_SHIFT_EAGER))
      return new ShiftEagerParser(AbstractDepParser.FLAG_PREDICT, xml, map, decoder);
    else if (algorithmName.equals(AbstractDepParser.ALG_SHIFT_POP))
      return new ShiftPopParser(AbstractDepParser.FLAG_PREDICT, xml, map, decoder);
    else
      return null;
  }

}
