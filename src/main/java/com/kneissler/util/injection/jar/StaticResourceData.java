package com.kneissler.util.injection.jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class StaticResourceData  {
	private byte[] data;
	private String contentType;

	private StaticResourceData(byte[] data, String contentType) {
		this.data = data;
		this.contentType = contentType;
	}

	public static StaticResourceData tryLoad(ClassLoader classLoader, String resourceName, String contentType) {
		String name = classLoader instanceof  JarClassLoader
				? ((JarClassLoader)classLoader).getJarFile().getJarName()
				: classLoader.toString();
		System.out.println("Try loading resource "+resourceName+" of type "+contentType+" from classloader "+name);
		try (InputStream in = classLoader.getResourceAsStream(resourceName)) {
			if (in == null) {
				System.out.println("Found no resource "+resourceName+" of type "+contentType+" in classloader "+name);
				return null;
			} else {
				byte[] data = readAllBytes(in);
				System.out.println("Read "+data.length+" bytes for resource "+resourceName);
				return new StaticResourceData(data, contentType);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] readAllBytes(InputStream in) {
		// JAVA 9 return in.readAllBytes();
		// read bytes from the input stream and store them in buffer
		try {			
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int len;

			while ((len = in.read(buffer)) != -1) {
				// write bytes from the buffer into output stream
				os.write(buffer, 0, len);
			}
			return os.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getDataAsString() {
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
