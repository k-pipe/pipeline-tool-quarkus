package pipelining.util.loggedtask;

import pipelining.util.loggedtask.items.CompositeItem;

public class JsonTest {

	public static void main(String... args) {
		CompositeItem log = new CompositeItem("Test");
		log.addConsole().add("ConsoleLine");
		log.message("Message");
		log.warn("Warning");
		CompositeItem subSection = log.section("SubSection");
		subSection.error("Error");
		/* String str = Json.encodePrettily(log);
		System.out.println(str);
		CompositeItem item2 = Json.decodeValue(str, CompositeItem.class);
		item2.getItems().forEach(i -> System.out.println(i.getClass()));
		 */
	}

}
