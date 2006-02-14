This document contains a couple of notes about some design decisions
and some notes about flash that you may find useful.


Design decisions
----------------
- I have kept accessors named like their Java counterparts, rather
  than embracing flash's property setter/getter methods (which are really
  cool), but I am starting to lean the other way and may rewrite some stuff.

- I am embracing flash's event distribution model because it saved me a bunch
  of work.



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

- constructors do not defaultly call super()- be sure to do it explicitely.
  Maybe we should get in the habit of doing it in Java for consistency and
  explicitness.
