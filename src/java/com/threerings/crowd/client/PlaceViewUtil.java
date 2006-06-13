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

package com.threerings.crowd.client;

import java.awt.Container;

import com.threerings.crowd.Log;
import com.threerings.crowd.data.PlaceObject;

/**
 * Provides a mechanism for dispatching notifications to all user
 * interface elements in a hierarchy that implement the {@link PlaceView}
 * interface. Look at the documentation for {@link PlaceView} for more
 * explanation.
 */
public class PlaceViewUtil
{
    /**
     * Dispatches a call to {@link PlaceView#willEnterPlace} to all UI
     * elements in the hierarchy rooted at the component provided via the
     * <code>root</code> parameter.
     *
     * @param root the component at which to start traversing the UI
     * hierarchy.
     * @param plobj the place object that is about to be entered.
     */
    public static void dispatchWillEnterPlace (
        Object root, PlaceObject plobj)
    {
        // dispatch the call on this component if it implements PlaceView
        if (root instanceof PlaceView) {
            try {
                ((PlaceView)root).willEnterPlace(plobj);
            } catch (Exception e) {
                Log.warning("Component choked on willEnterPlace() " +
                            "[component=" + root + ", plobj=" + plobj + "].");
                Log.logStackTrace(e);
            }
        }

        // now traverse all of this component's children
        if (root instanceof Container) {
            Container cont = (Container)root;
            int ccount = cont.getComponentCount();
            for (int i = 0; i < ccount; i++) {
                dispatchWillEnterPlace(cont.getComponent(i), plobj);
            }
        }
    }

    /**
     * Dispatches a call to {@link PlaceView#didLeavePlace} to all UI
     * elements in the hierarchy rooted at the component provided via the
     * <code>root</code> parameter.
     *
     * @param root the component at which to start traversing the UI
     * hierarchy.
     * @param plobj the place object that is about to be entered.
     */
    public static void dispatchDidLeavePlace (
        Object root, PlaceObject plobj)
    {
        // dispatch the call on this component if it implements PlaceView
        if (root instanceof PlaceView) {
            try {
                ((PlaceView)root).didLeavePlace(plobj);
            } catch (Exception e) {
                Log.warning("Component choked on didLeavePlace() " +
                            "[component=" + root + ", plobj=" + plobj + "].");
                Log.logStackTrace(e);
            }
        }

        // now traverse all of this component's children
        if (root instanceof Container) {
            Container cont = (Container)root;
            int ccount = cont.getComponentCount();
            for (int i = 0; i < ccount; i++) {
                dispatchDidLeavePlace(cont.getComponent(i), plobj);
            }
        }
    }
}
