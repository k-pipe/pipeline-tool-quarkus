package com.kneissler.util.richfile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Macro {

	public static final Pattern ARG_REGEX = Pattern.compile(".*\\[([0-9+]+)\\].*");
	private static final String ARG_SEPARATOR = " ";

	private final String name;
	private final int numArgs;
	private final List<String> body;

	public Macro(final String name, final int numArgs) {
		this.name = name;
		this.numArgs = numArgs;
		this.body = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public int getNumArgs() {
		return numArgs;
	}

	public void addLine(final String line) {
		body.add(line);
	}

	public List<String> resolve(String argsString) {
		return resolve(argsString.trim().split(" "));
	}

	public List<String> resolve(String[] args) {
		return body.stream().map(line -> resolveLine(line, args)).collect(Collectors.toList());
	}

	private String resolveLine(final String line, final String[] args) {
		String res = line;
		Matcher m;
		while ((m = ARG_REGEX.matcher(res)).matches()) {
			String spec = m.group(1);
			boolean plus = spec.endsWith("+");
			if (plus) {
				spec = spec.substring(0, spec.length()-1);
			}
			int argPos = Integer.parseInt(spec);
			StringBuilder subs = new StringBuilder();
			subs.append(args[argPos]);
			if (plus) {
				for (int i = argPos+1; i < args.length; i++) {
					subs.append(ARG_SEPARATOR);
					subs.append(args[i]);
				}
			}
			res = replace(res, m.start(1)-1, m.end(1)+1, subs.toString());
		}
		return res;
	}

	public static String replace(final String string, final int start, final int end, String subs) {
		//System.out.println("Replace "+string+" from "+start+" to "+end+" with "+subs);
		return string.substring(0, start)+subs+string.substring(end);
	}

}
