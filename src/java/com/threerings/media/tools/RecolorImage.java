//
// $Id: RecolorImage.java,v 1.3 2003/01/08 04:09:03 mdb Exp $

package com.threerings.media.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.Log;
import com.threerings.media.image.ImageUtil;

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

        JPanel file = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        file.add(new JLabel("Image file:"), HGroupLayout.FIXED);
        file.add(_imagePath = new JTextField());
        _imagePath.setEditable(false);
        JButton browse = new JButton("Browse...");
        browse.setActionCommand("browse");
        browse.addActionListener(this);
        file.add(browse, HGroupLayout.FIXED);
        add(file, VGroupLayout.FIXED);

        JPanel controls = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        controls.add(new JLabel("Source color:"), HGroupLayout.FIXED);
        controls.add(_source = new JTextField());
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
        dists.add(_hueD = new JTextField("0.05"));
        dists.add(_saturationD = new JTextField("0.8"));
        dists.add(_valueD = new JTextField("0.6"));
        add(dists, VGroupLayout.FIXED);

        hlay = new HGroupLayout(HGroupLayout.STRETCH);
        JPanel offsets = new JPanel(hlay);
        offsets.add(new JLabel("HSV offsets:"), HGroupLayout.FIXED);
        offsets.add(_hueO = new JTextField("0.1"));
        offsets.add(_saturationO = new JTextField("0.0"));
        offsets.add(_valueO = new JTextField("0.0"));
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
        } else {
            _chooser = new JFileChooser(cwd);
        }
    }

    public void actionPerformed (ActionEvent event)
    {
        String cmd = event.getActionCommand();

        if (cmd.equals("convert")) {
            // obtain the target color and offset
            try {
                int color = Integer.parseInt(_source.getText(), 16);

                float hueD = Float.parseFloat(_hueD.getText());
                float satD = Float.parseFloat(_saturationD.getText());
                float valD = Float.parseFloat(_valueD.getText());
                float[] dists = new float[] { hueD, satD, valD };

                float hueO = Float.parseFloat(_hueO.getText());
                float satO = Float.parseFloat(_saturationO.getText());
                float valO = Float.parseFloat(_valueO.getText());
                float[] offsets = new float[] { hueO, satO, valO };

                BufferedImage image = ImageUtil.recolorImage(
                    _image, new Color(color), dists, offsets);
                _newImage.setIcon(new ImageIcon(image));
                _status.setText("Recolored image.");
                repaint();

            } catch (NumberFormatException nfe) {
                _status.setText("Invalid value: " + nfe.getMessage());
            }

        } else if (cmd.equals("update")) {
            // obtain the target color and offset
            try {
                int source = Integer.parseInt(_source.getText(), 16);
                int target = Integer.parseInt(_target.getText(), 16);
                float[] shsv = rgbToHSV(source);
                float[] thsv = rgbToHSV(target);

                // set the offsets based on the differences
                _hueO.setText("" + (thsv[0] - shsv[0]));
                _saturationO.setText("" + (thsv[1] - shsv[1]));
                _valueO.setText("" + (thsv[2] - shsv[2]));

            } catch (NumberFormatException nfe) {
                _status.setText("Invalid value: " + nfe.getMessage());
            }

        } else if (cmd.equals("browse")) {
            int result = _chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                setImage(_chooser.getSelectedFile());
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
            frame.show();

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected BufferedImage _image;
    protected JFileChooser _chooser;
    protected JTextField _imagePath;

    protected JLabel _oldImage;
    protected JLabel _newImage;
    protected JPanel _colorLabel;

    protected JTextField _source;
    protected JTextField _target;

    protected JTextField _hueO;
    protected JTextField _saturationO;
    protected JTextField _valueO;

    protected JTextField _hueD;
    protected JTextField _saturationD;
    protected JTextField _valueD;

    protected JTextField _status;

    protected static final String IMAGE_PATH =
        // "bundles/components/pirate/torso/regular/standing.png";
        "bundles/components/pirate/head/regular/standing.png";
}
