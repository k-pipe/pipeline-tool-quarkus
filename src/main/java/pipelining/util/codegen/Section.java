package pipelining.util.codegen;

import java.util.ArrayList;
import java.util.List;

public class Section<E extends Enum<E>> {

	private final int headerLevel;
	private final String headerText;
	private List<String> description;
	private Table<E> table;
	
	public Section(int level, String header) {
		this.headerLevel = level;
		this.headerText = header;
		this.description = null;
	}
	
	public int headerLevel() {
		return headerLevel;
	}

	public String headerText() {
		return headerText;
	}

	public void setTable(Table<E> table) {
		this.table = table;
	}
	
	public Table<E> getTable() {
		return table;
	}

	public List<String> getDescription() {
		return description;
	}

	public void addDescription(List<String> paragraph) {
		if (description == null) {
			description = new ArrayList<>();
		} else {
			description.add("");
		}
		description.addAll(paragraph);
	}
}
