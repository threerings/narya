//
// $Id: KeyTranslatorImpl.java,v 1.6 2003/02/10 18:48:16 ray Exp $

package com.threerings.util;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.util.HashIntMap;

/**
 * A basic implementation of the {@link KeyTranslator} interface that
 * provides facilities for mapping key codes to action command strings for
 * use by the {@link KeyboardManager}.
 */
public class KeyTranslatorImpl implements KeyTranslator
{
    /**
     * Adds a mapping from a key press to an action command string that
     * will auto-repeat at a default repeat rate.
     */
    public void addPressCommand (int keyCode, String command)
    {
        addPressCommand(keyCode, command, DEFAULT_REPEAT_RATE);
    }

    /**
     * Adds a mapping from a key press to an action command string that
     * will auto-repeat at the specified repeat rate.  Overwrites any
     * existing mapping and repeat rate that may have already been
     * registered.
     *
     * @param rate the number of times each second that the key press
     * should be repeated while the key is down, or <code>0</code> to
     * disable auto-repeat for the key.
     */
    public void addPressCommand (int keyCode, String command, int rate)
    {
        addPressCommand(keyCode, command, rate, DEFAULT_REPEAT_DELAY);
    }

    /**
     * Adds a mapping from a key press to an action command string that
     * will auto-repeat at the specified repeat rate after the specified
     * auto-repeat delay has expired.  Overwrites any existing mapping for
     * the specified key code that may have already been registered.
     *
     * @param rate the number of times each second that the key press
     * should be repeated while the key is down; passing <code>0</code>
     * will result in no repeating.
     * @param repeatDelay the delay in milliseconds before auto-repeating
     * key press events will be generated for the key.
     */
    public void addPressCommand (
        int keyCode, String command, int rate, long repeatDelay)
    {
        KeyRecord krec = getKeyRecord(keyCode);
        krec.pressCommand = command;
        krec.repeatRate = rate;
        krec.repeatDelay = repeatDelay;
    }

    /**
     * Adds a mapping from a key release to an action command string.
     * Overwrites any existing mapping that may already have been
     * registered.
     */
    public void addReleaseCommand (int keyCode, String command)
    {
        KeyRecord krec = getKeyRecord(keyCode);
        krec.releaseCommand = command;
    }

    /**
     * Returns the key record for the specified key, creating it and
     * inserting it in the key table if necessary.
     */
    protected KeyRecord getKeyRecord (int keyCode)
    {
        KeyRecord krec = (KeyRecord)_keys.get(keyCode);
        if (krec == null) {
            krec = new KeyRecord();
            _keys.put(keyCode, krec);
        }
        return krec;
    }

    // documentation inherited
    public boolean hasCommand (int keyCode)
    {
        return (_keys.get(keyCode) != null);
    }

    // documentation inherited
    public String getPressCommand (int keyCode)
    {
        KeyRecord krec = (KeyRecord)_keys.get(keyCode);
        return (krec == null) ? null : krec.pressCommand;
    }

    // documentation inherited
    public String getReleaseCommand (int keyCode)
    {
        KeyRecord krec = (KeyRecord)_keys.get(keyCode);
        return (krec == null) ? null : krec.releaseCommand;
    }

    // documentation inherited
    public int getRepeatRate (int keyCode)
    {
        KeyRecord krec = (KeyRecord)_keys.get(keyCode);
        return (krec == null) ? DEFAULT_REPEAT_RATE : krec.repeatRate;
    }

    // documentation inherited
    public long getRepeatDelay (int keyCode)
    {
        KeyRecord krec = (KeyRecord)_keys.get(keyCode);
        return (krec == null) ? DEFAULT_REPEAT_DELAY : krec.repeatDelay;
    }

    // documentation inherited
    public Iterator enumeratePressCommands ()
    {
        ArrayList commands = new ArrayList();
        Iterator iter = _keys.values().iterator();
        while (iter.hasNext()) {
            KeyRecord krec = (KeyRecord)iter.next();
            commands.add(krec.pressCommand);
        }
        return commands.iterator();
    }

    // documentation inherited
    public Iterator enumerateReleaseCommands ()
    {
        ArrayList commands = new ArrayList();
        Iterator iter = _keys.values().iterator();
        while (iter.hasNext()) {
            KeyRecord krec = (KeyRecord)iter.next();
            commands.add(krec.releaseCommand);
        }
        return commands.iterator();
    }

    protected class KeyRecord
    {
        /** The command to be posted when the key is pressed. */
        public String pressCommand;

        /** The command to be posted when the key is released. */
        public String releaseCommand;

        /** The rate in presses per second at which the key is to be
         * auto-repeated. */
        public int repeatRate;

        /** The delay in milliseconds that must expire with the key still
         * pressed before auto-repeated key presses will begin. */
        public long repeatDelay;
    }

    /** The keys for which commands are registered. */
    protected HashIntMap _keys = new HashIntMap();

    /** The default key press repeat rate. */
    protected static final int DEFAULT_REPEAT_RATE = 5;

    /** The default delay in milliseconds before auto-repeated key presses
     * will begin. */
    protected static final long DEFAULT_REPEAT_DELAY = 500L;
}
