package com.kneissler.ui;

import com.kneissler.ui.domain.*;
import org.jkube.application.Application;
import org.jkube.http.Http;
import org.jkube.http.HttpSettings;
import org.jkube.json.Json;
import org.jkube.logging.Log;
import org.jkube.pipeline.definition.PipelineConnector;
import org.jkube.pipeline.definition.PipelineStep;
import org.jkube.util.Expect;
import org.jkube.util.Utf8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.jkube.logging.Log.log;

public class PipelineViewerClient {

//	private static final String URL = "http://localhost:18080/api/";
//	private static final String URL = "http://localhost:9000/api/";
	private static final String CUSTOMER = "customers";
	private static final String JOB = "jobs";
	private static final String PIPELINE = "pipelines";
	private static final String STEP = "steps";
	private static final String PIPELINE_CONTENT = "pipeline-contents";
	private static final String PIPELINE_ITEM = "pipeline-items";
	private static final String PIPELINE_ITEM_BATCH = "add-pipeline-items";
	private static final HttpSettings settings = createHttpSettings();
	private static final Map<String, String> COLORMAP = Map.of(
			"WRAPPER", "magenta",
			"LOG", "black",
			"OUT", "black",
			"SUCCESS", "green",
			"ERROR", "red");

	private final String url;
	private Map<PipelineStep, Integer> stepNumbers;

	public PipelineViewerClient(String url) {
		this.url = url;
	}

	private static HttpSettings createHttpSettings() {
		HttpSettings res = new HttpSettings();
		res.headers.put("Authorization", "Basic YWRtaW46YWRtaW4=");
		res.headers.put("Content-Type", "application/json");
		res.headers.put("Accept", "application/json");
		return res;
	}

	public void deleteAllPipelineData(String customerID, String runID, String stepId) {
		// TODO
		System.err.println("Deleting of pipelinedata not yet implemented");
		//forAllItems(pipeline, item -> delete(PIPELINE_ITEM, item));

	}

	public void deleteRun(String customerID, String runID) {
		System.err.println("Deleting of run not yet implemented");
		// TODO
	}

	/**
         * Create Pipelines and Pipeline items from json-lines file. Customer and Job will be created if not yet existent.
         *
         * @param pipeStream a stream providing pipeline items
         * @param customerID id of customer
         * @param runID      id of job
         * @param connectors information about all pipes that use the pipe file as input. All must have identical
         *                   source step/name
         */
	public int sumbitPipelineData(InputStream pipeStream, String customerID, String runID, List<PipelineConnector> connectors, String resourceURL) {
		Customer customer = getOrCreateCustomer(customerID);
		Job job = getOrCreateJob(runID, customer);
		List<String> items = readItems(pipeStream);
		System.out.println("Items read: "+items.size());
		PipelineContent content = createPipelineContent(resourceURL, items);
		createSourcePipeline(job, checkSourcesEqualAndReturn(connectors), content);
		connectors.forEach(pc -> createTargetPipeline(job, pc, content));
		return items.size();
	}

	/**
	 * Set the state of a Job. Customer and job will be created if not yet existent.
	 *
	 * @param customerID the id of the customer
	 * @param runID      the id of the run/job
	 * @param state      the new state
	 */
	public void setState(String customerID, String runID, JobState state) {
		Customer customer = getOrCreateCustomer(customerID);
		Job job = getOrCreateJob(runID, customer);
		job.setState(state);
		patchJob(job);
	}

	/**
	 * Set the visualization a Job. Customer and job will be created if not yet existent.
	 *
	 * @param customerID the id of the customer
	 * @param runID      the id of the run/job
	 * @param imageData  binary data of the image
	 * @param imageType  type of the image
	 */
	public void setImage(String customerID, String runID, byte[] imageData, String imageType) {
		System.out.println("Image data: "+imageData.length+" bytes");
		Customer customer = getOrCreateCustomer(customerID);
		Job job = getOrCreateJob(runID, customer);
		job.setImage(imageData);
		job.setImageContentType(imageType);
		patchJob(job);
	}


