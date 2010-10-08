/**
 * Copyright (c) 2010, University of Würzburg<br>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Würzburg nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
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
package org.cleartk.classifier.grmm;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;

import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.NameNumber;
import org.cleartk.classifier.jar.JarSequentialClassifier;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Labels;
import edu.umass.cs.mallet.base.types.LabelsSequence;
import edu.umass.cs.mallet.grmm.learning.ACRF;

/**
  * <br>
 * Copyright (c) 2010, University of Würzburg<br>
 * All rights reserved.
 * <p>
 * @author Martin Toepfer
 */
public class GrmmClassifier extends
		JarSequentialClassifier<String[], String[], List<NameNumber>> {

	protected ACRF acrf;
	protected String outcomeExample;

	public GrmmClassifier(JarFile modelFile) throws Exception {
		super(modelFile);

		ZipEntry modelEntry = modelFile
				.getEntry(GrmmClassifierBuilder.JAR_ENTRY_MODEL);
		InputStream stream = modelFile.getInputStream(modelEntry);
		GZIPInputStream gzipInputStream = new GZIPInputStream(stream);
		ObjectInputStream objectStream = new ObjectInputStream(gzipInputStream);
		this.acrf = (ACRF) objectStream.readObject();
		ZipEntry outcomeExampleEntry = modelFile
				.getEntry(GrmmClassifierBuilder.JAR_ENTRY_OUTCOME_EXAMPLE);
		this.outcomeExample = outcomeExampleEntry.getComment();
	}

	/**
	 * This method classifies several instances at once
	 * 
	 * @param features
	 *            a list of lists of features - each list in the list represents
	 *            one instance to be classified. The list should correspond to
	 *            some logical sequence of instances to be classified (e.g.
	 *            tokens in a sentence or lines in a document) that corresponds
	 *            to the model that has been built for this classifier.
	 * @throws CleartkException
	 */
	public List<String[]> classifySequence(final List<List<Feature>> features)
			throws CleartkException {
		// generate format that is appropriate for the acrf input pipe:
		String data = "";
		{
			StringWriter out = new StringWriter();
			PrintWriter printWriter = new PrintWriter(out);
			for (List<Feature> f : features) {
				List<NameNumber> nameNumbers = this.featuresEncoder
						.encodeAll(f);
				GrmmDataWriter.writeEncoded(nameNumbers,
						this.outcomeExample.split(" "), printWriter);
			}
			data = out.toString();
		}
		// classify:
		Pipe pipe = acrf.getInputPipe();
		LabelsSequence bestLabels = acrf.getBestLabels(new Instance(data, null,
				"", null, pipe));
		List<String[]> returnValues = new ArrayList<String[]>(features.size());
		for (int i = 0; i < bestLabels.size(); i++) {
			Labels labels = bestLabels.getLabels(i);
			String[] outcomes = new String[labels.size()];
			for (int j = 0; j < labels.size(); j++) {
				outcomes[j] = labels.get(j).getBestLabel().toString();
			}
			returnValues.add(outcomes);
		}
		return returnValues;
	}
}
