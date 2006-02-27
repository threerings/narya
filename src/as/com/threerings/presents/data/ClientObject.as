//
// $Id: ClientObject.java 3300 2005-01-08 22:05:00Z ray $
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

package com.threerings.presents.data {

import mx.utils.ObjectUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Every client in the system has an associated client object to which
 * only they subscribe. The client object can be used to deliver messages
 * solely to a particular client as well as to publish client-specific
 * data.
 */
public class ClientObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>receivers</code> field. */
    public static const RECEIVERS :String = "receivers";
    // AUTO-GENERATED: FIELDS END

    /** Used to publish all invocation service receivers registered on
     * this client. */
    public var receivers :DSet;

    /**
     * Returns a short string identifying this client.
     */
    public function who () :String
    {
        return "(" + getOid() + ")";
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(receivers);
    }
                            
    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        receivers = (ins.readObject() as DSet);
    }

    // AUTO-GENERATED: METHODS START
    public function setReceivers (value :DSet) :void
    {
        requestAttributeChange(RECEIVERS, value, this.receivers);
        this.receivers = (value == null) ? null
                                         : (ObjectUtil.copy(value) as DSet);
    }
    // AUTO-GENERATED: METHODS END
}
}
