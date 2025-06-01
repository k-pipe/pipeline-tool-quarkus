package com.kneissler.util.codegen;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MarkupFile {

	private final String path;
	private final List<List<String>> paragraphs;
	private Scanner scanner;

	public MarkupFile(String path) {
		this.path = path;
		this.paragraphs = new ArrayList<>();
		parse();
	}
	
	private void parse() {
		//System.out.println("Reading "+path);
		try (FileInputStream in = new FileInputStream(path)) {
			scanner = new Scanner(in);
			List<String> paragraph = null;
			do {
				paragraph = scanParagraph();
				if (!paragraph.isEmpty()) {
					paragraphs.add(paragraph);
				}
			} while(!paragraph.isEmpty());
		} catch (IOException e) {
			throw new RuntimeException("Could not open file", e);
		}
//		paragraphs.forEach(p -> {
//			System.out.println();
//			p.forEach(l -> System.out.println(l));
//		});
	}

	private List<String> scanParagraph() {
		List<String> paragraph = new ArrayList<String>();
		boolean done = false;
		do {
			String line = scanner.hasNextLine() ? scanner.nextLine() : null;
			if ((line != null) && !line.isBlank()) {
				paragraph.add(line);
			} else {
				done = (!paragraph.isEmpty()) || (line == null);
			}
		} while(!done);
		return paragraph;
	}

	public  List<List<String>>  getParagraphs() {
		return paragraphs;
	}
	
}
