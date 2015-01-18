package com.almondtools.picklock.examples;



public final class RandomNumberProvider implements NumberProvider {

	private static NumberProvider INSTANCE;
	
	private int nr;
	
	public static NumberProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RandomNumberProvider((int) (Math.random() * 100.0));
		}
		return INSTANCE;
	}
	
	private RandomNumberProvider(int nr) {
		this.nr = nr;
	}

	/* (non-Javadoc)
	 * @see com.almondtools.picklock.examples.NumberProvider#nextNr()
	 */
	@Override
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
