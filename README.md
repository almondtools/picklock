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
Can we view Picklock as a statically typed interface to Reflection? In Theory we would use picklock with a statically known features.
In this case only the compiler limits us from ruling out broken reflection mappings at compile time.

But it is rather easy to be safe from broken reflection mappings. Just write a test for each pair of closed classes and feature interfaces, e.g.
for the upper example:
  
```Java
import static com.almondtools.picklock.PicklockMatcher.providesFeaturesOf;

...
	@Test
	public void testPreventRuntimeErrorsOnPicklocking() throws Exception {
		assertThat(House.class, providesFeaturesOf(Picklocked.class));
		assertThat(House.class, providesFeaturesOf(PicklockedKey.class));
		assertThat(House.class, providesFeaturesOf(PicklockedLock.class));
	}
...
```	


Using Picklock
==============

Maven Dependency
----------------

```xml
<dependency>
	<groupId>com.github.almondtools</groupId>
	<artifactId>picklock</artifactId>
	<version>0.2.2</version>
</dependency>
```

Bugs and Issues
---------------
If you find a bug or some other inconvenience with picklock:
- Open an Issue
- If possible provide a code example which reproduces the problem
- Optional: Provide a pull request which fixes (or works around) the problem

If you miss a feature:
- Open an Issue describing the missing feature

If you find bad or misleading english in the documentation:
- Tell me
