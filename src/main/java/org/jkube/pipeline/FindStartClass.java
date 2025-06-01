package org.jkube.pipeline;

import org.jkube.application.Application;
import org.jkube.job.Job;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jkube.application.Application.fail;
import static org.jkube.logging.Log.*;

public class FindStartClass {

	public static final String JAR_FILE = "/main.jar";
	private static final String SOURCE_FOLDER = "src/main/java/";
	//private static final String CLASSES_IN_JAR = "BOOT-INF/classes/";
	private static final String CLASSES_IN_JAR = "./";
	private static final String JAVA_EXTENTION = ".java";
	private static final String CLASS_EXTENSION = ".class";

	private static final List<String> EXCLUDED_CLASS_PREFIXES = List.of("com.fasterxml.", "org.apache.", "org.codehaus.", "com.ctc.", "javax.", "META-INF.", "org.jkube.");

	public static Class<? extends Job> findJobClass() {
		Class<? extends Job> jobClass = onException(FindStartClass::tryFindJobClass).fallbackNull();
		if (jobClass == null) {
			fail("Could not find job class");
		}
		log("Main class found: " + jobClass);
		return jobClass;
	}

	private static Class<? extends Job> tryFindJobClass() throws IOException {
		return Application.isRunningInDocker()
		 ? tryFindJobClassInJar()
		 : tryFindJobClassInSources();
	}

	private static Class<? extends Job> tryFindJobClassInJar() throws IOException {
		log("Scanning classes in " + CLASSES_IN_JAR +" of jar "+JAR_FILE);
		Path jarClasses = FileSystems.newFileSystem(Path.of(JAR_FILE), Collections.emptyMap()).getPath(CLASSES_IN_JAR);
		return searchJobClass(jarClasses, CLASSES_IN_JAR, CLASS_EXTENSION);
	}

	public static void main(String[] args) throws IOException {
		String jar = "/Volumes/UserData/git/JavaLand/org.jkube.step.example/target/main.jar";
		log("Scanning classes in " + CLASSES_IN_JAR +" of jar "+jar);
		Path jarClasses = FileSystems.newFileSystem(Path.of(jar), Collections.emptyMap()).getPath(CLASSES_IN_JAR);
		System.out.println("Found: "+searchJobClass(jarClasses, CLASSES_IN_JAR, CLASS_EXTENSION));
	}

	private static Class<? extends Job> tryFindJobClassInSources() {
		log("Scanning source files in "+SOURCE_FOLDER);
		return searchJobClass(Path.of(SOURCE_FOLDER), SOURCE_FOLDER, JAVA_EXTENTION);
	}

	private static Class<? extends Job> searchJobClass(final Path path, final String prefix, final String suffix) {
		Set<Class<? extends Job>> candidates = new HashSet<>();
		onException(() -> Files.walk(path).forEach(p -> check(p, prefix, suffix, candidates)))
				.fail("Could not walk file tree "+path);
		if (candidates.isEmpty()) {
			fail("Could not find any job class");
		}
		if (candidates.size() > 1) {
			fail("Found multiple job classes: " + candidates);
		}
		return candidates.iterator().next();
	}

	private static void check(final Path path, final String prefix, final String suffix, final Set<Class<? extends Job>> candidates) {
		String filename = path.toString().replaceAll("\\\\", "/");
		if (filename.startsWith(prefix) && filename.endsWith(suffix)) {
			String className = filename
					.substring(prefix.length(), filename.length() - suffix.length())
					.replaceAll("/", ".");
			final Class<? extends Job> jobclass = onException(() -> jobClass(className)).fallback(null);
			if (jobclass != null) {
				candidates.add(jobclass);
			}
		}
	}

	private static Class<? extends Job> jobClass(final String name) throws ClassNotFoundException {
		for (String prefix : EXCLUDED_CLASS_PREFIXES) {
			if (name.startsWith(prefix)) {
				return null;
			}
		}
		log("Checking class "+name);
		try {
			Class<?> cl = Class.forName(name);
			if (Job.class.isAssignableFrom(cl) && !Modifier.isAbstract(cl.getModifiers())) {
				return (Class<? extends Job>) cl;
			}
		} catch (Throwable e) {
			warn("Class {} not created: {} ", name, e);
		}
		return null;
	}

}
