//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.bureau.data {

import com.threerings.io.ObjectInputStream;

import com.threerings.presents.dobj.DObject;

public class AgentObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>bureauId</code> field. */
    public static const BUREAU_ID :String = "bureauId";

    /** The field name of the <code>bureauType</code> field. */
    public static const BUREAU_TYPE :String = "bureauType";

    /** The field name of the <code>code</code> field. */
    public static const CODE :String = "code";

    /** The field name of the <code>className</code> field. */
    public static const CLASS_NAME :String = "className";

    /** The field name of the <code>clientOid</code> field. */
    public static const CLIENT_OID :String = "clientOid";
    // AUTO-GENERATED: FIELDS END

    /** The id of the bureau the agent is running in. This is normally a unique id corresponding
     *  to the game or item that requires some server-side processing. */
    public var bureauId :String;

    /** The type of bureau that the agent is running in. This is normally derived from the kind
     *  of media that the game or item has specified for its code and determines the method of
     *  launching the bureau when the first agent is requested. */
    public var bureauType :String;

    /** The location of the code for the agent. This could be a URL to an action script file or
     *  some other description that the bureau can use to load and execute the agent's code. */
    public var code :String;

    /** The main class within the code to use when launching an agent. Whther this value is 
     *  used depends on the type of bureau and will be resolve in the bureau client. */
    public var className :String;

    /** The id of the client running this agent (only set after the agent is assigned to a 
     *  bureau and is running). */
    public var clientOid :int;

    // AUTO-GENERATED: METHODS START
//     /**
//      * Requests that the <code>bureauId</code> field be set to the
//      * specified value. The local value will be updated immediately and an
//      * event will be propagated through the system to notify all listeners
//      * that the attribute did change. Proxied copies of this object (on
//      * clients) will apply the value change when they received the
//      * attribute changed notification.
//      */
//     public function setBureauId (value :String) :void
//     {
//         var ovalue :String = this.bureauId;
//         requestAttributeChange(
//             BUREAU_ID, value, ovalue);
//         this.bureauId = value;
//     }

//     /**
//      * Requests that the <code>bureauType</code> field be set to the
//      * specified value. The local value will be updated immediately and an
//      * event will be propagated through the system to notify all listeners
//      * that the attribute did change. Proxied copies of this object (on
//      * clients) will apply the value change when they received the
//      * attribute changed notification.
//      */
//     public function setBureauType (value :String) :void
//     {
//         var ovalue :String = this.bureauType;
//         requestAttributeChange(
//             BUREAU_TYPE, value, ovalue);
//         this.bureauType = value;
//     }

//     /**
//      * Requests that the <code>code</code> field be set to the
//      * specified value. The local value will be updated immediately and an
//      * event will be propagated through the system to notify all listeners
//      * that the attribute did change. Proxied copies of this object (on
//      * clients) will apply the value change when they received the
//      * attribute changed notification.
//      */
//     public function setCode (value :String) :void
//     {
//         var ovalue :String = this.code;
//         requestAttributeChange(
//             CODE, value, ovalue);
//         this.code = value;
//     }

//     /**
//      * Requests that the <code>className</code> field be set to the
//      * specified value. The local value will be updated immediately and an
//      * event will be propagated through the system to notify all listeners
//      * that the attribute did change. Proxied copies of this object (on
//      * clients) will apply the value change when they received the
//      * attribute changed notification.
//      */
//     public function setClassName (value :String) :void
//     {
//         var ovalue :String = this.className;
//         requestAttributeChange(
//             CLASS_NAME, value, ovalue);
//         this.className = value;
//     }

//     /**
//      * Requests that the <code>clientOid</code> field be set to the
//      * specified value. The local value will be updated immediately and an
//      * event will be propagated through the system to notify all listeners
//      * that the attribute did change. Proxied copies of this object (on
//      * clients) will apply the value change when they received the
//      * attribute changed notification.
//      */
//     public function setClientOid (value :int) :void
//     {
//         var ovalue :int = this.clientOid;
//         requestAttributeChange(
//             CLIENT_OID, Integer.valueOf(value), Integer.valueOf(ovalue));
//         this.clientOid = value;
//     }
//     // AUTO-GENERATED: METHODS END

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        bureauId = ins.readField(String) as String;
        bureauType = ins.readField(String) as String;
        code = ins.readField(String) as String;
        className = ins.readField(String) as String;
        clientOid = ins.readInt();
    }
}
}
