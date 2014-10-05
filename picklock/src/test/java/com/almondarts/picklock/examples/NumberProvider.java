package com.almondarts.picklock.examples;



public final class NumberProvider {

	private static NumberProvider INSTANCE;
	
	private int nr;
	
	public static NumberProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new NumberProvider((int) (Math.random() * 100.0));
		}
		return INSTANCE;
	}
	
	private NumberProvider(int nr) {
		this.nr = nr;
	}

	public int nextNr() {
		return nr++;
	}

	private void reset(int newseed) {
		this.nr = newseed;
	}
	
	public void reset() {
		reset((int) (Math.random() * 100.0));
	}
}
