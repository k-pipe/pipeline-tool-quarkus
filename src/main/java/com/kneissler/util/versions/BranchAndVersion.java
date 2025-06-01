package com.kneissler.util.versions;

import com.kneissler.util.injection.jar.JarName;

public class BranchAndVersion extends VersionPattern {

	private final SemanticVersion version; 

	public BranchAndVersion(String string) {
		super(string);
		if (isPattern()) {
			throw new IllegalArgumentException("wildards not allowed in branch/version specification");
		}
		this.version = new SemanticVersion(versionElements);
	}
	
	public BranchAndVersion(int version) {
		super(version);
		this.version = new SemanticVersion(versionElements);
	}

	public SemanticVersion getVersion() {
		return version;
	}

	public String getBranch() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < branchElements.length; i++) {
			if (i > 0) {
				sb.append(JarName.SEPARATOR2);
			}
			sb.append(branchElements[i]);
		}
		return sb.toString();
	}

	public boolean isSuperior(BranchAndVersion other) {
		return version.isAfter(other.version);
	}

	public static BranchAndVersion wildcard() {
		return wildcard("*");
	}

	private static BranchAndVersion wildcard(String branch) {
		return new BranchAndVersion(branch+"-*");
	}
	
}
