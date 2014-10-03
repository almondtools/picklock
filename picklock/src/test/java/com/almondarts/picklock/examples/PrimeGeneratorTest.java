package com.almondarts.picklock.examples;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;


public class PrimeGeneratorTest {

	private PrimeGenerator pg;
	
	@Before
	public void before() throws Exception {
		pg = new PrimeGenerator(16);
	}
	
	@Test
	public void testFirstPrime() throws Exception {
		assertThat(pg.getPrime(0), equalTo(2));
	}

	@Test
	public void testSecondPrime() throws Exception {
		assertThat(pg.getPrime(1), equalTo(3));
	}

	@Test
	public void testThirdPrime() throws Exception {
		assertThat(pg.getPrime(2), equalTo(5));
	}

	@Test
	public void testNextPrimes() throws Exception {
		assertThat(pg.getPrime(3), equalTo(7));
		assertThat(pg.getPrime(4), equalTo(11));
		assertThat(pg.getPrime(5), equalTo(13));
	}

	@Test
	public void testNextPrimesWithNewBuffer() throws Exception {
		assertThat(pg.getPrime(6), equalTo(17));
		assertThat(pg.getPrime(7), equalTo(19));
		assertThat(pg.getPrime(8), equalTo(23));
	}

}
