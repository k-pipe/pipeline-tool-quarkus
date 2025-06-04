package pipelining.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

import static pipelining.logging.Log.onException;

public class Utf8 {

	public static String read(InputStream inputStream) throws IOException {
		try (Scanner s = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A")) {
			String res =  s.hasNext() ? s.next() : "";
			if (s.ioException() != null) {
				throw s.ioException();
			}
			return res;
		}
	}

	public static String toString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static byte[] toBytes(String string) {
		return string.getBytes(StandardCharsets.UTF_8);
	}

	public static PrintStream printStream(OutputStream outputStream) {
		return new PrintStream(outputStream, true, StandardCharsets.UTF_8);
	}

	public static Iterator<String> lineIterator(final InputStream inputStream) {
		// DO NOT use Scanner! Scanner can make unexpected line breaks!
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		return new Iterator<String>() {
			String nextLine = null;

			@Override
			public boolean hasNext() {
				nextLine = onException(() -> br.readLine()).fallbackNull();
				if (nextLine == null) {
					onException(() -> br.close()).fail("could not close input stream");
				}
				return nextLine != null;
			}

			@Override
			public String next() {
				return nextLine;
			}
		};
	}
}
