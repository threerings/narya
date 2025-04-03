//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.data;

import javax.annotation.Generated;
import com.threerings.util.Name;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.OidList;
import com.threerings.presents.dobj.ServerMessageEvent;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.SpeakMarshaller;
import com.threerings.crowd.chat.data.SpeakObject;
import com.threerings.crowd.chat.data.UserMessage;

import static com.threerings.crowd.Log.log;

/**
 * A distributed object that contains information on a place that is occupied by bodies. This place
 * might be a chat room, a game room, an island in a massively multiplayer piratical universe,
 * anything that has occupants that might want to chat with one another.
 */
public class PlaceObject extends DObject
    implements SpeakObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>occupants</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String OCCUPANTS = "occupants";

    /** The field name of the <code>occupantInfo</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String OCCUPANT_INFO = "occupantInfo";

    /** The field name of the <code>speakService</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String SPEAK_SERVICE = "speakService";
    // AUTO-GENERATED: FIELDS END

    /**
     * Exists to make calls into the manager look sensible:
     *
     * <pre>_plobj.manager.invoke("someMethod", args);</pre>
     *
     * and to route events through the right distributed object manager if we are running in
     * standalone/single-player mode where both client and server are running in the same VM.
     *
     * <em>Note:</em> For this to work, your manager needs to override
     *    <code>PlaceManager.allowManagerCall()</code> and return true for the desired method name.
     *    But maybe you should consider using an InvocationService instead.
     */
    public class ManagerCaller
    {
        /**
         * @deprecated this is pretty darn unsafe. Why don't you just set up a Service?
         */
        @Deprecated
        public void invoke (String method, Object ... args) {
            _omgr.postEvent(new ServerMessageEvent(_oid, method, args));
        }
        protected ManagerCaller (DObjectManager omgr) {
            _omgr = omgr;
        }
        protected DObjectManager _omgr;
    }

    /**
     * Allows the client to call methods on the manager.
     */
    public transient ManagerCaller manager;

    /**
     * Tracks the oid of the body objects of all of the occupants of this place.
     */
    public OidList occupants = new OidList();

    /**
     * Contains an info record (of type {@link OccupantInfo}) for each occupant that contains
     * information about that occupant that needs to be known by everyone in the place.
     * <em>Note:</em> Don't obtain occupant info records directly from this set when on the server,
     * use <code>PlaceManager.getOccupantInfo()</code> instead (along with
     * <code>PlaceManager.updateOccupantInfo()</code>) because it does some special processing to
     * ensure that readers and updaters don't step on one another even if they make rapid fire
     * changes to a user's occupant info.
     */
    public DSet<OccupantInfo> occupantInfo = new DSet<OccupantInfo>();

    /** Used to generate speak requests on this place object. */
    public SpeakMarshaller speakService;

    /**
     * Called on the client when the location director receives this place object to configure our
     * manager caller using the client's distributed object manager.
     */
    public void initManagerCaller (DObjectManager omgr)
    {
        manager = new ManagerCaller(omgr);
    }

    /**
     * Used to indicate whether broadcast chat messages should be dispatched on this place object.
     */
    public boolean shouldBroadcast ()
    {
        return true;
    }

    /**
     * Looks up a user's occupant info by name.
     *
     * @return the occupant info record for the named user or null if no user in the room has that
     * username.
     */
    public OccupantInfo getOccupantInfo (Name username)
    {
        try {
            for (OccupantInfo info : occupantInfo) {
                if (info.username.equals(username)) {
                    return info;
                }
            }
        } catch (Throwable t) {
            log.warning("PlaceObject.getOccupantInfo choked.", t);
        }
        return null;
    }

    // documentation inherited
    public void applyToListeners (ListenerOp op)
    {
        for (int ii = 0, ll = occupants.size(); ii < ll; ii++) {
            op.apply(this, occupants.get(ii));
        }
    }

    // documentation inherited
    public String getChatIdentifier (UserMessage message)
    {
        return SpeakObject.DEFAULT_IDENTIFIER;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that <code>oid</code> be added to the <code>occupants</code>
     * oid list. The list will not change until the event is actually
     * propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToOccupants (int oid)
    {
        requestOidAdd(OCCUPANTS, occupants, oid);
    }

    /**
     * Requests that <code>oid</code> be removed from the
     * <code>occupants</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromOccupants (int oid)
    {
        requestOidRemove(OCCUPANTS, occupants, oid);
    }

    /**
     * Requests that the specified entry be added to the
     * <code>occupantInfo</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToOccupantInfo (OccupantInfo elem)
    {
        requestEntryAdd(OCCUPANT_INFO, occupantInfo, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>occupantInfo</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromOccupantInfo (Comparable<?> key)
    {
        requestEntryRemove(OCCUPANT_INFO, occupantInfo, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>occupantInfo</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateOccupantInfo (OccupantInfo elem)
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setOccupantInfo (DSet<OccupantInfo> value)
    {
        requestAttributeChange(OCCUPANT_INFO, value, this.occupantInfo);
        DSet<OccupantInfo> clone = (value == null) ? null : value.clone();
        this.occupantInfo = clone;
    }

    /**
     * Requests that the <code>speakService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setSpeakService (SpeakMarshaller value)
    {
        SpeakMarshaller ovalue = this.speakService;
        requestAttributeChange(
            SPEAK_SERVICE, value, ovalue);
        this.speakService = value;
    }
    // AUTO-GENERATED: METHODS END
}
