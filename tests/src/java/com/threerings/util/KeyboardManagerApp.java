//
// $Id: KeyboardManagerApp.java,v 1.6 2004/08/27 02:21:05 mdb Exp $
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
            KeyboardManager keymgr = new KeyboardManager();
            keymgr.setTarget(top, xlate);
            keymgr.setEnabled(true);

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
