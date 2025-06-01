package com.kneissler.job.specification;

public class ImageDefinition {
	private String repository;
	private String image;
	private String tag;

	public ImageDefinition() {
	}

	public ImageDefinition(final String repository, final String image, final String tag) {
		this.repository = repository;
		this.image = image;
		this.tag = tag;
	}

	public String getRepository() {
		return repository;
	}

	public String getImage() {
		return image;
	}

	public String getTag() {
		return tag;
	}

	@Override
	public String toString() {
		return repository + '/' + image + ':'+tag;
	}
}
