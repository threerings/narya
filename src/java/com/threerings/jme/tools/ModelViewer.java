//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Line;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.util.LoggingSystem;

import com.samskivert.swing.GroupLayout;
import com.samskivert.util.Config;

import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;

import com.threerings.jme.JmeCanvasApp;
import com.threerings.jme.model.Model;

/**
 * A simple viewer application that allows users to examine models and their
 * animations by loading them from their uncompiled <code>.properties</code> /
 * <code>.xml</code> representations or their compiled <code>.dat</code>
 * representations.
 */
public class ModelViewer extends JmeCanvasApp
{
    public static void main (String[] args)
    {
        new ModelViewer(args.length > 0 ? args[0] : null);
    }
    
    /**
     * Creates and initializes an instance of the model viewer application.
     *
     * @param path the path of the model to view, or <code>null</code> for
     * none
     */
    public ModelViewer (String path)
    {
        super(1024, 768);
        _msg = new MessageManager("rsrc.i18n").getBundle("jme.viewer");
        _path = path;
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        _frame = new JFrame(_msg.get("m.title"));
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JMenuBar menu = new JMenuBar();
        _frame.setJMenuBar(menu);
        
        JMenu file = new JMenu(_msg.get("m.file_menu"));
        menu.add(file);
        Action load = new AbstractAction(_msg.get("m.file_load")) {
            public void actionPerformed (ActionEvent e) {
                showLoadDialog();
            }
        };
        load.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
        load.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
        file.add(load);
        
        file.addSeparator();
        Action quit = new AbstractAction(_msg.get("m.file_quit")) {
            public void actionPerformed (ActionEvent e) {
                System.exit(0);
            }
        };
        quit.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Q);
        quit.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
        file.add(quit);
        
        _frame.getContentPane().add(getCanvas(), BorderLayout.CENTER);
        
        _controls = GroupLayout.makeVBox(GroupLayout.NONE, GroupLayout.TOP);
        _controls.setPreferredSize(new Dimension(100, 100));
        _frame.getContentPane().add(_controls, BorderLayout.EAST);
        
        _status = new JLabel(" ");
        _status.setHorizontalAlignment(JLabel.LEFT);
        _frame.getContentPane().add(_status, BorderLayout.SOUTH);
        
        _frame.pack();
        _frame.setVisible(true);
        
        LoggingSystem.getLoggingSystem().setLevel(Level.WARNING);
        
