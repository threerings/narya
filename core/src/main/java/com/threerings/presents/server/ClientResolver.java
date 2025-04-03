//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import com.samskivert.util.Invoker;

import com.threerings.util.Name;

import com.threerings.presents.annotation.MainInvoker;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;

import static com.threerings.presents.Log.log;

/**
 * Used to resolve client data when a user starts a session (or when some other entity needs access
 * to a client object). Implementations will want to extend this class and override {@link
 * #resolveClientData}, making the necessary database calls and populating the client object
 * appropriately.
 */
public class ClientResolver extends Invoker.Unit
{
    /**
     * Thrown during resolution if the client disconnects.
     */
    public static class ClientDisconnectedException extends Exception
    {
    }

    /**
     * Initializes this instance.
     *
     * @param username the username of the user to be resolved.
     */
    public void init (Name username)
    {
        _username = username;
    }

    /**
     * Adds a resolution listener to this active resolver.
     */
    public void addResolutionListener (ClientResolutionListener listener)
    {
        _listeners.add(listener);
    }

    /**
     * Creates the {@link ClientObject} derived class that should be created to kick off the
     * resolution process.
     */
    public ClientObject createClientObject ()
    {
        return new ClientObject();
    }

    /**
     * Creates a record that will be maintained only on the server to track client related bits.
     */
    public ClientLocal createLocalAttribute ()
    {
        return new ClientLocal();
    }

    /**
     * Called once our client object is registered with the distributed object system.
     */
    public void objectAvailable (ClientObject object)
    {
        // we've got our object, so shunt ourselves over to the invoker thread to perform database
        // loading
        _clobj = object;
        _invoker.postUnit(this);
    }

    @Override
    public boolean invoke ()
    {
        try {
            // allow our derived class to do its database loads
            resolveClientData(_clobj);
        } catch (Exception cause) {
            // keep this around until we're back on the dobj thread
            _failure = cause;
        }
        return true;
    }

    @Override
    public void handleResult ()
    {
        // if we haven't failed, finish resolution on the dobj thread
        if (_failure == null) {
            try {
                finishResolution(_clobj);
            } catch (Exception e) {
                _failure = e;
            }
        }

        // if we still haven't failed, then we're good to go
        if (_failure == null) {
            // and let the listeners in on the secret as well
            reportSuccess();

        } else {
            // destroy the dangling user object
            _omgr.destroyObject(_clobj.getOid());

            // let our listener know that we're hosed
            reportFailure(_failure);
        }
    }

    @Override
    public String toString ()
    {
        return "ClientResolver:" + _username;
    }

    /**
     * This method is called on the invoker thread which means that it can do things like blocking
     * database requests and generally whatever is necessary to load up all the client data that is
     * desired by the implentation system. Any exceptions that are thrown will be caught and
     * reported as a failure to the client resolution listener.
     */
    protected void resolveClientData (ClientObject clobj)
        throws Exception
    {
        // fill in the username
        clobj.username = _username;
    }

    /**
     * This method is called on the dobj thread after resolveClientData returns normally, it should
     * finish populating the client object with any data that is NOT loaded from a database.
     */
    protected void finishResolution (ClientObject clobj)
    {
        // nothing to do by default
    }

    /**
     * Reports success to our resolution listeners.
     */
    protected void reportSuccess ()
    {
        for (int ii = 0, ll = _listeners.size(); ii < ll; ii++) {
            ClientResolutionListener crl = _listeners.get(ii);
            try {
                // add a reference for each listener
                _clobj.reference();
                crl.clientResolved(_username, _clobj);
            } catch (Exception e) {
                log.warning("Client resolution listener choked in clientResolved() " + crl, e);
            }
        }
    }

    /**
     * Reports failure to our resolution listeners.
     */
    protected void reportFailure (Exception cause)
    {
        for (int ii = 0, ll = _listeners.size(); ii < ll; ii++) {
            ClientResolutionListener crl = _listeners.get(ii);
            try {
                crl.resolutionFailed(_username, cause);
            } catch (Exception e) {
                log.warning("Client resolution listener choked in resolutionFailed()", "crl", crl,
                            "username", _username, "cause", cause, e);
            }
        }
    }

    /**
     * Throws an exception if the client being resolved is no longer connected.
     */
    protected void enforceConnected ()
        throws ClientDisconnectedException
    {
        if (_clmgr.getClient(_username) == null) {
            throw new ClientDisconnectedException();
        }
    }

    /** The name of the user whose client object is being resolved. */
    protected Name _username;

    /** The entities to notify of success or failure. */
    protected List<ClientResolutionListener> _listeners = Lists.newArrayList();

    /** The resolving client object. */
    protected ClientObject _clobj;

    /** A place to keep an exception around for a moment. */
    protected Exception _failure;

    // dependencies
    protected @Inject @MainInvoker Invoker _invoker;
    protected @Inject RootDObjectManager _omgr;
    protected @Inject ClientManager _clmgr;
}
