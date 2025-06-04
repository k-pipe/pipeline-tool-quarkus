package pipelining.util.codegen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JavaFile {

	private String name;
	private boolean isInterface;
	private String packageName;
	private List<String> imports;
	private String genericArgs;
	private String extended;
	private List<String> interfaces;
	private JavaDoc javadoc;
	private List<JavaElement> elements;
	private final PrintStream javaFile;

	public JavaFile(File file, String name, String packageName, boolean isInterface) {
		this.name= name;
		this.isInterface = isInterface;
		this.packageName = packageName;
		this.imports = new ArrayList<>();
		this.interfaces = new ArrayList<>();
		this.elements = new ArrayList<>();
		try {
			javaFile = new PrintStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not write file "+file);
		}
	}

	public JavaFile addImports(String... imported) {
		for (String imp : imported) {
			if (imp != null) {
				imports.add(imp);
			}
		}
		return this;
	}

	public JavaFile addInterfaces(String... implemented) {
		for (String imp : implemented) {
			if (imp != null) {
				interfaces.add(imp);
			}
		}
		return this;
	}

	public JavaFile setExtends(String extended) {
		this.extended = extended;
		return this;
	}

	public JavaFile setGenericArgs(String genericArgs) {
		this.genericArgs = genericArgs;
		return this;
	}

	public JavaFile javaDoc(String multiLine) {
		this.javadoc = new JavaDoc(multiLine);
		return this;
	}

	public JavaFile javaDoc(List<String> lines) {
		return lines != null ? javaDoc(lines.stream().collect(Collectors.joining("\n"))) : this;
	}

	public JavaFile add(JavaElement element) {
		if (element != null) {
			elements.add(element);
		}
		return this;
	}

	public JavaFile add(List<JavaElement> elements) {
		this.elements.addAll(elements);
		return this;
	}

	public void generate() {
		createHeader();
		if (javadoc != null) {
			javadoc.generate(0, this);
		}
		startClass();
		for (JavaElement element : elements) {
			element.generate(this);
		}
		endClass();
	}

	private void createHeader() {
		out(0,"// This is an automatically created file, do not modify or extend it!");
		out(0,"package "+packageName+";");
		emptyLine();
		for (String imp : imports) {
			out(0, "import "+imp+";");
		}
		emptyLine();		
	}

	private void startClass() {
		out(0, "@SuppressWarnings(\"unused\")");
		StringBuilder sb = new StringBuilder();
		sb.append("public "+(isInterface ? "interface" : "class")+" "+name);
		if (genericArgs != null) {
			sb.append(genericArgs);
		}
		sb.append(" ");
		if (extended != null) {
			sb.append("extends "+extended+" ");
		}
		if (!interfaces.isEmpty()) {
			sb.append("implements ");
			sb.append(interfaces.stream().collect(Collectors.joining(",")));
			sb.append(" ");
		}
		sb.append("{");
		out(0, sb.toString());
	}

	private void endClass() {
		out(0, "}");
		javaFile.close();
	}

	void out(int inset, String line) {
		for (int i = 0; i < inset; i++) {
			javaFile.print("   ");
		}
		javaFile.println(line);
	}

	void emptyLine() {
		javaFile.println();
	}

}
