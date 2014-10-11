package com.almondarts.picklock;

import static com.almondarts.picklock.UnlockableMatcher.canBeTreatedAs;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ObjectAccessTest {

	private LockedObject object;

	@Before
	public void before() {
		object = new LockedObject();
	}

	@Test
		public void testUnlockable() throws Exception {
			assertThat(LockedObject.class, canBeTreatedAs(UnlockedObject.class));
			assertThat(LockedObject.class, not(canBeTreatedAs(UnlockedNotMatchingMethodObject.class)));
			assertThat(LockedObject.class, not(canBeTreatedAs(UnlockedNotMatchingGetterObject.class)));
			assertThat(LockedObject.class, not(canBeTreatedAs(UnlockedNotMatchingSetterObject.class)));
			assertThat(LockedObjectWithDeclaredExceptions.class, canBeTreatedAs(UnlockedWithCorrectExceptions.class));
			assertThat(LockedObjectWithDeclaredExceptions.class, not(canBeTreatedAs(UnlockedWithMissingExceptions.class)));
			assertThat(LockedObjectWithDeclaredExceptions.class, not(canBeTreatedAs(UnlockedWithFalseExceptions.class)));
		}

	@Test
	public void testMethodInvocation() throws Exception {
		UnlockedObject unlocked = ObjectAccess.unlock(object).features(UnlockedObject.class);
		assertThat(unlocked.myMethod("123", true), equalTo(123));
		assertThat(unlocked.myMethod("123", false), equalTo(0));
		assertThat(unlocked.myMethod("ABC", false), equalTo(0));
	}

	@Test
	public void testSetGetField() throws Exception {
		UnlockedObject unlocked = ObjectAccess.unlock(object).features(UnlockedObject.class);
		unlocked.setMyField("123");
		assertThat(object.myPublicMethod(), equalTo(123));
		assertThat(unlocked.getMyField(), equalTo("123"));

		unlocked.setMyField("ABC");
		assertThat(object.myPublicMethod(), equalTo(0));
		assertThat(unlocked.getMyField(), equalTo("ABC"));
	}

	@Test
	public void testNotExistingMethodInvocation() throws Exception {
		try {
			UnlockedNotMatchingMethodObject unlocked = ObjectAccess.unlock(object).features(UnlockedNotMatchingMethodObject.class);
			unlocked.notExistingMethod();
		} catch (NoSuchMethodException e) {
			assertThat(e.toString(), containsString("notExistingMethod"));
		}
	}

	@Test
	public void testNotExistingGetter() throws Exception {
		try {
			UnlockedNotMatchingGetterObject unlocked = ObjectAccess.unlock(object).features(UnlockedNotMatchingGetterObject.class);
			unlocked.getNotExisting();
		} catch (NoSuchMethodException e) {
			assertThat(e.toString(), containsString("getNotExisting"));
		}
	}

	@Test
	public void testNotExistingSetter() throws Exception {
		try {
			UnlockedNotMatchingSetterObject unlocked = ObjectAccess.unlock(object).features(UnlockedNotMatchingSetterObject.class);
			unlocked.setNotExisting(true);
		} catch (NoSuchMethodException e) {
			assertThat(e.toString(), containsString("setNotExisting"));
		}
	}

	@Test
	public void testSuperClassAccessForMethods() throws Exception {
		UnlockedObject unlocked = ObjectAccess.unlock(object).features(UnlockedObject.class);
		assertThat(unlocked.superMethod(), equalTo(5.0));
	}

	@Test
	public void testSuperClassAccessForProperties() throws Exception {
		UnlockedObject unlocked = ObjectAccess.unlock(object).features(UnlockedObject.class);
		unlocked.setSuperField(1.0);
		assertThat(unlocked.getSuperField(), equalTo(1.0));
	}

	@Test
	public void testCorrectExceptionSignature() throws Exception {
		UnlockedWithCorrectExceptions unlocked = ObjectAccess.unlock(new LockedObjectWithDeclaredExceptions()).features(UnlockedWithCorrectExceptions.class);
		try {
			String msg = unlocked.myMethod(null);
			fail("expected io exception, but found: " + msg);
		} catch (IOException e) {
			//expected
		}
	}
	
	@Test(expected=NoSuchMethodException.class)
	public void testMissingExceptionSignature() throws Exception {
		ObjectAccess.unlock(new LockedObjectWithDeclaredExceptions()).features(UnlockedWithMissingExceptions.class);
	}
	
	@Test(expected=NoSuchMethodException.class)
	public void testFalseExceptionSignature() throws Exception {
		ObjectAccess.unlock(new LockedObjectWithDeclaredExceptions()).features(UnlockedWithFalseExceptions.class);
	}
	
	public static interface UnlockedObject {
		void setMyField(String value);

		String getMyField();

		int myMethod(String string, boolean flag);

		double getSuperField();

		void setSuperField(double d);

		double superMethod();
	}

	public static interface UnlockedNotMatchingMethodObject {

		boolean notExistingMethod();
	}

	public static interface UnlockedNotMatchingSetterObject {
		
		void setNotExisting(boolean b);
	}

	public static interface UnlockedNotMatchingGetterObject {

		boolean getNotExisting();
	}

	public static interface UnlockedWithCorrectExceptions {

		String myMethod(String string) throws IOException;
	}

	public static interface UnlockedWithMissingExceptions {

		String myMethod(String string);
	}

	public static interface UnlockedWithFalseExceptions {

		String myMethod(String string) throws ClassCastException;
	}

}
