package pipelining.util.html;

public class HTML {

	public static HTMLDocument createDocument(String title) {
		return new HTMLDocument(title, 0);
	}
	
	public static HTMLDocument createDocument(String title, int tableOfContentLevel) {
		return new HTMLDocument(title, tableOfContentLevel);
	}

}
