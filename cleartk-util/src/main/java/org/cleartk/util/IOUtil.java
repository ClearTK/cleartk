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
package org.cleartk.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class IOUtil {

  /**
   * This method attempts to create an input stream from the given path by:
   * <ol>
   * <li>Trying to parse it as a URL</li>
   * <li>Trying to find it on the classpath, relative to the given class</li>
   * <li>Trying to find it as a file on the file system</li>
   * </ol>
   * 
   * @param cls
   *          this will generally be the class of the caller.
   * @param path
   *          should be the path of a resource on the classpath (e.g. "/models/en-sent.bin") or the
   *          path of a file on the local file system (e.g. "src/main/resources/models/en-sent.bin")
   * @return A newly opened InputStream. The caller is responsible for closing the stream.
   * @throws IOException
   *           if the path was not found as a URL, resource or file.
   */
  public static InputStream getInputStream(Class<?> cls, String path) throws IOException {
    InputStream inputStream;
    try {
      inputStream = new URL(path).openStream();
    } catch (MalformedURLException e) {
      inputStream = cls.getResourceAsStream(path);
      if (inputStream == null) {
        try {
          inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e1) {
          throw new IOException(String.format("unable to find %s "
              + "as a resource on the classpath, as a url or as a file.", path));
        }
      }
    }
    return new BufferedInputStream(inputStream);
  }
}
