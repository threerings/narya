//
// $Id: DirectionViz.java,v 1.2 2004/08/27 02:21:05 mdb Exp $
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

package com.threerings.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Renders the output of {@link DirectionUtil#getDirection} just for
 * kicks.
 */
public class DirectionViz extends JPanel
    implements MouseMotionListener
{
    public DirectionViz ()
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

        g.setColor(Color.blue);
        g.drawLine(_center.x, _center.y, _spot.x, _spot.y);

        int orient = DirectionUtil.getFineDirection(_center, _spot);
        g.drawString(DirectionUtil.toShortString(orient), _spot.x, _spot.y);
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
        frame.getContentPane().add(new DirectionViz(), BorderLayout.CENTER);
        frame.setSize(300, 300);
        frame.show();
    }

    protected Point _center;
    protected Point _spot = new Point();
}
