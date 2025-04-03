//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.io;

/**
 * Marks an object as streamable, meaning that it can be written to {@link ObjectOutputStream}
 * instances and read from {@link ObjectInputStream} instances.
 *
 * <p> All non-{@code transient}, non-{@link NotStreamable} fields will be automatically written
 * and restored for a {@link Streamable} instance. Classes that wish to stream transient fields or
 * customize the streaming process should implement methods with the following signatures: </p>
 *
 * <p><code>
 * public void writeObject ({@link ObjectOutputStream} out);
 * public void readObject ({@link ObjectInputStream} in);
 * </code></p>
 *
 * <p> They can then handle the entirety of the streaming process, or call {@link
 * ObjectOutputStream#defaultWriteObject} and {@link ObjectInputStream#defaultReadObject} from
 * within their {@code writeObject} and {@code readObject} methods to perform the standard
 * streaming in addition to their customized behavior.</p>
 *
 * <p>Streamable classes must <em>either</em> have a zero-argument constructor, in which case any
 * number of other constructors are allowed, but the zero-argument constructor will be used when
 * unserializing an instance; or they must have exactly one non-zero-argument constructor, and that
 * constructor will be called when unserializing an instance with "zero/null" values (meaning all
 * primitive types will be passed the appropriate zero value, and all reference types will be
 * passed null. This latter approach can even be used by classes with final fields, as the
 * zero/null values will be overwritten during unstreaming.</p>
 */
public interface Streamable
{
    /**
     * A marker interface for streamable classes that expect to be extended anonymously, but for
     * which the implicit outer class reference can (and should) be ignored. This allows one to
     * package up units of code and ship them between peers, or even between client and server.
     */
    public interface Closure extends Streamable {}
}
