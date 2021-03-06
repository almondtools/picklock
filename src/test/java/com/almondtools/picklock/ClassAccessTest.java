package com.almondtools.picklock;

import static com.almondtools.picklock.ClassUnlockableMatcher.canBeTreatedAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ClassAccessTest {

	private UnlockedObject unlocked;

	@Before
	public void before() throws Exception {
		unlocked = ClassAccess.unlock(LockedObjectWithPrivateConstructor.class).features(UnlockedObject.class);
	}

	@Test
	public void testUnlockable() throws Exception {
		assertThat(LockedObjectWithPrivateConstructor.class, canBeTreatedAs(UnlockedObject.class));
		assertThat(LockedObjectWithPrivateConstructor.class, not(canBeTreatedAs(UnlockedNotMatchingObject.class)));
		assertThat(LockedObjectWithPrivateConstructor.class, not(canBeTreatedAs(UnlockedFantasyObject.class)));
	}

	@Test
	public void testConstructorInvocation() throws Exception {
		assertThat(unlocked.create().getMyField(), equalTo("initialized"));
	}

	@Test
	public void testStaticInvocation() throws Exception {
		unlocked.setDEFAULT(null);
		assertThat(unlocked.reset().getMyField(), nullValue());
	}

	@Test
	public void testStaticSetGet() throws Exception {
		unlocked.setDEFAULT(null);
		assertThat(unlocked.getDEFAULT(), nullValue());
		unlocked.setDEFAULT("default");
		assertThat(unlocked.reset().getMyField(), equalTo("default"));
		assertThat(unlocked.getDEFAULT(), equalTo("default"));
	}

	@Test
	public void testStaticSetAfterGet() throws Exception {
		String result = unlocked.getDEFAULT();
		unlocked.setDEFAULT("");
		unlocked.setDEFAULT(result);
		assertThat(unlocked.getDEFAULT(), nullValue());
	}

	@Test(expected=PicklockException.class)
	public void testWrongSignature() throws Exception {
		ClassAccess.unlock(LockedObjectWithPrivateConstructor.class).features(UnlockedNotMatchingObject.class);
	}

	public static interface UnlockedObject {

		public LockedObjectWithPrivateConstructor create();

		public LockedObjectWithPrivateConstructor reset();

		public void setDEFAULT(String value);

		public String getDEFAULT();

	}

	public static interface UnlockedNotMatchingObject {

		public void setNOTEXISTING(String value);

		public String getNOTEXISTING();

	}

	public static interface UnlockedFantasyObject {

		public void method(String arg);

	}

}
