package pipelining.util.codegen;

import java.io.File;

public class CodeGenerator {

	private static final String JAVA_EXTENSION = ".java";

	private final String packageName;
	private final String sourcePath;

	public CodeGenerator(String packageName, String sourcePath) {
		this.packageName = packageName;
		this.sourcePath = sourcePath;
	}

	public JavaFile create(String name, boolean isInterface) {
		File dir = new File(sourcePath+File.separator+packageName.replaceAll("\\.", "/"));
		if (!dir.exists()) {
			System.out.println("Creating directory "+dir.getPath());
			if (!dir.mkdirs()) {
				throw new RuntimeException("Could not create "+dir.getAbsolutePath());
			}
		}
		System.out.println("Creating "+name+JAVA_EXTENSION+" in "+dir.getPath());
		return new JavaFile(new File(dir, name+JAVA_EXTENSION), name, packageName, isInterface);
	}

	public JavaFile newInterface(String name) {
		return create(name, true);
	}

	public JavaFile newClass(String name) {
		return create(name, false);
	}

}
