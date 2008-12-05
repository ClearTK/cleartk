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
package org.cleartk.classifier.encoder.factory;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.encoder.EncoderFactory;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 *
 * @author Philipp Wetzler
 */
public class FileSystemFallbackEncoderFactory extends FileSystemEncoderFactory {

	public static final String PARAM_FALLBACK_ENCODER_FACTORY_CLASS = "FallbackEncoderFactoryClass";
	
	@Override
	protected void initialize(UimaContext context) {
		super.initialize(context);
		
		if( featuresEncoder != null && outcomeEncoder != null )
			return;

		try {
			String fallbackEncoderFactoryName = (String) UIMAUtil.getRequiredConfigParameterValue(context, PARAM_FALLBACK_ENCODER_FACTORY_CLASS);
			EncoderFactory fallbackFactory = (EncoderFactory) Class.forName(fallbackEncoderFactoryName).newInstance();

			featuresEncoder = fallbackFactory.createFeaturesEncoder(context);
			outcomeEncoder = fallbackFactory.createOutcomeEncoder(context);
		} catch (ResourceInitializationException e) {
			// TODO: Improve exception handling
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			// TODO: Improve exception handling
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			// TODO: Improve exception handling
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			// TODO: Improve exception handling
			throw new RuntimeException(e);
		}
	}
}
