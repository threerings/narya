//
// $Id: ViewerFrame.java 20143 2005-03-30 01:12:48Z mdb $

package com.threerings.stage.tools.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import com.samskivert.swing.util.MenuUtil;

import com.threerings.cast.CharacterManager;
import com.threerings.media.ManagedJFrame;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotSceneModel;

import com.threerings.stage.Log;
import com.threerings.stage.data.StageScene;
import com.threerings.stage.data.StageSceneModel;
import com.threerings.stage.tools.xml.StageSceneParser;
import com.threerings.stage.util.StageContext;

/**
 * The viewer frame is the main application window.
 */
public class ViewerFrame extends ManagedJFrame
{
    /**
     * Creates a frame in which the viewer application can operate.
     */
    public ViewerFrame (GraphicsConfiguration gc)
    {
	super(gc);

        // set up the frame options
        setTitle("Scene Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set the frame and content panel background to black
        setBackground(Color.black);
        getContentPane().setBackground(Color.black);

        // create the "File" menu
        KeyStroke accel = null;
        JMenu menuSettings = new JMenu("File");

        // open...
        accel = KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuSettings, "Open scene...", KeyEvent.VK_O,
                             accel, this, "openScene");

        // decoys!
        MenuUtil.addMenuItem(menuSettings, "Decoys!", this, "getJiggy");

        // quit
        menuSettings.addSeparator();
        accel = KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuSettings, "Quit", KeyEvent.VK_Q,
                             accel, this, "handleQuit");

        // create the menu bar
        JMenuBar bar = new JMenuBar();
        bar.add(menuSettings);

        // add the menu bar to the frame
        setJMenuBar(bar);
    }

    /**
     * Instructs us to create our scene panel.
     */
    public void init (StageContext ctx, CharacterManager charmgr)
    {
        _panel = new ViewerScenePanel(ctx, charmgr);
	getContentPane().add(_panel, BorderLayout.CENTER);
    }

    /**
     * Callback for opening a new scene.
     */
    public void openScene (ActionEvent event)
    {
//         String where = ToolPrefs.config.getValue(
//             "viewer.last_dir", System.getProperty("user.dir"));
        String where = System.getProperty("user.dir");
        JFileChooser chooser = new JFileChooser(where);
        chooser.setFileFilter(new FileFilter () {
            public boolean accept (File f) {
                return (f.isDirectory() || f.getName().endsWith(".xml"));
            }
            public String getDescription () {
                return "XML Files";
            }
        });
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File filescene = chooser.getSelectedFile();
            loadScene(filescene.getPath());
//             ToolPrefs.config.setValue("viewer.last_dir", filescene.getParent());
        }
    }

    public void getJiggy (ActionEvent event)
    {
        _panel.createDecoys();
    }

    /**
     * Handles a request to get the fuck out of dodge.
     */
    public void handleQuit (ActionEvent evt)
    {
        System.exit(0);
    }

    public void loadScene (String path)
    {
        String errmsg = null;

        try {
            StageSceneParser parser = new StageSceneParser();
            StageSceneModel model = (StageSceneModel)parser.parseScene(path);
            if (model == null) {
                errmsg = "No scene found in scene file '" + path + "'.";

            } else {
                SpotSceneModel ssmodel = SpotSceneModel.getSceneModel(model);
                Location defloc = null;
                // find the default entrance to this scene
                for (int ii = 0; ii < ssmodel.portals.length; ii++) {
                    Portal port = ssmodel.portals[ii];
                    if (port.portalId == ssmodel.defaultEntranceId) {
                        defloc = port.getOppLocation();
                        break;
                    }
                }
                if (defloc == null) {
                    Log.warning("Scene has no def. entrance '" + path + "'.");
                }

                _panel.setScene(new StageScene(model, null), defloc);
            }

        } catch (Exception e) {
            errmsg = "Error parsing scene file '" + path + "'.";
            Log.logStackTrace(e);
        }

        if (errmsg != null) {
            JOptionPane.showMessageDialog(
                this, errmsg, "Load error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected ViewerScenePanel _panel;
}
