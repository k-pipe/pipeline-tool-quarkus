package org.jkube.codegen;

import java.util.List;

public class ParagraphScanner {

	private final List<String> lines;
	private int nextPos;
	
	public ParagraphScanner(List<String> lines) {
		this.lines = lines;
		this.nextPos = 0;
	}
	
	public String nextLine() {
		if (nextPos >= lines.size()) {
			throw new RuntimeException("End of paragraph reached");
		}
		return lines.get(nextPos++);
	}

	public boolean hasNextLine() {
		return nextPos < lines.size();
	}

}
