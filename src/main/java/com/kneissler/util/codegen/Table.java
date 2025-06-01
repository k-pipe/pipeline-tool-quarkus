package com.kneissler.util.codegen;

import org.jkube.util.Expect;

import java.util.*;
import java.util.stream.Collectors;

public class Table<E> {

	private static final String BOOLEAN_CHOICE = "X";

	private final List<Map<E, String>> rows;
	private final List<E> columnSequence;
	
	public Table(ParagraphScanner scanner, List<E> columns) {
		this(scanner, columns, false);
	}

	public Table(ParagraphScanner scanner, List<E> columns, boolean allowMissingColumns) {
		rows = new ArrayList<>();
		String line = scanner.nextLine();
		while (line.isEmpty()) {
			line = scanner.nextLine();
		}
		columnSequence = parseHeader(line,columns, allowMissingColumns);
		line = scanner.nextLine();
		expectSeparator(line, columnSequence.size());
		boolean done = false;
		while(!done) {
			line = scanner.hasNextLine() ? scanner.nextLine() : "";
			done = line.isEmpty();
			if (!done) {
				addRow(line, columnSequence, allowMissingColumns);
			}
		}
	}

	private List<E> parseHeader(String line, List<E> columns, boolean allowMissingColumns) {
		List<E> res = new ArrayList<>();
		for (String s : parseLine(line)) {
			res.add(find(columns, s));
		}
		if (!allowMissingColumns && !columns.isEmpty()) {
			throw new RuntimeException("Columns not found: "+columns.stream().map(Object::toString).collect(Collectors.joining(",")));
		}
		return res;
	}

	private E find(List<E> columns, String string) {
		Iterator<E> i = columns.iterator();
		while (i.hasNext()) {
			E e = i.next();
			if (e.toString().equalsIgnoreCase(string)) {
				i.remove();
				return e;
			}
		}
		throw new RuntimeException("No such column: "+string);
	}

	private void addRow(String line, List<E> columnSequence, boolean allowMissingColumns) {
		List<String> items = parseLine(line);
		if ((items.size() > columnSequence.size()) || ((items.size() < columnSequence.size()) && !allowMissingColumns)) {
			throw new RuntimeException("expected "+columnSequence.size()+" items in row, found "+items.size()+": ");
		}
		Map<E, String> row = new HashMap<>();
		for (int i = 0; i < items.size(); i++) {
			E key = columnSequence.get(i);
			String val = items.get(i);
			if (val.equals("\"")) {
				if (rows.isEmpty()) {
					throw new RuntimeException("repeat marker \" in first row not allowed");
				}
				val =rows.get(rows.size()-1).get(key);
			}
			row.put(key, val);
		}
		rows.add(row);
	}

	private void expectSeparator(String line, int num) {
		List<String> items = parseLine(line);
		if (items.size() != num) {
			throw new RuntimeException("expected "+num+" items in row, found "+items.size());
		}
		for (String s : items) {
			if (!(s.equals(":---:") || s.equals(":---") || s.equals("---:") || s.equals("---"))) {
				throw new RuntimeException("expected separator instead of "+s);
			}
		}
	}

	private List<String> parseLine(String line) {
		if (line.startsWith("|")) {
			line = line.substring(1);
		}
		List<String> res = new ArrayList<>();
		for (String s : line.split("\\|")) {
			String st = s.trim();
			//if (!st.isEmpty()) {
			res.add(st);
			//}
		}
		return res;
	}

	public boolean hasColumn(E column) {
		return columnSequence.contains(column);
	}
	
	public List<Map<E, String>> getRows() {
		return rows;
	}

	public Map<E, String> findRow(E column, String value) {
		Map<E, String> found = null;
		for (Map<E, String> row : rows) {
			if (row.get(column).equals(value)) {
				Expect.isNull(found).elseFail("found multiple in "+column+" with value "+value);
				found = row;
			}
		}
		Expect.notNull(found).elseFail("could not find any in "+column+" with value "+value);
		return found;
	}

	public List<Map<E, String>> findRows(E column, String value) {
		List<Map<E, String>> found = new ArrayList<>();
		for (Map<E, String> row : rows) {
			if (row.get(column).equals(value)) {
				found.add(row);
			}
		}
		return found;
	}

	public static <E> boolean isX(Map<E, String> row, E column) {
		String elem = row.get(column);
		return (elem != null) && elem.equalsIgnoreCase(BOOLEAN_CHOICE);
	}
	
}
