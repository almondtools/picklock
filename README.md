Picklock
========
Picklock (meaning a duplicate key) is about accessing private members of java objects and classes utilizing a strongly typed interface.
Picklock uses Java Reflection and Java Proxys to create an unlocked Facade of an Object or a Class.   

The Objectives of Picklock follow here:

Picklocking a sealed class
==========================
For the following examples consider following class
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

Executing hidden behaviour
==========================
Not knowing the house key will not give you access to the house and its furniture. Yet we want to call the open method.

The classic way to access the house would be reflection:
```Java
    public List<Furniture> open(House house) throws Exception {
		Class<? extends House> houseClass = house.getClass(); // class lookup
		Method open = houseClass.getDeclaredMethod("open", new Class<?>[0]); // method lookup, type signature wrapping
		open.setAccessible(true); // access enabling
		open.invoke(house,new Object[0]); // non object oriented call, argument wrapping
		return house.listFurniture();
	}
```

The picklock style for example:
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

Snooping Private State
======================
Assume that we do not want to call the open method, but we want to know the house key we have to pass to the open method.

For example:
```Java
    public List<Furniture> open(House house) throws NoSuchMethodException {
		PicklockedKey picklockableHouse = ObjectAccess.unlock(house).features(PicklockedKey.class);
		Key key = picklockableHouse.getHouseKey(); // aquire the private key
		house.open(key); // execute the public method
		return house.listFurniture();
	}
	
	interface PicklockedKey {
		Key getHouseKey();
	}
```

Changing Private State
======================
Assume that we do not want to spy out the correct key, but we want to change the key/lock to a more convenient behaviour.

For example:
```Java
    public List<Furniture> open(House house) throws NoSuchMethodException {
    	Key key = new Key();
		PicklockedLock picklockableHouse = ObjectAccess.unlock(house).features(PicklockedLock.class);
		picklockableHouse.setHouseKey(key);
		house.open(key);
		return house.listFurniture();
	}
	
	interface PicklockedLock {
		void setHouseKey(Key key);
	}
```


Handling Singletons
===================
Some frameworks make heavy use of singletons. The singleton pattern has a small unconvenience considering testability: 
classes using a singleton almost always contain a fixed dependency that is not testable.

Picklock enables us to bypass the design decisions temporarily, when needed.

We will use following singleton for the following examples:

```Java
public final class TheOneAndOnly {

	private static TheOneAndOnly instance;
	
	private boolean unique;
	
	private TheOneAndOnly()  {
		unique = true;
	}
	
	public boolean isUnique() {
		return unique;
	}
	
	public static TheOneAndOnly getInstance() {
		if (instance == null) {
			instance = new TheOneAndOnly();
		}
		return instance;
	}
	
} 
```

Obviously we cannot make the method isUnique return false without hacking. So lets go ahead

Changing the Singleton Instance directly
========================================
We can of course directly modify the singleton with picklock, e.g.

```Java
	@Test
	public void testDirectSingletonModification() throws Exception {
		TheOneAndOnly instance = TheOneAndOnly.getInstance();
		ObjectAccess.unlock(instance).features(Picklocked.class).setUnique(false);
		assertThat(instance.isUnique(), is(false));
	}
	
	interface Picklocked {
		void setUnique(boolean unique);
	}
```

A true singleton would do exactly what we want.

Intruding into the Singleton Factory
====================================
Another way would be to access and change the static property instance.

```Java
	@Test
	public void testSingletonSingletonFactoryIntrusion() throws Exception {
		TheOneAndOnly instance = ClassAccess.unlock(TheOneAndOnly.class).features(PicklockedStatic.class).getInstance();
		ObjectAccess.unlock(instance).features(Picklocked.class).setUnique(false);
		assertThat(TheOneAndOnly.getInstance().isUnique(), is(false));
	}
	
	interface Picklocked {
		void setUnique(boolean unique);
	}
	
	interface PicklockedStatic {
		TheOneAndOnly getInstance();
	}
```

To provide static properties, we have to use the class ClassAccess instead of ObjectAccess. The conventions for the interface are similar
to the interfaces for ObjectAccess. Note that the interface has itself non-static methods, the accessed methods/properties are static.  

Singleton Injection
===================
Now there is a third possibility - inject the correctly configured singleton. This could be very helpful when the singleton class is very complex
and we want to use a mock instead of a modified singleton.

```Java
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
	
	interface PicklockedStaticWithConstructor {
		TheOneAndOnly create();
		void setInstance(TheOneAndOnly instance);
	}
```
  
As one can see we use the ClassAccess object to invoke the private constructor. Any interface method named create delegates to a constructor with a
matching signature.

Picklock's Benefits
===================
Regarding the picklock solutions, there is
* No need to lookup the class of the object to access
* No need to lookup the method or properties by string names
* No need to wrap argument types and arguments
* No need to enable invocations of private methods
* No need to invocate functions in a non-object-oriented way

Why and when should we prefer picklock reflection over java reflection?
=======================================================================

Java reflection
* is not performant
* is cumbersome (several string lookups, access enabling)
* is string based (and not robust at member renamings)
* may cause security leaks (if the input string is unvalidated user input, e.g. a url parameter)
* could in most applications be replaced by appropriate design patterns
 
Picklock covers only one aspect of reflection => access to private but statically known members. Picklock 
* is convenient to use (2 steps: unlock interface, call method/property)
* is not string based, one basically only needs the object to access and a type definition of the interface you want use for access 
* uses a kind of adaptor/decorator design pattern, which is the object oriented way of exposing a new feature
* prevents some security issues (user input cannot control reflective interface, because picklock does not depend on strings) 


Maintaining and Tracking Picklocked Objects
===========================================
Can we view Picklock as a statically typed interface to Reflection? Not really! Although we operate on pure java interfaces, these interfaces are dynamically
mapped to the target objects. Renaming of class members could break this mapping.

But contrary to reflection we could easily assert that our picklock assumptions on a certain object hold. Just write a test:
* determine any pair of types (locked class, unlocked facade class)
* write a test containing only "ObjectAccess.check(house.getClass()).isUnlockable(PicklockedHouse.class);"
* if picklocking a static interface the test would look like "ClassAccess.check(TheOneAndOnly.class).isUnlockable(PicklockedStatic.class);"
* this test would always fail, if the internals used by Picklock do not exist any more (e.g. because the member names changed)

Such a test is nearly as robust as static compile time checking. 


Now why should we use this?
===========================
I see some scenarios of application:

1. Sometimes we have to come up with foreign code that we cannot easily change. Perhaps it was not designed for TDD or dependency injection. In this case we have to access private members, and we can do this with picklock. The alternative would be to patch the code with getters and setters, which is not very rewarding.

2. Worse: We have to come up with legacy code (meaning code, that is not maintained any more). Legacy code has one positive property. Internals are usually not changed often (because that could break the whole system). So if the private members of a legacy code object is not reachable, we can make it reachable with picklock.

3. Better: In some cases we want to publish an API, which is well designed. If the developer follows the "Law of Demeter" and "Tell don't Ask" he might have been very restrictive in using getter and setter methods. However if he is a good developer he writes tested code and testing could be a problem if the private state is completely hidden. Now picklock enables him to access the private State in unit tests but prevents other users from depending on inner state of API objects.
