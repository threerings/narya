//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.ActionScript;

import com.threerings.crowd.client.PlaceController;

import static com.threerings.crowd.Log.log;

/**
 * The place config class encapsulates the configuration information for a
 * particular type of place. The hierarchy of place config objects mimics
 * the hierarchy of place managers and controllers. Both the place manager
 * and place controller are provided with the place config object when the
 * place is created.
 *
 * <p> The place config object is also the mechanism used to instantiate the
 * appropriate place manager and controller. Every place must have an
 * associated place config derived class that overrides {@link
 * #createController} and {@link #getManagerClassName}, returning the
 * appropriate place controller and manager class for that place.
 */
public abstract class PlaceConfig extends SimpleStreamableObject
{
    /**
     * Returns the class that should be used to create a controller for this
     * place. The controller class must derive from {@link PlaceController}.
     *
     * @deprecated Override {@link #createController} directly.
     */
    @Deprecated
    public Class<?> getControllerClass ()
    {
        return null;
    }

    /**
     * Create the controller that should be used for this place.
     */
    public PlaceController createController ()
    {
        Class<?> cclass = getControllerClass();
        if (cclass == null) {
            throw new RuntimeException(
                "PlaceConfig.createController() must be overridden.");
        }

        log.warning("Providing backwards compatibility. PlaceConfig." +
                    "createController() should be overridden directly.");
        try {
            return (PlaceController)cclass.newInstance();
        } catch (Exception e) {
            log.warning("Failed to instantiate controller class '" + cclass + "'.", e);
            return null;
        }
    }

    /**
     * Returns the name of the class that should be used to create a manager
     * for this place. The manager class must derive from {@link
     * com.threerings.crowd.server.PlaceManager}. <em>Note:</em> this method
     * differs from {@link #createController} because we want to avoid compile
     * time linkage of the place config object (which is used on the client) to
     * server code. This allows a code optimizer (DashO Pro, for example) to
     * remove the server code from the client, knowing that it is never used.
     */
    public abstract String getManagerClassName ();

    @Override
    @ActionScript(name="toStringBuilder")
    protected void toString (StringBuilder buf)
    {
        buf.append("type=").append(StringUtil.shortClassName(this));
        buf.append(", ");
        super.toString(buf);
    }
}
