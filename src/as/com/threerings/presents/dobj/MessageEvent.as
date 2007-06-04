//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.dobj {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.util.StringBuilder;
import com.threerings.util.Wrapped;

/**
 * A message event is used to dispatch a message to all subscribers of a
 * distributed object without actually changing any of the fields of the
 * object. A message has a name, by which different subscribers of the
 * same object can distinguish their different messages, and an array of
 * arguments by which any contents of the message can be delivered.
 *
 * @see DObjectManager#postEvent
 */
public class MessageEvent extends NamedEvent
{
    /**
     * Constructs a new message event on the specified target object with
     * the supplied name and arguments.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param name the name of the message event.
     * @param args the arguments for this message. This array should
     * contain only values of valid distributed object types.
     */
    public function MessageEvent (
            targetOid :int = 0, name :String = null, args :Array = null)
    {
        super(targetOid, name);

        // only init these values if they were specified
        if (arguments.length > 0) {
            _args = args;
        }
    }

    /**
     * Returns the arguments to this message.
     */
    public function getArgs () :Array
    {
        return _args;
    }

    /**
     * Replaces the arguments associated with this message event.
     * <em>Note:</em> this should only be called on events that have not
     * yet been dispatched into the distributed object system.
     */
    public function setArgs (args :Array) :void
    {
        _args = args;
    }

    /**
     * Applies this attribute change to the object.
     */
    override public function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        // nothing to do here
        return true;
    }

    // documentation inherited
    override protected function notifyListener (listener :Object) :void
    {
        if (listener is MessageListener) {
            listener.messageReceived(this);
        }
    }

    // documentation inherited
    override protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("MSG:");
        super.toStringBuf(buf);
        buf.append(", args=", _args);
    }

    // documentation inherited
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeField(_args);
    }

    // documentation inherited
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _args = (ins.readField(Array) as Array);

        if (_args != null) {
            for (var ii :int = _args.length - 1; ii >= 0; ii--) {
                if (_args[ii] is Wrapped) {
                    _args[ii] = (_args[ii] as Wrapped).unwrap();
                }
            }
        }
    }

    protected var _args :Array;
}
}
