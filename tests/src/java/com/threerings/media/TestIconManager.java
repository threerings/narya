//
// $Id: TestIconManager.java,v 1.3 2003/01/13 22:57:45 mdb Exp $

package com.threerings.media;

import java.awt.BorderLayout;
import javax.swing.*;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.image.ImageManager;
import com.threerings.media.tile.TileManager;
import com.threerings.resource.ResourceManager;

/**
 * Does something extraordinary.
 */
public class TestIconManager
{
    public static void main (String[] args)
    {
        try {
            JFrame frame = new JFrame("TestIconManager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ResourceManager rmgr = new ResourceManager("rsrc");
            ImageManager imgr = new ImageManager(rmgr, frame);
            TileManager tmgr = new TileManager(imgr);
            IconManager iconmgr = new IconManager(
                tmgr, "rsrc/config/media/iconmgr.properties");

            JPanel panel = new JPanel(new HGroupLayout());
            for (int i = 0; i < 8; i++) {
                panel.add(new JButton(iconmgr.getIcon("test", i)));
            }

            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.pack();
            SwingUtil.centerWindow(frame);
            frame.show();

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
