 /** 
 * Copyright (c) 2007-2009, Regents of the University of Colorado 
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
package org.cleartk.classifier;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.Initializable;
import org.cleartk.util.UIMAUtil;

/**
 * <br>Copyright (c) 2007-2009, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */
public class AnnotationHandlerFactory {

	public static AnnotationHandler<?> createAnnotationHandler(UimaContext context, String paramName) throws ResourceInitializationException {
		return create(context, paramName, AnnotationHandler.class);
	}
	
	public static SequentialAnnotationHandler<?> createSequentialAnnotationHandler(UimaContext context, String paramName) throws ResourceInitializationException {
		return create(context, paramName, SequentialAnnotationHandler.class);
	}
	
	private static <T> T create(UimaContext context, String paramName, Class<T> superClass) throws ResourceInitializationException {
		// get the class name from the parameter
		Object className = UIMAUtil.getRequiredConfigParameterValue(context, paramName);

		// create a new instance
		T instance;
		try {
			Class<?> cls = Class.forName((String) className);
			instance = cls.asSubclass(superClass).newInstance();
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
		// initialize and return the SequentialAnnotationHandler
		if (instance instanceof Initializable) {
			((Initializable)instance).initialize(context);
		}
		return instance;
	}
	
}
