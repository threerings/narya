//
// $Id: NearestViz.java,v 1.3 2004/08/27 02:20:56 mdb Exp $
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
public class NearestViz extends JPanel
    implements MouseMotionListener
{
    public NearestViz ()
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
        g.drawLine(_center.x, _center.y, _center.x + dx, _center.y - dy);
        g.drawLine(_center.x, _center.y, _center.x - dx, _center.y + dy);

        // locate the point nearest the spot
        Point p2 = new Point(_center.x + dx, _center.y - dy);
        Point n = new Point();
        GeomUtil.nearestToLine(_center, p2, _spot, n);

        g.setColor(Color.red);
        g.drawOval(n.x - 4, n.y - 4, 8, 8);

        // kick theta along for the next tick because it's fun
        _theta += (float)(Math.PI/100);
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
        frame.getContentPane().add(new NearestViz(), BorderLayout.CENTER);
        frame.setSize(300, 300);
        SwingUtil.centerWindow(frame);
        frame.show();
    }

    protected float _theta = (float)(3 * Math.PI / 5);
    protected Point _center;
    protected Point _spot = new Point();
}
