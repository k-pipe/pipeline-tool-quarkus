package pipelining.markdown;

public class MarkdownLink {

	private final String name;
	private final String reference;

	public MarkdownLink(final String name, final String reference) {
		this.name = name;
		this.reference = reference;
	}

	public String getName() {
		return name;
	}

	public String getReference() {
		return reference;
	}

}
