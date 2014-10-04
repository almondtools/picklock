package com.almondarts.picklock.examples;

import static com.almondarts.picklock.ObjectAccess.unlock;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.almondarts.picklock.ClassAccess;

public class RomanDecoratorTest {

	@Test
	public void testToRoman0() {
		assertThat(RomanProvider.toRoman(0), equalTo("0"));
	}

	@Test
	public void testToRoman1to3() {
		assertThat(RomanProvider.toRoman(1), equalTo("I"));
		assertThat(RomanProvider.toRoman(2), equalTo("II"));
		assertThat(RomanProvider.toRoman(3), equalTo("III"));
	}

	@Test
	public void testToRoman4() {
		assertThat(RomanProvider.toRoman(4), equalTo("IV"));
	}

	@Test
	public void testParts5() {
		assertThat(RomanProvider.toRoman(5), equalTo("V"));
	}

	/**
	 * this test will not work since numberProvider is a Singleton with hidden/inaccessible interior
	 */
	@Test
	@Ignore
	public void testDecorate() throws Exception {
		NumberProvider numberProvider = NumberProvider.getInstance();
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecorate1() throws Exception {
		NumberProvider numberProvider = NumberProvider.getInstance();
		access(numberProvider).setNr(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecorate2() throws Exception {
		NumberProvider numberProvider = access().create(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	private UnlockedConstructorNumberProvider access() throws NoSuchMethodException {
		return ClassAccess.unlock(NumberProvider.class).features(UnlockedConstructorNumberProvider.class);
	}

	private UnlockedNumberProvider access(NumberProvider numberProvider) throws NoSuchMethodException {
		return unlock(numberProvider).features(UnlockedNumberProvider.class);
	}

	private static interface UnlockedNumberProvider {
		void setNr(int nr);
	}

	private static interface UnlockedConstructorNumberProvider {
		NumberProvider create(int seed);
	}

}
