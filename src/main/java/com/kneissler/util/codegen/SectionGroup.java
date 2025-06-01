package com.kneissler.util.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionGroup<E extends Enum<E>> {

	private boolean multi;
	private boolean allowMissing;
	private String header;
	private int level;
	private List<Section<E>> sections;
	private E[] columns;
	
	private SectionGroup(List<Section<E>> sections, int level, String header, E[] columns, boolean multi, boolean allowMissing) {
		this.sections = sections;
		this.level = level;
		this.header = header;
		this.multi = multi;
		this.columns = columns;
		this.allowMissing = allowMissing;
	}

	public static <E extends Enum<E>> SectionGroup<E> singleSection(Section<E> s, E[] columns, boolean allowMissing) {
		return new SectionGroup<E>(Collections.singletonList(s), s.headerLevel(), s.headerText(), columns, false, allowMissing);
	}
	

	public static <E extends Enum<E>> SectionGroup<E> singleSection(int level, String header, E[] columns, boolean allowMissing) {
		return singleSection(new Section<>(level, header), columns, allowMissing);
	}

	public static <E extends Enum<E>> SectionGroup<E> multiSection(int superLevel, String superHeader, E[] columns, boolean allowMissing) {
		return new SectionGroup<E>(new ArrayList<>(), superLevel, superHeader, columns, true, allowMissing);
	}
	
	public boolean isMulti() {
		return multi;
	}

	public List<Section<E>> getSections() {
		return sections;
	}

	public int getHeaderLevel() {
		return level;
	}

	public boolean allowMissing() {
		return allowMissing;
	}

	public String getHeaderText() {
		return header;
	}

	public E[] getColumns() {
		return columns;
	}

}
