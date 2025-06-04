package pipelining.logging.exception;

@FunctionalInterface
public interface Interruptable {
	void run() throws InterruptedException;
}
