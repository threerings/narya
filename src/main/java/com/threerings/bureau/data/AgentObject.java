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

package com.threerings.bureau.data;

import javax.annotation.Generated;
import com.threerings.presents.dobj.DObject;

/**
 * Contains information for configuring and communicating with an agent.
 */
public class AgentObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>bureauId</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String BUREAU_ID = "bureauId";

    /** The field name of the <code>bureauType</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String BUREAU_TYPE = "bureauType";

    /** The field name of the <code>code</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String CODE = "code";

    /** The field name of the <code>className</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String CLASS_NAME = "className";

    /** The field name of the <code>clientOid</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String CLIENT_OID = "clientOid";
    // AUTO-GENERATED: FIELDS END

    /** The id of the bureau the agent is running in. This is normally a unique id corresponding
     *  to the game or item that requires some server-side processing. */
    public String bureauId;

    /** The type of bureau that the agent is running in. This is normally derived from the kind
     *  of media that the game or item has specified for its code and determines the method of
     *  launching the bureau when the first agent is requested. */
    public String bureauType;

    /** The location of the code for the agent. This could be a URL to an action script file or
     *  some other description that the bureau can use to load and execute the agent's code. */
    public String code;

    /** The main class within the code to use when launching an agent. Whether this value is
     *  used depends on the type of bureau and will be resolve in the bureau client. */
    public String className;

    /** The id of the client running this agent (only set after the agent is assigned to a
     *  bureau and run). */
    public int clientOid;

    /**
     * Returns a brief string that identifies this agent. Use this instead of
     * {@link Object#toString} when you wish to report an agent object in a log message.
     */
    @Override
    public String which ()
    {
        return "[bid=" + bureauId + ", type=" + bureauType + "]";
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>bureauId</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setBureauId (String value)
    {
        String ovalue = this.bureauId;
        requestAttributeChange(
            BUREAU_ID, value, ovalue);
        this.bureauId = value;
    }

    /**
     * Requests that the <code>bureauType</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setBureauType (String value)
    {
        String ovalue = this.bureauType;
        requestAttributeChange(
            BUREAU_TYPE, value, ovalue);
        this.bureauType = value;
    }

    /**
     * Requests that the <code>code</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setCode (String value)
    {
        String ovalue = this.code;
        requestAttributeChange(
            CODE, value, ovalue);
        this.code = value;
    }

    /**
     * Requests that the <code>className</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setClassName (String value)
    {
        String ovalue = this.className;
        requestAttributeChange(
            CLASS_NAME, value, ovalue);
        this.className = value;
    }

    /**
     * Requests that the <code>clientOid</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setClientOid (int value)
    {
        int ovalue = this.clientOid;
        requestAttributeChange(
            CLIENT_OID, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.clientOid = value;
    }
    // AUTO-GENERATED: METHODS END
}
