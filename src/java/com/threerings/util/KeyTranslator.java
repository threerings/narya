//
// $Id: KeyTranslator.java,v 1.6 2004/08/27 02:20:36 mdb Exp $
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

package com.threerings.util;

import java.util.Iterator;

/**
 * The key translator interface provides a means whereby the keyboard
 * manager can map a key code to the logical {@link
 * com.samskivert.swing.Controller} action command that it represents.
 */
public interface KeyTranslator
{
    /**
     * Returns whether there is an action command for the key
     * corresponding to the given keycode.  The translator may have an
     * action command for either a key press or a key release of the key,
     * or both.
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

    /**
     * Returns the number of times each second that key presses are to be
     * automatically repeated while the key is held down, or
     * <code>0</code> to disable auto-repeat for the key.
     */
    public int getRepeatRate (int keyCode);

    /**
     * Returns the delay in milliseconds before generating auto-repeated
     * key press events for the specified key.
     */
    public long getRepeatDelay (int keyCode);

    /**
     * Returns an iterator that iterates over the available press
     * commands.
     */
    public Iterator enumeratePressCommands ();

    /**
     * Returns an iterator that iterates over the available release
     * commands.
     */
    public Iterator enumerateReleaseCommands ();
}
