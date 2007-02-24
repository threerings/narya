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

import com.threerings.util.StringBuilder;
import com.threerings.util.Wrapped;

/**
 * An attribute changed event is dispatched when a single attribute of a
 * distributed object has changed. It can also be constructed to request
 * an attribute change on an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class AttributeChangedEvent extends NamedEvent
{
    /**
     * Returns the new value of the attribute.
     */
    public function getValue () :Object
    {
        return _value;
    }

    /**
     * Returns the value of the attribute prior to the application of this
     * event.
     */
    public function getOldValue () :Object
    {
        return _oldValue;
    }

    /**
     * Applies this attribute change to the object.
     */
    override public function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        // if we have no old value, that means we're not running on the
        // master server and we have not already applied this attribute
        // change to the object, so we must grab the previous value and
        // actually apply the attribute change
        if (_oldValue === UNSET_OLD_ENTRY) {
            _oldValue = target[_name]; // fucking wack-ass language
        }

        target[_name] = _value;
        return true;
    }

    /**
     * Constructs a new attribute changed event on the specified target
     * object with the supplied attribute name and value. <em>Do not
     * construct these objects by hand.</em> Use {@link
     * DObject#changeAttribute} instead.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param name the name of the attribute (data member) that has
     * changed.
     * @param value the new value of the attribute (in the case of
     * primitive types, the reflection-defined object-alternative is
     * used).
     */
    public function AttributeChangedEvent (
            targetOid :int = 0, name :String = null, value :Object = null,
            oldValue :Object = null)
    {
        super(targetOid, name);
        _value = value;
        _oldValue = oldValue;
    }

    // documentation inherited
    override protected function notifyListener (listener :Object) :void
    {
        if (listener is AttributeChangeListener) {
            (listener as AttributeChangeListener).attributeChanged(this);
        }
    }

    // documentation inherited
    override protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("CHANGE:");
        super.toStringBuf(buf);
        buf.append(", value=", _value);
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_value);
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _value = ins.readObject();

        // possibly convert the value
        if (_value is Wrapped) {
            _value = (_value as Wrapped).unwrap();
        }
    }

    protected var _value :Object;
    protected var _oldValue :Object = UNSET_OLD_ENTRY;
}
}
