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

package com.threerings.presents.server;

import java.util.ArrayList;

import com.samskivert.util.Invoker;
import com.threerings.util.Name;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;

/**
 * Used to resolve client data when a user starts a session (or when some
 * other entity needs access to a client object). Implementations will
 * want to extend this class and override {@link #resolveClientData},
 * making the necessary database calls and populating the client object
 * appropriately.
 */
public class ClientResolver extends Invoker.Unit
    implements Subscriber<ClientObject>
{
    /**
     * Initiailizes this instance.
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
     * Returns the {@link ClientObject} derived class that should be
     * created to kick off the resolution process.
     */
    public Class getClientObjectClass ()
    {
        return ClientObject.class;
    }

    // documentation inherited
    public void objectAvailable (ClientObject object)
    {
        // we've got our object, so shunt ourselves over to the invoker
        // thread to perform database loading
        _clobj = object;
        PresentsServer.invoker.postUnit(this);

        // we no longer need to be a subscriber of the object now that it
        // has been created
        _clobj.removeSubscriber(this);
    }

    // documentation inherited
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // pass the buck
        reportFailure(cause);
    }

    // documentation inherited
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

    // documentation inherited
    public void handleResult ()
    {
        // if we failed in invoke() report it here
        if (_failure != null) {
            // destroy the dangling user object
            PresentsServer.omgr.destroyObject(_clobj.getOid());

            // let our listener know that we're hosed
            reportFailure(_failure);

        } else {
            // otherwise, do more resolution goodness on the dobj thread
            finishResolution(_clobj);

            // let the client manager know that we're all clear
            PresentsServer.clmgr.mapClientObject(_username, _clobj);

            // and let the listeners in on the secret as well
            for (int ii = 0, ll = _listeners.size(); ii < ll; ii++) {
                ClientResolutionListener crl = _listeners.get(ii);
                try {
                    // add a reference for each listener
                    _clobj.reference();
                    crl.clientResolved(_username, _clobj);
                } catch (Exception e) {
                    Log.warning("Client resolution listener choked during " +
                                "resolution notification [crl=" + crl +
                                ", clobj=" + _clobj + "].");
                    Log.logStackTrace(e);
                }
            }
        }
    }

    // documentation inherited
    public String toString ()
    {
        return "ClientResolver:" + _username;
    }

    /**
     * This method is called on the invoker thread which means that it can
     * do things like blocking database requests and generally whatever is
     * necessary to load up all the client data that is desired by the
     * implentation system. Any exceptions that are thrown will be caught
     * and reported as a failure to the client resolution listener.
     */
    protected void resolveClientData (ClientObject clobj)
        throws Exception
    {
        // nothing to do by default
    }

    /**
     * This method is called on the dobj thread after resolveClientData
     * returns normally, it should finish populating the client object
     * with any data that is NOT loaded from a database.
     */
    protected void finishResolution (ClientObject clobj)
    {
        // nothing to do by default
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
                Log.warning("Client resolution listener choked during " +
                            "failure notification [crl=" + crl +
                            ", username=" + _username +
                            ", cause=" + cause + "].");
                Log.logStackTrace(e);
            }
        }
    }

    /** The name of the user whose client object is being resolved. */
    protected Name _username;

    /** The entities to notify of success or failure. */
    protected ArrayList<ClientResolutionListener> _listeners =
        new ArrayList<ClientResolutionListener>();

    /** The resolving client object. */
    protected ClientObject _clobj;

    /** A place to keep an exception around for a moment. */
    protected Exception _failure;
}
