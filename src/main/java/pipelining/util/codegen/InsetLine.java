package pipelining.util.codegen;

public class InsetLine {

	private final int inset;
	private final String line;

	public InsetLine(int inset, String line) {
		this.inset = inset;
		this.line = line;
	}

	public int getInset() {
		return inset;
	}

	public String getLine() {
		return line;
	}

}
