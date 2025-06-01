package com.kneissler.util.injection.jar;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class JarContent {

	private final List<String> entries = new ArrayList<>();

	public JarContent(URL jarUrl) throws FileNotFoundException {
		collectEntries(jarUrl);
	}

	public void collectEntries(URL jarUrl) throws FileNotFoundException {
		//System.out.println("JarURL: "+jarUrl);
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(jarUrl.openConnection().getInputStream()))) {
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				if (!ze.isDirectory()) {
					//System.out.println(" - "+ze.getName());
					entries.add(ze.getName());
				}
			}
		} catch (final Exception e) {
			throw new JarLoadingException("problem reading zip file from "+jarUrl, e);
		}
	}

	public List<String> getNames() {
		return entries;
	}

}
