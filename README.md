Picklock
========
Picklock (meaning a duplicate key) is about accessing private members of java objects and classes utilizing a strongly typed interface.
Picklock uses Java Reflection and Java Proxys to create an unlocked Facade of an Object or a Class.   

The Objectives of Picklock follow her:

Picklocking a sealed class
==========================
Consider following java class
```Java
public class House {

	private Key houseKey;
	private boolean locked;
	private List<Furniture> furniture;
	...

	public boolean open(Key key) {
		if (houseKey.equals(key)) {
			open();
		}
		return !locked;
	}

	private void open() {
		locked = false;
	}

	public List<Furniture> listFurniture() {
		if (locked) {
			throw new UnsupportedOperationException("cannot list furniture if house is locked");
		}
		return furniture;
	}

	...
}
```

Not knowing the house key will not give you access to the house and its furniture.

The classic way to access the house would:
```Java
    public List<Furniture> open(House house) throws Exception {
		Class<? extends House> houseClass = house.getClass(); // class lookup
		Method open = houseClass.getDeclaredMethod("open", new Class<?>[0]); // method lookup, type signature wrapping
		open.setAccessible(true); // access enabling
		open.invoke(house,new Object[0]); // non object oriented call, argument wrapping
		return listFurniture();
	}
```

The picklock style would be following
```Java
    public List<Furniture> open(House house) throws NoSuchMethodException {
		PicklockedHouse picklockedHouse = ObjectAccess.unlock(house).features(PicklockedHouse.class); // unlock interface
		picklockedHouse.open(); // call unlocked method
		return house.listFurniture();
	}
	
	interface PicklockedHouse {
		void open();
	}
```

Of course one may also unlock methods with arguments, simply repeat the signature of the private method you want to use in
the picklocked interface.

Regarding the picklock solution, there is
* No need to lookup the class of House
* No need to lookup the method open
* No need to wrap argument types and arguments
* No need to enable invocations
* No need to invocate functions in a non-object-oriented way

Snooping Private State
======================
...

Changing Private State
======================
...

Instantiating Singletons
========================
...

Random Access Reflection is bad
===============================

There are several reasons why one should avoid classical random access reflection in java, e.g.
* reflection is not performant
* reflection is cumbersome (several string lookups, security disabling)
* reflection using Strings is not robust to renamings
* reflection is an indicator of bad software design
* reflection may cause security leaks (if the input string is unvalidated user input, e.g. a url parameter)
* reflection may cause problems with the type system

Controlled Private Access Reflection via Picklock is preferrable
================================================================

Picklock enables you to use reflection without may of the common problems of reflection
* picklock is convenient to use (2 steps: unlock interface, call method)
* picklock uses method invocation, no Strings at All (mapping of methods is done by conventions)
* picklock uses a kind of Adaptor/Decorator design pattern, which is the object oriented way of exposing a new feature
* picklock prevents security issues because it does not depend on Strings
* picklock is independent from the concrete type of the accessed object


Maintaining and Tracking Picklocked Objects
===========================================
A main Problem of Java Reflection is, that it fails at runtime, not at compile time and not at build time. This makes reflection unpredictable and risky. 

Picklock attempts will probably never fail at compile time, but some coding discipline is sufficient, to let picklock fail at build time (i.e. at test time). What is to be done:
* determine any pair of types (locked class, unlocked facade class)
* write a test containing only "ObjectAccess.check(house.getClass()).isUnlockable(PicklockedHouse.class);"
* this test would always fail, if the internals used by Picklock do not exist any more (e.g. because the foreign implementation changed)


Now why should we use this?
===========================
Critics may mention that Picklock lets you work around bad code. But we all know that it is better to write code in a good wayrather than finding methods to work around it. **Yes and No!**

I see some scenarios of application:

1. Sometimes we have to come up with foreign code that we cannot easily change. Perhaps it was not designed for TDD or dependency injection. In this case we have to access private members, and we can do this with picklock. The alternative would be to patch the code with getters and setters, which is not very rewarding.

2. Worse: We have to come up with legacy code (meaning code, that is not maintained any more). Legacy code has one positive property. Internals are usually not changed often (because that could break the whole system). So if the private members of a legacy code object is not reachable, we can make it reachable with picklock.

3. Better: In some cases we want to publish an API, which is well designed. If the developer follows the "Law of Demeter" and "Tell don't Ask" he might have been very restrictive in using getter and setter methods. However if he is a good developer he writes tested code and testing could be a problem if the private state is completely hidden. Now picklock enables him to access the private State in unit tests but prevents other users from depending on inner state of API objects.
