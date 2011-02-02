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

public class ClearParserUtil {

	public static AbstractDepParser createParser(String modelFileName,
			String algorithmName) throws IOException {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(
				modelFileName));
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

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(zin));
				StringBuilder build = new StringBuilder();
				String string;

				while ((string = reader.readLine()) != null) {
					build.append(string);
					build.append("\n");
				}

				xml = new DepFtrXml(new ByteArrayInputStream(build.toString()
						.getBytes()));
			}

			if (zEntry.getName().equals(ENTRY_LEXICA)) {
				System.out.println("- loading lexica");
				map = new DepFtrMap(xml, new BufferedReader(
						new InputStreamReader(zin)));
			} else if (zEntry.getName().equals(ENTRY_MODEL)) {
				System.out.println("- loading model");
				decoder = new OneVsAllDecoder(new BufferedReader(
						new InputStreamReader(zin)));
			}
		}

		if (algorithmName.equals(AbstractDepParser.ALG_SHIFT_EAGER))
			return new ShiftEagerParser(AbstractDepParser.FLAG_PREDICT, xml,
					map, decoder);
		else if (algorithmName.equals(AbstractDepParser.ALG_SHIFT_POP))
			return new ShiftPopParser(AbstractDepParser.FLAG_PREDICT, xml, map,
					decoder);
		else
			return null;
	}

}
