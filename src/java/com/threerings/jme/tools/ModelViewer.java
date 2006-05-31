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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.JmeException;
import com.jme.util.LoggingSystem;
import com.jme.util.TextureManager;
import com.jme.util.geom.Debugger;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.Spacer;
import com.samskivert.util.Config;

import com.threerings.resource.ResourceManager;
import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;

import com.threerings.jme.JmeCanvasApp;
import com.threerings.jme.Log;
import com.threerings.jme.model.Model;
import com.threerings.jme.model.TextureProvider;

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
        // write standard output and error to a log file
        if (System.getProperty("no_log_redir") == null) {
            String dlog = "viewer.log";
            try {
                PrintStream logOut = new PrintStream(
                    new FileOutputStream(dlog), true);
                System.setOut(logOut);
                System.setErr(logOut);

            } catch (IOException ioe) {
                Log.warning("Failed to open debug log [path=" + dlog +
                            ", error=" + ioe + "].");
            }
        }
        LoggingSystem.getLoggingSystem().setLevel(Level.WARNING);
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
        _rsrcmgr = new ResourceManager("rsrc");
        _msg = new MessageManager("rsrc.i18n").getBundle("jme.viewer");
        _path = path;
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        _frame = new JFrame(_msg.get("m.title"));
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JMenuBar menu = new JMenuBar();
        _frame.setJMenuBar(menu);
        
        JMenu file = new JMenu(_msg.get("m.file_menu"));
        file.setMnemonic(KeyEvent.VK_F);
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
        
        JMenu view = new JMenu(_msg.get("m.view_menu"));
        view.setMnemonic(KeyEvent.VK_V);
        menu.add(view);
        
        Action wireframe = new AbstractAction(_msg.get("m.view_wireframe")) {
            public void actionPerformed (ActionEvent e) {
                _wfstate.setEnabled(!_wfstate.isEnabled());
            }
        };
        wireframe.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);
        wireframe.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
        view.add(new JCheckBoxMenuItem(wireframe));
        view.addSeparator();
        
        _pivots = new JCheckBoxMenuItem(_msg.get("m.view_pivots"));
        _pivots.setMnemonic(KeyEvent.VK_P);
        _pivots.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        view.add(_pivots);
        
        _bounds = new JCheckBoxMenuItem(_msg.get("m.view_bounds"));
        _bounds.setMnemonic(KeyEvent.VK_B);
        _bounds.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
        view.add(_bounds);
        
        _normals = new JCheckBoxMenuItem(_msg.get("m.view_normals"));
        _normals.setMnemonic(KeyEvent.VK_N);
        _normals.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        view.add(_normals);
        view.addSeparator();
        
        Action rlight = new AbstractAction(_msg.get("m.view_light")) {
            public void actionPerformed (ActionEvent e) {
                if (_rldialog == null) {
                    _rldialog = new RotateLightDialog();
                    _rldialog.pack();
                }
                _rldialog.setVisible(true);
            }
        };
        rlight.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
        rlight.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        view.add(new JMenuItem(rlight));
        
        _frame.getContentPane().add(getCanvas(), BorderLayout.CENTER);
        
        JPanel bpanel = new JPanel(new BorderLayout());
        _frame.getContentPane().add(bpanel, BorderLayout.SOUTH);
        
        _animctrls = new JPanel();
        _animctrls.setBorder(BorderFactory.createEtchedBorder());
        bpanel.add(_animctrls, BorderLayout.NORTH);
        _animctrls.add(new JLabel(_msg.get("m.anim_select")));
        _animctrls.add(_animbox = new JComboBox());
        _animctrls.add(new JButton(
            new AbstractAction(_msg.get("m.anim_start")) {
                public void actionPerformed (ActionEvent e) {
                    _model.startAnimation((String)_animbox.getSelectedItem());
                }
            }));
        _animctrls.add(_animstop = new JButton(
            new AbstractAction(_msg.get("m.anim_stop")) {
                public void actionPerformed (ActionEvent e) {
                    _model.stopAnimation();
                }
            }));
        _animstop.setEnabled(false);
        _animctrls.add(new Spacer(50, 1));
        _animctrls.add(new JLabel(_msg.get("m.anim_speed")));
        _animctrls.add(_animspeed = new JSlider(-100, +100, 0));
        _animspeed.setMajorTickSpacing(10);
        _animspeed.setSnapToTicks(true);
        _animspeed.setPaintTicks(true);
        _animspeed.addChangeListener(new ChangeListener() {
            public void stateChanged (ChangeEvent e) {
                updateAnimationSpeed();
            }
        });
        _animctrls.setVisible(false);
        
        _status = new JLabel(" ");
        _status.setHorizontalAlignment(JLabel.LEFT);
        _status.setBorder(BorderFactory.createEtchedBorder());
        bpanel.add(_status, BorderLayout.SOUTH);
        
        _frame.pack();
        _frame.setVisible(true);

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
    protected void initDisplay ()
        throws JmeException
    {
        super.initDisplay();
        _ctx.getRenderer().setBackgroundColor(ColorRGBA.gray);
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
        
        // set default states
        MaterialState mstate = _ctx.getRenderer().createMaterialState();
        mstate.getDiffuse().set(ColorRGBA.white);
        mstate.getAmbient().set(ColorRGBA.white);
        _ctx.getGeometry().setRenderState(mstate);
        _ctx.getGeometry().setRenderState(
            _wfstate = _ctx.getRenderer().createWireframeState());
        _wfstate.setEnabled(false);
        
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
        grid.getBatch(0).getDefaultColor().set(0.25f, 0.25f, 0.25f, 1f);
        grid.setLightCombineMode(LightState.OFF);
        grid.setModelBound(new BoundingBox());
        grid.updateModelBound();
        _ctx.getGeometry().attachChild(grid);
        grid.updateRenderState();
        
        // attach a dummy node to draw debugging views
        _ctx.getGeometry().attachChild(new Node("debug") {
            public void onDraw (Renderer r) {
                if (_model == null) {
                    return;
                }
                if (_pivots.getState()) {
                    drawPivots(_model, r);
                }
                if (_bounds.getState()) {
                    Debugger.drawBounds(_model, r);
                }
                if (_normals.getState()) {
                    Debugger.drawNormals(_model, r, 5f, true);
                }
            }
        });
    }
    
    /**
     * Draws the pivot axes of the given node and its children.
     */
    protected void drawPivots (Spatial spatial, Renderer r)
    {
        if (_axes == null) {
            _axes = new Line("axes",
                new Vector3f[] { Vector3f.ZERO, Vector3f.UNIT_X, Vector3f.ZERO,
                    Vector3f.UNIT_Y, Vector3f.ZERO, Vector3f.UNIT_Z }, null,
                new ColorRGBA[] { ColorRGBA.red, ColorRGBA.red,
                    ColorRGBA.green, ColorRGBA.green, ColorRGBA.blue,
                    ColorRGBA.blue }, null);
            _axes.setRenderState(r.createZBufferState());
        }
        _axes.getRenderState(ZBufferState.RS_ZBUFFER).apply();
        _axes.getWorldTranslation().set(spatial.getWorldTranslation());
        _axes.getWorldRotation().set(spatial.getWorldRotation());
        _axes.draw(r);
        
        if (spatial instanceof Node) {
            Node node = (Node)spatial;
            for (int ii = 0, nn = node.getQuantity(); ii < nn; ii++) {
                drawPivots(node.getChild(ii), r);
            }
        }
    }
    
    @Override // documentation inherited
    protected void initLighting ()
    {
        _dlight = new DirectionalLight();
        _dlight.setEnabled(true);
        _dlight.getDirection().set(-1f, 0f, -1f).normalizeLocal();
        _dlight.getAmbient().set(0.25f, 0.25f, 0.25f, 1f);
        
        LightState lstate = _ctx.getRenderer().createLightState();
        lstate.attach(_dlight);
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
        Model model = CompileModel.compile(file);
        if (model != null) {
            setModel(model, file);
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
        setModel(Model.readFromFile(file, false), file);
    }
    
    /**
     * Sets the model once it's been loaded.
     *
     * @param file the file from which the model was loaded
     */
    protected void setModel (Model model, File file)
    {
        if (_model != null) {
            _ctx.getGeometry().detachChild(_model);
        }
        _ctx.getGeometry().attachChild(_model = model);
        _model.lockStaticMeshes(_ctx.getRenderer(), true, true);
        
        // resolve the textures from the file's directory
        final File dir = file.getParentFile();
        _model.resolveTextures(new TextureProvider() {
            public TextureState getTexture (String name) {
                TextureState tstate = _tstates.get(name);
                if (tstate == null) {
                    File file;
                    if (name.startsWith("/")) {
                        file = _rsrcmgr.getResourceFile(name.substring(1));
                    } else {
                        file = new File(dir, name);
                    }
                    Texture tex = TextureManager.loadTexture(file.toString(),
                        Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
                    if (tex == null) {
                        Log.warning("Couldn't find texture [path=" + file +
                            "].");
                        return null;
                    }
                    tex.setWrap(Texture.WM_WRAP_S_WRAP_T);
                    tstate = _ctx.getRenderer().createTextureState();
                    tstate.setTexture(tex);
                    _tstates.put(name, tstate);
                }
                return tstate;
            }
            protected HashMap<String, TextureState> _tstates =
                new HashMap<String, TextureState>();
        });
        _model.updateRenderState();
        
        // configure the animation panel
        String[] anims = _model.getAnimationNames();
        if (anims.length == 0) {
            _animctrls.setVisible(false);
            return;
        }
        _model.addAnimationObserver(_animobs);
        _animctrls.setVisible(true);
        _animbox.setModel(new DefaultComboBoxModel(anims));
        updateAnimationSpeed();
    }
    
    /**
     * Updates the model's animation speed based on the position of the
     * animation speed slider.
     */
    protected void updateAnimationSpeed ()
    {
        _model.setAnimationSpeed(
            FastMath.pow(2f, _animspeed.getValue() / 25f));
    }
    
    /** The resource manager. */
    protected ResourceManager _rsrcmgr;
    
    /** The translation bundle. */
    protected MessageBundle _msg;
    
    /** The path of the initial model to load. */
    protected String _path;
    
    /** The viewer frame. */
    protected JFrame _frame;
    
    /** Debug view switches. */
    protected JCheckBoxMenuItem _pivots, _bounds, _normals;
    
    /** The animation controls. */
    protected JPanel _animctrls;
    
    /** The animation selector. */
    protected JComboBox _animbox;
    
    /** The "stop animation" button. */
    protected JButton _animstop;
    
    /** The animation speed slider. */
    protected JSlider _animspeed; 
    
    /** The status bar. */
    protected JLabel _status;
    
    /** The model file chooser. */
    protected JFileChooser _chooser;
    
    /** The light rotation dialog. */
    protected RotateLightDialog _rldialog;
    
    /** The scene light. */
    protected DirectionalLight _dlight;
    
    /** Used to toggle wireframe rendering. */
    protected WireframeState _wfstate;
    
    /** The currently loaded model. */
    protected Model _model;
    
    /** Reused to draw pivot axes. */
    protected Line _axes;
    
    /** Enables and disables the stop button when animations start and stop. */
    protected Model.AnimationObserver _animobs =
        new Model.AnimationObserver() {
        public boolean animationStarted (Model model, String name) {
            _animstop.setEnabled(true);
            return true;
        }
        public boolean animationCompleted (Model model, String name) {
            _animstop.setEnabled(false);
            return true;
        }
        public boolean animationCancelled (Model model, String name) {
            _animstop.setEnabled(false);
            return true;
        }
    };
    
    /** Allows users to move the directional light around. */
    protected class RotateLightDialog extends JDialog
        implements ChangeListener
    {
        public RotateLightDialog ()
        {
            super(_frame, _msg.get("m.rotate_light"), false);
            
            JPanel cpanel = GroupLayout.makeVBox();
            getContentPane().add(cpanel, BorderLayout.CENTER);
            
            JPanel apanel = new JPanel();
            apanel.add(new JLabel(_msg.get("m.azimuth")));
            apanel.add(_azimuth = new JSlider(-180, +180, 0));
            _azimuth.addChangeListener(this);
            cpanel.add(apanel);
            
            JPanel epanel = new JPanel();
            epanel.add(new JLabel(_msg.get("m.elevation")));
            epanel.add(_elevation = new JSlider(-90, +90, 45));
            _elevation.addChangeListener(this);
            cpanel.add(epanel);
            
            JPanel bpanel = new JPanel();
            bpanel.add(new JButton(new AbstractAction(_msg.get("m.close")) {
                public void actionPerformed (ActionEvent e) {
                    setVisible(false);
                }
            }));
            getContentPane().add(bpanel, BorderLayout.SOUTH);
        }
        
        // documentation inherited from interface ChangeListener
        public void stateChanged (ChangeEvent e)
        {
            float az = _azimuth.getValue() * FastMath.DEG_TO_RAD,
                el = _elevation.getValue() * FastMath.DEG_TO_RAD;
            _dlight.getDirection().set(
                -FastMath.cos(az) * FastMath.cos(el),
                -FastMath.sin(az) * FastMath.cos(el),
                -FastMath.sin(el));
        }
        
        // documentation inherited from interface ActionListener
        public void actionPerformed (ActionEvent e)
        {
            setVisible(false);
        }
        
        /** Azimuth and elevation sliders. */
        protected JSlider _azimuth, _elevation;
    }
    
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
