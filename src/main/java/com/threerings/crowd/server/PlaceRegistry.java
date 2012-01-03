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

package com.threerings.crowd.server;

import java.util.Iterator;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.samskivert.util.Lifecycle;

import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import static com.threerings.crowd.Log.log;

/**
 * The place registry keeps track of all of the active places in the server. It should be used to
 * create new places and it will take care of instantiating and initializing a place manager to
 * manage newly created places.
 */
@Singleton
public class PlaceRegistry
    implements Lifecycle.ShutdownComponent
{
    /** Used in conjunction with {@link PlaceRegistry#createPlace(PlaceConfig,PreStartupHook)}. */
    public static interface PreStartupHook
    {
        void invoke (PlaceManager plmgr);
    }

    /**
     * Creates and initializes the place registry. This is called by the server during its
     * initialization phase.
     */
    @Inject public PlaceRegistry (Lifecycle cycle)
    {
        cycle.addComponent(this);
    }

    /**
     * Returns the place manager associated with the specified place object id or null if no such
     * place exists.
     */
    public PlaceManager getPlaceManager (int placeOid)
    {
        return _pmgrs.get(placeOid);
    }

    /**
     * Creates and registers a new place manager with no delegates.
     *
     * @see #createPlace(PlaceConfig,List)
     */
    public PlaceManager createPlace (PlaceConfig config)
        throws InstantiationException, InvocationException
    {
        return createPlace(config, null, null);
    }

    /**
     * Creates and registers a new place manager along with the place object to be managed. The
     * registry takes care of tracking the creation of the object and informing the manager when it
     * is created.
     *
     * @param config the configuration object for the place to be created. The {@link PlaceManager}
     * derived class that should be instantiated to manage the place will be determined from the
     * config object.
     * @param delegates a list of {@link PlaceManagerDelegate} instances to be registered with the
     * manager prior to it being initialized and started up. <em>Note:</em> these delegates will
     * have dependencies injected into them prior to registering them with the manager.
     *
     * @return a reference to the place manager, which will have been configured with its place
     * object and started up (via a call to {@link PlaceManager#startup}.
     *
     * @exception InstantiationException thrown if an error occurs trying to instantiate and
     * initialize the place manager.
     * @exception InvocationException thrown if the place manager returns failure from the call to
     * {@link PlaceManager#checkPermissions}. The error string returned by that call will be
     * provided as in the exception.
     */
    public PlaceManager createPlace (PlaceConfig config, List<PlaceManagerDelegate> delegates)
        throws InstantiationException, InvocationException
    {
        return createPlace(config, delegates, null);
    }

    /**
     * Don't use this method, see {@link #createPlace(PlaceConfig)}.
     *
     * @param hook an optional pre-startup hook that allows a place manager to be configured prior
     * to having {@link PlaceManager#startup} called. This mainly exists because it used to be
     * possible to do such things. Try not to use this in new code.
     */
    public PlaceManager createPlace (PlaceConfig config, PreStartupHook hook)
        throws InstantiationException, InvocationException
    {
        return createPlace(config, null, hook);
    }

    /**
     * Returns an enumeration of all of the registered place objects. This should only be accessed
     * on the dobjmgr thread and shouldn't be kept around across event dispatches.
     */
    public Iterator<PlaceObject> enumeratePlaces ()
    {
        final Iterator<PlaceManager> itr = _pmgrs.values().iterator();
        return new Iterator<PlaceObject>() {
            public boolean hasNext () {
                return itr.hasNext();
            }
            public PlaceObject next () {
                PlaceManager plmgr = itr.next();
                return (plmgr == null) ? null : plmgr.getPlaceObject();
            }
            public void remove () {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns an enumeration of all of the registered place managers.  This should only be
     * accessed on the dobjmgr thread and shouldn't be kept around across event dispatches.
     */
    public Iterator<PlaceManager> enumeratePlaceManagers ()
    {
        return _pmgrs.values().iterator();
    }

    // from interface Lifecycle.ShutdownComponent
    public void shutdown ()
    {
        // shut down all active places
        for (Iterator<PlaceManager> iter = enumeratePlaceManagers(); iter.hasNext(); ) {
            PlaceManager pmgr = iter.next();
            try {
                pmgr.shutdown();
            } catch (Exception e) {
                log.warning("Place manager failed shutting down", "where", pmgr.where(), e);
            }
        }
    }

    /**
     * Creates a place manager using the supplied config, injects dependencies into and registers
     * the supplied list of delegates, runs the supplied pre-startup hook and finally returns it.
     */
    protected PlaceManager createPlace (PlaceConfig config, List<PlaceManagerDelegate> delegates,
                                        PreStartupHook hook)
        throws InstantiationException, InvocationException
    {
        PlaceManager pmgr = null;

        try {
            // create a place manager using the class supplied in the place config
            pmgr = createPlaceManager(config);

            // if we have delegates, inject their dependencies and add them
            if (delegates != null) {
                for (PlaceManagerDelegate delegate : delegates) {
                    _injector.injectMembers(delegate);
                    pmgr.addDelegate(delegate);
                }
            }

            // let the pmgr know about us and its configuration
            pmgr.init(this, _invmgr, _omgr, selectLocator(config), config);

        } catch (Exception e) {
            log.warning(e);
            throw new InstantiationException("Error creating PlaceManager for " + config);
        }

        // let the manager abort the whole process if it fails any permissions checks
        String errmsg = pmgr.checkPermissions();
        if (errmsg != null) {
            // give the place manager a chance to clean up after its early initialization process
            pmgr.permissionsFailed();
            throw new InvocationException(errmsg);
        }

        // and create and register the place object
        PlaceObject plobj = pmgr.createPlaceObject();
        _omgr.registerObject(plobj);

        // stick the manager into our table
        _pmgrs.put(plobj.getOid(), pmgr);

        // start the place manager up with the newly created place object
        try {
            if (hook != null) {
                hook.invoke(pmgr);
            }
            pmgr.startup(plobj);

        } catch (Exception e) {
            log.warning("Error starting place manager", "obj", plobj, "pmgr", pmgr, e);
        }

        return pmgr;
    }

    /**
     * Creates an instance of a {@link PlaceManager} using the information in the supplied place
     * config. Derived classes may wish to specialize this process for certain places for example
     * loading user supplied place management code from a special class loader that sandboxes their
     * code.
     */
    protected PlaceManager createPlaceManager (PlaceConfig config)
        throws Exception
    {
        @SuppressWarnings("unchecked") Class<? extends PlaceManager> clazz =
            (Class<? extends PlaceManager>)Class.forName(config.getManagerClassName());
        return _injector.getInstance(clazz);
    }

    /**
     * Selects the body locator to be used by the PlaceManager created for the supplied config.
     */
    protected BodyLocator selectLocator (PlaceConfig config)
    {
        return _locator;
    }

    /**
     * Called by the place manager when it has been shut down.
     */
    protected void unmapPlaceManager (PlaceManager pmgr)
    {
        int ploid = pmgr.getPlaceObject().getOid();
        // remove it from the table
        if (_pmgrs.remove(ploid) == null) {
            log.warning("Requested to unmap unmapped place manager", "pmgr", pmgr);

//         } else {
//             Log.info("Unmapped place manager [class=" + pmgr.getClass().getName() +
//                      ", ploid=" + ploid + "].");
        }
    }

    /** We use this to inject dependencies into place managers that we create. */
    @Inject protected Injector _injector;

    /** The invocation manager with which we operate. */
    @Inject protected InvocationManager _invmgr;

    /** The distributed object manager with which we operate. */
    @Inject protected RootDObjectManager _omgr;

    /** Used to look body objects up by name. */
    @Inject protected BodyLocator _locator;

    /** A mapping from place object id to place manager. */
    protected IntMap<PlaceManager> _pmgrs = IntMaps.newHashIntMap();
}
