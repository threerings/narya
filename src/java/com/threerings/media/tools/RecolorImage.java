//
// $Id$
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

package com.threerings.media.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.IntField;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.event.DocumentAdapter;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Interator;

import com.threerings.media.image.ColorPository;
import com.threerings.media.image.Colorization;
import com.threerings.media.image.ImageUtil;
import com.threerings.media.image.tools.xml.ColorPositoryParser;

/**
 * Tests the image recoloring code.
 */
public class RecolorImage extends JPanel
    implements ActionListener
{
    public RecolorImage ()
    {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        VGroupLayout vlay = new VGroupLayout(VGroupLayout.STRETCH);
        vlay.setOffAxisPolicy(VGroupLayout.STRETCH);
        setLayout(vlay);

        JPanel images = new JPanel(new HGroupLayout());
        images.add(_oldImage = new JLabel());
        images.add(_newImage = new JLabel());
        add(new JScrollPane(images));

        // Image file
        JPanel file = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        file.add(new JLabel("Image file:"), HGroupLayout.FIXED);
        file.add(_imagePath = new JTextField());
        _imagePath.setEditable(false);
        JButton browse = new JButton("Browse...");
        browse.setActionCommand("browse");
        browse.addActionListener(this);
        file.add(browse, HGroupLayout.FIXED);
        JButton reload = new JButton("Reload");
        reload.setActionCommand("reload");
        reload.addActionListener(this);
        file.add(reload, HGroupLayout.FIXED);
        add(file, VGroupLayout.FIXED);

        // Colorization file
        JPanel colFile = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        colFile.add(new JLabel("Colorize file:"), HGroupLayout.FIXED);
        colFile.add(_colFilePath = new JTextField());
        _colFilePath.setEditable(false);
        browse = new JButton("Browse...");
        browse.setActionCommand("browse_colorize");
        browse.addActionListener(this);
        colFile.add(browse, HGroupLayout.FIXED);
        add(colFile, VGroupLayout.FIXED);

        JPanel colMode = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        colMode.add(_mode = new JToggleButton("All Colorizations"));
        colMode.add(_classList = new JComboBox());
        ActionListener al = new ActionListener () {
            public void actionPerformed (ActionEvent ae) {
                convert();
            }
        };
        _mode.addActionListener(al);
        _classList.addActionListener(al);

        add(colMode, VGroupLayout.FIXED);

        JPanel controls = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        controls.add(new JLabel("Source color:"), HGroupLayout.FIXED);
        controls.add(_source = new JTextField("FF0000"));
        _colorLabel = new JPanel();
        _colorLabel.setSize(48, 48);
        _colorLabel.setOpaque(true);
        controls.add(_colorLabel, HGroupLayout.FIXED);
        controls.add(new JLabel("Target color:"), HGroupLayout.FIXED);
        controls.add(_target = new JTextField());
        JButton update = new JButton("Update");
        update.setActionCommand("update");
        update.addActionListener(this);
        controls.add(update, HGroupLayout.FIXED);
        add(controls, VGroupLayout.FIXED);

        HGroupLayout hlay = new HGroupLayout(HGroupLayout.STRETCH);
        JPanel dists = new JPanel(hlay);
        dists.add(new JLabel("HSV distances:"), HGroupLayout.FIXED);
        dists.add(_hueD = new SliderAndLabel(0.0f, 1.0f, 0.05f));
        dists.add(_saturationD = new SliderAndLabel(0.0f, 1.0f, 0.8f));
        dists.add(_valueD = new SliderAndLabel(0.0f, 1.0f, 0.6f));
        add(dists, VGroupLayout.FIXED);

        hlay = new HGroupLayout(HGroupLayout.STRETCH);
        JPanel offsets = new JPanel(hlay);
        offsets.add(new JLabel("HSV offsets:"), HGroupLayout.FIXED);
        offsets.add(_hueO = new SliderAndLabel(-1.0f, 1.0f, 0.1f));
        offsets.add(_saturationO = new SliderAndLabel(-1.0f, 1.0f, 0.0f));
        offsets.add(_valueO = new SliderAndLabel(-1.0f, 1.0f, 0.0f));
        add(offsets, VGroupLayout.FIXED);

        add(_status = new JTextField(), VGroupLayout.FIXED);
        _status.setEditable(false);

        hlay = new HGroupLayout();
        hlay.setJustification(HGroupLayout.CENTER);
        JPanel buttons = new JPanel(hlay);
        JButton convert = new JButton("Convert");
        convert.setActionCommand("convert");
        convert.addActionListener(this);
        buttons.add(convert);
        add(buttons, VGroupLayout.FIXED);

        // listen for mouse clicks
        images.addMouseListener(new MouseAdapter() {
            public void mousePressed (MouseEvent event) {
                RecolorImage.this.mousePressed(event);
            }
        });

        // we'll be using a file chooser
        String cwd = System.getProperty("user.dir");
        if (cwd == null) {
            _chooser = new JFileChooser();
            _colChooser = new JFileChooser();
        } else {
            _chooser = new JFileChooser(cwd);
            _colChooser = new JFileChooser(cwd);
        }
    }

    /**
     * Performs colorizations.
     */
    protected void convert ()
    {
        if (_image == null) {
            return;
        }

        // obtain the target color and offset
        try {
            BufferedImage image;
            if (_mode.isSelected()) {
                // All recolorings from file.
                image = getAllRecolors();
                if (image == null) {
                    return;
                }
            } else {
                // Normal recoloring
                int color = Integer.parseInt(_source.getText(), 16);

                float hueD = _hueD.getValue();
                float satD = _saturationD.getValue();
                float valD = _valueD.getValue();
                float[] dists = new float[] { hueD, satD, valD };

                float hueO = _hueO.getValue();
                float satO = _saturationO.getValue();
                float valO = _valueO.getValue();
                float[] offsets = new float[] { hueO, satO, valO };

                image = ImageUtil.recolorImage(
                    _image, new Color(color), dists, offsets);
            }
            _newImage.setIcon(new ImageIcon(image));
            _status.setText("Recolored image.");
            repaint();

        } catch (NumberFormatException nfe) {
            _status.setText("Invalid value: " + nfe.getMessage());
        }
    }

    /**
     * Gets an image with all recolorings of the selection colorization class.
     */
    public BufferedImage getAllRecolors ()
    {
        if (_colRepo == null) {
            return null;
        }

        ColorPository.ClassRecord colClass =
            _colRepo.getClassRecord((String)_classList.getSelectedItem());
        int classId = colClass.classId;

        ArrayList imgs = new ArrayList();
        Interator iter = colClass.colors.keys();
        BufferedImage img = new BufferedImage(_image.getWidth(),
            _image.getHeight()*colClass.colors.size(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics gfx = img.getGraphics();
        int y = 0;

        while (iter.hasNext()) {
            Colorization coloriz =
                _colRepo.getColorization(classId, iter.nextInt());
            BufferedImage subImg = ImageUtil.recolorImage(
                    _image, coloriz.rootColor, coloriz.range, coloriz.offsets);

            gfx.drawImage(subImg, 0, y, null, null);

            y += subImg.getHeight();
        }

        return img;
    }

    public void actionPerformed (ActionEvent event)
    {
        String cmd = event.getActionCommand();

        if (cmd.equals("convert")) {
            convert();
        } else if (cmd.equals("update")) {
            // obtain the target color and offset
            try {
                int source = Integer.parseInt(_source.getText(), 16);
                int target = Integer.parseInt(_target.getText(), 16);
                float[] shsv = rgbToHSV(source);
                float[] thsv = rgbToHSV(target);

                // set the offsets based on the differences
                _hueO.setValue(thsv[0] - shsv[0]);
                _saturationO.setValue(thsv[1] - shsv[1]);
                _valueO.setValue(thsv[2] - shsv[2]);

            } catch (NumberFormatException nfe) {
                _status.setText("Invalid value: " + nfe.getMessage());
            }

        } else if (cmd.equals("browse")) {
            int result = _chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                setImage(_chooser.getSelectedFile());
            }
        } else if (cmd.equals("reload")) {
            setImage(_chooser.getSelectedFile());
        } else if (cmd.equals("browse_colorize")) {
            int result = _colChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                setColorizeFile(_colChooser.getSelectedFile());
            }
        }
    }

    public void setImage (File path)
    {
        try {
            _image = ImageIO.read(path);
            _imagePath.setText(path.getPath());
            _oldImage.setIcon(new ImageIcon(_image));

        } catch (IOException ioe) {
            _status.setText("Error opening image file: " + ioe);
        }
    }

    /**
     * Loads up the colorization classes from the specified file.
     */
    public void setColorizeFile (File path)
    {
        try {
            if (path.getName().endsWith("xml")) {
                ColorPositoryParser parser = new ColorPositoryParser();
                _colRepo =
                    (ColorPository)(parser.parseConfig(path));
            } else {
                _colRepo =
                    ColorPository.loadColorPository(new FileInputStream(path));
            }

            _classList.removeAllItems();
            Iterator iter = _colRepo.enumerateClasses();
            ArrayList names = new ArrayList();
            while (iter.hasNext()) {
                String str = ((ColorPository.ClassRecord)iter.next()).name;
                names.add(str);
            }

            Collections.sort(names);

            iter = names.iterator();
            while (iter.hasNext()) {
                _classList.addItem((String)iter.next());
            }

            _classList.setSelectedIndex(0);
            _colFilePath.setText(path.getPath());
        }  catch (Exception ex) {
            _status.setText("Error opening colorization file: " + ex);
        }
    }

    protected static float[] rgbToHSV (int rgb)
    {
        float[] hsv = new float[3];
        Color color = new Color(rgb);
        Color.RGBtoHSB(color.getRed(), color.getGreen(),
                       color.getBlue(), hsv);
        return hsv;
    }

    public void mousePressed (MouseEvent event)
    {
        // if the click was in the bounds of the source image, grab the
        // pixel color and use that to set the "source" color
        int x = event.getX(), y = event.getY();
        Rectangle ibounds = _oldImage.getBounds();
        if (ibounds.contains(x, y)) {
            int argb = _image.getRGB(x - ibounds.x, y - ibounds.y);
            String cstr = Integer.toString(argb & 0xFFFFFF, 16);
            _source.setText(cstr.toUpperCase());
            _colorLabel.setBackground(new Color(argb));
            _colorLabel.repaint();
        }
    }

    /**
     * Class with linked slider and label arranged vertically.
     */
    protected class SliderAndLabel extends JPanel
    {
        public SliderAndLabel (float minf, float maxf, float valuef)
        {
            int min = (int)(minf*CONVERSION);
            int max = (int)(maxf*CONVERSION);
            int value = (int)(valuef*CONVERSION);
            setLayout(new VGroupLayout(VGroupLayout.STRETCH));
            _intField = new JLabel(String.valueOf(value/CONVERSION));
            _slider = new JSlider(min, max, value);

            _slider.addChangeListener(new ChangeListener() {
                public void stateChanged (ChangeEvent ce) {
                    _intField.setText(String.valueOf(
                        ((float)_slider.getValue())/CONVERSION));

                    convert();
                }
            });
            add(_intField);
            add(_slider);
        }

        public float getValue ()
        {
            return _slider.getValue()/CONVERSION;
        }

        public void setValue (float val)
        {
            _slider.setValue((int)(val*CONVERSION));
        }

        protected JSlider _slider;
        protected JLabel _intField;

        protected final static float CONVERSION = 1000.0f;
    }

    public static void main (String[] args)
    {
        try {
            JFrame frame = new JFrame("Image recoloring test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            RecolorImage panel = new RecolorImage();

            // load up the image from the command line if one was
            // specified
            if (args.length > 0) {
                panel.setImage(new File(args[0]));
            }

            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setSize(600, 600);
            SwingUtil.centerWindow(frame);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected BufferedImage _image;
    protected JFileChooser _chooser;
    protected JFileChooser _colChooser;
    protected JTextField _imagePath;
    protected JTextField _colFilePath;

    protected JLabel _oldImage;
    protected JLabel _newImage;
    protected JPanel _colorLabel;

    protected JTextField _source;
    protected JTextField _target;

    protected SliderAndLabel _hueO;
    protected SliderAndLabel _saturationO;
    protected SliderAndLabel _valueO;

    protected SliderAndLabel _hueD;
    protected SliderAndLabel _saturationD;
    protected SliderAndLabel _valueD;

    protected JTextField _status;

    protected JComboBox _classList;
    protected JToggleButton _mode;

    protected ColorPository _colRepo;

    protected static final String IMAGE_PATH =
        // "bundles/components/pirate/torso/regular/standing.png";
        "bundles/components/pirate/head/regular/standing.png";
}
