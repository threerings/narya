//
// $Id: EditorDialogUtil.java 9223 2003-05-27 02:35:00Z mdb $

package com.threerings.stage.tools.editor.util;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

import com.threerings.util.DirectionUtil;

public class EditorDialogUtil
{
    /**
     * Add a button to a container with the given parameters and
     * action listener.
     *
     * @param l the listener.
     * @param container the container.
     * @param name the button name.
     * @param cmd the action command.
     */
    public static void addButton (ActionListener l, Container container,
				  String name, String cmd)
    {
	JButton button = new JButton(name);
	button.addActionListener(l);
	button.setActionCommand(cmd);
	container.add(button);
    }

    /**
     * Create and return a combo box seeded with the various possible
     * orientation direction names.
     *
     * @param l the listener.
     *
     * @return the combo box.
     */
    public static JComboBox getOrientationComboBox (ActionListener l)
    {
	JComboBox box = new JComboBox(DirectionUtil.getDirectionNames());
	box.addActionListener(l);
	box.setActionCommand("orient");
	return box;
    }

    /**
     * Centers the supplied dialog in its parent's bounds.
     */
    public static void center (JFrame parent, JInternalFrame dialog)
    {
        Dimension psize = parent.getSize();
        Dimension dsize = dialog.getSize();
        dialog.setLocation((psize.width-dsize.width)/2,
                           (psize.height-dsize.height)/2);
    }

    /**
     * Display a dialog centered within the given frame.
     *
     * @param parent the parent frame.
     * @param dialog the dialog.
     */
    public static void display (JFrame parent, JInternalFrame dialog)
    {
        center(parent, dialog);
        parent.getLayeredPane().add(dialog, JLayeredPane.POPUP_LAYER);
	dialog.setVisible(true);
    }	

    /**
     * Removes the supplied dialog from its parent container, but does not
     * dispose it.
     */
    public static void dismiss (JInternalFrame dialog)
    {
        Container parent = dialog.getParent();
        if (parent != null) {
            parent.remove(dialog);
            parent.repaint();
        }
        dialog.setVisible(false);
    }

    /**
     * Handles safely dismissing and disposing of the supplied dialog.
     */
    public static void dispose (JInternalFrame dialog)
    {
        dismiss(dialog);
        dialog.dispose();
    }
}
