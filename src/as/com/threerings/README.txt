This document contains a couple of notes about some design decisions
and some notes about flash that you may find useful.


Design decisions
----------------
- I have kept accessors named like their Java counterparts, rather
  than embracing flash's property setter/getter methods (which are really
  cool), but I am starting to lean the other way and may rewrite some stuff.

- I am embracing flash's event distribution model because it saved me a bunch
  of work.

- We could use the setter methods on DObject properties to generate dobj
  events, but so far I haven't gone there.

- We need a realistic HashMap implementation. Using Object properties
  (a-la my SimpleMap) is not going to cut it because keys must always
  be Strings and there's no way to *really* remove a value from an Object
  (you can set the property to null, but now the property is forever defined:
  the key is not cleared)

  mx.utils.UIDUtil.getUID() can be used to generate a (huge) unique String
  for any object for use as a key or something.

  It might be worth waiting, I think it's very probable that Adobe will
  add in a Hashtable class to the standard libraries...

- Since we cannot do streaming via reflection like we do in Java, each
  Streamable class needs to define its own readObject/writeObject methods.
  At one point we thought that maybe we could just write the class
  and have a script examine the class definition and automatically generate
  those two methods, but I don't think that's going to save us much.
  Variables cannot be marked as transient, and we often have to change the
  type locally: actionscript has Number and int which correspond to
  float/double and int/short/byte when we stream to the server. So we'd have
  to do a bunch of crazy comment annotations on each variable to be streamed
  in any class and at that point we may as well just write the streamable
  methods, IMO.


Notes
-----
- In actionscript, 'package' is simply a block command to sweep whatever is
  defined inside the block so that it's in that package. This means
  that in addition to classes being in a package, freestanding functions
  and I believe variables and constants can be in a package.

  We are not putting freestanding functions anywhere. Make a util class with
  static methods.

- ActionScript does not have inner classes. Only one public class may be
  defined in a file, and the filename must match the public class.
  However, protected classes cannot be defined within the package block!
  So it seems like the model is:

  package com.foo {
      public class FooBar {
          // stuff
      }
  } // end: package foo

  class HelperClass {
      // helper stuff
  }


  To me, this makes it seem as if the helper class is now globally scoped,
  which of course is the exact opposite of what is desired. This may
  not be the case, I haven't played with it much yet.

  What especially sucks is that any imports must be repeated down below
  for the helper class, including importing the class defined just above.
  Again, it's unclear to me whether those imports are now globally scoped
  and will spill over onto other files... What a giant pain.

  ***Update: it turns out that the primary class in a file may be declared
  with internal accessibility. So HelperClass could live in its own file
  and access 'internal' methods on the main class. That is probably
  preferable to having them in the same file but having to re-import anyway
  and accessing only public properties of the main class from the helper.


- Similarly, I'm unclear about sandboxes. If a user-created .swf is playing
  inside ours, I don't know if it can interact with our classes, and if so,
  what happens if it proceeds to define a class like
  com.threerings.presents.client.Client?

- <strike>constructors do not defaultly call super()- be sure to do it explicitely.
  Maybe we should get in the habit of doing it in Java for consistency and
  explicitness.</strike>
  CORRECTION: super() is called implicitely, just as in Java.

- It's annoying how there can be only one constructor: if you have classA
  that has a 1-arg constructor and it is extended by classB, then the implicit
  super() is inserted, but this results in runtime error because the classA
  constructor is not being passed an arg. You'd think this would be caught
  at compile time...

- I'm a little shaky still about how I'm going to handle arrays. In
  ActionScript all arrays (except ByteArray) are the same type: Array.
  If class A is extended by B and C, the server could pass around an array of
  A, filled with B and C elements. I would have no way on the client to
  inspect an array like that and know to tell the server that it's an A[].
  Punting completely on Arrays for now.

- The RENDER Event is dispatched prior to each rendering, it's
  basically like tick(): it gives anything that cares a chance to update    
  prior to being painted. It doesn't specify what the hell to listen on for
  this event, but since all DisplayObjects are event dispatchers then
  listening on any display object (including the stage) should work...

  But, the damn thing doesn't get dispatched if there will be no render,    
  even if the code is still running- like when the flash player window is   
  minimized or obscured. Lovely.

  I will play around with trying to just use a Timer with a 1ms interval,
  and see if the frequency is limited to the actual framerate.

- All methods must be marked with the 'override' keyword if they override
  a method in their parent, except for toString(), even though it's defined
  for Object. Ya got me...

- 'protected' means something slightly different from java: other classes
   in the same package cannot access protected members, only subclasses may:

Java            Class Package Subclass World
  private       Y     N       N        N
  <default>     Y     Y       N        N
  protected     Y     Y       Y        N
  public        Y     Y       Y        Y

ActionScript  
  private       Y     N       N        N
  internal      Y     Y       N        N
  protected     Y     N       Y        N
  public        Y     Y       Y        Y


- Beware of non-existant integer math:

  var i :int = 3;
  var o :Object = someArray[i / 2];
  // o is now undefined, because we accessed array element "1.5".
  // I think arrays are just hashes, so probably you could store
  // values at element 1.5 if you desired...

- Similarly, methods in String take Number arguments (wha?) when for character
  index positions. Totally nonsensical.

- Hey! Array has two constructors! How can I do that?
  - Probably they have one constructor with varargs, and it simply checks
  to see if there is only 1 arg and if it's an int, and then does something
  different. Although, we can't really be sure, because these classes are
  magic and special and don't have a corresponding .as file we can check out.

- I've been casting using 'as':
     var s :String = (someObject as String);

  But I've learned that there's another way that didn't seem to be listed
  anywhere in the language reference but is more like what we'll want:
     var s :String = String(someObject);

  The difference is that the first one tries to coerce the value to be
  of the specified type, and if it fails returns null. The second is
  more like a cast in Java, in that if it fails it generates an Error at
  runtime.

  Note that if the types are coercable, each one will succeed in the same way:
     var o :Object = 2.5; // create a Number object
     var x :int = (o as int);
     var y :int = int(o);
     // both of these work and turn the Number 2.5 into int 2.

  Perhaps we'll want a util method that always generates an error if the
  object's type is not identical or a subclass of the casted-to type.


- Pitfall! This is perfectly legal:
     var b :int = 3;
     var b :int = 4;

  This will generate a compile warning:
     var b :int = 3;
     var b :String = "three";
  It generates the warning on assigning 3 to b, because it has looked
  into the future and decided that b is a String, even though it's an
  int on that line.

  And:

  var b :int = 3;
  for (var ii:int = 0; ii < b; ii++) {
      var b :Number = "3.3";
  }
  trace(b); // prints "3.3", even though we've left the loop
