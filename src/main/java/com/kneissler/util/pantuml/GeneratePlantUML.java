package com.kneissler.util.pantuml;

import net.sourceforge.plantuml.SourceStringReader;

import java.io.OutputStream;

import static org.jkube.logging.Log.onException;

public class GeneratePlantUML {

	private static final String PREFIX = "@startuml\n";
	private static final String SUFFIX = "@enduml\n";

	public static final String generatePNG(String plantUMLString, OutputStream pngOutputStream) {
		return onException(() -> new SourceStringReader(PREFIX+plantUMLString+SUFFIX).generateImage(pngOutputStream))
				.fail("Could not create uml diagram");
	}

}