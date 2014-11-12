package com.almondarts.picklock.examples.house;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.almondarts.picklock.ObjectAccess;


public class HouseTest {

	private Key key;
	private House house;

	@Before
	public void before() {
		key = new Key();
		house = new House(key);
		house.add(new Safe());
		house.lock(key);
	}
	
	@Test
	public void testHouseOwner() throws Exception {
		boolean open = house.open(key);
		assertThat(open, is(true));
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture, contains(instanceOf(Safe.class)));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testBrute() throws Exception {
		house.listFurniture();
	}

	@Test
	public void testStranger() throws Exception {
		boolean open = house.open(new Key());
		assertThat(open, is(false));
	}

	@Test
	public void testLockpicker() throws Exception {
		Picklocked picklockableHouse = ObjectAccess.unlock(house).features(Picklocked.class);
		picklockableHouse.open();
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture, contains(instanceOf(Safe.class)));
	}
	
	interface Picklocked {
		void open();
	}
}
