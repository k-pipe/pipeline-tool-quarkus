package org.jkube.job.implementation;

import org.jkube.job.Job;
import org.jkube.job.pipeline.BasePipelineJob;
import org.jkube.pipeline.EnumPipesOutImpl;
import org.jkube.pipeline.PipesInImpl;
import org.jkube.pipeline.StringPipesOutImpl;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jkube.logging.Log.log;
import static org.jkube.logging.Log.onException;

public class JobRunner {

	private final Job job;
	private final String inputDir;
	private final String outputDir;
	private Class<?> itemClass;
	private Class<?> configClass;
	private List<Object> pipelineOutValues; // possible enum values of corresponding to output pipes, null if E is String

	public JobRunner(final Class<? extends Job> jobClass, String inputDir, String outputDir) {
		this.job = onException(() -> tryCreateInstance(jobClass)).fail("Could not create job instance");
		if (job instanceof BasePipelineJob) {
			extractGenericArguments((BasePipelineJob)job);
		}
		this.inputDir = inputDir;
		this.outputDir = outputDir;
	}

	private void extractGenericArguments(final BasePipelineJob job) {
		job.captureTypes();
		log("Class Information:");
		log("Pipeline class: "+job.getClass());
		log("Configuration class: "+job.getGenericTypeC());
		log("Item class: "+job.getGenericTypeD());
		log("Result class: "+job.getGenericTypeE());
		itemClass = job.getGenericTypeD();
		configClass = job.getGenericTypeC();
		pipelineOutValues = job.getGenericTypeE().isEnum() ? Arrays.asList(job.getGenericTypeE().getEnumConstants()) : null;
		if (pipelineOutValues != null) {
			log("Result values: " + pipelineOutValues.stream().map(Object::toString).collect(Collectors.joining(", ")));
		}
	}

	private Job tryCreateInstance(final Class<? extends Job> jobClass)
			throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		final Constructor<? extends Job> constructor = jobClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();
	}

	public boolean run() {
		JobIO jobIO = new JobIO(job, configClass);
		Workdir workdir = new Workdir(Path.of(inputDir), Path.of(outputDir));
		// set input fields
		for (final Field input : jobIO.getInputs()) {
			if (jobIO.isFile(input)) {
				jobIO.setField(input, workdir.getInput(jobIO.getInputName(input)).toFile());
			} else if (jobIO.isOptional(input)) {
				jobIO.setField(input, optionalFile(workdir.getInput(jobIO.getInputName(input))));
			} else if (jobIO.isFileSystem(input)) {
				jobIO.setField(input, workdir.getFileSystem(jobIO.getInputName(input)));
			} else if (jobIO.isPipesIn(input)) {
				jobIO.setField(input, new PipesInImpl<>(jobIO.getInputName(input), workdir, itemClass));
			} else if (!workdir.readInputFile(jobIO.getInputName(input), jobIO.getOutStream(input))) {
				return false;
			}
		}
		// set output fields that are files or pipeout
		for (final Field output : jobIO.getOutputs()) {
			if (jobIO.isFile(output)) {
				jobIO.setField(output, workdir.getOutput(jobIO.getOutputName(output)).toFile());
			} else if (jobIO.isPipesOut(output)) {
				jobIO.setField(output, pipelineOutValues == null
						? new StringPipesOutImpl<>(jobIO.getOutputName(output), workdir)
			            : new EnumPipesOutImpl(jobIO.getOutputName(output), workdir, pipelineOutValues));
			}
		}
		job.run();
		// create outputs (unless they are files or pipesOut)
		for (final Field output : jobIO.getOutputs()) {
			if (!jobIO.isFile(output) && !jobIO.isPipesOut(output)) {
				if (!workdir.writeOutputFile(jobIO.getOutputName(output), jobIO.getInStream(output))) {
					return false;
				}
			}
		}
		return true;
	}

	private Optional<File> optionalFile(final Path input) {
		File file = input.toFile();
		return file.exists() ? Optional.of(file) : Optional.empty();
	}

}
