//
// $Id: PreferencesDialog.java 9938 2003-06-20 03:55:58Z mdb $

package com.threerings.stage.tools.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.stage.tools.editor.util.EditorDialogUtil;

/**
 * A dialog for editing preferences.
 */
public class PreferencesDialog extends JInternalFrame
    implements ActionListener
{
    /**
     * Creates a preferences dialog.
     */
    public PreferencesDialog ()
    {
        super("Editor Preferences", true);

        // set up a layout manager for the panel
        JPanel top = (JPanel)getContentPane();
        GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        top.setLayout(gl);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create the display for the test tile directory pref
        JPanel sub = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
        sub.add(new JLabel("Directory to search for test tiles:"),
                GroupLayout.FIXED);
        EditorDialogUtil.addButton(this, sub,
            EditorConfig.getTestTileDirectory(), "testtiledir");
        top.add(sub);

        sub = new JPanel(new HGroupLayout());
        EditorDialogUtil.addButton(this, sub, "OK", "ok");
        top.add(sub);

        pack();
    }

    /**
     * Handle action events.
     */
    public void actionPerformed (ActionEvent e)
    {
        String cmd = e.getActionCommand();

        if (cmd.equals("testtiledir")) {
            changeTestTileDir((JButton) e.getSource());

        } else if (cmd.equals("ok")) {
            EditorDialogUtil.dispose(this);

        } else {
            Log.warning("Unknown action command [cmd=" + cmd + "].");
        }
    }

    /**
     * Pop up a file selection box for specifying the directory to look
     * in for test tiles.
     */
    protected void changeTestTileDir (JButton button)
    {
        JFileChooser chooser;

        // figure out which 
        File f = new File(button.getText());
        if (!f.exists()) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser(f);
        }

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(f);

        int result = chooser.showDialog(this, "Select");
        if (JFileChooser.APPROVE_OPTION == result) {
            f = chooser.getSelectedFile();
            String newdir = f.getPath();
            button.setText(newdir);
            EditorConfig.setTestTileDirectory(newdir);
        }
    }
}
