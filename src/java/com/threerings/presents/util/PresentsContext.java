//
// $Id: PresentsContext.java,v 1.6 2003/11/24 17:51:08 mdb Exp $

package com.threerings.presents.util;

import com.samskivert.util.Config;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

/**
 * Provides access to standard services needed by code that is part of or
 * uses the Presents package.
 */
public interface PresentsContext
{
    /**
     * Provides a configuration object from which various services can
     * obtain configuration values and via the properties file that forms
     * the basis of the configuration object, those services can be
     * customized.
     */
    public Config getConfig ();

    /**
     * Returns a reference to the client. This reference should be valid
     * for the life of the application.
     */
    public Client getClient ();

    /**
     * Returns a reference to the distributed object manager. This
     * reference is only valid for the duration of a session.
     */
    public DObjectManager getDObjectManager ();
}
