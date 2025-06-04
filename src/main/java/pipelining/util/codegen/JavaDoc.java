package pipelining.util.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kneissjn
 *
 */
public class JavaDoc {

	private List<String> lines;

	public JavaDoc(String multiLine) {
		lines = new ArrayList<String>();
		for (String line : multiLine.split("\n")) {
			lines.add(line);
		}
	}

	public void generate(int inset, JavaFile javaFile) {
		javaFile.out(inset, "/**");
		lines.forEach(l -> javaFile.out(inset, " * "+l));
		javaFile.out(inset, " */");
	}

}
