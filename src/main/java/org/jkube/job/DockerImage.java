package org.jkube.job;

public class DockerImage {

	private final ImageType type;
	private final String provider;
	private final Integer generation;
	private final String path;
	private final String image;
	private final String tag;

	public DockerImage(final ImageType type, final String provider, final Integer generation, final String image, String path, final String tag) {
		this.type = type;
		this.provider = provider;
		this.generation = generation;
		this.image = image;
		this.path = path;
		this.tag = tag;
	}

	public static DockerImage bundled(final String image, String path) {
		return new DockerImage(ImageType.BUNDLED, null, null, image, path, null);
	}

	public static DockerImage managed(final String provider, final String image, String path, Integer generation) {
		return new DockerImage(ImageType.MANAGED, provider, generation, image, path, null);
	}

	public static DockerImage generic(final String image, String tag) {
		return new DockerImage(ImageType.GENERIC, null, null, image, null, tag);
	}

	@Override
	public String toString() {
		return switch (type) {
			case BUNDLED -> image+"("+path+")";
			case MANAGED -> image+"("+path+","+provider+","+generation+")";
			case GENERIC -> tag == null ? image : image+":"+tag;
		};
	}

	public String getProvider() {
		return provider;
	}

	public String getImage() {
		return image;
	}

	public String getPath() {
		return path;
	}
	public boolean isManaged() {
		return type.equals(ImageType.MANAGED);
	}
	public boolean isBundled() {
		return type.equals(ImageType.BUNDLED);
	}

	public String getTag() {
		return tag;
	}

	public Integer getGeneration() {
		return generation;
	}
}
