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

- 'protected' doesn't mean the same thing in ActionScript: it means that
  ONLY subclasses can access a field/method, other classes in the same
  package cannot access it.

Java            Class Package Subclass World
  private       Y     N       N        N
  <default>     Y     Y       N        N
  protected     Y     Y       Y        N
  public        Y     Y       Y        Y

Actionscript  
  private       Y     N       N        N
  protected     Y     Y       N        N
  internal      Y     Y       Y        N  (but not a subclass in a diff pkg!)
  public        Y     Y       Y        Y

  SO: if you want a method to be accessable to a subclass in a different
  package, it must be public!

- Beware of integer math:

  var i :int = 3;
  var o :Object = someArray[i / 2];
  // o is now undefined, because we accessed array element "1.5".
  // I think arrays are just hashes, so probably you could store
  // values at element 1.5 if you desired...

- Similarly, methods in String take Number arguments (wha?) when for character
  index positions. Totally nonsensical.

- Hey! Array has two constructors! How can I do that?
