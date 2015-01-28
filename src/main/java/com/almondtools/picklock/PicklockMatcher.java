package com.almondtools.picklock;

import static com.almondtools.picklock.SignatureUtil.methodSignature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeMatcher;

public class PicklockMatcher extends TypeSafeMatcher<Class<?>> {

	private Class<?> interfaceClazz;

	public PicklockMatcher(Class<?> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}

	public static PicklockMatcher providesFeaturesOf(Class<?> interfaceClazz) {
		return new PicklockMatcher(interfaceClazz);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(" can unlock features of ").appendValue(interfaceClazz.getSimpleName());
	}

	@Override
	protected void describeMismatchSafely(Class<?> item, Description mismatchDescription) {
		List<Method> conflicts = ObjectAccess.check(item).isUnlockable(interfaceClazz);
		if (!conflicts.isEmpty()) {
			mismatchDescription
				.appendText("cannot map following members in ")
				.appendValue(item.getSimpleName())
				.appendText(": ")
				.appendList("\n", "\n", "", describe(conflicts));
		}
	}

	private List<SelfDescribing> describe(List<Method> conflicts) {
		List<SelfDescribing> descriptions = new ArrayList<SelfDescribing>(conflicts.size());
		for (Method conflict : conflicts) {
			descriptions.add(new Signature(methodSignature(conflict.getName(), conflict.getReturnType(), conflict.getParameterTypes(), conflict.getExceptionTypes())));
		}
		return descriptions;
	}

	@Override
	protected boolean matchesSafely(Class<?> item) {
		return ObjectAccess.check(item).isUnlockable(interfaceClazz).isEmpty();
	}

	private final class Signature implements SelfDescribing {
		private final String signature;

		private Signature(String signature) {
			this.signature = signature;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(signature);
		}
	}

}
