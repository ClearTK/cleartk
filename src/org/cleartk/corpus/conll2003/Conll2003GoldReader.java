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
package org.cleartk.corpus.conll2003;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.cleartk.ne.type.NamedEntity;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.type.Chunk;
import org.cleartk.type.Sentence;
import org.cleartk.type.SimpleAnnotation;
import org.cleartk.type.Token;
import org.cleartk.util.ViewURIUtil;
import org.cleartk.util.UIMAUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 *
 * This collection reader reads in the CoNLL 2003 named entity data.  The 
 * data can be retrieved from http://www.cnts.ua.ac.be/conll2003/ner/
 * 
 */
public class Conll2003GoldReader extends CollectionReader_ImplBase
{
	/**
	 * "org.cleartk.corpus.conll2003.Conll2003GoldReader.PARAM_CONLL_2003_DATA_FILE"
	 * is a single, required string parameter that points to CoNLL data
	 * (e.g. ner/eng.train).
	 */
	public static final String PARAM_CONLL_2003_DATA_FILE = "org.cleartk.corpus.conll2003.Conll2003GoldReader.PARAM_CONLL_2003_DATA_FILE";

	/**
	 * "org.cleartk.corpus.conll2003.Conll2003GoldReader.PARAM_LOAD_NAMED_ENTITIES"
	 * is a single, required boolean parameter that determines if the named entities
	 * are loaded (i.e. named entity mention annotations are created) or if just the
	 * plain text from the files is loaded.
	 */
	public static final String PARAM_LOAD_NAMED_ENTITIES = "org.cleartk.corpus.conll2003.Conll2003GoldReader.PARAM_LOAD_NAMED_ENTITIES";
	public static final String DOCSTART = "-DOCSTART-";
										  
	BufferedReader reader;
	
	boolean hasNext = true;
	
	int documentIndex = 0;
	String documentPath;
	
	boolean loadNamedEntities = true;
	int entityIdIndex = 0;
	
	public void initialize() throws ResourceInitializationException
	{
		try
		{
			documentPath = (String) UIMAUtil.getRequiredConfigParameterValue(getUimaContext(), PARAM_CONLL_2003_DATA_FILE);
			loadNamedEntities = (Boolean) UIMAUtil.getDefaultingConfigParameterValue(getUimaContext(), PARAM_LOAD_NAMED_ENTITIES, true);
			
			File conllFile = new File(documentPath);
		    reader = new BufferedReader(new FileReader(conllFile));
			//advance the reader past the first occurrence of a document start and
		    //blank line.
		    String line;
		    while((line = reader.readLine()) != null)
			{
				if(line.trim().startsWith(DOCSTART))
				{
					reader.readLine(); //read the blank line
					break;
				}
			}

		    sentenceTokens = new ArrayList<Token>();
			sentenceChunks = new ArrayList<Chunk>();
			chunkTokens = new ArrayList<Token>();
			namedEntityTokens = new ArrayList<Token>();
			
			super.initialize();
		}
		catch(FileNotFoundException fnfe)
		{
			throw new ResourceInitializationException(fnfe);
		}
		catch(IOException ioe)
		{
			throw new ResourceInitializationException(ioe);
		}
	}

	List<String> documentData;  //contains every line from for the current document
 	StringBuffer documentText;  //collects the text of the tokens and then we set the document text with contents
	int sentenceStart;          //the start (character offset) of the current sentence
	List<Token> sentenceTokens; //the tokens for the current sentence
	List<Chunk> sentenceChunks; //the chunks for the current sentence
	
	int tokenPosition;             //the index of the current token

	int chunkStart;             //the start (char offset) of the current chunk
	String currentChunkType;    //the type of the current chunk (including the I- or B- prefix)
	List<Token> chunkTokens;    //the tokens for the current chunk
	
	int namedEntityStart;       //the start (char offset) of the current named entity
	String currentNamedEntityType; //the type of the current named entity (including the I- or B- prefix)
	List<Token> namedEntityTokens; //the tokens for the current named entity
	
