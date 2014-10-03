package com.almondarts.picklock.examples;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PrimeGenerator {

	private static final int DEFAULT_BUFFER_SIZE = 4096;

	private final int bufferSize;
	private boolean[] divisableBuffer;
	private int base;
	private List<Integer> primes;

	public PrimeGenerator(int bufferSize) {
		this.bufferSize = bufferSize;
		this.divisableBuffer = new boolean[bufferSize];
		this.base = 2;
		this.primes = new ArrayList<Integer>();
		this.primes.add(2);
	}

	public PrimeGenerator() {
		this(DEFAULT_BUFFER_SIZE);
	}

	public int getPrime(int index) {
		if (primes.size() <= index) {
			fillPrimes(index);
		}
		return primes.get(index);
	}

	private void fillPrimes(int upTo) {
		List<Integer> newPrimes = new LinkedList<Integer>(primes);
		while (primes.size() <= upTo) {
			
			fillSieve(newPrimes);
			
			int lastPrime = primes.get(primes.size() - 1);
			int from = Math.max(lastPrime, base);
			int to = Math.min(lastPrime * lastPrime, divisableBuffer.length + base);
			do {
				newPrimes = collectPrimes(from, to);
				from = to;
				to = to + lastPrime;
			} while (newPrimes.isEmpty() && to < divisableBuffer.length + base);
			
			if (from == divisableBuffer.length + base) {
				nextBufferWindow();
			}
		}
	}

	private void fillSieve(List<Integer> newPrimes) {
		for (Integer prime : newPrimes) {
			int firstDivisableNumber = findFirstDivisableBy(prime);
			int start = firstDivisableNumber - base;
			for (int i = start; i < divisableBuffer.length; i+= prime) {
				divisableBuffer[i] = true;
			}
		}
	}

	private List<Integer> collectPrimes(int from, int to) {
		List<Integer> newPrimes = new LinkedList<Integer>();
		for (int i = from - base + 1; i < to - base; i++) {
			if (!divisableBuffer[i]) {
				int prime = i + base;
				newPrimes.add(prime);
				primes.add(prime);
			}
		}
		return newPrimes;
	}

	private void nextBufferWindow() {
		base += bufferSize;
		divisableBuffer = new boolean[bufferSize];
	}

	private int findFirstDivisableBy(int prime) {
		if (isDivisible(base, prime)) {
			return base;
		} else {
			int number = nextMultiple(base, prime);
			return Math.min(number, divisableBuffer.length + base);
		}
	}

	private int nextMultiple(int number, int prime) {
		int div = number / prime;
		int incdiv = div + 1;
		return prime * incdiv;
	}

	private boolean isDivisible(int number, int prime) {
		return number % prime == 0;
	}
}
