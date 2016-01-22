Picklock works with Java 7 (maybe even with Java 6), Picklock's younger sister (same subject, other api) [XrayInterface](http://almondtools.github.io/xrayinterface/) requires Java 8.


Picklock
========

Picklock enables you to access private static and non-static members (fields or methods) of classes without using the java reflection api.

The basic idea of Picklock is that any member we want to use is missing in the public interface of the class to access.
So our first step towards getting, setting or invoking a member of a given object is to define the interface we want to access:

    interface Example {

      void setValue(String value);        // you expect a setter for the 'private String value'
      String getValue();                  // you expect a getter for the 'private String value'
      int callExample(char[] characters); // you expect a public method instead of 'private int callExample(char[])'
      
    }
    
In the next step we connect this interface with the object of interest:

    Example example = ObjectAccess.unlock(object).features(Example.class);

The we may call any interface on the retrieved object, allowing us to set, get or invoke the members we want to, e.g.

    example.setValue("hello world");
    
    assert "hello world".equals(example.getValue());


Picklock uses the java reflection api, but it does not bother one with:

1. lookup of the class of the object to access

    ```Java
    Class<?> clazz = object.getClass();
    ```
2. wrapping of param types into arrays

    ```Java
    Class<?>[] paramTypes = new Class<?>[]{char[].class};
    ```
3. lookup of the member to access
    
    ```Java
    Method m = clazz.getDeclaredMethod("callExample", args);
    ```
4. enabling access for private methods/fields
    
    ```Java
    m.setAccessible(true);
    ```
5. wrapping arguments into arrays
    
    ```Java
    Object[] args = new Object[]{"chars".toCharArray()};
    ```
6. indirectly setting, getting, invoking members
    
    ```Java
    Object result = m.invoke(object, args);
    ```
7. converting the result type 
    
    ```Java
    int intResult = ((Integer) result).intValue();
    ```

Looks interesting? Then have a look at some examples ... 

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

Adhoc Matchers
==============
If you are strongly familiar with unit testing you probably faced the problem that you got a result object with a complex hidden inner state (i.e. many
variables without accessors). Sometimes one can test the inner state by calling other methods (relying on the inner state), but often this makes testing
even more complicated. So how can Picklock help you testing such objects. Look at this test for the already known class `House`:

```Java
    @Test
	public void testMatchingHouses() throws Exception {
		assertThat(house, IsEquivalent.equivalentTo(PicklockingMatcher.class)
			.withHouseKey(key)
			.withLocked(true)
			.withFurniture(hasSize(1)));
	}
	
	interface PicklockingMatcher extends Matcher<House> {

		PicklockingMatcher withHouseKey(Key key);
		PicklockingMatcher withLocked(boolean locked);
		PicklockingMatcher withFurniture(Matcher<Collection<? extends Object>> furniture);
	}
```

As you can see:

- define a `Matcher` for the object of interest
- give it some Builder methods (similar to setter methods but the return type is your matcher and it is not `set` but `with`)
- a builder methods parameter 
  - may be the expected value of the assigned property
  - or a value matcher that should be applied to the assigned property

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
	<version>0.2.7</version>
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