	/**
	 * Set the state of a step. Customer and job will be created if not yet existent.
	 *
	 * @param customerID the id of the customer
	 * @param runID      the id of the run/job
	 * @param pipelineStep the corresponding pipeline step
	 * @param state      the new state of the step
	 */
	public void setStepState(String customerID, String runID, PipelineStep pipelineStep, StepState state) {
		Customer customer = getOrCreateCustomer(customerID);
		Job job = getOrCreateJob(runID, customer);
		Step step = getOrCreateStep(pipelineStep, job);
		step.setState(state);
		patchStep(step);
	}

	/**
	 * Set the state of a step. Customer and job will be created if not yet existent.
	 * @param customerID the id of the customer
	 * @param runID      the id of the run/job
	 * @param pipelineStep the corresponding pipeline step
	 * @param state      the new state of the step
	 * @param log		 the log file
	 * @param error		 error messages or null
	 */
	public void setStepInfo(String customerID, String runID, PipelineStep pipelineStep, StepState state, String log, String error) {
		Customer customer = getOrCreateCustomer(customerID);
		Job job = getOrCreateJob(runID, customer);
		Step step = getOrCreateStep(pipelineStep, job);
		step.setState(state);
		step.setLog(formatLog(log));
		step.setError(formatLog(error));
		putStep(step);
	}

