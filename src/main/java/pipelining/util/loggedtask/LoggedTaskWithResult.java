package pipelining.util.loggedtask;

public abstract class LoggedTaskWithResult<R> extends LoggedTask {

	protected R result;
	
	public LoggedTaskWithResult(String title) {
		super(title);
	}
	
	protected abstract R determineResult();
	
	@Override
	protected void execute() {
		result = determineResult();
		if (result != null) {
			setDidSomething();
		}
	}
	
	public R getResult() {
		if (!wasExecuted()) {
			run();
		}
		return result;
	}

}
