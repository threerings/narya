//
// $Id: Streamable.java,v 1.1 2002/07/23 05:42:34 mdb Exp $

package com.threerings.io;

/**
 * Marks an object as streamable, meaning that it can be written to {@link
 * ObjectOutputStream} instances and read from {@link ObjectInputStream}
 * instances.
 *
 * <p> All non-<code>transient</code> public fields will be automatically
 * written and restored for a {@link Streamable} instance. Classes that
 * wish to stream non-public fields or customize the streaming process
 * should implement methods with the following signatures:
 *
 * <p><code>
 * public void writeObject ({@link ObjectOutputStream} out);
 * public void readObject ({@link ObjectInputStream} in);
 * </code>
 *
 * <p> They can then handle the entirety of the streaming process, or call
 * {@link ObjectOutputStream#defaultWriteObject} and {@link
 * ObjectInputStream#defaultReadObject} from within their
 * <code>writeObject</code> and <code>readObject</code> methods to perform
 * the standard streaming in addition to their customized behavior.
 */
public interface Streamable
{
}
