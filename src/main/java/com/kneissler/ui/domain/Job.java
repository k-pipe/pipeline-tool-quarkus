package com.kneissler.ui.domain;


import java.util.ArrayList;
import java.util.List;

/**
 * A Job.
 */
public class Job extends Entity {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private byte[] image;

    private String imageContentType;

    private JobState state;

    private String createdDate;

    private String script;

    private Customer customer;

    private List<Step> steps = new ArrayList<>();

    public Long getId() {
        return this.id;
    }

    public Job id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Job name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public String getScript() {
        return this.script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public Job customer(Customer customer) {
        this.setCustomer(customer);
        return this;
    }

    public List<Step> getSteps() {
        return this.steps;
    }

    public void setSteps(List<Step> steps) {
        if (this.steps != null) {
            this.steps.forEach(i -> i.setJob(null));
        }
        if (steps != null) {
            steps.forEach(i -> i.setJob(this));
        }
        this.steps = steps;
    }

    public Job steps(List<Step> steps) {
        this.setSteps(steps);
        return this;
    }

    public Job addStep(Step step) {
        this.steps.add(step);
        step.setJob(this);
        return this;
    }

    public Job removeStep(Step step) {
        this.steps.remove(step);
        step.setJob(null);
        return this;
    }

}

