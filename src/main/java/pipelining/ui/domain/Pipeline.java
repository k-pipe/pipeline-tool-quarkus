package pipelining.ui.domain;

/**
 * A Pipeline.
 */
public class Pipeline extends Entity {

    private Long id;

    private String name;

    private PipelineType type;

    private Step step;

    private PipelineContent pipelineContent;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Pipeline id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Pipeline name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PipelineType getType() {
        return this.type;
    }

    public Pipeline type(PipelineType type) {
        this.setType(type);
        return this;
    }

    public void setType(PipelineType type) {
        this.type = type;
    }

    public Step getStep() {
        return this.step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Pipeline step(Step step) {
        this.setStep(step);
        return this;
    }

    public PipelineContent getPipelineContent() {
        return this.pipelineContent;
    }

    public Pipeline pipelineContent(PipelineContent pipelineContent) {
        this.setPipelineContent(pipelineContent);
        return this;
    }

    public void setPipelineContent(PipelineContent pipelineContent) {
        this.pipelineContent = pipelineContent;
    }

}