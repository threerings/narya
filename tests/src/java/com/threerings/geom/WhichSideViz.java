//
// $Id: WhichSideViz.java,v 1.1 2002/06/28 01:29:32 mdb Exp $

package com.threerings.geom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.util.SwingUtil;

/**
 * Renders the nearest point on a line just for kicks.
 */
public class WhichSideViz extends JPanel
    implements MouseMotionListener
{
    public WhichSideViz ()
    {
        addMouseMotionListener(this);
    }

    public void doLayout ()
    {
        super.doLayout();
        _center = new Point(getWidth() / 2, getHeight() / 2);
    }

    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        int dx = (int)Math.round(Math.cos(_theta) * 200);
        int dy = (int)Math.round(Math.sin(_theta) * 200);

        g.setColor(Color.blue);
        g.drawLine(_center.x, _center.y, _center.x + dx, _center.y + dy);
        g.setColor(Color.white);
        g.drawLine(_center.x, _center.y, _center.x - dx, _center.y - dy);

        int nx = (int)Math.round(1000*Math.cos(_theta + Math.PI/2)),
            ny = (int)Math.round(1000*Math.sin(_theta + Math.PI/2));
        g.setColor(Color.pink);
        g.drawLine(_center.x, _center.y, nx, ny);

        // figure out which side the line is on
        String str;
        int side = GeomUtil.whichSide(_center, _theta, _spot);
        if (side > 0) {
            str = "R";
        } else if (side < 0) {
            str = "L";
        } else {
            str = "O";
        }

        g.setColor(Color.black);
        g.drawString(str, _spot.x, _spot.y);

        // kick theta along for the next tick because it's fun
//         _theta += (float)(Math.PI/100);
    }

    public void mouseDragged (MouseEvent event)
    {
    }

    public void mouseMoved (MouseEvent event)
    {
        _spot.x = event.getX();
        _spot.y = event.getY();
        repaint();
    }

    public static void main (String[] args)
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new WhichSideViz(), BorderLayout.CENTER);
        frame.setSize(300, 300);
        SwingUtil.centerWindow(frame);
        frame.show();
    }

    protected float _theta = (float)(3 * Math.PI / 5);
    protected Point _center;
    protected Point _spot = new Point();
}
