//
// $Id: DirectionButton.java 5784 2003-01-08 04:17:40Z mdb $

package com.threerings.stage.tools.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.DefaultButtonModel;

import com.threerings.media.image.ColorUtil;

import com.threerings.util.DirectionCodes;

/**
 * A button that allows for selection from the compass directions.
 */
public class DirectionButton extends AbstractButton
{
    /**
     * Construct a 41x41 DirectionButton.
     */
    public DirectionButton ()
    {
        this(41);
    }

    /**
     * Construct a DirectionButton with the specified preferred diameter.
     */
    public DirectionButton (int preferredDiameter)
    {
        _prefdia = preferredDiameter;
        configSize(_prefdia);

        setModel(new DefaultButtonModel());

        addMouseListener(new MouseAdapter() {
            public void mousePressed (MouseEvent event)
            {
                if (isEnabled()) {
                    _armed = getDirection(event.getX(), event.getY());
                    repaint();
                }
            }

            public void mouseReleased (MouseEvent event)
            {
                if ((_armed != -1) &&
                    isEnabled() &&
                    (_armed == getDirection(event.getX(), event.getY())) &&
                    (_direction != _armed)) {

                    _direction = _armed;
                    fireStateChanged();
                }
                _armed = -1;
                repaint();
            }

                    
        });
    }

    /**
     * Paint this component and the selected direction,
     * dimmed if we're inactive.
     */
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // draw the selected direction circle
        g.setColor(isEnabled() ? Color.red
                               : ColorUtil.blend(Color.red, getBackground()));
        g.fillOval(_coords[_direction][0], _coords[_direction][1],
                   _cdia, _cdia);

        if (_armed != -1) {
            g.setColor(ColorUtil.blend(Color.black, getBackground()));
            g.fillOval(_coords[_armed][0], _coords[_armed][1],
                       _cdia, _cdia);
        }

        // draw a circle around the various direction circles
        g.setColor(isEnabled() ? Color.black
                               : ColorUtil.blend(Color.black, getBackground()));
        g.drawOval(0, 0, _dia, _dia);

        // draw each direction circle
        for (int ii=DirectionCodes.SOUTHWEST;
             ii < DirectionCodes.DIRECTION_COUNT;
             ii++) {

            g.drawOval(_coords[ii][0], _coords[ii][1], _cdia, _cdia);
        }
    }

    /**
     * Get the direction that is currently selected.
     */
    public int getDirection ()
    {
        return _direction;
    }

    /**
     * Set the currently displayed direction
     */
    public void setDirection (int direction)
    {
        if (direction != _direction) {
            _direction = direction;
            fireStateChanged();
            repaint();
        }
    }

    /**
     * Get the direction specified by the mouse coordinates
     */
    protected int getDirection (int x, int y)
    {
        for (int ii=0; ii < DirectionCodes.DIRECTION_COUNT; ii++) {
            if ((x > _coords[ii][0]) && (x < (_coords[ii][0] + _cdia)) &&
                (y > _coords[ii][1]) && (y < (_coords[ii][1] + _cdia))) {
                return ii;
            }
        }
        return -1;
    }

    // documentation inherited
    public void setSize (Dimension d)
    {
        super.setSize(d);

        configSize(Math.min(d.width, d.height));
    }

    /**
     * Reconfigure the way we look for a new size
     */
    protected void configSize (int diameter)
    {
        // recompute our sizes
        _dia = diameter - 1;
        _cdia = _dia / 4;

        int mid = (_dia - _cdia) / 2;

        int num = DirectionCodes.DIRECTION_COUNT; // oh we all know it's 8.

        for (int ii=0; ii < num; ii++) {
            double rads = Math.PI * 2 * ii / num;

            // 0 radians specifies EAST, so we offset
            int dir = (ii + DirectionCodes.EAST) % num; 

            _coords[dir][0] = mid + ((int) (mid * Math.cos(rads)));
            _coords[dir][1] = mid + ((int) (mid * Math.sin(rads)));
        }
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        return new Dimension(_prefdia, _prefdia);
    }

    /** The current direction displayed by the button. */
    protected int _direction = DirectionCodes.SOUTH;

    // if we're "armed" for a possible state change, which direction is armed?
    // (-1 for unarmed)
    protected int _armed = -1;

    /** Diameter of the drawn enclosing circle. */
    protected int _dia;

    /** Diameter of selection circles. */
    protected int _cdia;

    /** Coordinates of each of the selection circles. */
    protected int[][] _coords = new int[DirectionCodes.DIRECTION_COUNT][2];

    /** Our preferred diameter. */
    protected int _prefdia;
}
