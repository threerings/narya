//
// $Id: PlaceViewUtil.java,v 1.2 2001/10/05 23:57:26 mdb Exp $

package com.threerings.cocktail.party.client;

import java.awt.Container;

import com.threerings.cocktail.party.Log;
import com.threerings.cocktail.party.data.PlaceObject;

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
