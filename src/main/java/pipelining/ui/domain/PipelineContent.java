package pipelining.ui.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * A PipelineContent.
 */
public class PipelineContent extends Entity {

    private Long id;

    private String resourceUrl;

    private Set<PipelineItem> pipelineItems = new HashSet<>();

    private Set<Pipeline> pipelines = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return "PipelineContent-"+id;
    }

    public PipelineContent resourceURL(String resourceURL) {
        setResourceUrl(resourceURL);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceUrl() {
        return this.resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public Set<PipelineItem> getPipelineItems() {
        return this.pipelineItems;
    }

    public void setPipelineItems(Set<PipelineItem> pipelineItems) {
        if (this.pipelineItems != null) {
            this.pipelineItems.forEach(i -> i.setPipelineContent(null));
        }
        if (pipelineItems != null) {
            pipelineItems.forEach(i -> i.setPipelineContent(this));
        }
        this.pipelineItems = pipelineItems;
    }

    public PipelineContent addPipelineItem(PipelineItem pipelineItem) {
        this.pipelineItems.add(pipelineItem);
        pipelineItem.setPipelineContent(this);
        return this;
    }

    public PipelineContent removePipelineItem(PipelineItem pipelineItem) {
        this.pipelineItems.remove(pipelineItem);
        pipelineItem.setPipelineContent(null);
        return this;
    }

    public Set<Pipeline> getPipelines() {
        return this.pipelines;
    }

    public void setPipelines(Set<Pipeline> pipelines) {
        if (this.pipelines != null) {
            this.pipelines.forEach(i -> i.setPipelineContent(null));
        }
        if (pipelines != null) {
            pipelines.forEach(i -> i.setPipelineContent(this));
        }
        this.pipelines = pipelines;
    }

    public PipelineContent addPipeline(Pipeline pipeline) {
        this.pipelines.add(pipeline);
        pipeline.setPipelineContent(this);
        return this;
    }

    public PipelineContent removePipeline(Pipeline pipeline) {
        this.pipelines.remove(pipeline);
        pipeline.setPipelineContent(null);
        return this;
    }

    @Override
    public String toString() {
        return "PipelineContent{" +
            "id=" + getId() +
            ", resourceUrl='" + getResourceUrl() + "'" +
            "}";
    }
}
