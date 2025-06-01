package com.kneissler.job.client;

import org.jkube.http.Http;

public class TestDelete {

	public static void main(String... args) {
		System.out.println(Http.delete("http://34.149.44.140/service/default/resource/jobs/new/import-job-tbps3nvytycm"));
	}
}
