package com.kneissler.util.versions;

import com.kneissler.util.injection.jar.JarName;

import java.util.Arrays;

public class SemanticVersion {
		
	private int[] versionNumber;

	public SemanticVersion(String string) {
		if (string.trim().isEmpty()) {
			throw new IllegalArgumentException("no version number specified");
		}
		String[] split = string.split("\\.");
		versionNumber = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			versionNumber[i] = Integer.parseInt(split[i]);
		}
	}

	public SemanticVersion(int[] versionElements) {
		this.versionNumber = versionElements;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < versionNumber.length; i++) {
			if (i > 0) {
				res.append(JarName.SEPARATOR3);
			}
			res.append(Integer.toString(versionNumber[i]));
		}
		return res.toString();
	}
	
	public void incVersion(int incrementPosition) {
		if (incrementPosition >= versionNumber.length) {
			versionNumber = Arrays.copyOf(versionNumber, incrementPosition+1);
		}
		versionNumber[incrementPosition]++;
		for (int i = incrementPosition+1; i < versionNumber.length; i++) {
			versionNumber[i] = 0;
		}
	}

	public void incMajor() {
		incVersion(0);
	}

	public void incMinor() {
		incVersion(1);
	}

	public void incPatch() {
		incVersion(2);
	}

	public void incBuild() {
		incVersion(3);
	}
	
	public int getVersion(int position) {
		return position >= versionNumber.length ? 0 : versionNumber[position];
	}
	
	public boolean isAfter(SemanticVersion other) {
		int max = Math.max(versionNumber.length, other.versionNumber.length);
		for (int i = 0; i < max; i++) {
			int n1 = getVersion(i);
			int n2 = other.getVersion(i);
			if (n1 != n2) {
				return n1 > n2;
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SemanticVersion)) {
			return false;
		}
		SemanticVersion other = (SemanticVersion) o;
		int max = Math.max(versionNumber.length, other.versionNumber.length);
		for (int i = 0; i < max; i++) {
			int n1 = getVersion(i);
			int n2 = other.getVersion(i);
			if (n1 != n2) {
				return false;
			}
		}
		return true;
	}
	
}
