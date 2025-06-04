package pipelining.ui.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * A Step.
 */
public class Step extends Entity {

    private Long id;

    private StepState state;

    private Integer order;

    private String name;

    private String error;

    private String log;

    private String configJson;

    private Job job;

    private Set<Pipeline> pipelines = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public Step id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StepState getState() {
        return this.state;
    }

    public Step state(StepState state) {
        this.setState(state);
        return this;
    }

    public void setState(StepState state) {
        this.state = state;
    }

    public Integer getOrder() {
        return this.order;
    }

    public Step order(Integer order) {
        this.setOrder(order);
        return this;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getName() {
        return this.name;
    }

    public Step name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getError() {
        return this.error;
    }

    public Step error(String error) {
        this.setError(error);
        return this;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getLog() {
        return this.log;
    }

    public Step log(String log) {
        this.setLog(log);
        return this;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getConfigJson() {
        return this.configJson;
    }

    public Step configJson(String configJson) {
        this.setConfigJson(configJson);
        return this;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public Job getJob() {
        return this.job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Step job(Job job) {
        this.setJob(job);
        return this;
    }

    public Set<Pipeline> getPipelines() {
        return this.pipelines;
    }

    public void setPipelines(Set<Pipeline> pipelines) {
        if (this.pipelines != null) {
            this.pipelines.forEach(i -> i.setStep(null));
        }
        if (pipelines != null) {
            pipelines.forEach(i -> i.setStep(this));
        }
        this.pipelines = pipelines;
    }

    public Step pipelines(Set<Pipeline> pipelines) {
        this.setPipelines(pipelines);
        return this;
    }

    public Step addPipeline(Pipeline pipeline) {
        this.pipelines.add(pipeline);
        pipeline.setStep(this);
        return this;
    }

    public Step removePipeline(Pipeline pipeline) {
        this.pipelines.remove(pipeline);
        pipeline.setStep(null);
        return this;
    }

}