        run();
    }
    
    @Override // documentation inherited
    public boolean init ()
    {
        if (!super.init()) {
            return false;
        }
        if (_path != null) {
            loadModel(new File(_path));
        }
        return true;
    }
    
    @Override // documentation inherited
    protected void initInput ()
    {
        super.initInput();
        
        _camhand.setTiltLimits(FastMath.PI / 16.0f, FastMath.PI * 7.0f / 16.0f);
        _camhand.setZoomLimits(1f, 100f);
        _camhand.tiltCamera(-FastMath.PI * 7.0f / 16.0f);
        
        MouseOrbiter orbiter = new MouseOrbiter();
        _canvas.addMouseListener(orbiter);
        _canvas.addMouseMotionListener(orbiter);
        _canvas.addMouseWheelListener(orbiter);
    }
    
    @Override // documentation inherited
    protected void initRoot ()
    {
        super.initRoot();
        
        // set a default material
        MaterialState mstate = _ctx.getRenderer().createMaterialState();
        mstate.getDiffuse().set(ColorRGBA.white);
        mstate.getAmbient().set(ColorRGBA.white);
        _ctx.getGeometry().setRenderState(mstate);
        
        // create a grid on the XY plane to provide some reference
        Vector3f[] points = new Vector3f[GRID_SIZE*2 + GRID_SIZE*2];
        float halfLength = (GRID_SIZE - 1) * GRID_SPACING / 2;
        int idx = 0;
        for (int xx = 0; xx < GRID_SIZE; xx++) {
            points[idx++] = new Vector3f(
                -halfLength + xx*GRID_SPACING, -halfLength, 0f);
            points[idx++] = new Vector3f(
                -halfLength + xx*GRID_SPACING, +halfLength, 0f);
        }
        for (int yy = 0; yy < GRID_SIZE; yy++) {
            points[idx++] = new Vector3f(
                -halfLength, -halfLength + yy*GRID_SPACING, 0f);
            points[idx++] = new Vector3f(
                +halfLength, -halfLength + yy*GRID_SPACING, 0f);
            
        }
        Line grid = new Line("grid", points, null, null, null);
        grid.getDefaultColor().set(0.25f, 0.25f, 0.25f, 1f);
        grid.setLightCombineMode(LightState.OFF);
        _ctx.getGeometry().attachChild(grid);
        grid.updateRenderState();
    }
    
    @Override // documentation inherited
    protected void initLighting ()
    {
        DirectionalLight dlight = new DirectionalLight();
        dlight.setEnabled(true);
        dlight.getDirection().set(-1f, 0f, -1f).normalizeLocal();
        dlight.getAmbient().set(0.25f, 0.25f, 0.25f, 1f);
        
        LightState lstate = _ctx.getRenderer().createLightState();
        lstate.attach(dlight);
        _ctx.getGeometry().setRenderState(lstate);
        _ctx.getGeometry().setLightCombineMode(LightState.REPLACE);
    }
    
    /**
     * Shows the load model dialog.
     */
    protected void showLoadDialog ()
    {
        if (_chooser == null) {
            _chooser = new JFileChooser();
            _chooser.setDialogTitle(_msg.get("m.load_title"));
            _chooser.setFileFilter(new FileFilter() {
                public boolean accept (File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    String path = file.toString();
                    return path.endsWith(".properties") ||
                        path.endsWith(".dat");
                }
                public String getDescription () {
                    return _msg.get("m.load_filter");
                }
            });
            File dir = new File(_config.getValue("dir", "."));
            if (dir.exists()) {
                _chooser.setCurrentDirectory(dir);
            }
        }
        if (_chooser.showOpenDialog(_frame) == JFileChooser.APPROVE_OPTION) {
            loadModel(_chooser.getSelectedFile());
        }
        _config.setValue("dir", _chooser.getCurrentDirectory().toString());
    }
    
    /**
     * Attempts to load a model from the specified location.
     */
    protected void loadModel (File file)
    {
        String fpath = file.toString();
        try {
            if (fpath.endsWith(".properties")) {
                compileModel(file);
            } else if (fpath.endsWith(".dat")) {
                loadCompiledModel(file);        
            } else {
                throw new Exception(_msg.get("m.invalid_type"));
            }
            _status.setText(_msg.get("m.loaded_model", fpath));
            
        } catch (Exception e) {
            e.printStackTrace();
            _status.setText(_msg.get("m.load_error", fpath, e));
        }
    }
    
    /**
     * Attempts to compile and load a model.
     */
    protected void compileModel (File file)
        throws Exception
    {
        _status.setText(_msg.get("m.compiling_model", file));
        Model model = CompileModelTask.compileModel(file);
        if (model != null) {
            setModel(model);
            return;
        }
        // if compileModel returned null, the .dat file is up-to-date
        String fpath = file.toString();
        int didx = fpath.lastIndexOf('.');
        fpath = (didx == -1) ? fpath : fpath.substring(0, didx);
        loadCompiledModel(new File(fpath + ".dat"));
    }
    
    /**
     * Attempts to load a model that has already been compiled.
     */
    protected void loadCompiledModel (File file)
        throws IOException
    {
        _status.setText(_msg.get("m.loading_model", file));
        setModel(Model.readFromFile(file, false));
    }
    
    /**
     * Sets the model once it's been loaded.
     */
    protected void setModel (Model model)
    {
        if (_model != null) {
            _ctx.getGeometry().detachChild(_model);
        }
        _ctx.getGeometry().attachChild(_model = model);
        _model.setLocalScale(0.04f);
        _model.updateRenderState();
    }
    
    /** The translation bundle. */
    protected MessageBundle _msg;
    
    /** The path of the initial model to load. */
    protected String _path;
    
    /** The viewer frame. */
    protected JFrame _frame;
    
    /** The control panel. */
    protected JPanel _controls;
    
    /** The status bar. */
    protected JLabel _status;
    
    /** The model file chooser. */
    protected JFileChooser _chooser;
    
    /** The currently loaded model. */
    protected Model _model;
    
    /** Moves the camera using mouse input. */
    protected class MouseOrbiter extends MouseAdapter
        implements MouseMotionListener, MouseWheelListener
    {
        @Override // documentation inherited
        public void mousePressed (MouseEvent e)
        {
            _mloc.setLocation(e.getX(), e.getY());
        }
        
        // documentation inherited from interface MouseMotionListener
        public void mouseMoved (MouseEvent e)
        {
        }
        
        // documentation inherited from interface MouseMotionListener
        public void mouseDragged (MouseEvent e)
        {
            int dx = e.getX() - _mloc.x, dy = e.getY() - _mloc.y;
            _mloc.setLocation(e.getX(), e.getY());
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                _camhand.tiltCamera(dy * FastMath.PI / 1000);
                _camhand.orbitCamera(-dx * FastMath.PI / 1000);
            } else {
                _camhand.zoomCamera(dy);
            }
        }
        
        // documentation inherited from interface MouseWheelListener
        public void mouseWheelMoved (MouseWheelEvent e)
        {
            _camhand.zoomCamera(e.getWheelRotation() * 10f);
        }
        
        /** The last recorded position of the mouse cursor. */
        protected Point _mloc = new Point();
    }
    
    /** The app configuration. */
    protected static Config _config =
        new Config("com/threerings/jme/tools/ModelViewer");
    
    /** The number of lines on the grid in each direction. */
    protected static final int GRID_SIZE = 32;
    
    /** The spacing between lines on the grid. */
    protected static final float GRID_SPACING = 2.5f;
}
