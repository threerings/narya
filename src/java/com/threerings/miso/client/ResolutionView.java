//
// $Id: ResolutionView.java,v 1.1 2003/05/12 02:03:31 mdb Exp $

package com.threerings.miso.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;

import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.util.IntTuple;

import com.threerings.media.util.MathUtil;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;

/**
 * Used to debug scene block resolution visually.
 */
public class ResolutionView extends JPanel
{
    public ResolutionView (MisoScenePanel panel)
    {
        _panel = panel;
        _metrics = panel.getSceneMetrics();
    }

    public Dimension getPreferredSize ()
    {
        return new Dimension(TILE_SIZE*MAX_WIDTH, TILE_SIZE*MAX_HEIGHT);
    }

    public synchronized void queuedBlock (SceneBlock block)
    {
        _blocks.put(blockKey(block), Color.yellow);
        repaint();
    }

    public synchronized void resolvingBlock (SceneBlock block)
    {
        IntTuple key = blockKey(block);
        if (_blocks.containsKey(key)) {
            _blocks.put(key, Color.red);
            repaint();
        }
    }

    public synchronized void resolvedBlock (SceneBlock block)
    {
        IntTuple key = blockKey(block);
        if (_blocks.containsKey(key)) {
            _blocks.put(key, Color.green);
            repaint();
        }
    }

    public synchronized void blockCleared (SceneBlock block)
    {
        _blocks.remove(blockKey(block));
        repaint();
    }

    public synchronized void newScene ()
    {
        _blocks.clear();
        repaint();
    }

    public synchronized void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        Graphics2D gfx = (Graphics2D)g;
        AffineTransform xform = gfx.getTransform();
        gfx.translate(getWidth()/2, getHeight()/2);
        gfx.scale(0.25, 0.25);
        for (Iterator iter = _blocks.keySet().iterator(); iter.hasNext(); ) {
            IntTuple key = (IntTuple)iter.next();
            Color color = (Color)_blocks.get(key);
            Polygon poly = MisoUtil.getTilePolygon(
                _metrics, key.left, key.right);
            gfx.setColor(color);
            gfx.fill(poly);
            gfx.setColor(Color.black);
            gfx.draw(poly);
        }
        gfx.scale(0.25, 0.25);
        gfx.draw(_panel.getViewBounds());
        gfx.setColor(Color.red);
        gfx.draw(_panel.getInfluentialBounds());
        gfx.setTransform(xform);
    }

    protected final IntTuple blockKey (SceneBlock block)
    {
        Rectangle bounds = block.getBounds();
        return new IntTuple(MathUtil.floorDiv(bounds.x, bounds.width),
                            MathUtil.floorDiv(bounds.y, bounds.height));
    }

    protected MisoScenePanel _panel;
    protected MisoSceneMetrics _metrics;
    protected HashMap _blocks = new HashMap();

    protected static final int TILE_SIZE = 10;
    protected static final int MAX_WIDTH = 30;
    protected static final int MAX_HEIGHT = 30;
}
