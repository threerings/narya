//
// $Id: KeyboardManager.java,v 1.11 2002/10/09 08:17:02 mdb Exp $

package com.threerings.util;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;

import com.samskivert.swing.Controller;
import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;

/**
 * The keyboard manager observes keyboard actions on a particular
 * component and posts commands associated with the key presses to the
 * {@link Controller} hierarchy.  It allows specifying the key repeat
 * rate, and will begin repeating a key immediately after it is held down
 * rather than depending on the system-specific key repeat delay/rate.
 */
public class KeyboardManager
{
    /**
     * Constructs a keyboard manager.
     *
     * @param target the component whose keyboard events are to be observed.
     * @param xlate the key translator used to map keyboard events to
     * controller action commands.
     */
    public KeyboardManager (JComponent target, KeyTranslator xlate)
    {
        // save off references
        _target = target;
        _xlate = xlate;

        // listen to ancestor events so that we can cease our business if
        // we lose the focus
        target.addAncestorListener(new AncestorAdapter() {
            public void ancestorAdded (AncestorEvent e) {
                KeyboardManager.this.gainedFocus();
                if (_wadapter == null) {
                    addWindowListener();
                }
            }
            public void ancestorRemoved (AncestorEvent e) {
                KeyboardManager.this.lostFocus();
            }
        });

        // capture low-level keyboard events via the keyboard focus manager
        KeyboardFocusManager keymgr =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keymgr.addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent (KeyEvent e) {
                return KeyboardManager.this.dispatchKeyEvent(e);
            }
        });
    }

    /**
     * Sets whether the keyboard manager accepts keyboard input.
     */
    public void setEnabled (boolean enabled)
    {
        // release all keys if we were enabled and are soon to not be
        if (!enabled && _enabled) {
            releaseAllKeys();
        }
        _enabled = enabled;
    }

    /**
     * Sets the expected delay in milliseconds between each key
     * press/release event the keyboard manager should expect to receive
     * while a key is repeating.
     */
    public void setRepeatDelay (long delay)
    {
        _repeatDelay = delay;
    }

    /**
     * Sets the delay in milliseconds between each repeat key action
     * command posted by the keyboard manager while a key is down.
     */
    public void setPressDelay (long delay)
    {
        _pressDelay = delay;
    }

    /**
     * Releases all keys and ceases any hot repeating action that may be
     * going on.
     */
    public void releaseAllKeys ()
    {
        Iterator iter = _keys.elements();
        while (iter.hasNext()) {
            ((KeyInfo)iter.next()).release();
        }
    }

    /**
     * Called when the {@link KeyboardFocusManager} has a key event for us
     * to scrutinize.  Returns whether we've handled the key event.
     */
    protected boolean dispatchKeyEvent (KeyEvent e)
    {
        // bail if we're not enabled, we haven't the focus, or we're not
        // showing on-screen
        if (!_enabled || !_focus || !_target.isShowing()) {
            return false;
        }

        // handle key press and release events
        switch (e.getID()) {
        case KeyEvent.KEY_PRESSED:
            KeyboardManager.this.keyPressed(e);
            return true;

        case KeyEvent.KEY_RELEASED:
            KeyboardManager.this.keyReleased(e);
            return true;

        case KeyEvent.KEY_TYPED:
            // silently absorb key typed events
            return true;

        default:
            return false;
        }
    }

    /**
     * Called when the keyboard manager gains focus and should begin
     * handling keys again if it was previously enabled.
     */
    protected void gainedFocus ()
    {
        _focus = true;
    }

    /**
     * Called when the keyboard manager loses focus and should cease
     * handling keys.
     */
    protected void lostFocus ()
    {
        releaseAllKeys();
        _focus = false;
    }

    /**
     * Adds a window focus event listener to the target component's window
     * so that the keyboard manager can enabled and disable handling of
     * keyboard events appropriately.
     */
    protected void addWindowListener ()
    {
        _wadapter = new WindowAdapter() {
            public void windowGainedFocus (WindowEvent e) {
                KeyboardManager.this.gainedFocus();
            }
            public void windowLostFocus (WindowEvent e) {
                KeyboardManager.this.lostFocus();
            }
        };

        Window window = SwingUtilities.getWindowAncestor(_target);
        window.addWindowFocusListener(_wadapter);
    }

    // documentation inherited
    protected void keyPressed (KeyEvent e)
    {
        logKey("keyPressed", e);

        // get the action command associated with this key
        int keyCode = e.getKeyCode();
        if (_xlate.hasCommand(keyCode)) {
            // get the info object for this key, creating one if necessary
            KeyInfo info = (KeyInfo)_keys.get(keyCode);
            if (info == null) {
                info = new KeyInfo(keyCode);
                _keys.put(keyCode, info);
            }

            // remember the last time this key was pressed
            info.setPressTime(System.currentTimeMillis());
        }
    }

    // documentation inherited
    protected void keyReleased (KeyEvent e)
    {
        logKey("keyReleased", e);

        // get the info object for this key
        KeyInfo info = (KeyInfo)_keys.get(e.getKeyCode());
        if (info != null) {
            // remember the last time we received a key release
            info.setReleaseTime(System.currentTimeMillis());
        }
    }

    /**
     * Logs the given message and key.
     */
    protected void logKey (String msg, KeyEvent e)
    {
        if (DEBUG_EVENTS) {
            int keyCode = e.getKeyCode();
            Log.info(msg + " [key=" + KeyEvent.getKeyText(keyCode) + "].");
        }
    }

    protected class KeyInfo implements Interval
    {
        /**
         * Constructs a key info object for the given key code.
         */
        public KeyInfo (int keyCode)
        {
            _keyCode = keyCode;
            _keyText = KeyEvent.getKeyText(_keyCode);
            _pressCommand = _xlate.getPressCommand(_keyCode);
            _releaseCommand = _xlate.getReleaseCommand(_keyCode);
        }

        /**
         * Sets the last time the key was pressed.
         */
        public synchronized void setPressTime (long time)
        {
            _lastPress = time;
            _lastRelease = time;

//             if (_iid == -1) {
//                 // register an interval to post the command associated
//                 // with the key press until the key is decidedly released
//                 _iid = IntervalManager.register(this, _pressDelay, null, true);
//                 if (DEBUG_EVENTS) {
//                     Log.info("Pressing key [key=" + _keyText + "].");
//                 }

                if (_pressCommand != null) {
                    // post the initial key press command
                    Controller.postAction(_target, _pressCommand);
                }
//             }
        }

        /**
         * Sets the last time the key was released.
         */
        public synchronized void setReleaseTime (long time)
        {
            release();
//             _lastRelease = time;

//             // handle key release events received so quickly after the key
//             // press event that the press/release times are exactly equal
//             // and, in intervalExpired(), we would therefore be unable to
//             // distinguish between the key being initially pressed and the
//             // actual true key release that's taken place.

//             // the only case I can think of that might result in this
//             // happening is if the event manager class queues up a key
//             // press and release event succession while other code is
//             // executing, and when it comes time for it to dispatch the
//             // events in its queue it manages to dispatch both of them to
//             // us really-lickety-split.  one would still think at least a
//             // few milliseconds should pass between the press and release,
//             // but in any case, we arguably ought to be watching for and
//             // handling this case for posterity even though it would seem
//             // unlikely or impossible, and so, now we do, which is a good
//             // thing since it appears this does in fact happen, and not so
//             // infrequently.
//             if (_lastPress == _lastRelease) {
//                 if (DEBUG_EVENTS) {
//                     Log.warning("Insta-releasing key due to equal key " +
//                                 "press/release times [key=" + _keyText + "].");
//                 }
//                 release();
//             }
        }

        /**
         * Releases the key if pressed and cancels any active key repeat
         * interval.
         */
        public synchronized void release ()
        {
            // bail if we're not currently pressed
//             if (_iid == -1) {
//                 return;
//             }

            if (DEBUG_EVENTS) {
                Log.info("Releasing key [key=" + _keyText + "].");
            }

            // remove the repeat interval
            if (_iid != -1) {
                IntervalManager.remove(_iid);
                _iid = -1;
            }

            if (_siid != -1) {
                // remove the sub-interval
                IntervalManager.remove(_siid);
                _siid = -1;
            }

            if (_releaseCommand != null) {
                // post the key release command
                Controller.postAction(_target, _releaseCommand);
            }
        }

        // documentation inherited
        public synchronized void intervalExpired (int id, Object arg)
        {
            long now = System.currentTimeMillis();
            long deltaPress = now - _lastPress;
            long deltaRelease = now - _lastRelease;

            if (KeyboardManager.DEBUG_INTERVAL) {
                Log.info("Interval [id=" + id + ", key=" + _keyText +
                         ", deltaPress=" + deltaPress +
                         ", deltaRelease=" + deltaRelease + "].");
            }

            if (id == _iid) {
                // handle a normal interval where we either (a) create a
                // sub-interval if we can't yet determine definitively
                // whether the key is still down, (b) cease repeating if
                // we're certain the key is now up, or (c) repeat the key
                // command if we're certain the key is still down
                if (_lastRelease != _lastPress) {
                    if (deltaRelease < _repeatDelay) {
                        // register a one-shot sub-interval to
                        // definitively check whether the key was released
                        long delay = _repeatDelay - deltaRelease;
                        _siid = IntervalManager.register(
                            this, delay, new Long(_lastPress), false);
                        if (KeyboardManager.DEBUG_INTERVAL) {
                            Log.info("Registered sub-interval " +
                                     "[id=" + _siid + "].");
                        }

                    } else {
                        // we know the key was released, so cease repeating
                        release();
                    }

                } else if (_pressCommand != null) {
                    // post the key press command again
                    Controller.postAction(_target, _pressCommand);
                }

            } else if (id == _siid) {
                // handle the sub-interval that checks whether the key has
                // really been released since the normal interval expired
                // at an inopportune time for a definitive check

                // clear out the non-recurring sub-interval identifier
                _siid = -1;

                // make sure the key hasn't been pressed again since the
                // sub-interval was registered
                if (_lastPress != ((Long)arg).longValue()) {
                    if (KeyboardManager.DEBUG_INTERVAL) {
                        Log.warning("Key pressed since sub-interval was " +
                                    "registered, aborting release check " +
                                    "[key=" + _keyText + "].");
                    }
                    return;
                }

                // provide the last word on whether the key was released
                if ((_lastRelease != _lastPress) &&
                    deltaRelease >= _repeatDelay) {
                    release();

                } else if (_pressCommand != null) {
                    // post the key command again
                    Controller.postAction(_target, _pressCommand);
                }
            }
        }

        /**
         * Returns a string representation of the key info object.
         */
        public String toString ()
        {
            return "[key=" + _keyText + "]";
        }

        /** The unique interval identifier for the sub-interval used to
         * handle the case where the main interval wakes up to repeat the
         * currently pressed key and the last key release event was
         * received more recently than the expected repeat delay. */
        protected int _siid = -1;

        /** The unique interval identifier for the key repeat interval. */
        protected int _iid = -1;

        /** The last time a key released event was received for this key. */
        protected long _lastRelease;

        /** The last time a key pressed event was received for this key. */
        protected long _lastPress;

        /** The press action command associated with this key. */
        protected String _pressCommand;

        /** The release action command associated with this key. */
        protected String _releaseCommand;

        /** A text representation of this key. */
        protected String _keyText;

        /** The key code associated with this key info object. */
        protected int _keyCode;
    }

    /** Whether to output debugging info for individual key events. */
    protected static final boolean DEBUG_EVENTS = false;

    /** Whether to output debugging info for interval callbacks. */
    protected static final boolean DEBUG_INTERVAL = false;

    /** The default repeat delay. */
    protected static final long DEFAULT_REPEAT_DELAY = 50L;

    /** The default key press delay. */
    protected static final long DEFAULT_PRESS_DELAY = 150L;

    /** The expected approximate milliseconds between each key
     * release/press event while the key is being auto-repeated. */
    protected long _repeatDelay = DEFAULT_REPEAT_DELAY;

    /** The milliseconds to sleep between sending repeat key commands. */
    protected long _pressDelay = DEFAULT_PRESS_DELAY;

    /** A hashtable mapping key codes to {@link KeyInfo} objects. */
    protected HashIntMap _keys = new HashIntMap();

    /** Whether the keyboard manager currently has the keyboard focus. */
    protected boolean _focus = false;

    /** Whether the keyboard manager is accepting keyboard input. */
    protected boolean _enabled = true;

    /** The window focus event listener. */
    protected WindowAdapter _wadapter;

    /** The component that receives keyboard events and that we associate
     * with posted controller commands. */
    protected JComponent _target;

    /** The translator that maps keyboard events to controller commands. */
    protected KeyTranslator _xlate;
}
