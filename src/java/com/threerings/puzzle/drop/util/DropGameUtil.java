//
// $Id: DropGameUtil.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.drop.util;

import java.awt.event.KeyEvent;

import com.threerings.util.KeyTranslatorImpl;

import com.threerings.puzzle.drop.client.DropControllerDelegate;
import com.threerings.puzzle.util.PuzzleGameUtil;

/**
 * Drop puzzle game related utilities.
 */
public class DropGameUtil
{
    /**
     * Returns a key translator configured with mappings suitable for a
     * drop puzzle game.
     */
    public static KeyTranslatorImpl getKeyTranslator ()
    {
        // start with the standard puzzle key mappings
        KeyTranslatorImpl xlate = PuzzleGameUtil.getKeyTranslator();

        // add all press key mappings
        xlate.addPressCommand(KeyEvent.VK_LEFT,
                              DropControllerDelegate.MOVE_BLOCK_LEFT,
                              MOVE_RATE, MOVE_DELAY);
        xlate.addPressCommand(KeyEvent.VK_RIGHT,
                              DropControllerDelegate.MOVE_BLOCK_RIGHT,
                              MOVE_RATE, MOVE_DELAY);
        xlate.addPressCommand(KeyEvent.VK_UP,
                              DropControllerDelegate.ROTATE_BLOCK_CCW, 0);
        xlate.addPressCommand(KeyEvent.VK_DOWN,
                              DropControllerDelegate.ROTATE_BLOCK_CW, 0);
        xlate.addPressCommand(KeyEvent.VK_SPACE,
                              DropControllerDelegate.START_DROP_BLOCK, 0);

        // add all release key mappings
        xlate.addReleaseCommand(KeyEvent.VK_SPACE,
                                DropControllerDelegate.END_DROP_BLOCK);

        return xlate;
    }

    /** The move key repeat rate in moves per second. */
    protected static final int MOVE_RATE = 7;

    /** The delay in milliseconds before the move keys begin to repeat. */
    protected static final long MOVE_DELAY = 300L;
}
