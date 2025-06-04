package pipelining.util.injection.jar;

import pipelining.util.versions.JarVersion;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JarStore {

	private static final String JAR_EXTENSION = ".jar";
	
	private final String path;
	private final String host;
	private final int port;
	private final String protocol;
	private final String tokenParam;
	private final String snapshotPath;
	private final String releasePath;

	public JarStore(String protocol, String host, int port, String path, String tokenParam, String snapshotPath, String releasePath) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.path = path;
		this.tokenParam = tokenParam;
		this.snapshotPath = snapshotPath;
		this.releasePath = releasePath;
	}
	
	public static JarStore localFileStore(String path) {
		return new JarStore("file", "", -1, path, "", "", "");
	}

	public static JarStore httpStore(String host, int port, String path, String tokenParam, String snapshotPath, String releasePath) {
		return new JarStore("http", host, port, path, tokenParam, snapshotPath, releasePath);
	}

	public URL getUrl(JarFile jarFile) {
		return getUrl(path(jarFile));
	}
	
	public URL getUrl(String jarPath) {
		try {
			return new URL(protocol, host, port, jarPath);
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException("Could not create jar URL for " +protocol+","+ host+","+jarPath);
		}
	}

	public String path(JarFile jarFile) {
		return path + pathElement(jarFile.isSnapshot()) + jarFile.getJarName()+tokenParam;
	}

	public JarFile getJarFile(JarNode node, JarVersion version) {
		return new JarFile(node.getName(), version);
	}
	
	private String pathElement(boolean snapshot) {
		return snapshot ? snapshotPath : releasePath;
	}
	
	public List<String> listVersions(String jarname, boolean snapshot) {
		try {
			return listVersions(getUrl(path+pathElement(snapshot)+tokenParam), jarname);
		} catch (IOException e) {
			System.err.println("Could not list jar versions");
			e.printStackTrace();
			return null;
		}
	}

	private List<String> listVersions(URL url, String jarName) throws IOException {
		if (url.getProtocol().toLowerCase().equals("file")) {
			return listVersionFromDir(url.getPath(), jarName);
		} else {			
			System.out.println("Listing files from "+url);
			return listVersionsFromService((HttpURLConnection)url.openConnection(), jarName);
		}
	}

	private List<String> listVersionFromDir(String path, String jarNamePrefix) {
		List<String> res = new ArrayList<>();
		for (String file : new File(path).list()) {
			if (file.startsWith(jarNamePrefix) && file.endsWith(JAR_EXTENSION)) {
				res.add(file.substring(jarNamePrefix.length(), file.length()-JAR_EXTENSION.length()));
			}	
		}
		return res;
	}

	private List<String> listVersionsFromService(HttpURLConnection con, String jarNamePrefix) throws IOException {
			//System.out.println("Loading versions from "+getUrl(path + "/").toString());
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/json");
			if (con.getResponseCode() != 200) {
				System.err.println("Could not get directory");
			}				
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			//System.out.println("Received: "+content);
			in.close();
			return extractVersion(content.toString(), jarNamePrefix);
	}

	private List<String> extractVersion(String response, String jarnamePrefix) {
		List<String> res = new ArrayList<>();
		int pos = response.indexOf(":");
		if (pos < 0) {
			throw new RuntimeException("expected :");
		}
		String rem = response.substring(pos+1);
		if (!rem.startsWith("[")) {
			throw new RuntimeException("expected [");
		}
		if (!rem.endsWith("]}")) {
			throw new RuntimeException("expected ]}");
		}
		String[] split = rem.substring(1, rem.length()-2).split(",");
		for (String s : split) {
			if (!s.startsWith("\"")) {
				throw new RuntimeException("expected \"");
			}
			if (!s.endsWith("\"")) {
				throw new RuntimeException("expected \"");
			}
			String ins = s.substring(1,s.length()-1);
			if (ins.startsWith(jarnamePrefix) && ins.endsWith(JAR_EXTENSION)) {
				res.add(ins.substring(jarnamePrefix.length(), ins.length()-JAR_EXTENSION.length()));
			}
		}
		return res;
	}

}
