//
// $Id: EditorToolBarPanel.java 17780 2004-11-10 23:00:07Z ray $

package com.threerings.stage.tools.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import com.samskivert.swing.*;

import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileIcon;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.UniformTileSet;

public class EditorToolBarPanel extends JPanel implements ActionListener
{
    public EditorToolBarPanel (TileManager tilemgr, EditorModel model)
    {
	_model = model;

	// use of flowlayout positions the toolbar and floats properly
	setLayout(new FlowLayout(FlowLayout.LEFT));

        // get our toolbar icons
        UniformTileSet tbset = tilemgr.loadTileSet(ICONS_PATH, 40, 40);

	// create the toolbar
	JToolBar toolbar = new JToolBar();

	// add all of the toolbar buttons
	_buttons = new ArrayList();
	for (int ii = 0; ii < EditorModel.NUM_ACTIONS; ii++) {
	    // get the button icon images
            Tile tile = tbset.getTile(ii);
            if (tile != null) {
                String cmd = EditorModel.CMD_ACTIONS[ii];
                String tip = EditorModel.TIP_ACTIONS[ii];

                // create the button
                JButton b = addButton(toolbar, cmd, tip, new TileIcon(tile));

                // add it to the set of buttons we're managing
                _buttons.add(b);

            } else {
                Log.warning("Unable to load toolbar icon " +
                            "[index=" + ii + "].");
            }
	}

	// default to the first button
        setSelectedButton((JButton)_buttons.get(0));

	// add the toolbar
	add(toolbar);
    }

    protected JButton addButton (JToolBar toolbar, String cmd, String tip,
				 TileIcon icon)
    {
	// create the button and configure accordingly
	JButton button = new JButton(new DimmedIcon(icon));
        button.setSelectedIcon(icon);
	button.addActionListener(this);
	button.setActionCommand("tbar_" + cmd);
	button.setToolTipText(tip);

	// add the button to the toolbar
	toolbar.add(button);

	return button;
    }

    protected void setSelectedButton (JButton button)
    {
	for (int ii = 0; ii < _buttons.size(); ii++) {
	    JButton tb = (JButton)_buttons.get(ii);
	    tb.setSelected(tb == button);
	}
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();

	if (cmd.startsWith("tbar")) {

	    // select the chosen mode in the toolbar
	    setSelectedButton((JButton)e.getSource());

	    // update the active mode in the model, stripping the
	    // "tbar_" prefix from the command string
	    _model.setActionMode(cmd.substring(5));

	} else {
	    Log.warning("Unknown action command [cmd=" + cmd + "].");
	}
    }

    /** The buttons in the tool bar. */
    protected ArrayList _buttons;

    /** The editor data model. */
    protected EditorModel _model;

    protected static final String ICONS_PATH =
        "media/stage/tools/editor/toolbar_icons.png";
}