	private String formatLog(String log) {
		if (log == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (String line : log.split("\n")) {
			sb.append(htmlLog(line));
		}
		return sb.toString();
	}

	private String htmlLog(String line) {
		int pos = line.indexOf('>');
		String color = null;
		String log = null;
		if (pos < 0) {
			log = line;
		} else {
			color = COLORMAP.get(line.substring(0, pos));
			log = color == null ? line : line.substring(pos + 1);
		}
		if (color == null) {
			color = "black";
		}
		return "<span style=\"color:"+color+"\">"+log+"</span><br>";
	}


	/**
	 * Set the comfig information of a step. Customer and job will be created if not yet existent.
	 *
	 * @param customerID the id of the customer
	 * @param runID      the id of the run/job
	 * @param pipelineStep the corresponding pipeline step
	 * @param config     the contents of the config file (json string)
	 */
	public void setStepConfig(String customerID, String runID, PipelineStep pipelineStep, String config) {
		Customer customer = getOrCreateCustomer(customerID);
		Job job = getOrCreateJob(runID, customer);
		Step step = getOrCreateStep(pipelineStep, job);
		step.setConfigJson(config);
		patchStep(step);
	}

	/**
	 * Delete a job and all its steps and pipelines.
	 *
	 * @param customerID the id of the customer
	 * @param runID      the id of the run/job
	 */
	public void deleteJob(String customerID, String runID) {
		Optional<Customer> customer = findCustomerByName(customerID);
		if (customer.isPresent()) {
			System.out.println("Found customer "+customer.get().getId()+" with jobs="+customer.get().getJobs());
			Optional<Job> job = findJob(customer.get(), runID);
			if (job.isPresent()) {
				forAllSteps(job.get(), this::deleteStep);
				delete(JOB, job.get());
			}
		}
	}

	private void deleteStep(Step step) {
		forAllPipelines(step, this::deletePipeline);
		delete(STEP, step);
	}

	private void deletePipeline(Pipeline pipeline) {
		forAllItems(pipeline, item -> delete(PIPELINE_ITEM, item));
		delete(PIPELINE, pipeline);
	}

	/**
	 * for testing only: fill with some dummy data
	 */
	public void test() {
		log("Filling backend with dummy test data...");
		String json1 = "{\"id\": \"doc3\"}";
		String json2 = "{\"id\": \"doc4\"}";
		String json3 = "{\"id\": \"doc5\"}";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (PrintStream ps = new PrintStream(out)) {
			ps.println(json1);
			ps.println(json2);
			ps.println(json3);
		}
		String customer = "Customer 1";
		String run = "Test Run";
		//	deleteJob(customer, run);
		//System.exit(1);
		String sourceStep = "SourceStep";
		PipelineStep source = new PipelineStep(sourceStep);
		String targetStep = "TargetStep";
		PipelineStep target = new PipelineStep(targetStep);
		PipelineConnector conn1 = new PipelineConnector(source, target, "from.pipe", "to.pipe", "DUMMY2");
		sumbitPipelineData(new ByteArrayInputStream(out.toByteArray()), customer, run, List.of(conn1), "testrul");
		setStepConfig(customer, run, source, "{\"param\": 123}");
		log("Shutting down");
		System.exit(0);
	}

	private Pipeline createSourcePipeline(Job job, PipelineConnector connector, PipelineContent content) {
		Step step = getOrCreateStep(connector.getSource(), job);
		return createPipeline(connector.getNameAtSource(), PipelineType.OUTPUT, step, content);
	}

	private Pipeline createTargetPipeline(Job job, PipelineConnector connector, PipelineContent content) {
		Step step = getOrCreateStep(connector.getTarget(), job);
		return createPipeline(connector.getNameAtTarget(), PipelineType.INPUT, step, content);
	}

	private PipelineConnector checkSourcesEqualAndReturn(List<PipelineConnector> connectors) {
		if (connectors.isEmpty()) {
			Application.fail("no connectors provided");
		}
		PipelineConnector first = null;
		for (PipelineConnector pc : connectors) {
			if (first == null) {
				first = pc;
			} else {
				if (!pc.getSource().equals(first.getSource())) {
					Application.fail("different source steps: " + pc.getSource() + " vs. " + first.getSource());
				}
				if (!pc.getNameAtSource().equals(first.getNameAtSource())) {
					Application.fail("different source names: " + pc.getNameAtSource() + " vs. " + first.getNameAtSource());
				}
			}
		}
		return first;
	}

	private List<String> readItems(InputStream pipeStream) {
		List<String> res = new ArrayList<>();
		Utf8.lineIterator(pipeStream).forEachRemaining(res::add);
		return res;
	}

	private Customer getOrCreateCustomer(String customerID) {
		return findCustomerByName(customerID).orElseGet(() -> post(CUSTOMER, new Customer().name(customerID)));
	}

	private Job getOrCreateJob(String runID, Customer customer) {
		return findJob(customer, runID).orElseGet(() -> post(JOB, new Job().name(runID).customer(customer)));
	}

	private Step getOrCreateStep(PipelineStep step, Job job) {
		return findStep(job, step.getId()).orElseGet(() -> post(STEP, new Step()
				.name(step.getId())
				.order(stepNumbers.get(step))
				.job(job)
		));
	}

	private Pipeline createPipeline(String pipelineName, PipelineType type, Step step, PipelineContent pipelineContent) {
		return post(PIPELINE, new Pipeline().name(pipelineName).type(type).step(step).pipelineContent(pipelineContent));
	}

	private PipelineItem createItem(String json, int index, PipelineContent pipelineContent) {
		return post(PIPELINE_ITEM, new PipelineItem().jsonData(json).order(index).pipelineContent(pipelineContent));
	}

	private PipelineContent createPipelineContent(String resourceURL, List<String> itemLines) {
		PipelineContent content = post(PIPELINE_CONTENT, new PipelineContent().resourceURL(resourceURL));
		int index = 0;
		List<PipelineItem> itemlist = new ArrayList<>();
		for (String itemLine : itemLines) {
			index++;
			itemlist.add(new PipelineItem().jsonData(itemLine).order(index).pipelineContent(content));
		}
		post(PIPELINE_ITEM_BATCH, itemlist);
		return content;
	}

	private Optional<Job> findJob(Customer customer, String name) {
		return findByNameAndParent(customer, name,"customer", JOB, Job.class);
	}

	private Optional<Step> findStep(Job job, String name) {
		return findByNameAndParent(job, name,"job", STEP, Step.class);
	}

	private Optional<Pipeline> findPipeline(Step step, String name) {
		return findByNameAndParent(step, name,"step", PIPELINE, Pipeline.class);
	}

	private void forAllSteps(Job job, Consumer<Step> consumer) {
		getAllChildren(STEP, "job", job, Step.class).forEach(consumer::accept);
	}

	private void forAllPipelines(Step step, Consumer<Pipeline> consumer) {
		getAllChildren(PIPELINE, "step", step, Pipeline.class).forEach(consumer::accept);
	}

	private void forAllItems(Pipeline pipeline, Consumer<PipelineItem> consumer) {
		getAllChildren(PIPELINE_ITEM, "pipeline", pipeline, PipelineItem .class).forEach(consumer::accept);
	}

	private <E> E post(String type, E entity) {
		System.out.println("Posting "+type+": "+entity);
		Optional<String> response = Http.post(settings, url + type, Json.toString(entity));
		String result = Expect.present(response).elseFail("Could not post object of type " + type);
		//System.out.println("Got result: "+result);
		return (E) Json.fromString(result, entity.getClass());
	}

	private void patchJob(Job job) {
		patch(JOB, job.getId(), job);
	}

	private void patchStep(Step step) {
		patch(STEP, step.getId(), step);
	}

	private void putStep(Step step) {
		put(STEP, step);
	}

	private void put(String type, Entity entity) {
		System.out.println("Putting "+type+": "+entity);
		Optional<String> response = Http.put(settings, url+type+"/"+entity.getId(), Json.toString(entity));
		String result = Expect.present(response).elseFail("Could not put object of type " + type);
		//log("Response from put: " + response);
	}

	private Optional<Customer> findCustomerByName(String name) {
		// TODO customer end point does not have criteria
		//return getEntityOnPage(URL + CUSTOMER+"?name.equals="+ URLEncoder.encode(name, StandardCharsets.UTF_8), Customer.class);
		return getAll(CUSTOMER, Customer.class).stream().filter(c -> name.equals(c.getName())).findFirst();
	}

	private <E extends Entity> Optional<E> findByNameAndParent(Entity parent, String name, String parentLink, String type, Class<E> clazz) {
		return getEntityOnPage(url + type
				+"?name.equals="+URLEncoder.encode(name, StandardCharsets.UTF_8)
				+"&"+parentLink+"Id.equals="+parent.getId(),
				clazz);
	}

	private <E extends Entity> Optional<E> getEntityOnPage(String url, Class<E> clazz) {
		String pageUrl = url+"&page=0&size=1";
		System.out.println("Getting page from "+pageUrl);
		String pageString = Expect.present(Http.get(settings, pageUrl)).elseFail("could not read page");
		E[] page = null; // not implemented (E[]) Json.fromString(pageString, clazz.arrayType());
		System.out.println("got page of size "+page.length);
		if (page.length == 0) {
			return Optional.empty();
		} else {
			return Optional.of(page[0]);
		}
	}

	private void patch(String type, long id, Object entity) {
		System.out.println("Patching "+type+": "+entity);
		Optional<String> response = Http.patch(settings, url+type+"/"+id, Json.toString(entity));
		String result = Expect.present(response).elseFail("Could not patch object of type " + type);
		//log("Response from put: " + response);
	}

	private void delete(String type, Entity e) {
		System.out.println("Deleting "+type+": "+e);
		if (!Http.delete(settings, url+type+"/"+e.getId())) {
			Application.fail("could not delete "+type+" with id "+e.getId());
		}
	}

	private <E extends Entity> List<E> readAllPages(String url, Class<E> clazz) {
		List<E> res = new ArrayList<>();
		int pageId = 0;
		boolean done = false;
		while (!done) {
			String pageUrl = url + "page=" + pageId + "&size=100";
			Log.log("Retrieving page from {}", pageUrl);
			String pageString = Expect.present(Http.get(settings, pageUrl))
					.elseFail("could not read page");
			E[] page = (E[]) Json.fromString(pageString, clazz.arrayType());
			for (E e : page) {
				res.add(e);
			}
			done = page.length == 0;
			pageId++;
		}
		System.out.println("Got "+res.size()+" entities from "+(pageId-1)+" pages");
		return res;
	}

	private <E extends Entity> List<E> getAllChildren(String type, String parentLink, Entity parent, Class<E> clazz) {
		return readAllPages(url+type+"?"+parentLink+"Id.equals="+parent.getId()+"&", clazz);
	}

	private <E extends Entity> List<E> getAll(String type, Class<E> clazz) {
		return readAllPages(url+type+"?", clazz);
	}

	public void setStepNumbers(Map<PipelineStep, Integer> stepNumbers) {
		this.stepNumbers = stepNumbers;
	}
}
