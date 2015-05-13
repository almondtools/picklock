package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.Test;

public class PicklockMatcherTest {

	@Test
	public void testDescribeTo() throws Exception {
		PicklockMatcher matcher = new PicklockMatcher(UnlockedInterface.class);
		Description description = new StringDescription();

		matcher.describeTo(description);

		assertThat(description.toString(), equalTo("can unlock features of <interface com.almondtools.picklock.PicklockMatcherTest$UnlockedInterface>"));
	}

	@Test
	public void testDescribeMismatchSafely() throws Exception {
		PicklockMatcher matcher = new PicklockMatcher(UnlockedInterface.class);
		Description description = new StringDescription();

		matcher.describeMismatch(Object.class, description);

		assertThat(description.toString(), containsString("cannot map following members in <class java.lang.Object>: "));
		assertThat(description.toString(), containsString("void setStr(String)"));
		assertThat(description.toString(), containsString("int getI()"));
	}

	interface UnlockedInterface {

		void setStr(String str);

		int getI();
	}

}
