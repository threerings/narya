//
// $Id: DirectionViz.java,v 1.1 2002/06/26 02:54:56 mdb Exp $

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
