//
// $Id: ResolutionView.java,v 1.2 2003/05/15 22:49:38 mdb Exp $

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
        assignColor(block, Color.yellow);
        repaint();
    }

    public synchronized void resolvingBlock (SceneBlock block)
    {
        IntTuple key = blockKey(block);
        if (_blocks.containsKey(key)) {
            assignColor(block, Color.red);
            repaint();
        }
    }

    public synchronized void resolvedBlock (SceneBlock block)
    {
        IntTuple key = blockKey(block);
        if (_blocks.containsKey(key)) {
            assignColor(block, Color.green);
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

    protected void assignColor (SceneBlock block, Color color)
    {
        IntTuple key = blockKey(block);
        BlockGlyph glyph = (BlockGlyph)_blocks.get(key);
        if (glyph == null) {
            glyph = new BlockGlyph(_metrics, key.left, key.right);
            _blocks.put(key, glyph);
        }
        glyph.color = color;
    }

    public synchronized void paintComponent (Graphics g)
    {
        super.paintComponent(g);
        Graphics2D gfx = (Graphics2D)g;

        Rectangle vbounds = _panel.getViewBounds();
        gfx.translate((getWidth()-vbounds.width/16)/2 - vbounds.x/16,
                      (getHeight()-vbounds.height/16)/2 - vbounds.y/16);

        AffineTransform xform = gfx.getTransform();
        gfx.scale(0.25, 0.25);

        // draw our block glyphs
        for (Iterator iter = _blocks.values().iterator(); iter.hasNext(); ) {
            ((BlockGlyph)iter.next()).paint(gfx);
        }

        // draw the view bounds
        gfx.scale(0.25, 0.25);
        gfx.draw(vbounds);
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

    protected static class BlockGlyph
    {
        public Color color;

        public BlockGlyph (MisoSceneMetrics metrics, int bx, int by)
        {
            _bpoly = MisoUtil.getTilePolygon(metrics, bx, by);
        }

        public void paint (Graphics2D gfx)
        {
            gfx.setColor(color);
            gfx.fill(_bpoly);
            gfx.setColor(Color.black);
            gfx.draw(_bpoly);
        }

        protected Polygon _bpoly;
    }

    protected MisoScenePanel _panel;
    protected MisoSceneMetrics _metrics;
    protected HashMap _blocks = new HashMap();

    protected static final int TILE_SIZE = 10;
    protected static final int MAX_WIDTH = 30;
    protected static final int MAX_HEIGHT = 30;
}
