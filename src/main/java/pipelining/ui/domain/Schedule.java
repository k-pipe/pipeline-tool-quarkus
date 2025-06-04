package pipelining.ui.domain;


import java.io.Serializable;
import java.time.Instant;

/**
 * A Schedule.
 */
public class Schedule implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String cronSpec;

    private String repository;

    private String version;

    private Instant lastExecuted;

    private Boolean enabled;

    private Job lastRun;

    private Customer customer;

    public Long getId() {
        return this.id;
    }

    public Schedule id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Schedule name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronSpec() {
        return this.cronSpec;
    }

    public Schedule cronSpec(String cronSpec) {
        this.setCronSpec(cronSpec);
        return this;
    }

    public void setCronSpec(String cronSpec) {
        this.cronSpec = cronSpec;
    }

    public String getRepository() {
        return this.repository;
    }

    public Schedule repository(String repository) {
        this.setRepository(repository);
        return this;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getVersion() {
        return this.version;
    }

    public Schedule version(String version) {
        this.setVersion(version);
        return this;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Instant getLastExecuted() {
        return this.lastExecuted;
    }

    public Schedule lastExecuted(Instant lastExecuted) {
        this.setLastExecuted(lastExecuted);
        return this;
    }

    public void setLastExecuted(Instant lastExecuted) {
        this.lastExecuted = lastExecuted;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public Schedule enabled(Boolean enabled) {
        this.setEnabled(enabled);
        return this;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Job getLastRun() {
        return this.lastRun;
    }

    public void setLastRun(Job job) {
        this.lastRun = job;
    }

    public Schedule lastRun(Job job) {
        this.setLastRun(job);
        return this;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Schedule customer(Customer customer) {
        this.setCustomer(customer);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Schedule)) {
            return false;
        }
        return id != null && id.equals(((Schedule) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Schedule{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", cronSpec='" + getCronSpec() + "'" +
            ", repository='" + getRepository() + "'" +
            ", version='" + getVersion() + "'" +
            ", lastExecuted='" + getLastExecuted() + "'" +
            ", enabled='" + getEnabled() + "'" +
            "}";
    }
}
