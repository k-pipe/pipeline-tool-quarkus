package pipelining.util.versions;

import pipelining.util.injection.jar.JarName;

import java.util.Arrays;

/** 
 * Format: 
 * branch1-branch2-...[-##.##...]
 * 
 * Branch elements may be wildcard or end with wildcard.
 * Numbers may wildcard. After number wildcard can not come any numbers (only more wildcards).
 * Numbers may be omitted completely.
 */
public class VersionPattern { 

	private static final String STRING_WILDCARD = "*";
	private static final char CHAR_WILDCARD = '*';
	private static final int INT_WILDCARD = -1;
	private static final String MASTER = "master";

	protected final String[] branchElements; 
	protected final int[] versionElements; 
	protected final boolean isPattern; // does not specify a distinct tag but a set of tags (e.g. using wildcards)

	public VersionPattern(String string) {
		if (string.isBlank()) {
			string = MASTER;
		}
		boolean wc = false;
		String version;
		if (string.contains(JarName.SEPARATOR2)) {
			String[] split = string.trim().split(JarName.SEPARATOR2);
			this.branchElements = Arrays.copyOf(split, split.length-1);
			version = split[split.length-1];
		} else {
			this.branchElements = new String[] { string };
			version = "";
		}
		for (int i = 0; i < branchElements.length; i++) {
			if (branchElements[i].isEmpty()) {
				throw new RuntimeException("empty element not allowed, use * for wildcard");
			}
			if (endsWithWildcard(branchElements[i])) {
				wc = true;
			}
		}
		if (version.isEmpty()) {
			this.versionElements = new int[0];
		} else {
			String[] split2 = version.split("\\.");
			versionElements = new int[split2.length];
			boolean numberwc = false;
			for (int i = 0;  i < versionElements.length; i++) {
				if (split2[i].equals(STRING_WILDCARD)) {
					versionElements[i] = INT_WILDCARD;
					numberwc = true;
				} else {
					if (split2[i].isEmpty()) {
						throw new RuntimeException("empty element not allowed, use * for wildcard");
					}
					if (numberwc) {
						throw new RuntimeException("after wildcard in version number pattern only more wildcards are allowed");					
					}
					versionElements[i] = Integer.parseInt(split2[i]);
				}
			}
			if (numberwc) {
				wc = true;
			}
		}
		isPattern = wc;
	}

	public VersionPattern(int version) {
		branchElements = null;
		versionElements = new int[] { version };
		isPattern = false;
	}

	private static boolean endsWithWildcard(String string) {
		return string.charAt(string.length()-1) == CHAR_WILDCARD;
	}

	/**
	 * @return true if different tags may match the pattern, i.e. it contains wildcards or no version elements  
	 */
	public boolean isPattern() {
		return isPattern;
	}

	/**
	 * @return true if no version numbers or wildcards are specified, i.e. if the latest in defined branch shall be used
	 */
	public boolean isLatest() {
		return versionElements.length == 0;
	}

	/**
	 * @return true if tag is fully specified 
	 */
	public boolean isSpecific() {
		return !isLatest() && !isPattern();
	}

	/**
	 * @return a BranchAndVersion object that specifies the latest in the current branch
	 */
	public BranchAndVersion latestInBranch() {
		StringBuilder branch = new StringBuilder();
		for (String s : branchElements) {
			if (branch.length() != 0) {
				branch.append(JarName.SEPARATOR2);
			}
			branch.append(s);
		}
		return new BranchAndVersion(branch.toString());
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (branchElements != null) {
			for (int i = 0; i < branchElements.length; i++) {
				if (i > 0) {
					sb.append(JarName.SEPARATOR2);
				}
				sb.append(branchElements[i]);
			}
			if (versionElements.length > 0) {
				sb.append(JarName.SEPARATOR2);			
			}
		}
		for (int i = 0; i < versionElements.length; i++) {
			if (i > 0) {
				sb.append(JarName.SEPARATOR3);
			}
			int n = versionElements[i];
			sb.append(n == INT_WILDCARD ? STRING_WILDCARD : Integer.toString(n));
		}
		return sb.toString();
	}

	public boolean matches(BranchAndVersion bv) {
		if (versionElements.length != 0) {
			if (versionElements.length != bv.versionElements.length) {
				return false;
			}
			for (int i = 0; i < versionElements.length; i++) {
				if (!matches(versionElements[i], bv.versionElements[i])) {
					return false;
				}
			}
		}
		if (branchElements == null) {
			return bv.branchElements == null;
		}
		if (branchElements.length != bv.branchElements.length) {
			return false;
		}
		for (int i = 0; i < branchElements.length; i++) {
			if (!matches(branchElements[i], bv.branchElements[i])) {
				return false;
			}
		}
		return true;
	}

	private boolean matches(String pattern, String string) {
		if (endsWithWildcard(pattern)) {
			return string.startsWith(pattern.substring(0, pattern.length()-1));
		} else {
			return string.equals(pattern);
		}
	}

	private boolean matches(int pattern, int version) {
		return (pattern == version) || (pattern == INT_WILDCARD);
	}

	public BranchAndVersion createMockVersion() {
		StringBuilder sb = new StringBuilder();
		for (String be : branchElements) {
			sb.append(be.equals(STRING_WILDCARD) ? "dummy" : be);
			sb.append(JarName.SEPARATOR2);
		}
		for (int i = 0; i < versionElements.length; i++) {
			if (i > 0) {
				sb.append(JarName.SEPARATOR3);
			}
			sb.append(Math.max(0, versionElements[i]));
		}
		return new BranchAndVersion(sb.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(branchElements);
		result = prime * result + (isPattern ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(versionElements);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionPattern other = (VersionPattern) obj;
		if (!Arrays.equals(branchElements, other.branchElements))
			return false;
		if (isPattern != other.isPattern)
			return false;
		if (!Arrays.equals(versionElements, other.versionElements))
			return false;
		return true;
	}

	public BranchAndVersion asBranchAndVersion() {
		return new BranchAndVersion(toString());
	}

}