	public void getNext(CAS cas) throws IOException, CollectionException
	{
		try
		{
			JCas jCas = cas.getJCas();
			
			//read in the data for the next document from the reader
			documentData = new ArrayList<String>();
			String line;
			while((line = reader.readLine()) != null &&
				   !line.startsWith(DOCSTART))
			{
				documentData.add(line.trim());
			}

			if(line == null)
				hasNext = false;
			else
				line = reader.readLine().trim(); //advance past the blank line that follows "-DOCSTART- -X- O O" 
												 //(we don't want an empty sentence on the front end!)
			documentText = new StringBuffer();
			
			initSentence();
			tokenPosition = 0;
			chunkStart = 0;
			currentChunkType = "";
			chunkTokens.clear();
			namedEntityStart = 0;
			currentNamedEntityType = "";
			namedEntityTokens.clear();
			
			for(String dataLine : documentData)
			{
				if(dataLine.trim().equals(""))
				{
					createChunk(jCas);
					currentChunkType = "";
					createNamedEntity(jCas);
					currentNamedEntityType = "";
					
					Sentence sentence = new Sentence(jCas, sentenceStart, documentText.length());
					sentence.addToIndexes();
					
					initSentence();
				}
				else
				{
					String[] dataPieces = dataLine.split(" ");
					String tok = dataPieces[0];
					String pos = dataPieces[1];
					String chunkType = dataPieces[2];
					if(currentChunkType.equals(""))
						initChunk(chunkType);
					String namedEntityType = dataPieces[3];
					if(currentNamedEntityType.equals(""))
						initNamedEntity(namedEntityType);
					
					
					Token token = new Token(jCas, documentText.length(), documentText.length()+tok.length());
					token.setPos(pos);
					token.addToIndexes();
					
					boolean chunkStartsWithB = startsWithB(currentChunkType, chunkType);
					if(!chunkType.equals(currentChunkType) && !chunkStartsWithB){ 
						createChunk(jCas);
						initChunk(chunkType);
					}
					
					boolean namedEntityStartsWithB = startsWithB(currentNamedEntityType, namedEntityType);
					
					if(!namedEntityType.equals(currentNamedEntityType) && !namedEntityStartsWithB)
					{
						createNamedEntity(jCas);
						initNamedEntity(namedEntityType);
					}

					sentenceTokens.add(token);
					chunkTokens.add(token);
					namedEntityTokens.add(token);
					documentText.append(tok+" ");
				}
			}
			
			jCas.setDocumentText(documentText.toString());
			
			String identifier = String.format("%s#%s", documentPath, documentIndex);
			ViewURIUtil.setURI(cas, identifier);
			++documentIndex;

		}
		catch(CASException ce)
		{
			throw new CollectionException(ce);
		}

	}

	private void initSentence()
	{
		sentenceStart = documentText.length();
		sentenceTokens.clear();
		sentenceChunks.clear();
	}

	private void createChunk(JCas jCas)
	{
		if(!currentChunkType.equals("O"))
		{
			Chunk chunk = new Chunk(jCas, chunkStart, documentText.length()-1);
			chunk.setChunkType(currentChunkType.substring(2));
			chunk.addToIndexes();
			sentenceChunks.add(chunk);
		}
	}
	
	private void initChunk(String chunkType)
	{
		chunkStart = documentText.length();
		chunkTokens.clear();
		currentChunkType = chunkType;
	}
	
	private void createNamedEntity(JCas jCas)
	{
		if(!currentNamedEntityType.equals("O") && loadNamedEntities)
		{
			NamedEntity ne = new NamedEntity(jCas);
			ne.setEntityClass("SPC");
			ne.setEntityId(""+entityIdIndex++);
			ne.setEntityType(currentNamedEntityType.substring(2));
			ne.setEntitySubtype(currentNamedEntityType.substring(2));
			ne.addToIndexes();
			
			NamedEntityMention nem = new NamedEntityMention(jCas, namedEntityStart, documentText.length()-1);
			nem.setMentionType("NAM");
			SimpleAnnotation annotation = new SimpleAnnotation(jCas, namedEntityStart, documentText.length()-1);
			annotation.addToIndexes();
//			Chunk chunk = new Chunk(jCas, namedEntityStart, documentText.length()-1);
//			chunk.setTokens(UIMAUtil.toFSArray(jCas, namedEntityTokens));
//			chunk.setChunkType("CoNLL NEM annotation");
//			chunk.addToIndexes();
			nem.setAnnotation(annotation);
			nem.setHead(annotation);
			nem.setMentionedEntity(ne);
			nem.addToIndexes();
			
			ne.setMentions(UIMAUtil.toFSArray(jCas, Collections.singletonList(nem)));
		}
	}
	
	private void initNamedEntity(String namedEntityType)
	{
		namedEntityStart = documentText.length();
		namedEntityTokens.clear();
		currentNamedEntityType = namedEntityType;
	}

	/**
	 * Determines if the read type is the same as the current type with the only
	 * difference being that the current type (bType) starts with "B-" and the
	 * read type starts with "I-".  
	 * @param bType - the current type for the chunk or named entity
	 * @param iType - the read chunk or named entity type for the token being examined.
	 * @return true if we should consder iType to be the same as bType so we know
	 * not to make a new chunk or named entity.
	 */
	private boolean startsWithB(String bType, String iType)
	{
		if(bType.startsWith("B") && 
		   iType.startsWith("I") &&
		   iType.substring(1).equals(bType.substring(1)))
		{
			return true;
		}
		return false;
	}
	
	public void close() throws IOException
	{
		reader.close();
	}

	public Progress[] getProgress()
	{
	    return new Progress[] { new ProgressImpl(documentIndex, 5000, Progress.ENTITIES) };
	}

	public boolean hasNext() throws IOException, CollectionException
	{
		return hasNext;
	}

}
