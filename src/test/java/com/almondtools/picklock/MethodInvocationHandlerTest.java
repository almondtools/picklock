package com.almondtools.picklock;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class MethodInvocationHandlerTest  {

	@Test
	public void testNull() throws Throwable {
		assertThat(MethodInvocationHandler.NULL.invoke(new Object(), (Object) null), nullValue());
		assertThat(MethodInvocationHandler.NULL.invoke(new Object(), new Object[0]), nullValue());
		assertThat(MethodInvocationHandler.NULL.invoke(new Object(), new Object()), nullValue());
	}
	
}
