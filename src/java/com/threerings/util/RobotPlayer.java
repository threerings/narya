//
// $Id: RobotPlayer.java,v 1.1 2002/01/18 23:32:14 shaper Exp $

package com.threerings.util;

import java.awt.Component;
import java.util.ArrayList;

import com.samskivert.swing.Controller;
import com.samskivert.util.CollectionUtil;
import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;

import com.threerings.media.util.RandomUtil;

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
public class RobotPlayer implements Interval
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
        if (active && !isActive()) {
            // start the robot player
            _iid = IntervalManager.register(this, _robotDelay, null, true);

        } else if (!active && isActive()) {
            // stop the robot player
            IntervalManager.remove(_iid);
            _iid = -1;
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
        return (_iid != -1);
    }

    // documentation inherited
    public void intervalExpired (int id, Object arg)
    {
        // post a random key press command
        int idx = RandomUtil.getInt(_press.size());
        String command = (String)_press.get(idx);
        // Log.info("Posting artificial command [cmd=" + command + "].");
        Controller.postAction(_target, command);
    }

    /** The default robot delay. */
    protected static final long DEFAULT_ROBOT_DELAY = 500L;

    /** The unique robot interval identifier. */
    protected int _iid = -1;

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
