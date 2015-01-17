package com.almondarts.picklock.examples.innerclass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondarts.picklock.ObjectAccess;

public class ExampleObjectTest {

	@Test
	public void testInnerStaticClassResult() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleObject unlockedExampleObject = ObjectAccess.unlock(exampleObject).features(UnlockedExampleObject.class);
		InnerStatic s = unlockedExampleObject.createInnerStatic();
		assertThat(s.getState(), equalTo("state"));
	}

	@Test
	public void testInnerStaticClassArgument() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleObject unlockedExampleObject = ObjectAccess.unlock(exampleObject).features(UnlockedExampleObject.class);
		assertThat(unlockedExampleObject.useInnerStatic(new InnerStatic() {

			@Override
			public String getState() {
				return null;
			}

			@Override
			public void setState(String state) {
			}
		}), is(false));
		assertThat(unlockedExampleObject.useInnerStatic(new InnerStatic() {

			@Override
			public String getState() {
				return "state";
			}

			@Override
			public void setState(String state) {
			}
		}), is(true));
	}

	@Test(expected=NoSuchMethodException.class)
	public void testInnerStaticMappingExceptionOnResult() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		ObjectAccess.unlock(exampleObject).features(UnlockedExampleExceptionResult.class);
	}

	@Test(expected=NoSuchMethodException.class)
	public void testInnerStaticMappingExceptionOnParams() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		ObjectAccess.unlock(exampleObject).features(UnlockedExampleExceptionParam.class);
	}

	@Test
	public void testInnerStaticRoundtrip() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleOther unlockedExampleObject = ObjectAccess.unlock(exampleObject).features(UnlockedExampleOther.class);
		InnerStaticOther s = unlockedExampleObject.createInnerStatic();
		assertThat(unlockedExampleObject.useInnerStatic(s), is(true));
	}

	interface UnlockedExampleObject {
		@AutoPicklock
		InnerStatic createInnerStatic();

		boolean useInnerStatic(@AutoPicklock InnerStatic arg);

	}

	interface InnerStatic {
		String getState();

		void setState(String state);
	}

	interface UnlockedExampleOther {
		@AutoPicklock("InnerStatic")
		InnerStaticOther createInnerStatic();

		boolean useInnerStatic(@AutoPicklock("InnerStatic") InnerStaticOther arg);

	}

	interface InnerStaticOther {
	}

	interface UnlockedExampleExceptionParam {

		boolean useInnerStatic(@AutoPicklock InnerStaticException arg);

	}

	interface UnlockedExampleExceptionResult {
		
		@AutoPicklock
		InnerStaticException createInnerStatic();

	}

	interface InnerStaticException {
	}

}
