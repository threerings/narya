//
// $Id: KeyTranslator.java,v 1.1 2001/12/12 16:55:06 shaper Exp $

package com.threerings.yohoho.puzzle.util;

/**
 * The key translator interface provides a means whereby the keyboard
 * manager can map a key code to the logical {@link
 * com.samskivert.swing.Controller} action command that it represents.
 */
public interface KeyTranslator
{
    /**
     * Returns whether there is an action command for the key
     * corresponding to the given keycode.  The translator may have a
     * mapping for an action command for a key press or a key release of
     * the key, or both.
     */
    public boolean hasCommand (int keyCode);

    /**
     * Returns the action command string associated with a key press of
     * the key corresponding to the given key code, or <code>null</code>
     * if there is no associated command.
     */
    public String getPressCommand (int keyCode);

    /**
     * Returns the action command string associated with a key release of
     * the key corresponding to the given key code, or <code>null</code>
     * if there is no associated command.
     */
    public String getReleaseCommand (int keyCode);
}
