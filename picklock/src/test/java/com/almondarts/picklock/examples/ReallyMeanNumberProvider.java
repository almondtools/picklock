package com.almondarts.picklock.examples;



public final class ReallyMeanNumberProvider implements NumberProvider{

	private static final ReallyMeanNumberProvider FINAL_INSTANCE = new ReallyMeanNumberProvider();
	
	
	private int nr;
	
	public static ReallyMeanNumberProvider getInstance() {
		return FINAL_INSTANCE;
	}
	
	private ReallyMeanNumberProvider() {
		this.nr = (int) (Math.random() * 100.0);
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
