package com.kneissler.ui.domain;


/**
 * A PipelineItem.
 */
public class PipelineItem extends Entity {

    private Long id;

    private Integer order;

    private String jsonData;

    private PipelineContent pipelineContent;

    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return Integer.toString(order);
    }

    public PipelineItem id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrder() {
        return this.order;
    }

    public PipelineItem order(Integer order) {
        this.setOrder(order);
        return this;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getJsonData() {
        return this.jsonData;
    }

    public PipelineItem jsonData(String jsonData) {
        this.setJsonData(jsonData);
        return this;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public PipelineContent getPipelineContent() {
        return this.pipelineContent;
    }

    public void setPipelineContent(PipelineContent pipelineContent) {
        this.pipelineContent = pipelineContent;
    }

    public PipelineItem pipelineContent(PipelineContent pipelineContent) {
        this.setPipelineContent(pipelineContent);
        return this;
    }

}
