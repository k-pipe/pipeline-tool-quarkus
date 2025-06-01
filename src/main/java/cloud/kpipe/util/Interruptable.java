package cloud.kpipe.util;

@FunctionalInterface
public interface Interruptable {
	void run() throws InterruptedException;
}
