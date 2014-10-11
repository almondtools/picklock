package com.almondarts.picklock.examples;

import static com.almondarts.picklock.ObjectAccess.unlock;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.almondarts.picklock.ClassAccess;

public class RomanProviderTest {

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
	 * this test will not work since numberProvider is a Singleton with
	 * hidden/inaccessible interior
	 */
	@Test
	@Ignore
	public void testDecorate() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecoratePrivateField() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		unlock(numberProvider)
				.features(UnlockedNumberProvider.class)
				.setNr(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecorateConstructor() throws Exception {
		NumberProvider numberProvider = ClassAccess
				.unlock(RandomNumberProvider.class)
				.features(UnlockedStaticNumberProvider.class)
				.create(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecoratePrivateMethod() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		unlock(numberProvider)
				.features(UnlockedNumberProvider.class)
				.reset(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecorateStaticField() throws Exception {
		NumberProvider numberProvider = ClassAccess
				.unlock(RandomNumberProvider.class)
				.features(UnlockedStaticNumberProvider.class)
				.create(21);
		ClassAccess
				.unlock(RandomNumberProvider.class)
				.features(UnlockedStaticNumberProvider.class)
				.setINSTANCE(numberProvider);
		RomanProvider decorator = new RomanProvider(RandomNumberProvider.getInstance());
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecorateStaticFinalField() throws Exception {
		UnlockedReallyMeanNumberProvider bluePrint = ClassAccess
				.unlock(ReallyMeanNumberProvider.class)
				.features(UnlockedReallyMeanNumberProvider.class);

		ReallyMeanNumberProvider numberProvider = bluePrint.create();
		bluePrint.setFINAL_INSTANCE(numberProvider);
		unlock(numberProvider)
				.features(UnlockedNumberProvider.class)
				.setNr(21);

		RomanProvider decorator = new RomanProvider(ReallyMeanNumberProvider.getInstance());
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	private static interface UnlockedNumberProvider {
		void setNr(int nr);

		void reset(int resetNr);
	}

	private static interface UnlockedStaticNumberProvider {
		RandomNumberProvider create(int seed);

		void setINSTANCE(NumberProvider provider);
	}

	private static interface UnlockedReallyMeanNumberProvider {
		ReallyMeanNumberProvider create();

		void setFINAL_INSTANCE(ReallyMeanNumberProvider provider);
	}

}
