package pipelining.util.request;


public class Request {

	public static void badRequest(String message) {
		issue(message, new BadRequestException(message));
	}
	
	public static void badRequest() {
		badRequest("Bad request");
	}

	public static void notFound(String message) {
		issue(message, new NotFoundException(message));
	}
	
	public static void notFound() {
		notFound("Not found");
	}

	private static void issue(String message, RuntimeException exception) {
		//Log.warn(message, exception);
		throw exception;
		
	}

}
