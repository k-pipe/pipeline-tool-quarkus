package pipelining.util.injection.jar;

public class JarLoadingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JarLoadingException(String string) {
		super(string);
	}

	public JarLoadingException(String string, Exception e) {
		super(string, e);
	}

}
