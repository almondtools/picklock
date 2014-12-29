package com.almondarts.picklock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

@SuppressWarnings("unused")
public class StaticMethodInvokerTest {

	@Test
	public void testInvoke() throws Throwable {
		Object invoke = new StaticMethodInvoker(WithStaticMethod.class, WithStaticMethod.class.getDeclaredMethod("staticMethod", int.class)).invoke(new Object[] { 1 });
		assertThat((String) invoke, equalTo("1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvokeFailingSignature() throws Throwable {
		new StaticMethodInvoker(WithStaticMethod.class, WithStaticMethod.class.getDeclaredMethod("staticMethod", int.class)).invoke(new Object[] { "1" });
	}

	@Test(expected = IOException.class)
	public void testInvokeCheckedException() throws Throwable {
		new StaticMethodInvoker(WithStaticMethod.class, WithStaticMethod.class.getDeclaredMethod("staticException", int.class)).invoke(new Object[] { 2 });
	}

	@Test(expected = NullPointerException.class)
	public void testInvokeUncheckedException() throws Throwable {
		new StaticMethodInvoker(WithStaticMethod.class, WithStaticMethod.class.getDeclaredMethod("staticException", int.class)).invoke(new Object[] { 1 });
	}

	private static class WithStaticMethod {
		private static String staticMethod(int i) {
			return String.valueOf(i);
		}

		private static String staticException(int i) throws IOException {
			if (i == 1) {
				throw new NullPointerException();
			} else {
				throw new IOException();
			}
		}
	}
}
