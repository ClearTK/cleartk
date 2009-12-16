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
package org.cleartk.classifier.mallet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.SequentialClassifier;
import org.cleartk.classifier.CleartkSequentialAnnotator;
import org.cleartk.classifier.Train;
import org.cleartk.util.JCasUtil;
import org.junit.After;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.util.HideOutput;
import org.uutuc.util.TearDownUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * One thing that may seem confusing about the code below is why I didn't create
 * a single List of instances to be shared by the two annotation handlers and
 * the test method. The reason is that there are three instances of
 * MalletCRFClassifierTest that are created when this test is run. The first can
 * be accessed with 'this' in the test method. The other two are created when
 * the annotation handler is created by UIMAUtil.create(). Therefore, sharing
 * instance data at the MalletCRFClassifierTest level is not going to work.
 * 
 * @author Philip Ogren
 */

public class MalletCRFClassifierTest {

	public static class TestAnnotator extends CleartkSequentialAnnotator<String> {
		public void process(JCas cas) throws AnalysisEngineProcessException {
			try {
				this.processSimple(cas);
			} catch (CleartkException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
		public void processSimple(JCas cas) throws AnalysisEngineProcessException, CleartkException {
			if (this.isTraining()) {
				List<Instance<String>> instances =  createInstances();
				//consume 100 identical sequences
				for(int i=0; i<100; i++) {
					this.dataWriter.writeSequence(instances);
				}
			} else {
				List<Instance<String>> instances =  createInstances();
				List<String> outcomes =	this.classifySequence(instances);
				assertEquals(instances.size(), outcomes.size());
				testLabels(outcomes, "O O O O O O O O O O O O O O O B-GENE I-GENE I-GENE O B-GENE I-GENE O O O O O O O O O O O O O O O O O O O O O");
			}
		}
	}

	private String outputDirectory = "test/data/mallet/mallet-crf-classifier";

	@After
	public void tearDown() throws Exception {
		File outputDirectory = new File(this.outputDirectory);
		TearDownUtil.removeDirectory(outputDirectory);
		// Some files will get left around because mallet CRF doesn't close its
		// handle on the training-data.maxent file. If this ever gets fixed,
		// we should uncomment the following line:
		// Assert.assertFalse(outputDirectory.exists());
	}
	
	@Test
	public void runTest1() throws Exception {

		AnalysisEngine sequentialDataWriterAnnotator = AnalysisEngineFactory.createPrimitive(
				TestAnnotator.class, JCasUtil.getTypeSystemDescription(), 
				CleartkSequentialAnnotator.PARAM_OUTPUT_DIRECTORY, outputDirectory,
				CleartkSequentialAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, DefaultMalletCRFDataWriterFactory.class.getName());

		JCas jCas = JCasUtil.getJCas();
		sequentialDataWriterAnnotator.process(jCas);
		sequentialDataWriterAnnotator.collectionProcessComplete();

		BufferedReader reader = new BufferedReader(new FileReader(new File(outputDirectory, MalletCRFDataWriter.TRAINING_DATA_FILE_NAME)));
		reader.readLine();
		reader.close();
		HideOutput hider = new HideOutput();
		Train.main(outputDirectory);
		hider.restoreOutput();
		
		
		JarFile modelFile = new JarFile(new File(outputDirectory, "model.jar"));
		MalletCRFClassifier classifier = new MalletCRFClassifier(modelFile);
		modelFile.close();
		assertTrue(classifier instanceof SequentialClassifier<?>);
		
		List<List<Feature>> sequenceFeatures = new ArrayList<List<Feature>>();
		List<Instance<String>> instances = createInstances();
		for(Instance<String> instance : instances) {
			sequenceFeatures.add(instance.getFeatures());
		}
		
		List<String> outcomes = classifier.classifySequence(sequenceFeatures);
		assertEquals(sequenceFeatures.size(), outcomes.size());
		testLabels(outcomes, "O O O O O O O O O O O O O O O B-GENE I-GENE I-GENE O B-GENE I-GENE O O O O O O O O O O O O O O O O O O O O O");
		
		AnalysisEngine sequentialClassifierAnnotator = AnalysisEngineFactory.createPrimitive(
				TestAnnotator.class, JCasUtil.getTypeSystemDescription(),
				CleartkSequentialAnnotator.PARAM_CLASSIFIER_JAR_PATH, outputDirectory+"/model.jar");
		jCas.reset();
		sequentialClassifierAnnotator.process(jCas);
		sequentialClassifierAnnotator.collectionProcessComplete();

	}

	private static void testLabels(List<String> outcomes, String expectedData) {
		String[] expectedValues = expectedData.split(" ");
		assertEquals(expectedValues.length, outcomes.size());
		for(int i=0; i<expectedValues.length; i++) {
			assertEquals(String.format("missed label %1$d:", i),expectedValues[i], outcomes.get(i));
		}
	}
	private static Instance<String> createInstance(String data){
		Instance<String> instance = new Instance<String>();
		String[] columns = data.split(" ");
		instance.setOutcome(columns[0]);
		for(int i=1; i<columns.length; i++) {
			instance.add(new Feature(columns[i]));
		}
		return instance;
	}

	private static List<Instance<String>> createInstances(){
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		instances.add(createInstance("O Word_Three LCWord_three CapitalType_INITIAL_UPPERCASE L0OOB1 L1OOB2 R0_sequence R0_TypePath_Pos_NN R0_TypePath_Stem_sequenc R1_elements R1_TypePath_Pos_NNS R1_TypePath_Stem_element TypePath_Pos_CD TypePath_Stem_Three PrevNEMTokenLabel_L0OOB1 PrevNEMTokenLabel_L1OOB2"));
		instances.add(createInstance("O Word_sequence LCWord_sequence CapitalType_ALL_LOWERCASE Prefix3_seq Suffix3_nce Suffix4_ence Suffix5_uence L0_Three L0_TypePath_Pos_CD L0_TypePath_Stem_Three L1OOB1 R0_elements R0_TypePath_Pos_NNS R0_TypePath_Stem_element R1_are R1_TypePath_Pos_VBP R1_TypePath_Stem_are TypePath_Pos_NN TypePath_Stem_sequenc PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1OOB1"));
		instances.add(createInstance("O Word_elements LCWord_elements CapitalType_ALL_LOWERCASE Prefix3_ele Suffix3_nts Suffix4_ents Suffix5_ments L0_sequence L0_TypePath_Pos_NN L0_TypePath_Stem_sequenc L1_Three L1_TypePath_Pos_CD L1_TypePath_Stem_Three R0_are R0_TypePath_Pos_VBP R0_TypePath_Stem_are R1_shown R1_TypePath_Pos_VBN R1_TypePath_Stem_shown TypePath_Pos_NNS TypePath_Stem_element PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_are LCWord_are CapitalType_ALL_LOWERCASE L0_elements L0_TypePath_Pos_NNS L0_TypePath_Stem_element L1_sequence L1_TypePath_Pos_NN L1_TypePath_Stem_sequenc R0_shown R0_TypePath_Pos_VBN R0_TypePath_Stem_shown R1_to R1_TypePath_Pos_TO R1_TypePath_Stem_to TypePath_Pos_VBP TypePath_Stem_are PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_shown LCWord_shown CapitalType_ALL_LOWERCASE L0_are L0_TypePath_Pos_VBP L0_TypePath_Stem_are L1_elements L1_TypePath_Pos_NNS L1_TypePath_Stem_element R0_to R0_TypePath_Pos_TO R0_TypePath_Stem_to R1_be R1_TypePath_Pos_VB R1_TypePath_Stem_be TypePath_Pos_VBN TypePath_Stem_shown PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_to LCWord_to CapitalType_ALL_LOWERCASE L0_shown L0_TypePath_Pos_VBN L0_TypePath_Stem_shown L1_are L1_TypePath_Pos_VBP L1_TypePath_Stem_are R0_be R0_TypePath_Pos_VB R0_TypePath_Stem_be R1_required R1_TypePath_Pos_VBN R1_TypePath_Stem_requir TypePath_Pos_TO TypePath_Stem_to PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_be LCWord_be CapitalType_ALL_LOWERCASE L0_to L0_TypePath_Pos_TO L0_TypePath_Stem_to L1_shown L1_TypePath_Pos_VBN L1_TypePath_Stem_shown R0_required R0_TypePath_Pos_VBN R0_TypePath_Stem_requir R1_for R1_TypePath_Pos_IN R1_TypePath_Stem_for TypePath_Pos_VB TypePath_Stem_be PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_required LCWord_required CapitalType_ALL_LOWERCASE Prefix3_req Suffix3_red Suffix4_ired Suffix5_uired L0_be L0_TypePath_Pos_VB L0_TypePath_Stem_be L1_to L1_TypePath_Pos_TO L1_TypePath_Stem_to R0_for R0_TypePath_Pos_IN R0_TypePath_Stem_for R1_accurate R1_TypePath_Pos_JJ R1_TypePath_Stem_accur TypePath_Pos_VBN TypePath_Stem_requir PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_for LCWord_for CapitalType_ALL_LOWERCASE L0_required L0_TypePath_Pos_VBN L0_TypePath_Stem_requir L1_be L1_TypePath_Pos_VB L1_TypePath_Stem_be R0_accurate R0_TypePath_Pos_JJ R0_TypePath_Stem_accur R1_and R1_TypePath_Pos_CC R1_TypePath_Stem_and TypePath_Pos_IN TypePath_Stem_for PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_accurate LCWord_accurate CapitalType_ALL_LOWERCASE Prefix3_acc Suffix3_ate Suffix4_rate Suffix5_urate L0_for L0_TypePath_Pos_IN L0_TypePath_Stem_for L1_required L1_TypePath_Pos_VBN L1_TypePath_Stem_requir R0_and R0_TypePath_Pos_CC R0_TypePath_Stem_and R1_efficient R1_TypePath_Pos_JJ R1_TypePath_Stem_effici TypePath_Pos_JJ TypePath_Stem_accur PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_and LCWord_and CapitalType_ALL_LOWERCASE L0_accurate L0_TypePath_Pos_JJ L0_TypePath_Stem_accur L1_for L1_TypePath_Pos_IN L1_TypePath_Stem_for R0_efficient R0_TypePath_Pos_JJ R0_TypePath_Stem_effici R1_transcription R1_TypePath_Pos_NN R1_TypePath_Stem_transcript TypePath_Pos_CC TypePath_Stem_and PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_efficient LCWord_efficient CapitalType_ALL_LOWERCASE Prefix3_eff Suffix3_ent Suffix4_ient Suffix5_cient L0_and L0_TypePath_Pos_CC L0_TypePath_Stem_and L1_accurate L1_TypePath_Pos_JJ L1_TypePath_Stem_accur R0_transcription R0_TypePath_Pos_NN R0_TypePath_Stem_transcript R1_termination R1_TypePath_Pos_NN R1_TypePath_Stem_termin TypePath_Pos_JJ TypePath_Stem_effici PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_transcription LCWord_transcription CapitalType_ALL_LOWERCASE Prefix3_tra Suffix3_ion Suffix4_tion Suffix5_ption L0_efficient L0_TypePath_Pos_JJ L0_TypePath_Stem_effici L1_and L1_TypePath_Pos_CC L1_TypePath_Stem_and R0_termination R0_TypePath_Pos_NN R0_TypePath_Stem_termin R1_by R1_TypePath_Pos_IN R1_TypePath_Stem_by TypePath_Pos_NN TypePath_Stem_transcript PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_termination LCWord_termination CapitalType_ALL_LOWERCASE Prefix3_ter Suffix3_ion Suffix4_tion Suffix5_ation L0_transcription L0_TypePath_Pos_NN L0_TypePath_Stem_transcript L1_efficient L1_TypePath_Pos_JJ L1_TypePath_Stem_effici R0_by R0_TypePath_Pos_IN R0_TypePath_Stem_by R1_RNA R1_TypePath_Pos_NNP R1_TypePath_Stem_RNA TypePath_Pos_NN TypePath_Stem_termin PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_by LCWord_by CapitalType_ALL_LOWERCASE L0_termination L0_TypePath_Pos_NN L0_TypePath_Stem_termin L1_transcription L1_TypePath_Pos_NN L1_TypePath_Stem_transcript R0_RNA R0_TypePath_Pos_NNP R0_TypePath_Stem_RNA R1_polymerase R1_TypePath_Pos_NN R1_TypePath_Stem_polymeras TypePath_Pos_IN TypePath_Stem_by PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("B-GENE Word_RNA LCWord_rna CapitalType_ALL_UPPERCASE L0_by L0_TypePath_Pos_IN L0_TypePath_Stem_by L1_termination L1_TypePath_Pos_NN L1_TypePath_Stem_termin R0_polymerase R0_TypePath_Pos_NN R0_TypePath_Stem_polymeras R1_I R1_TypePath_Pos_PRP R1_TypePath_Stem_I TypePath_Pos_NNP TypePath_Stem_RNA PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("I-GENE Word_polymerase LCWord_polymerase CapitalType_ALL_LOWERCASE Prefix3_pol Suffix3_ase Suffix4_rase Suffix5_erase L0_RNA L0_TypePath_Pos_NNP L0_TypePath_Stem_RNA L1_by L1_TypePath_Pos_IN L1_TypePath_Stem_by R0_I R0_TypePath_Pos_PRP R0_TypePath_Stem_I R1_( R1_TypePath_Pos_-LRB- R1_TypePath_Stem_( TypePath_Pos_NN TypePath_Stem_polymeras PrevNEMTokenLabel_L0_B-GENE PrevNEMTokenLabel_L1_O Gazetteer_entrez_genes.txt Gazetteer_entrez_genes.txt"));
		instances.add(createInstance("I-GENE Word_I LCWord_i CapitalType_ALL_UPPERCASE NumericType_ROMAN_NUMERAL L0_polymerase L0_TypePath_Pos_NN L0_TypePath_Stem_polymeras L1_RNA L1_TypePath_Pos_NNP L1_TypePath_Stem_RNA R0_( R0_TypePath_Pos_-LRB- R0_TypePath_Stem_( R1_pol R1_TypePath_Pos_NN R1_TypePath_Stem_pol TypePath_Pos_PRP TypePath_Stem_I PrevNEMTokenLabel_L0_I-GENE PrevNEMTokenLabel_L1_B-GENE"));
		instances.add(createInstance("O Word_( LCWord_( L0_I L0_TypePath_Pos_PRP L0_TypePath_Stem_I L1_polymerase L1_TypePath_Pos_NN L1_TypePath_Stem_polymeras R0_pol R0_TypePath_Pos_NN R0_TypePath_Stem_pol R1_I R1_TypePath_Pos_PRP R1_TypePath_Stem_I TypePath_Pos_-LRB- TypePath_Stem_( PrevNEMTokenLabel_L0_I-GENE PrevNEMTokenLabel_L1_I-GENE"));
		instances.add(createInstance("B-GENE Word_pol LCWord_pol CapitalType_ALL_LOWERCASE L0_( L0_TypePath_Pos_-LRB- L0_TypePath_Stem_( L1_I L1_TypePath_Pos_PRP L1_TypePath_Stem_I R0_I R0_TypePath_Pos_PRP R0_TypePath_Stem_I R1_) R1_TypePath_Pos_-RRB- R1_TypePath_Stem_) TypePath_Pos_NN TypePath_Stem_pol PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_I-GENE Gazetteer_entrez_genes.txt"));
		instances.add(createInstance("I-GENE Word_I LCWord_i CapitalType_ALL_UPPERCASE NumericType_ROMAN_NUMERAL L0_pol L0_TypePath_Pos_NN L0_TypePath_Stem_pol L1_( L1_TypePath_Pos_-LRB- L1_TypePath_Stem_( R0_) R0_TypePath_Pos_-RRB- R0_TypePath_Stem_) R1_assayed R1_TypePath_Pos_VBD R1_TypePath_Stem_assay TypePath_Pos_PRP TypePath_Stem_I PrevNEMTokenLabel_L0_B-GENE PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_) LCWord_) L0_I L0_TypePath_Pos_PRP L0_TypePath_Stem_I L1_pol L1_TypePath_Pos_NN L1_TypePath_Stem_pol R0_assayed R0_TypePath_Pos_VBD R0_TypePath_Stem_assay R1_both R1_TypePath_Pos_DT R1_TypePath_Stem_both TypePath_Pos_-RRB- TypePath_Stem_) PrevNEMTokenLabel_L0_I-GENE PrevNEMTokenLabel_L1_B-GENE"));
		instances.add(createInstance("O Word_assayed LCWord_assayed CapitalType_ALL_LOWERCASE Prefix3_ass Suffix3_yed Suffix4_ayed L0_) L0_TypePath_Pos_-RRB- L0_TypePath_Stem_) L1_I L1_TypePath_Pos_PRP L1_TypePath_Stem_I R0_both R0_TypePath_Pos_DT R0_TypePath_Stem_both R1_in R1_TypePath_Pos_IN R1_TypePath_Stem_in TypePath_Pos_VBD TypePath_Stem_assay PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_I-GENE"));
		instances.add(createInstance("O Word_both LCWord_both CapitalType_ALL_LOWERCASE L0_assayed L0_TypePath_Pos_VBD L0_TypePath_Stem_assay L1_) L1_TypePath_Pos_-RRB- L1_TypePath_Stem_) R0_in R0_TypePath_Pos_IN R0_TypePath_Stem_in R1_a R1_TypePath_Pos_DT R1_TypePath_Stem_a TypePath_Pos_DT TypePath_Stem_both PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_in LCWord_in CapitalType_ALL_LOWERCASE L0_both L0_TypePath_Pos_DT L0_TypePath_Stem_both L1_assayed L1_TypePath_Pos_VBD L1_TypePath_Stem_assay R0_a R0_TypePath_Pos_DT R0_TypePath_Stem_a R1_cell-free R1_TypePath_Pos_JJ R1_TypePath_Stem_cell-fre TypePath_Pos_IN TypePath_Stem_in PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_a LCWord_a CapitalType_ALL_LOWERCASE L0_in L0_TypePath_Pos_IN L0_TypePath_Stem_in L1_both L1_TypePath_Pos_DT L1_TypePath_Stem_both R0_cell-free R0_TypePath_Pos_JJ R0_TypePath_Stem_cell-fre R1_transcription R1_TypePath_Pos_NN R1_TypePath_Stem_transcript TypePath_Pos_DT TypePath_Stem_a PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_cell-free LCWord_cell-free CapitalType_ALL_LOWERCASE ContainsHyphen_CONTAINS_HYPHEN Prefix3_cel Suffix3_ree Suffix4_free Suffix5_-free L0_a L0_TypePath_Pos_DT L0_TypePath_Stem_a L1_in L1_TypePath_Pos_IN L1_TypePath_Stem_in R0_transcription R0_TypePath_Pos_NN R0_TypePath_Stem_transcript R1_system R1_TypePath_Pos_NN R1_TypePath_Stem_system TypePath_Pos_JJ TypePath_Stem_cell-fre PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_transcription LCWord_transcription CapitalType_ALL_LOWERCASE Prefix3_tra Suffix3_ion Suffix4_tion Suffix5_ption L0_cell-free L0_TypePath_Pos_JJ L0_TypePath_Stem_cell-fre L1_a L1_TypePath_Pos_DT L1_TypePath_Stem_a R0_system R0_TypePath_Pos_NN R0_TypePath_Stem_system R1_and R1_TypePath_Pos_CC R1_TypePath_Stem_and TypePath_Pos_NN TypePath_Stem_transcript PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_system LCWord_system CapitalType_ALL_LOWERCASE Prefix3_sys Suffix3_tem L0_transcription L0_TypePath_Pos_NN L0_TypePath_Stem_transcript L1_cell-free L1_TypePath_Pos_JJ L1_TypePath_Stem_cell-fre R0_and R0_TypePath_Pos_CC R0_TypePath_Stem_and R1_in R1_TypePath_Pos_IN R1_TypePath_Stem_in TypePath_Pos_NN TypePath_Stem_system PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_and LCWord_and CapitalType_ALL_LOWERCASE L0_system L0_TypePath_Pos_NN L0_TypePath_Stem_system L1_transcription L1_TypePath_Pos_NN L1_TypePath_Stem_transcript R0_in R0_TypePath_Pos_IN R0_TypePath_Stem_in R1_vivo R1_TypePath_Pos_RB R1_TypePath_Stem_vivo TypePath_Pos_CC TypePath_Stem_and PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_in LCWord_in CapitalType_ALL_LOWERCASE L0_and L0_TypePath_Pos_CC L0_TypePath_Stem_and L1_system L1_TypePath_Pos_NN L1_TypePath_Stem_system R0_vivo R0_TypePath_Pos_RB R0_TypePath_Stem_vivo R1_after R1_TypePath_Pos_IN R1_TypePath_Stem_after TypePath_Pos_IN TypePath_Stem_in PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_vivo LCWord_vivo CapitalType_ALL_LOWERCASE L0_in L0_TypePath_Pos_IN L0_TypePath_Stem_in L1_and L1_TypePath_Pos_CC L1_TypePath_Stem_and R0_after R0_TypePath_Pos_IN R0_TypePath_Stem_after R1_transfection R1_TypePath_Pos_NN R1_TypePath_Stem_transfect TypePath_Pos_RB TypePath_Stem_vivo PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_after LCWord_after CapitalType_ALL_LOWERCASE L0_vivo L0_TypePath_Pos_RB L0_TypePath_Stem_vivo L1_in L1_TypePath_Pos_IN L1_TypePath_Stem_in R0_transfection R0_TypePath_Pos_NN R0_TypePath_Stem_transfect R1_of R1_TypePath_Pos_IN R1_TypePath_Stem_of TypePath_Pos_IN TypePath_Stem_after PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_transfection LCWord_transfection CapitalType_ALL_LOWERCASE Prefix3_tra Suffix3_ion Suffix4_tion Suffix5_ction L0_after L0_TypePath_Pos_IN L0_TypePath_Stem_after L1_vivo L1_TypePath_Pos_RB L1_TypePath_Stem_vivo R0_of R0_TypePath_Pos_IN R0_TypePath_Stem_of R1_rDNA R1_TypePath_Pos_NNP R1_TypePath_Stem_rDNA TypePath_Pos_NN TypePath_Stem_transfect PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_of LCWord_of CapitalType_ALL_LOWERCASE L0_transfection L0_TypePath_Pos_NN L0_TypePath_Stem_transfect L1_after L1_TypePath_Pos_IN L1_TypePath_Stem_after R0_rDNA R0_TypePath_Pos_NNP R0_TypePath_Stem_rDNA R1_minigene R1_TypePath_Pos_NN R1_TypePath_Stem_minigen TypePath_Pos_IN TypePath_Stem_of PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_rDNA LCWord_rdna CapitalType_MIXED_CASE L0_of L0_TypePath_Pos_IN L0_TypePath_Stem_of L1_transfection L1_TypePath_Pos_NN L1_TypePath_Stem_transfect R0_minigene R0_TypePath_Pos_NN R0_TypePath_Stem_minigen R1_constructs R1_TypePath_Pos_NNS R1_TypePath_Stem_construct TypePath_Pos_NNP TypePath_Stem_rDNA PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_minigene LCWord_minigene CapitalType_ALL_LOWERCASE Prefix3_min Suffix3_ene Suffix4_gene Suffix5_igene L0_rDNA L0_TypePath_Pos_NNP L0_TypePath_Stem_rDNA L1_of L1_TypePath_Pos_IN L1_TypePath_Stem_of R0_constructs R0_TypePath_Pos_NNS R0_TypePath_Stem_construct R1_into R1_TypePath_Pos_IN R1_TypePath_Stem_into TypePath_Pos_NN TypePath_Stem_minigen PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_constructs LCWord_constructs CapitalType_ALL_LOWERCASE Prefix3_con Suffix3_cts Suffix4_ucts Suffix5_ructs L0_minigene L0_TypePath_Pos_NN L0_TypePath_Stem_minigen L1_rDNA L1_TypePath_Pos_NNP L1_TypePath_Stem_rDNA R0_into R0_TypePath_Pos_IN R0_TypePath_Stem_into R1_3T6 R1_TypePath_Pos_CD R1_TypePath_Stem_3T6 TypePath_Pos_NNS TypePath_Stem_construct PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_into LCWord_into CapitalType_ALL_LOWERCASE L0_constructs L0_TypePath_Pos_NNS L0_TypePath_Stem_construct L1_minigene L1_TypePath_Pos_NN L1_TypePath_Stem_minigen R0_3T6 R0_TypePath_Pos_CD R0_TypePath_Stem_3T6 R1_cells R1_TypePath_Pos_NNS R1_TypePath_Stem_cell TypePath_Pos_IN TypePath_Stem_into PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_3T6 LCWord_3t6 CapitalType_ALL_UPPERCASE NumericType_ALPHANUMERIC L0_into L0_TypePath_Pos_IN L0_TypePath_Stem_into L1_constructs L1_TypePath_Pos_NNS L1_TypePath_Stem_construct R0_cells R0_TypePath_Pos_NNS R0_TypePath_Stem_cell R1_. R1_TypePath_Pos_. R1_TypePath_Stem_. TypePath_Pos_CD TypePath_Stem_3T6 PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_cells LCWord_cells CapitalType_ALL_LOWERCASE L0_3T6 L0_TypePath_Pos_CD L0_TypePath_Stem_3T6 L1_into L1_TypePath_Pos_IN L1_TypePath_Stem_into R0_. R0_TypePath_Pos_. R0_TypePath_Stem_. R1OOB1 TypePath_Pos_NNS TypePath_Stem_cell PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		instances.add(createInstance("O Word_. LCWord_. L0_cells L0_TypePath_Pos_NNS L0_TypePath_Stem_cell L1_3T6 L1_TypePath_Pos_CD L1_TypePath_Stem_3T6 R0OOB1 R1OOB2 TypePath_Pos_. TypePath_Stem_. PrevNEMTokenLabel_L0_O PrevNEMTokenLabel_L1_O"));
		return instances;
	}

}
