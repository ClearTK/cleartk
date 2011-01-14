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
import java.io.IOException;
import java.io.InputStream;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */

public class IOUtil {

	/**
	 * This method attempts to create an input stream from the given path by
	 * treating it as a resource that is available on the classpath. If this
	 * does not produce a good input stream, then it will attempt to create an
	 * input stream from the path by treating it as a file name. If this does
	 * not work either, then an IOException is thrown that explains that an
	 * input stream could not be created from the given path. This method
	 * assumes that the caller has a path but does not know whether it is a
	 * resource on the classpath or a file name.
	 * 
	 * @param cls
	 *            this will generally be the class of the caller.
	 * @param path
	 *            should be the path of a resource on the classpath (e.g.
	 *            "/models/en-sent.bin") or the path of a file on the local file
	 *            system (e.g. "src/main/resources/models/en-sent.bin")
	 * @return
	 * @throws IOException
	 */
	public static InputStream getInputStream(Class<?> cls, String path)
			throws IOException {
		InputStream inputStream = cls.getResourceAsStream(path);
		if (!isAvailable(inputStream)) {
			inputStream = new FileInputStream(path);
			if (!isAvailable(inputStream)) {
				throw new IOException("unable to find " + path
						+ " as a resource on the classpath or as a file.");
			}
		}
		return new BufferedInputStream(inputStream);
	}
	
	private static boolean isAvailable(InputStream modelInputStream) {
		try {
			if (modelInputStream.available() <= 0) {
				return false;
			}
		} catch (Throwable t) {
			return false;
		}
		return true;
	}
}
