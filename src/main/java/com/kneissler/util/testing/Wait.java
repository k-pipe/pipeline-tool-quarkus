package com.kneissler.util.testing;

import java.util.Random;

public class Wait {

	public static final double MAX_WAIT= 1e-5;

	private final static Random random = new Random();

	public static void seconds(double seconds)  {
		try {
			double milis = seconds*1e3;
			long milisInt = (long) Math.floor(milis);
			int nanos = (int) Math.round((milis - milisInt)*1e6);
			//System.out.println("Sleeping "+milisInt+" milis, "+nanos+" nanos");
			Thread.sleep(milisInt, nanos);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static boolean random(double maxWait)  {
		seconds(random.nextDouble()*maxWait);
		return true;
	}

	public static boolean random() {
		return random(MAX_WAIT);
	}

}
