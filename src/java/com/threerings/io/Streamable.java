//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
