package pipelining.util;

import pipelining.logging.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static pipelining.logging.Log.error;

public abstract class OutputReader {

	private static final long TIMEOUT_SECONDS = 5;
	private static final long SLEEP_TIME = 200;
	private final BufferedReader in;
	private final Thread thread;
	private final AtomicBoolean running;

	public abstract void processLine(String line);

	public OutputReader(InputStream input) {
		this.in = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
		this.running = new AtomicBoolean(true);
		this.thread = new Thread(this::run);
		this.thread.start();
	}

	private void run() {
		boolean hasMore = true;
		while (running.get() || hasMore) {
			try {
				String line = in.readLine();
				hasMore = line != null;
				if (hasMore) {
					tryProcessLine(line);
				} else {
					interruptable(() -> Thread.sleep(SLEEP_TIME));
				}
			} catch (Exception e) {
				tryProcessLine("ERROR> Excpetion reading line: "+e);
				e.printStackTrace();
			}
		}
	}

	public static void interruptable(Interruptable interruptable) {
		try {
			interruptable.run();
		} catch (InterruptedException e) {
			Log.debug("Thread was interrupted, propagating interruption.");
			Thread.currentThread().interrupt();
		}
	}


	private void tryProcessLine(String line) {
		try {
			processLine(line);
		} catch (Exception e) {		
			e.printStackTrace();
		}
	}

	public void stop() {
		running.set(false);
		interruptable(() -> {
			thread.interrupt();
			thread.join(TIMEOUT_SECONDS*1000);
		});
		if (thread.isAlive()) {
			error("Output reading thread did not terminate, stoppping it");
			thread.interrupt();
		}
	}

}
