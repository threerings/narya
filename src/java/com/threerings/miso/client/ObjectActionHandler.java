//
// $Id: ObjectActionHandler.java,v 1.2 2004/01/07 22:03:08 ray Exp $

package com.threerings.miso.client;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.Icon;

import com.samskivert.swing.RadialMenu;
import com.samskivert.util.StringUtil;

import com.threerings.miso.Log;
import com.threerings.miso.client.SceneObject;

/**
 * Objects in scenes can be configured to generate action events.  Those
 * events are grouped into types and an object action handler can be
 * registered to handle all actions of a particular type.
 */
public class ObjectActionHandler
{
    /**
     * Returns true if we should allow this object action, false if we
     * should not. This is used to completely hide actions that should not
     * be visible without the proper privileges.
     */
    public boolean actionAllowed (String action)
    {
        return true;
    }

    /**
     * Get the human readable object tip for the specified action.
     */
    public String getTipText (String action)
    {
        return action;
    }

    /**
     * Returns the tooltip icon for the specified action or null if the
     * action has no tooltip icon.
     */
    public Icon getTipIcon (String action)
    {
        return null;
    }

    /**
     * Return a {@link RadialMenu} or null if no menu needed.
     */
    public RadialMenu handlePressed (SceneObject sourceObject)
    {
        return null;
    }

    /**
     * Called when an action is generated for an object.
     */
    public void handleAction (SceneObject scobj, ActionEvent event)
    {
        Log.warning("Unknown object action [scobj=" + scobj +
                    ", action=" + event + "].");
    }

    /**
     * Returns the type associated with this action command (which is
     * mapped to a registered object action handler) or the empty string
     * if it has no type.
     */
    public static String getType (String command)
    {
        int cidx = StringUtil.blank(command) ? -1 : command.indexOf(':');
        return (cidx == -1) ? "" : command.substring(0, cidx);
    }

    /**
     * Returns the unqualified object action (minus the type, see {@link
     * #getType}).
     */
    public static String getAction (String command)
    {
        int cidx = StringUtil.blank(command) ? -1 : command.indexOf(':');
        return (cidx == -1) ? command : command.substring(cidx+1);
    }

    /**
     * Looks up the object action handler associated with the specified
     * command.
     */
    public static ObjectActionHandler lookup (String command)
    {
        return (ObjectActionHandler)_oahandlers.get(getType(command));
    }

    /**
     * Registers an object action handler which will be called when a user
     * clicks on an object in a scene that has an associated action.
     */
    public static void register (String prefix, ObjectActionHandler handler)
    {
        // make sure we know about potential funny business
        if (_oahandlers.containsKey(prefix)) {
            Log.warning("Warning! Overwriting previous object action " +
                        "handler registration, all hell could shortly " +
                        "break loose [prefix=" + prefix +
                        ", handler=" + handler + "].");
        }
        _oahandlers.put(prefix, handler);
    }

    /**
     * Removes an object action handler registration.
     */
    public static void unregister (String prefix)
    {
        _oahandlers.remove(prefix);
    }

    /** Our registered object action handlers. */
    protected static HashMap _oahandlers = new HashMap();
}
