package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;


@SuppressWarnings("unused")
public class ConstructorInvokerTest {

	@Test
	public void testInvokeWithoutProblems() throws Throwable {
		Object result = new ConstructorInvoker(WithConstructor.class.getDeclaredConstructor()).invoke(new Object[0]);
		assertThat(result, instanceOf(WithConstructor.class));
	}
	
	@Test
	public void testInvokeWithImplicitConstructor() throws Throwable {
		Object resultOnClass = new ConstructorInvoker(WithImplicitConstructor.class).invoke(new Object[0]);
		assertThat(resultOnClass, instanceOf(WithImplicitConstructor.class));

		Object resultOnConstructor = new ConstructorInvoker(WithImplicitConstructor.class.getDeclaredConstructor()).invoke(new Object[0]);
		assertThat(resultOnConstructor, instanceOf(WithImplicitConstructor.class));
	}
	
	@Test(expected=NullPointerException.class)
	public void testInvokeWithNPEConstructor() throws Throwable {
		Object result = new ConstructorInvoker(WithConstructor.class.getDeclaredConstructor(boolean.class)).invoke(new Object[]{Boolean.FALSE});
	}
	
	@Test(expected=IOException.class)
	public void testInvokeWithAIOBConstructor() throws Throwable {
		Object result = new ConstructorInvoker(WithConstructor.class.getDeclaredConstructor(boolean.class)).invoke(new Object[]{Boolean.TRUE});
	}
	
	private static class WithConstructor {
		
		public WithConstructor() {
		}

		public WithConstructor(boolean checked) throws IOException {
			if (checked) {
				throw new IOException();
			} else {
				throw new NullPointerException();
			}
		}
	}

	private static class WithImplicitConstructor {
	}

}
