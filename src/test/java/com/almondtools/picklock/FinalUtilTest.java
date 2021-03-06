package com.almondtools.picklock;

import static com.almondtools.picklock.FinalUtil.ensureNonFinal;
import static com.almondtools.picklock.FinalUtil.isFinal;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;


public class FinalUtilTest {

	@Test
	public void testEnsureNonFinal() throws Exception {
		Field field = WithFinal.class.getDeclaredField("field");
		ensureNonFinal(field);
		assertThat(isFinal(field), is(false));
	}
	
	private static class WithFinal {
		@SuppressWarnings("unused")
		private final String field = "";
	}

}
