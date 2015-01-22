package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

@SuppressWarnings("unused")
public class MethodInvokerTest {

	@Test
	public void testInvoke() throws Throwable {
		WithMethod object = new WithMethod();
		Object invoke = new MethodInvoker(WithMethod.class.getDeclaredMethod("staticMethod", int.class)).invoke(object, new Object[] { 1 });
		assertThat((String) invoke, equalTo("1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvokeFailingSignature() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(WithMethod.class.getDeclaredMethod("staticMethod", int.class)).invoke(object, new Object[] { "1" });
	}

	@Test(expected = IOException.class)
	public void testInvokeCheckedException() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(WithMethod.class.getDeclaredMethod("staticException", int.class)).invoke(object, new Object[] { 2 });
	}

	@Test(expected = NullPointerException.class)
	public void testInvokeUncheckedException() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(WithMethod.class.getDeclaredMethod("staticException", int.class)).invoke(object, new Object[] { 1 });
	}

	private static class WithMethod {
		private String staticMethod(int i) {
			return String.valueOf(i);
		}

		private String staticException(int i) throws IOException {
			if (i == 1) {
				throw new NullPointerException();
			} else {
				throw new IOException();
			}
		}
	}
}
