package org.jkube.util;

public class CodeLocation {

	private static final String THIS_PACKAGE = CodeLocation.class.getPackage().getName()+".";

	private String jarName;
	private final String fileName;
	private final String className;
	private final String methodName;
	private final int lineNumber;

	public static CodeLocation getCallLocationOutsideThisPackage() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		boolean first = true;
		for (StackTraceElement ste : stackTrace) {
			if (!first && !ste.getClassName().startsWith(THIS_PACKAGE)) {
				return new CodeLocation(ste);
			}
			first = false;
		}
		if (stackTrace.length < 2) {
			throw new RuntimeException("too short stacktrace");
		}
		return new CodeLocation(stackTrace[1]);
	}

	public CodeLocation(StackTraceElement ste) {
		try {
			this.jarName = ste.getClassLoaderName();
		} catch (NoSuchMethodError e) {
			this.jarName = "";
			// print stack trace: what else could we do? logging within logging is not a good idea
			e.printStackTrace();
		}
		this.className = ste.getClassName();
		this.methodName = ste.getMethodName();
		this.fileName = ste.getFileName();
		this.lineNumber = ste.getLineNumber();
	}

	public String getJarName() {
		return jarName;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getClassName() {
		return className;
	}

	public String filename() {
		return fileName;
	}

	public int lineNumber() {
		return lineNumber;
	}

}
