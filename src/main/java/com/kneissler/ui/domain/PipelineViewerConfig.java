package com.kneissler.ui.domain;

/**
 * A PipelineViewerConfig.
 */
public class PipelineViewerConfig extends Entity {

    private Long id;

    private String monitoringPage;

    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return "PipelineViewerConfig"+id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMonitoringPage() {
        return this.monitoringPage;
    }

    public void setMonitoringPage(String monitoringPage) {
        this.monitoringPage = monitoringPage;
    }

    @Override
    public String toString() {
        return "PipelineViewerConfig{" +
            "id=" + getId() +
            ", monitoringPage='" + getMonitoringPage() + "'" +
            "}";
    }
}
