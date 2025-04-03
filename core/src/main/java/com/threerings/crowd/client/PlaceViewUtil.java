//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.client;

import java.awt.Container;

import com.threerings.crowd.data.PlaceObject;

import static com.threerings.crowd.Log.log;

/**
 * Provides a mechanism for dispatching notifications to all user interface elements in a
 * hierarchy that implement the {@link PlaceView} interface. Look at the documentation for
 * {@link PlaceView} for more explanation.
 */
public class PlaceViewUtil
{
    /**
     * Dispatches a call to {@link PlaceView#willEnterPlace} to all UI elements in the hierarchy
     * rooted at the component provided via the <code>root</code> parameter.
     *
     * @param root the component at which to start traversing the UI hierarchy.
     * @param plobj the place object that is about to be entered.
     */
    public static void dispatchWillEnterPlace (Object root, PlaceObject plobj)
    {
        // dispatch the call on this component if it implements PlaceView
        if (root instanceof PlaceView) {
            try {
                ((PlaceView)root).willEnterPlace(plobj);
            } catch (Exception e) {
                log.warning("Component choked on willEnterPlace()", "component", root,
                    "plobj", plobj, e);
            }
        }

        // now traverse all of this component's children
        if (root instanceof Container) {
            Container cont = (Container)root;
            int ccount = cont.getComponentCount();
            for (int ii = 0; ii < ccount; ii++) {
                dispatchWillEnterPlace(cont.getComponent(ii), plobj);
            }
        }
    }

    /**
     * Dispatches a call to {@link PlaceView#didLeavePlace} to all UI elements in the hierarchy
     * rooted at the component provided via the <code>root</code> parameter.
     *
     * @param root the component at which to start traversing the UI hierarchy.
     * @param plobj the place object that is about to be entered.
     */
    public static void dispatchDidLeavePlace (Object root, PlaceObject plobj)
    {
        // dispatch the call on this component if it implements PlaceView
        if (root instanceof PlaceView) {
            try {
                ((PlaceView)root).didLeavePlace(plobj);
            } catch (Exception e) {
                log.warning("Component choked on didLeavePlace()", "component", root,
                    "plobj", plobj, e);
            }
        }

        // now traverse all of this component's children
        if (root instanceof Container) {
            Container cont = (Container)root;
            int ccount = cont.getComponentCount();
            for (int ii = 0; ii < ccount; ii++) {
                dispatchDidLeavePlace(cont.getComponent(ii), plobj);
            }
        }
    }
}
