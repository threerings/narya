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

package com.threerings.crowd.data;

import java.util.Iterator;

import com.threerings.util.Name;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.OidList;

import com.threerings.crowd.Log;
import com.threerings.crowd.chat.data.SpeakMarshaller;
import com.threerings.crowd.chat.data.SpeakObject;

/**
 * A distributed object that contains information on a place that is
 * occupied by bodies. This place might be a chat room, a game room, an
 * island in a massively multiplayer piratical universe, anything that has
 * occupants that might want to chat with one another.
 */
public class PlaceObject extends DObject
    implements SpeakObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>occupants</code> field. */
    public static final String OCCUPANTS = "occupants";

    /** The field name of the <code>occupantInfo</code> field. */
    public static final String OCCUPANT_INFO = "occupantInfo";

    /** The field name of the <code>speakService</code> field. */
    public static final String SPEAK_SERVICE = "speakService";
    // AUTO-GENERATED: FIELDS END

    /**
     * Tracks the oid of the body objects of all of the occupants of this
     * place.
     */
    public OidList occupants = new OidList();

    /**
     * Contains an info record (of type {@link OccupantInfo}) for each
     * occupant that contains information about that occupant that needs
     * to be known by everyone in the place. <em>Note:</em> Don't obtain
     * occupant info records directly from this set when on the server,
     * use <code>PlaceManager.getOccupantInfo()</code> instead (along with
     * <code>PlaceManager.updateOccupantInfo()</code>) because it does
     * some special processing to ensure that readers and updaters don't
     * step on one another even if they make rapid fire changes to a
     * user's occupant info.
     */
    public DSet<OccupantInfo> occupantInfo = new DSet<OccupantInfo>();

    /** Used to generate speak requests on this place object. */
    public SpeakMarshaller speakService;

    /**
     * Used to indicate whether broadcast chat messages should be dispatched
     * on this place object.
     */
    public boolean shouldBroadcast ()
    {
        return true;
    }

    /**
     * Looks up a user's occupant info by name.
     *
     * @return the occupant info record for the named user or null if no
     * user in the room has that username.
     */
    public OccupantInfo getOccupantInfo (Name username)
    {
        try {
            Iterator iter = occupantInfo.iterator();
            while (iter.hasNext()) {
                OccupantInfo info = (OccupantInfo)iter.next();
                if (info.username.equals(username)) {
                    return info;
                }
            }
        } catch (Throwable t) {
            Log.warning("PlaceObject.getOccupantInfo choked.");
            Log.logStackTrace(t);
        }
        return null;
    }

    // documentation inherited
    public void applyToListeners (ListenerOp op)
    {
        for (int ii = 0, ll = occupants.size(); ii < ll; ii++) {
            op.apply(occupants.get(ii));
        }
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that <code>oid</code> be added to the <code>occupants</code>
     * oid list. The list will not change until the event is actually
     * propagated through the system.
     */
    public void addToOccupants (int oid)
    {
        requestOidAdd(OCCUPANTS, oid);
    }

    /**
     * Requests that <code>oid</code> be removed from the
     * <code>occupants</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromOccupants (int oid)
    {
        requestOidRemove(OCCUPANTS, oid);
    }

    /**
     * Requests that the specified entry be added to the
     * <code>occupantInfo</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToOccupantInfo (DSet.Entry elem)
    {
        requestEntryAdd(OCCUPANT_INFO, occupantInfo, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>occupantInfo</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromOccupantInfo (Comparable key)
    {
        requestEntryRemove(OCCUPANT_INFO, occupantInfo, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>occupantInfo</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateOccupantInfo (DSet.Entry elem)
    {
        requestEntryUpdate(OCCUPANT_INFO, occupantInfo, elem);
    }

    /**
     * Requests that the <code>occupantInfo</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setOccupantInfo (DSet value)
    {
        requestAttributeChange(OCCUPANT_INFO, value, this.occupantInfo);
        this.occupantInfo = (value == null) ? null : (DSet)value.clone();
    }

    /**
     * Requests that the <code>speakService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setSpeakService (SpeakMarshaller value)
    {
        SpeakMarshaller ovalue = this.speakService;
        requestAttributeChange(
            SPEAK_SERVICE, value, ovalue);
        this.speakService = value;
    }
    // AUTO-GENERATED: METHODS END
}
