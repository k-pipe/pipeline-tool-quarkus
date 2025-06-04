package pipelining.util.codegen;

import java.util.ArrayList;
import java.util.List;

public class JavaElement {

	private int inset;
	private JavaDoc javaDoc;
	private List<InsetLine> lines;

	public JavaElement() {
		this.inset = 1;
		this.lines = new ArrayList<InsetLine>();
	}

	public JavaElement incInset() {
		inset++;
		return this;
	}

	public JavaElement decInset() {
		inset--;
		return this;
	}

	public JavaElement javaDoc(String multiline) {
		javaDoc = new JavaDoc(multiline);
		return this;
	}

	public JavaElement add(String line) {
		lines.add(new InsetLine(inset, line));		
		return this;
	}

	public JavaElement add(String... items) {
		return add(join(items));
	}

	private String join(String... items) {
		StringBuilder sb = new StringBuilder();
		for (String s : items) {
			if (sb.length() != 0) {
				sb.append(" ");
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public void generate(JavaFile javaFile) {
		javaFile.emptyLine();
		if (javaDoc != null) {
			javaDoc.generate(1, javaFile);
		}
		lines.forEach(il -> javaFile.out(il.getInset(), il.getLine()));
	}

}
