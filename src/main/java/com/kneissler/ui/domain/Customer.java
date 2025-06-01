package com.kneissler.ui.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Customer.
 */
public class Customer extends Entity {

    private Long id;
    private String name;

    private List<Job> jobs = new ArrayList<>();

    private Set<Schedule> schedules = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public Customer id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Customer name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Job> getJobs() {
        return this.jobs;
    }

    public void setJobs(List<Job> jobs) {
        if (this.jobs != null) {
            this.jobs.forEach(i -> i.setCustomer(null));
        }
        if (jobs != null) {
            jobs.forEach(i -> i.setCustomer(this));
        }
        this.jobs = jobs;
    }

    public Set<Schedule> getSchedules() {
        return this.schedules;
    }

    public void setSchedules(Set<Schedule> schedules) {
        if (this.schedules != null) {
            this.schedules.forEach(i -> i.setCustomer(null));
        }
        if (schedules != null) {
            schedules.forEach(i -> i.setCustomer(this));
        }
        this.schedules = schedules;
    }

    public Customer jobs(List<Job> jobs) {
        this.setJobs(jobs);
        return this;
    }

    public Customer addJob(Job job) {
        if (jobs == null)  {
            jobs = new ArrayList<>();
        }
        this.jobs.add(job);
        job.setCustomer(this);
        return this;
    }

    public Customer removeJob(Job job) {
        this.jobs.remove(job);
        job.setCustomer(null);
        return this;
    }

}
