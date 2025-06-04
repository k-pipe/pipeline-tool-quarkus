package pipelining.util.injection.jar;

import java.util.*;
import java.util.Map.Entry;

public class StaticResourceProvider {

	private static final Map<String, String> CONTENT_TYPE_BY_EXTENSION = new HashMap<>();
	static {
		CONTENT_TYPE_BY_EXTENSION.put("jpg", "image/jpeg"); 
		CONTENT_TYPE_BY_EXTENSION.put("png", "image/png");
		CONTENT_TYPE_BY_EXTENSION.put("js", "text/javascript; charset=UTF-8");
		CONTENT_TYPE_BY_EXTENSION.put("json", "application/json; charset=UTF-8");
		CONTENT_TYPE_BY_EXTENSION.put("html", "application/html; charset=UTF-8");
		CONTENT_TYPE_BY_EXTENSION.put("txt", "text/plain; charset=UTF-8");
	}

	private static final String DEFAULT_CONTENT = "text/plain; charset=UTF-8";

	private final Map<String, StaticResourceData> cachedResources = new HashMap<>();
	private final List<ClassLoader> classLoaders = new ArrayList<>();

	public StaticResourceProvider(Collection<? extends ClassLoader> classLoaders) {
		this.classLoaders.addAll(classLoaders);
	}

	public StaticResourceData getResource(String resourceName) {
		StaticResourceData res = cachedResources.get(resourceName);
		if (res == null) {
			res = tryLoad(resourceName);
			if (res != null) {
				cachedResources.put(resourceName, res);
			}
		}
		return res;
	}

	private StaticResourceData tryLoad(String resourceName) {
		for (ClassLoader cl : classLoaders) {
			StaticResourceData res = StaticResourceData.tryLoad(cl, resourceName, getContentType(resourceName));
			if (res != null) {
				return res;
			}
 		}
		return null;
	}

	private String getContentType(String resourceName) {
		for (Entry<String, String> e : CONTENT_TYPE_BY_EXTENSION.entrySet()) {
			if (resourceName.endsWith("."+e.getKey())) {
				return e.getValue();
			}
		};
		return DEFAULT_CONTENT;
	}

}
