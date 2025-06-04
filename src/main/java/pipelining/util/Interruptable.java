package pipelining.util;

@FunctionalInterface
public interface Interruptable {
	void run() throws InterruptedException;
}
