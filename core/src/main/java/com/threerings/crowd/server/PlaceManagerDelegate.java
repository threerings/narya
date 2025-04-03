//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.server;

import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

/**
 * Provides an extensible mechanism for encapsulating delegated functionality that works with the
 * place services.
 *
 * <p> Thanks to Java's lack of multiple inheritance, it will likely become necessary to factor
 * certain services that might be used by a variety of {@link PlaceManager} derived classes into
 * delegate classes because they do not fit into the single inheritance hierarchy that makes sense
 * for a particular application. To facilitate this process, this delegate class is provided which
 * the standard place manager can be made to call out to for all of the standard methods.
 */
public class PlaceManagerDelegate
{
    /**
     * Called by the place manager when this delegate is registered with it. This will happen
     * before any calls to {@link #didInit}, etc.
     */
    public void init (PlaceManager plmgr, RootDObjectManager omgr, InvocationManager invmgr)
    {
        _plmgr = plmgr;
        _omgr = omgr;
        _invmgr = invmgr;
    }

    /**
     * Called when the place manager is initialized.
     */
    public void didInit (PlaceConfig config)
    {
    }

    /**
     * Called when the place manager is started up.
     */
    public void didStartup (PlaceObject plobj)
    {
    }

    /**
     * Called when the place manager is shut down.
     */
    public void didShutdown ()
    {
    }

    /**
     * Called when a body enters the place.
     */
    public void bodyEntered (int bodyOid)
    {
    }

    /**
     * Called when a body leaves the place.
     */
    public void bodyLeft (int bodyOid)
    {
    }

    /**
     * Called when a body occupant info is updated.
     */
    public void bodyUpdated (OccupantInfo info)
    {
    }

    /**
     * Called when the last body leaves the place.
     */
    public void placeBecameEmpty ()
    {
    }

    /**
     * Invokes {@link PlaceManager#where}.
     */
    public String where ()
    {
        return _plmgr.where();
    }

    /**
     * Registers an invocation provider and notes the registration such that it will be
     * automatically cleared when our parent manager shuts down.
     */
    protected <T extends InvocationMarshaller<?>> T addProvider (
        InvocationProvider prov, Class<T> mclass)
    {
        return _plmgr.addProvider(prov, mclass);
    }

    /**
     * Registers an invocation dispatcher and notes the registration such that it will be
     * automatically cleared when our parent manager shuts down.
     */
    protected <T extends InvocationMarshaller<?>> T addDispatcher (InvocationDispatcher<T> disp)
    {
        return _plmgr.addDispatcher(disp);
    }

    /** A reference to the manager for which we are delegating. */
    protected PlaceManager _plmgr;

    /** A reference to our distributed object manager. */
    protected RootDObjectManager _omgr;

    /** A reference to our invocation manager. */
    protected InvocationManager _invmgr;
}
