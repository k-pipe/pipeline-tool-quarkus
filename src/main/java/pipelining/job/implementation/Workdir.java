package pipelining.job.implementation;

import pipelining.job.Run;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static pipelining.application.Application.fail;
import static pipelining.logging.Log.onException;

public class Workdir {

	private final Path inputPath;
	private final Path outputPath;

	public Workdir() {
		this(Path.of(Run.DOCKER_WORKDIR));
	}

	public Workdir(Path workdirPath) {
		this(workdirPath.resolve(Run.DEFAULT_INPUT), workdirPath.resolve(Run.DEFAULT_OUTPUT));
	}

	public Workdir(Path inputPath, Path outputPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		createIfNeeded();
	}

	public void createIfNeeded() {
		createIfNeeded(inputPath);
		createIfNeeded(outputPath);
	}

	private void createIfNeeded(final Path dir) {
		if (!dir.toFile().exists()) {
			if (!dir.toFile().mkdirs()) {
				fail("Could not create folder "+dir);
			}
		}
	}

	public boolean writeInputFile(String filename, InputStream in) {
		return stream2File(in,getInput(filename));
	}

	public boolean writeOutputFile(String filename, InputStream in) {
		return stream2File(in, getOutput(filename));
	}

	public boolean readInputFile(String filename, OutputStream out) {
		return file2Stream(getInput(filename), out);
	}

	public boolean readOutputFile(String filename, OutputStream out) {
		return file2Stream(getOutput(filename), out);
	}

	public Path getInput(final String filename) {
		return inputPath.resolve(filename);
	}


	public Path getOutput(final String filename) {
		return outputPath.resolve(filename);
	}

	public boolean file2Stream(final Path inFile, final OutputStream outStream) {
		return onException(() -> tryCopy(inFile, outStream))
				.warn("Could not copy data from "+inFile)
				.fallback(false);
	}

	private boolean tryCopy(final Path inFile, final OutputStream outStream) throws IOException {
		long len = Files.copy(inFile, outStream);
		outStream.close();
		return len > 0;
	}

	public boolean stream2File(final InputStream inStream, Path outFile) {
		return onException(() -> tryCopy(inStream, outFile))
				.warn("Could not copy data to "+outFile)
				.fallback(false);
	}

	private boolean tryCopy(final InputStream inStream, final Path outFile) throws IOException {
		long len = Files.copy(inStream, outFile);
		inStream.close();
		return len > 0;
	}

	public List<String> listInputs(final String suffix) {
		List<String> res = new ArrayList<>();
		final String[] files = inputPath.toFile().list();
		if (files != null) {
			for (String file : files) {
				if ((suffix == null) || file.endsWith(suffix)) {
					res.add(file);
				}
			}
		}
		return res;
	}

	public Path getInputDir() {
		return inputPath;
	}

	public Path getOutputDir() {
		return outputPath;
	}
}
