package com.almondarts.picklock.examples.singleton;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondarts.picklock.ClassAccess;
import com.almondarts.picklock.ObjectAccess;


public class TheOneAndOnlyTest {

	@Test
	public void testDirectSingletonModification() throws Exception {
		TheOneAndOnly instance = TheOneAndOnly.getInstance();
		ObjectAccess.unlock(instance).features(Picklocked.class).setUnique(false);
		assertThat(instance.isUnique(), is(false));
	}
	
	@Test
	public void testSingletonFactoryIntrusion() throws Exception {
		TheOneAndOnly instance = ClassAccess.unlock(TheOneAndOnly.class).features(PicklockedStatic.class).getInstance();
		ObjectAccess.unlock(instance).features(Picklocked.class).setUnique(false);
		assertThat(TheOneAndOnly.getInstance().isUnique(), is(false));
	}
	
	@Test
	public void testSingletonInjection() throws Exception {
		PicklockedStaticWithConstructor picklockedOneAndOnly = ClassAccess.unlock(TheOneAndOnly.class).features(PicklockedStaticWithConstructor.class);
		TheOneAndOnly instance = picklockedOneAndOnly.create();
		ObjectAccess.unlock(instance).features(Picklocked.class).setUnique(false);
		picklockedOneAndOnly.setInstance(instance);
		assertThat(TheOneAndOnly.getInstance().isUnique(), is(false));
	}
	
	interface Picklocked {
		void setUnique(boolean unique);
	}

	interface PicklockedStatic {
		TheOneAndOnly getInstance();
	}

	interface PicklockedStaticWithConstructor {
		TheOneAndOnly create();
		void setInstance(TheOneAndOnly instance);
	}

}
