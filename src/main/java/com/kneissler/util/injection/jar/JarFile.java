package com.kneissler.util.injection.jar;

import com.kneissler.util.versions.JarVersion;

public class JarFile {	


	private String name;
	private JarVersion version;

	public JarFile(String name, JarVersion version) {
		this.name = name;
		this.version = version;
	}

	public String getJarName() {
		return name+JarName.SEPARATOR1+version.getSuffix()+JarName.JAR_EXTENSION;
	}

	public boolean isSnapshot() {
		return version.isSnapshot();
	}

}
