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

package com.threerings.util;

import java.awt.Component;
import java.util.ArrayList;

import com.samskivert.swing.Controller;
import com.samskivert.util.CollectionUtil;
import com.samskivert.util.Interval;
import com.samskivert.util.RandomUtil;

/**
 * The robot player is a computer player with truly rudimentary artificial
 * intelligence that periodically posts random commands selected from the
 * available key press and release commands to the target component.
 *
 * Note that {@link java.awt.Robot} could have been used to post key
 * events to the target component rather than commands, but not all key
 * events can be simulated in that fashion (e.g., a right shift key
 * press), and this seemed somehow more proper in any case.
 */
public class RobotPlayer extends Interval
{
    /**
     * Constructs a robot player.
     */
    public RobotPlayer (Component target, KeyTranslator xlate)
    {
        // save off references
        _target = target;
        _xlate = xlate;

        // build the list of available commands
        CollectionUtil.addAll(_press, _xlate.enumeratePressCommands());
        CollectionUtil.addAll(_release, _xlate.enumerateReleaseCommands());
    }

    /**
     * Sets whether the robot player is actively posting action commands.
     */
    public void setActive (boolean active)
    {
        if (active != _active) {
            if (active) {
                schedule(_robotDelay, true);
            } else {
                cancel(); // stop the robot player
            }
            _active = active;
        }
    }

    /**
     * Sets the delay in milliseconds between posting each action command.
     */
    public void setRobotDelay (long delay)
    {
        _robotDelay = delay;

        // if the robot is active, reset it with the new delay time
        if (isActive()) {
            setActive(false);
            setActive(true);
        }
    }

    /**
     * Returns whether the robot is currently active and periodically
     * posting action commands.
     */
    public boolean isActive ()
    {
        return _active;
    }

    // documentation inherited
    public void expired ()
    {
        // post a random key press command
        int idx = RandomUtil.getInt(_press.size());
        String command = (String)_press.get(idx);
        // Log.info("Posting artificial command [cmd=" + command + "].");
        Controller.postAction(_target, command);
    }

    /** The default robot delay. */
    protected static final long DEFAULT_ROBOT_DELAY = 500L;

    /** Whether the robot is active or not. */
    protected boolean _active = false;

    /** The milliseconds between posting each action command. */
    protected long _robotDelay = DEFAULT_ROBOT_DELAY;

    /** The list of available key press action commands. */
    protected ArrayList _press = new ArrayList();

    /** The list of available key release action commands. */
    protected ArrayList _release = new ArrayList();

    /** The key translator that describes available keys and commands. */
    protected KeyTranslator _xlate;

    /** The target component associated with game action commands. */
    protected Component _target;
}
