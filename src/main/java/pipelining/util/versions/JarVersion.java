package pipelining.util.versions;

import pipelining.util.injection.jar.JarName;

public class JarVersion {

	private final BranchAndVersion branchAndVersion;
	private final String timestamp; // set for snapshots, omitted for releases

	public JarVersion(String jarSuffix) {
		if (jarSuffix.isBlank()) {
			throw new IllegalArgumentException("blank jar suffix received");
		}
		String[] split = jarSuffix.split(JarName.SEPARATOR1);
		if (split.length > 2) {
			throw new IllegalArgumentException("illegal number of underscores in jar suffix: "+jarSuffix);
		}
		this.branchAndVersion = new BranchAndVersion(split[0]);
		this.timestamp = split.length == 2 ? split[1] : null;
	}
	
	public JarVersion(BranchAndVersion branchAndVersion, String timestamp) {
		this.branchAndVersion = branchAndVersion;
		this.timestamp = timestamp;
	}

	public JarVersion(BranchAndVersion branchAndVersion) {
		this(branchAndVersion, null);
	}
	
	public JarVersion(int version) {
		branchAndVersion = new BranchAndVersion(version);
		timestamp = null;
	}

	public String getSuffix() {
		return branchAndVersion.toString()+(timestamp != null ? JarName.SEPARATOR1+timestamp : "");
	}

	public boolean isSnapshot() {
		return timestamp != null;
	}

	public boolean isMatching(VersionPattern pattern) {
		return pattern.matches(branchAndVersion);
	}

	public boolean isSuperior(JarVersion other) {
		if (isSnapshot()) {
			return other.isOlderOrSameDate(this);
		} else {
			return branchAndVersion.isSuperior(other.branchAndVersion);
		}
	}

	public boolean isOlderOrSameDate(JarVersion other) {
		return isSnapshot() && other.isSnapshot() && (timestamp.compareTo(other.timestamp) <= 0);
	}
	
	@Override
	public String toString() {
		return getSuffix();
	}

}
