//
// $Id: PuzzleGameUtil.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.util;

import java.awt.event.KeyEvent;
import com.threerings.util.KeyTranslatorImpl;
import com.threerings.puzzle.client.PuzzleController;

/**
 * Puzzle game related utilities.
 */
public class PuzzleGameUtil
{
    /**
     * Returns a key translator configured with basic puzzle game
     * mappings.
     */
    public static KeyTranslatorImpl getKeyTranslator ()
    {
        KeyTranslatorImpl xlate = new KeyTranslatorImpl();

        // add the standard pause keys
        xlate.addPressCommand(
            KeyEvent.VK_P, PuzzleController.TOGGLE_CHATTING);

        return xlate;
    }
}
