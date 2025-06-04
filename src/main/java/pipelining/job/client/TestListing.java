package pipelining.job.client;

public class TestListing {

	public static void main(String... args) {
		System.out.println(new JobClient("default").listFilesFrom("http://34.149.44.140/service/default/resource/"));
	}
}
