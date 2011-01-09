package org.cleartk.syntax.dependency.malt;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.UIMAUtil;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.util.JCasUtil;

public class MaltParser extends JCasAnnotator_ImplBase {

	public static final String ENGMALT_RESOURCE_NAME = "/models/engmalt.linear.mco";

	private static TypeSystemDescription getTypeSystem() {
		return TypeSystemDescriptionFactory.createTypeSystemDescription(
			"org.cleartk.token.TypeSystem",
			"org.cleartk.syntax.dependency.TypeSystem");
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		// get the resource path and strip the ".mco" suffix
		String fileName = MaltParser.class.getResource(ENGMALT_RESOURCE_NAME).getFile();
		String fileBase = fileName.substring(0, fileName.length() - 4);
		return getDescription(fileBase);
	}

	public static AnalysisEngineDescription getDescription(String modelFileName)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createPrimitiveDescription(
			MaltParser.class,
			getTypeSystem(),
			PARAM_MODEL_FILE_NAME,
			modelFileName);
	}

	@ConfigurationParameter(
		description = "The path to the model file, without the .mco suffix.",
		mandatory = true)
	private String modelFileName;

	public static final String PARAM_MODEL_FILE_NAME = ConfigurationParameterFactory.createConfigurationParameterName(
		MaltParser.class,
		"modelFileName");

	private MaltParserService service;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			OptionManager.instance().loadOptionDescriptionFile();
			OptionManager.instance().getOptionDescriptions().generateMaps();
			this.service = new MaltParserService();
			File modelFile = new File(this.modelFileName);
			String fileName = modelFile.getName();
			String workingDirectory = modelFile.getParent();
			String command = String.format("-c %s -m parse -w %s", fileName, workingDirectory);
			this.service.initializeParserModel(command);
		} catch (MaltChainedException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		try {
			this.service.terminateParserModel();
		} catch (MaltChainedException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : AnnotationRetrieval.getAnnotations(jCas, Sentence.class)) {
			List<Token> tokens = AnnotationRetrieval.getAnnotations(jCas, sentence, Token.class);

			// convert tokens into MaltParser input array
			List<String> inputStrings = new ArrayList<String>();
			int lineNo = -1;
			for (Token token : tokens) {
				lineNo += 1;
				String text = token.getCoveredText();
				String pos = token.getPos();

				// line format is <index>\t<word>\t_\t<pos>\t<pos>\t_
				String lineFormat = "%1$d\t%2$s\t_\t%3$s\t%3$s\t_";
				inputStrings.add(String.format(lineFormat, lineNo + 1, text, pos));
			}

			try {
				// parse with MaltParser
				String[] input = inputStrings.toArray(new String[inputStrings.size()]);
				DependencyStructure graph = this.service.parse(input);

				// convert MaltParser structure into annotations
				Map<Integer, DependencyNode> nodes = new HashMap<Integer, DependencyNode>();
				SortedSet<Integer> tokenIndices = graph.getTokenIndices();
				for (int i : tokenIndices) {
					org.maltparser.core.syntaxgraph.node.DependencyNode maltNode = graph.getTokenNode(i);
					Token token = tokens.get(maltNode.getIndex() - 1);
					DependencyNode node = new DependencyNode(jCas, token.getBegin(), token.getEnd());
					node.addToIndexes();
					nodes.put(i, node);
				}

				// add head links between node annotations
				SymbolTable table = graph.getSymbolTables().getSymbolTable("DEPREL");
				Map<DependencyNode, List<DependencyNode>> nodeChildren;
				nodeChildren = new HashMap<DependencyNode, List<DependencyNode>>();
				for (int i : tokenIndices) {
					org.maltparser.core.syntaxgraph.node.DependencyNode maltNode = graph.getTokenNode(i);
					int headIndex = maltNode.getHead().getIndex();
					if (headIndex != 0) {
						String label = maltNode.getHeadEdge().getLabelSymbol(table);
						DependencyNode node = nodes.get(i);
						DependencyNode head = nodes.get(headIndex);
						node.setHead(head);
						node.setDependencyType(label);

						// collect child information
						if (!nodeChildren.containsKey(head)) {
							nodeChildren.put(head, new ArrayList<DependencyNode>());
						}
						nodeChildren.get(head).add(node);
					}
				}

				// add child links between node annotations
				for (DependencyNode head : nodeChildren.keySet()) {
					head.setChildren(UIMAUtil.toFSArray(jCas, nodeChildren.get(head)));
				}
				for (DependencyNode node : JCasUtil.iterate(jCas, DependencyNode.class)) {
					if (node.getChildren() == null) {
						node.setChildren(new FSArray(jCas, 0));
					}
				}

			} catch (MaltChainedException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
	}
}
