//
// $Id: PortalDialog.java 9371 2003-06-02 20:28:21Z mdb $

package com.threerings.stage.tools.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.samskivert.swing.*;

import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.tools.EditablePortal;

import com.threerings.stage.data.StageScene;
import com.threerings.stage.tools.editor.util.EditorDialogUtil;

/**
 * The <code>PortalDialog</code> is used to present the user with a dialog
 * allowing them to enter the information associated with an
 * <code>EditablePortal</code>.  The dialog is used both to set up a new
 * portal and to edit an existing portal.
 */
public class PortalDialog extends JInternalFrame
    implements ActionListener
{
    /**
     * Constructs the portal dialog.
     */
    public PortalDialog ()
    {
	super("Edit Portal", true);

	// get a handle on the top-level panel
	JPanel top = (JPanel)getContentPane();

	// set up a layout manager for the panel
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	top.setLayout(gl);
	top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	// add the dialog instruction text
	top.add(new JLabel("Enter settings for this portal:"));

	// create a panel to contain the portal name info
	JPanel sub = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
	sub.add(new JLabel("Portal name:", SwingConstants.RIGHT));

	// create and add the portal name text entry field
	sub.add(_portalText = new JTextField());

	// add the portal name info to the top-level panel
	top.add(sub);

        // create a check box to allow making this the default
        // entrance portal
        _entrance = new JCheckBox("Default Entrance");
        _entrance.addActionListener(this);
        _entrance.setActionCommand("entrance");
        top.add(_entrance);

	// create a panel to contain the OK/Cancel buttons
	sub = new JPanel(new HGroupLayout());
	EditorDialogUtil.addButton(this, sub, "OK", "ok");

	// add the buttons to the top-level panel
	top.add(sub);

	pack();
    }

    /**
     * Prepare the dialog for display.  This method should be called
     * before <code>display()</code> is called.
     *
     * @param port the portal to edit.
     */
    public void prepare (StageScene scene, EditablePortal port)
    {
	_port = port;
        _scene = scene;

	// if the location is already a portal, fill the text entry field
	// with the current scene name, else clear it
	String text = port.name;
	_portalText.setText(text);

	// select the text edit field
	_portalText.setCaretPosition(0);
	_portalText.moveCaretPosition(text.length());

        // select the default entrance check box appropriately
        Portal entry = _scene.getDefaultEntrance();
        _entrance.setSelected(entry == null ||
                              entry.portalId == _port.portalId);

	// request the keyboard focus so that the destination scene
	// name can be typed immediately
	_portalText.requestFocusInWindow();
    }

    /**
     * Handle action events on the dialog user interface elements.
     */
    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();

        if (cmd.equals("entrance")) {
            _entrance.setSelected(_entrance.isSelected());

	} else if (cmd.equals("ok")) {
	    handleSubmit();

	} else {
	    Log.warning("Unknown action command [cmd=" + cmd + "].");
	}
    }

    /**
     * Handles the user submitting the dialog via the "OK" button.
     */
    protected void handleSubmit ()
    {
	// get the destination scene name
        _port.name = _portalText.getText();

        // update the scene's default entrance
        if (_entrance.isSelected()) {
            _scene.setDefaultEntrance(_port);

        } else if (_scene.getDefaultEntrance() == _port) {
            _scene.setDefaultEntrance(null);
        }

        // hide the dialog
        EditorDialogUtil.dismiss(this);
    }

    // documentation inherited
    protected void processKeyEvent (KeyEvent e)
    {
	switch (e.getKeyCode()) {
	case KeyEvent.VK_ENTER: handleSubmit(); break;
	case KeyEvent.VK_ESCAPE: setVisible(false); break;
	}
    }

    /** The scene. */
    protected StageScene _scene;

    /** The portal name text entry field. */
    protected JTextField _portalText;

    /** The portal default entrance check box. */
    protected JCheckBox _entrance;

    /** The location object denoting the portal location. */
    protected EditablePortal _port;

    /** The combo box listing the direction orientations. */
    protected JComboBox _orientcombo;
}
