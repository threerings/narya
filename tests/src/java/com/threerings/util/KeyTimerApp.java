//
// $Id: KeyTimerApp.java,v 1.1 2002/01/12 02:31:16 shaper Exp $

package com.threerings.yohoho.puzzle.util;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.threerings.yohoho.Log;

public class KeyTimerApp
{
    public KeyTimerApp ()
    {
        _frame = new TestFrame();
        _frame.setSize(400, 300);
    }

    public void run ()
    {
        _frame.show();
    }

    public static void main (String[] args)
    {
        KeyTimerApp app = new KeyTimerApp();
        app.run();
    }

    protected class TestFrame extends Frame implements KeyListener
    {
        public TestFrame ()
        {
            addKeyListener(this);
            _prStart = _rpStart = -1;
        }

        public void keyPressed (KeyEvent e)
        {
            long now = System.currentTimeMillis();
            _prStart = now;

            if (_rpStart != -1) {
                Log.info("RP\t" + (now - _rpStart));
            }

            logKey("keyPressed", e);
        }

        public void keyReleased (KeyEvent e)
        {
            long now = System.currentTimeMillis();
            _rpStart = now;

            Log.info("PR\t" + (now - _prStart));

            logKey("keyReleased", e);
        }

        public void keyTyped (KeyEvent e)
        {
            logKey("keyTyped", e);
        }

        /**
         * Logs the given message and key.
         */
        protected void logKey (String msg, KeyEvent e)
        {
            int keyCode = e.getKeyCode();
            Log.info(msg + " [key=" + KeyEvent.getKeyText(keyCode) + "].");
        }

        protected long _prStart, _rpStart;
    }

    protected Frame _frame;
}
