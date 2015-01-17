package com.almondarts.picklock.examples.innerclass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.almondarts.picklock.ObjectAccess;



public class ExampleObjectTest {

	@Test @Ignore
	public void testInnerStaticClassResult() throws Exception {
		ExampleObject exampleObject = new ExampleObject();
		UnlockedExampleObject unlockedExampleObject = ObjectAccess.unlock(exampleObject).features(UnlockedExampleObject.class);
		InnerStatic s = unlockedExampleObject.createInnerStatic();
		assertThat(s.getState(), equalTo("state"));
	}
	
	@Test @Ignore
	public void testInnerStaticClassArgument() throws Exception {
		ExampleObject exampleObject = new ExampleObject();
		UnlockedExampleObject unlockedExampleObject = ObjectAccess.unlock(exampleObject).features(UnlockedExampleObject.class);
		assertThat(unlockedExampleObject.useInnerStatic(new InnerStatic() {
			
			@Override
			public String getState() {
				return null;
			}
		}), is(false));
		assertThat(unlockedExampleObject.useInnerStatic(new InnerStatic() {
			
			@Override
			public String getState() {
				return "state";
			}
		}), is(true));
	}
	
	interface UnlockedExampleObject {
		@AutoPicklock
		InnerStatic createInnerStatic();
		boolean useInnerStatic(@AutoPicklock InnerStatic arg);
	}
	
	interface InnerStatic {
		String getState();
	}
}
