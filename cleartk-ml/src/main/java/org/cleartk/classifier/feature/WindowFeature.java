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
package org.cleartk.classifier.feature;

import org.cleartk.classifier.Feature;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * <p>
 * 
 * @author Philip Ogren
 * 
 */

public class WindowFeature extends Feature {
	public static final String ORIENTATION_LEFT = "L";

	public static final String ORIENTATION_RIGHT = "R";

	public static final String ORIENTATION_MIDDLE = "M";

	public static final String ORIENTATION_MIDDLE_REVERSE = "MR";

	private String orientation = null;

	private Integer position = null;

	private Feature windowedFeature = null;

	private Integer outOfBoundsDistance = 0;

	public WindowFeature(String name, Object value, String orientation, Integer position, Feature windowedFeature,
			Integer outOfBoundsDistance) {
		super(value);
		this.orientation = orientation;
		this.position = position;
		this.windowedFeature = windowedFeature;
		this.outOfBoundsDistance = outOfBoundsDistance;
		this.name = createName(name);
	}

	public WindowFeature(String name, Object value, String orientation, Integer position, Feature windowedFeature) {
		this(name, value, orientation, position, windowedFeature, null);
	}

	public WindowFeature(String name, Object value, String orientation, Integer position, Integer outOfBoundsDistance) {
		this(name, value, orientation, position, null, outOfBoundsDistance);
	}

	private String createName(String namePrefix) {
		if (namePrefix == null) namePrefix = "Window";

		StringBuffer sb = new StringBuffer();
		if (orientation != null) sb.append(orientation);
		if (position != null) sb.append(position);

		if (outOfBoundsDistance != null && outOfBoundsDistance > 0) sb.append("OOB" + outOfBoundsDistance);

		String windowedFeatureName = null;
		if (windowedFeature != null) windowedFeatureName = windowedFeature.getName();

		return Feature.createName(namePrefix, sb.toString(), windowedFeatureName);

	}

	public String getOrientation() {
		return orientation;
	}

	public int getOutOfBoundsDistance() {
		return outOfBoundsDistance;
	}

	public int getPosition() {
		return position;
	}

	public Feature getWindowedFeature() {
		return windowedFeature;
	}

}
