//
// $Id: KeyboardManagerApp.java,v 1.3 2002/01/22 18:11:33 shaper Exp $

package com.threerings.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;

import com.threerings.util.Log;

public class KeyboardManagerApp
{
    public KeyboardManagerApp ()
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
        KeyboardManagerApp app = new KeyboardManagerApp();
        app.run();
    }

    protected static class TestFrame extends JFrame
        implements ControllerProvider
    {
        public TestFrame ()
        {
            // create the test controller
            _ctrl = new TestController();

            setDefaultCloseOperation(EXIT_ON_CLOSE);

            JPanel top = new JPanel();

            // add some sample key mappings
            KeyTranslatorImpl xlate = new KeyTranslatorImpl();
            xlate.addPressCommand(KeyEvent.VK_LEFT, TestController.MOVE_LEFT);
            xlate.addPressCommand(
                KeyEvent.VK_RIGHT, TestController.MOVE_RIGHT);
            xlate.addPressCommand(KeyEvent.VK_SPACE, TestController.DROP);

            // create the keyboard manager
            KeyboardManager keymgr = new KeyboardManager(top, xlate);

            getContentPane().add(top);
        }

        // documentation inherited
        public Controller getController ()
        {
            return _ctrl;
        }

        /** The test controller. */
        protected Controller _ctrl;
    }

    protected static class TestController extends Controller
    {
        public static final String MOVE_LEFT = "move_left";
        public static final String MOVE_RIGHT = "move_right";
        public static final String DROP = "drop";

        // documentation inherited
        public boolean handleAction (ActionEvent action)
        {
            String cmd = action.getActionCommand();
            Log.info("handleAction [cmd=" + cmd + "].");
            return true;
        }
    }

    /** The test frame. */
    protected JFrame _frame;
}
