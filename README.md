The Narya library
=================

Narya provides a framework for building realtime distributed systems with an
emphasis on multiplayer online games (massive or otherwise). Its various
packages include:

* [presents]: A distributed object and remote procedure call framework for
  creating realtime distributed systems.
* [crowd]: A foundation for a multiplayer environment with notions of bodies
  and places, and support for chat and moving between places.
* admin: Useful administrative functionality including a mechanism for
  declaring configuration that can be viewed and modified at runtime by an
  administrative client.
* bureau: A framework for managing a network of code execution servers, which
  can be used to host ActionScript server-side code in addition to the Java
  server normally used by presents.

Tutorials, etc.
---------------

Three Rings hosts a site called [Game Gardens](http://www.gamegardens.com/)
where the Narya library is used to develop a wide array of multi-player online
games. On that site you can find [tutorials and sample
code](http://wiki.gamegardens.com/) relating to the use of Narya in game
development.

Building
--------

The library is built using [Ant](http://ant.apache.org/).

Invoke ant with any of the following targets:

    all: builds the distribution files and javadoc documentation
    compile: builds only the class files (dist/classes)
    javadoc: builds only the javadoc documentation (dist/docs)
    dist: builds the distribution jar files (dist/*.jar)

If you wish to build the ActionScript components, you will need to supply the
location of the Flex SDK on the command line:

    % ant -Dflexsdk.dir=SOMEPATH dist

Artifacts
---------

A Maven repository containing released versions of the Narya Java and
ActionScript artifacts are maintained here. To add a Narya dependency to a
Maven project, add the following to your `pom.xml`:

    <repositories>
      <repository>
        <id>narya-repo</id>
        <url>http://threerings.github.com/narya/maven</url>
      </repository>
    </repositories>
    <dependencies>
      <dependency>
        <groupId>com.threerings</groupId>
        <artifactId>narya</artifactId>
        <version>1.8</version>
      </dependency>
    </dependencies>

To add it to an Ivy, SBT, or other Maven repository using project, simply
remove the vast majority of the boilerplate above.

If you prefer to download pre-built binaries, those can be had here:

* [narya-1.8.jar](http://threerings.github.com/narya/maven/com/threerings/narya/1.8/narya-1.8.jar)
* [naryalib-1.8.swc](http://threerings.github.com/narya/maven/com/threerings/naryalib/1.8/naryalib-1.8.swc)

Related Libraries
-----------------

Narya is extended by the [Nenya](http://github.com/threerings/nenya) and
[Vilya](http://github.com/threerings/vilya) libraries which provide additional
game related functionality like 2D and 3D graphics support, animated character
rendering and a more sophisticated virtual world framework.

Distribution
------------

Narya is released under the LGPL. The most recent version of the library is
available at http://github.com/threerings/narya.

Contact
-------

Questions, comments, and other worldly endeavors can be handled via the [Three
Rings Libraries](http://groups.google.com/group/ooo-libs) Google Group.

Narya is actively developed by the scurvy dogs at
[Three Rings](http://www.threerings.net) Contributions are welcome.

[presents](http://threerings.github.com/narya/apidocs/com/threerings/presents/package-summary.html]
[crowd](http://threerings.github.com/narya/apidocs/com/threerings/crowd/package-summary.html]
