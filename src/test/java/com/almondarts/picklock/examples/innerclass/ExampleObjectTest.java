package com.almondarts.picklock.examples.innerclass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondarts.picklock.Construct;
import com.almondarts.picklock.ConstructorConfig;
import com.almondarts.picklock.Convert;
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

	@Test
	public void testInnerStaticClassArgumentNoStandardConstructor() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleWithConstructorConfig unlockedExampleObject = ObjectAccess.unlock(exampleObject).features(UnlockedExampleWithConstructorConfig.class);
		assertThat(unlockedExampleObject.useInnerStatic(new InnerStaticWithoutStandardConstructor() {
		}), is(true));
	}

	interface UnlockedExampleObject {
		@Convert
		InnerStatic createInnerStatic();

		boolean useInnerStatic(@Convert InnerStatic arg);

	}

	interface InnerStatic {
		String getState();

		void setState(String state);
	}

	interface UnlockedExampleOther {
		@Convert("InnerStatic")
		InnerStaticOther createInnerStatic();

		boolean useInnerStatic(@Convert("InnerStatic") InnerStaticOther arg);

	}

	interface InnerStaticOther {
	}

	interface UnlockedExampleExceptionParam {

		boolean useInnerStatic(@Convert InnerStaticException arg);

	}

	interface UnlockedExampleExceptionResult {
		
		@Convert
		InnerStaticException createInnerStatic();

	}

	interface InnerStaticException {
	}

	interface UnlockedExampleWithConstructorConfig {

		boolean useInnerStatic(@Convert InnerStaticWithoutStandardConstructor arg);

	}

	@Construct(InnerStaticConstructorConfig.class)
	interface InnerStaticWithoutStandardConstructor {
	}
	
	private static class InnerStaticConstructorConfig implements ConstructorConfig {

		@Override
		public Object[] arguments() {
			return new Object[]{"injectedstate"};
		}
		
		@Override
		public Class<?>[] signature() {
			return new Class<?>[]{String.class};
		}
		
	}
}
