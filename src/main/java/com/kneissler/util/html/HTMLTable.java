package com.kneissler.util.html;

import java.util.ArrayList;
import java.util.List;

public class HTMLTable extends HTMLElement {

	private final List<String> colHeaders;
	@SuppressWarnings("unused")
	private final boolean firstRowIsHeader;
	private final List<List<String>> rows = new ArrayList<>();

	public HTMLTable(List<String> colHeaders, boolean firstRowIsHeader) {
		this.colHeaders = colHeaders;
		this.firstRowIsHeader = firstRowIsHeader;
	}

	public void addRow(List<String> cells) {
		rows.add(cells);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		sb.append("\n");
		if (colHeaders != null) {
			sb.append("<tr>");
			for (String ch : colHeaders) {
				sb.append("<th>");			
				sb.append(ch);			
				sb.append("</th>");			
			}
			sb.append("</tr>\n");
		}
		for (List<String> row : rows) {
			addRow(sb, row);
		}
		sb.append("</table>");
		sb.append("\n");
		return sb.toString();
	}

	private void addRow(StringBuilder sb, List<String> row) {
		boolean header = firstRowIsHeader;
		sb.append("<tr>");
		for (String cell : row) {
			sb.append(header ? "<th>" : "<td>");
			sb.append(cell);
			sb.append(header ? "</th>" : "</td>");
			header = false;
		}
		sb.append("<tr>\n");
	}

}
