
// $Id: ObjectEditorDialog.java 16959 2004-08-30 22:09:53Z ray $

package com.threerings.stage.tools.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.util.StringUtil;

import com.threerings.media.image.ColorPository.ColorRecord;
import com.threerings.media.image.ColorPository;

import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.RecolorableTileSet;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.client.SceneObject;

import com.threerings.stage.tools.editor.util.EditorContext;
import com.threerings.stage.tools.editor.util.EditorDialogUtil;

/**
 * Used to edit object attributes.
 */
public class ObjectEditorDialog extends JInternalFrame
    implements ActionListener
{
    public ObjectEditorDialog (EditorContext ctx, EditorScenePanel panel)
    {
        super("Edit object attributes", true);
        _ctx = ctx;
        _panel = panel;

	// get a handle on the top-level panel
	JPanel top = (JPanel)getContentPane();

	// set up a layout manager for the panel
	VGroupLayout gl = new VGroupLayout(
            VGroupLayout.STRETCH, VGroupLayout.STRETCH, 5, VGroupLayout.CENTER);
	top.setLayout(gl);
	top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // object action editor elements
	JPanel sub = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
	sub.add(new JLabel("Object action command:"), HGroupLayout.FIXED);
	sub.add(_action = new JTextField());
        _action.addActionListener(this);
        _action.setActionCommand("ok");
	top.add(sub);

        // create the priority slider
        sub = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        sub.add(new JLabel("Render priority:"), HGroupLayout.FIXED);
        sub.add(_priority = new JSlider(-5, 5));
        _priority.setMajorTickSpacing(5);
        _priority.setMinorTickSpacing(1);
        _priority.setPaintTicks(true);
        top.add(sub);

        // create colorization selectors
        JPanel zations = HGroupLayout.makeButtonBox(HGroupLayout.LEFT);
        zations.add(new JLabel("Colorizations:"));
        zations.add(_primary = new JComboBox(NO_CHOICES));
        zations.add(_secondary = new JComboBox(NO_CHOICES));
        top.add(zations);

	// create our OK/Cancel buttons
	sub = HGroupLayout.makeButtonBox(HGroupLayout.CENTER);
	EditorDialogUtil.addButton(this, sub, "OK", "ok");
	EditorDialogUtil.addButton(this, sub, "Cancel", "cancel");
	top.add(sub);

	pack();
    }

    /**
     * Prepare the dialog for display.  This method should be called
     * before <code>display()</code> is called.
     *
     * @param scobj the object to edit.
     */
    public void prepare (SceneObject scobj)
    {
	_scobj = scobj;

        // set our title to the name of the tileset and the tile index
        String title;
        int tsid = TileUtil.getTileSetId(scobj.info.tileId);
        int tidx = TileUtil.getTileIndex(scobj.info.tileId);
        TileSet tset = null;
        try {
            tset = _ctx.getTileManager().getTileSet(tsid);
            title = tset.getName() + ": " + tidx;
        } catch (NoSuchTileSetException nstse) {
            title = "Error(" + tsid + "): " + tidx;
        }
        title += " (" + StringUtil.coordsToString(
            _scobj.info.x, _scobj.info.y) + ")";
        setTitle(title);

        // configure our elements
        String atext = (scobj.info.action == null ? "" : scobj.info.action);
        _action.setText(atext);
        _priority.setValue(scobj.getPriority());

        // if the object supports colorizations, configure those
        boolean haveZations = false;
        Object[] pzations = null;
        Object[] szations = null;
        if (tset != null) {
            String[] zations = null;
            if (tset instanceof RecolorableTileSet) {
                zations = ((RecolorableTileSet)tset).getColorizations();
            }
            if (zations != null) {
                pzations = computeZations(zations, 0);
                szations = computeZations(zations, 1);
            }
        }
        configureZations(_primary, pzations, _scobj.info.getPrimaryZation());
        configureZations(_secondary, szations,
                         _scobj.info.getSecondaryZation());

	// select the text edit field and focus it
	_action.setCaretPosition(0);
	_action.moveCaretPosition(atext.length());
	_action.requestFocusInWindow();
    }

    protected Object[] computeZations (String[] zations, int index)
    {
        if (zations.length <= index || StringUtil.blank(zations[index])) {
            return null;
        }
        ColorPository cpos = _ctx.getColorPository();
        ColorRecord[] crecs = cpos.enumerateColors(zations[index]);
        if (crecs == null) {
            return null;
        }
        Object[] czations = new Object[crecs.length+1];
        czations[0] = new ZationChoice(0, "none");
        for (int ii = 0; ii < crecs.length; ii++) {
            czations[ii+1] =
                new ZationChoice(crecs[ii].colorId, crecs[ii].name);
        }
        Arrays.sort(czations);
        return czations;
    }

    protected void configureZations (
        JComboBox combo, Object[] zations, int colorId)
    {
        int selidx = 0;
        combo.setEnabled(zations != null);
        if (zations != null) {
            combo.removeAllItems();
            for (int ii = 0; ii < zations.length; ii++) {
                combo.addItem(zations[ii]);
                if (((ZationChoice)zations[ii]).colorId == colorId) {
                    selidx = ii;
                }
            }
        }
        combo.setSelectedIndex(selidx);
    }

    // documentation inherited from interface
    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();

        if (cmd.equals("ok")) {
            _scobj.info.action = _action.getText();
            byte prio = (byte)_priority.getValue();
            if (prio != _scobj.getPriority()) {
                _scobj.setPriority(prio);
            }

            int ozations = _scobj.info.zations;
            ZationChoice pchoice = (ZationChoice)_primary.getSelectedItem();
            ZationChoice schoice = (ZationChoice)_secondary.getSelectedItem();
            _scobj.info.setZations(pchoice.colorId, schoice.colorId);
            if (ozations != _scobj.info.zations) {
                _scobj.refreshObjectTile(_panel);
            }

            _panel.objectEditorDismissed();

        } else if (cmd.equals("cancel")) {
            // do nothing except hide the dialog
            _panel.objectEditorDismissed();

        } else {
            System.err.println("Received unknown action: " + e);
            return;
        }

        // hide the dialog
        EditorDialogUtil.dismiss(this);
    }

    /** Used to display colorization choices. */
    protected static class ZationChoice
        implements Comparable
    {
        public short colorId;
        public String name;

        public ZationChoice (int colorId, String name) {
            this.colorId = (short)colorId;
            this.name = name;
        }

        public int compareTo (Object other) {
            return colorId - ((ZationChoice)other).colorId;
        }

        public String toString () {
            return name;
        }
    }

    protected EditorContext _ctx;
    protected EditorScenePanel _panel;
    protected JTextField _action;
    protected JSlider _priority;
    protected SceneObject _scobj;
    protected JComboBox _primary, _secondary;

    protected static final ZationChoice[] NO_CHOICES = new ZationChoice[] {
        new ZationChoice(0, "none") };
}
