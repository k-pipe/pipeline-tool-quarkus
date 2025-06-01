package com.kneissler.ui.domain;

/**
 * A HistoryEntry.
 */
public class HistoryEntry extends Entity {
    private Long id;

    private String lastUpdated;

    private String username;

    private HistoryType type;

    private String htmlUri;

    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return Long.toString(id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HistoryType getType() {
        return this.type;
    }

    public void setType(HistoryType type) {
        this.type = type;
    }

    public String getHtmlUri() {
        return this.htmlUri;
    }

    public void setHtmlUri(String htmlUri) {
        this.htmlUri = htmlUri;
    }

    @Override
    public String toString() {
        return "HistoryEntry{" +
            "id=" + getId() +
            ", lastUpdated='" + getLastUpdated() + "'" +
            ", username='" + getUsername() + "'" +
            ", type='" + getType() + "'" +
            ", htmlUri='" + getHtmlUri() + "'" +
            "}";
    }
}
