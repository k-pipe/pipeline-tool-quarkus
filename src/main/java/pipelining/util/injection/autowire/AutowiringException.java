package pipelining.util.injection.autowire;

public class AutowiringException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AutowiringException(String string) {
		super(string);
	}

	public AutowiringException(String string, Exception e) {
		super(string, e);
	}

}
