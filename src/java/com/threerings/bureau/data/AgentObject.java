//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.bureau.data;

import com.threerings.presents.dobj.DObject;

public class AgentObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>bureauId</code> field. */
    public static final String BUREAU_ID = "bureauId";

    /** The field name of the <code>agentCode</code> field. */
    public static final String AGENT_CODE = "agentCode";

    /** The field name of the <code>startConfirmed</code> field. */
    public static final String START_CONFIRMED = "startConfirmed";
    // AUTO-GENERATED: FIELDS END

    public String bureauId;
    public String agentCode;
    public boolean startConfirmed;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>bureauId</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setBureauId (String value)
    {
        String ovalue = this.bureauId;
        requestAttributeChange(
            BUREAU_ID, value, ovalue);
        this.bureauId = value;
    }

    /**
     * Requests that the <code>agentCode</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setAgentCode (String value)
    {
        String ovalue = this.agentCode;
        requestAttributeChange(
            AGENT_CODE, value, ovalue);
        this.agentCode = value;
    }

    /**
     * Requests that the <code>startConfirmed</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setStartConfirmed (boolean value)
    {
        boolean ovalue = this.startConfirmed;
        requestAttributeChange(
            START_CONFIRMED, Boolean.valueOf(value), Boolean.valueOf(ovalue));
        this.startConfirmed = value;
    }
    // AUTO-GENERATED: METHODS END
}
