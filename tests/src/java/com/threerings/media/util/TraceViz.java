//
// $Id: TraceViz.java,v 1.1 2002/11/15 09:29:40 shaper Exp $

package com.threerings.media.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.VGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.Log;

/**
 * Simple application for testing image trace functionality.
 */
public class TraceViz
{
    public TraceViz (String[] args)
        throws IOException
    {
        _frame = new JFrame();
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel content = (JPanel)_frame.getContentPane();
        content.setBackground(Color.white);
        content.setLayout(new VGroupLayout());

        // create a compatible image
        BufferedImage image = ImageIO.read(new File(args[0]));
        BufferedImage cimage = ImageUtil.createImage(
            image.getWidth(null), image.getHeight(null));
        Graphics g = cimage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        Image timage = ImageUtil.createTracedImage(
            cimage, Color.red, 5, 0.4f, 0.1f);
        content.add(new JLabel(new ImageIcon(cimage)));
        content.add(new JLabel(new ImageIcon(timage)));
    }

    public void run ()
    {
        _frame.pack();
        SwingUtil.centerWindow(_frame);
        _frame.show();
    }

    public static void main (String[] args)
    {
        try {
            TraceViz app = new TraceViz(args);
            app.run();

        } catch (Exception e) {
            Log.warning("Failed to run application: " + e);
            Log.logStackTrace(e);
        }
    }

    protected JFrame _frame;
}
