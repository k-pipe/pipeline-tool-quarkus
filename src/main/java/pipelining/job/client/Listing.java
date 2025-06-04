package pipelining.job.client;

import java.util.HashMap;
import java.util.List;

public class Listing extends HashMap<String, List<String>> {

	public List<String> get() {
		return values().iterator().next();
	}

}
