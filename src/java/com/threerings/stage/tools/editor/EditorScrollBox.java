//
// $Id: EditorScrollBox.java 19363 2005-02-17 21:36:41Z ray $

package com.threerings.stage.tools.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;

import java.awt.image.BufferedImage;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import com.samskivert.swing.ScrollBox;
import com.samskivert.swing.util.SwingUtil;

/**
 * A scrollbox that shows our position in the editor.
 */
public class EditorScrollBox extends ScrollBox
{
    /**
     * Create the tightly-coupled EditorScrollBox.
     */
    public EditorScrollBox (EditorScenePanel panel)
    {
        super(panel.getHorizModel(), panel.getVertModel());

        _panel = panel;
        _panel.setEditorScrollBox(this);
        _panel.addComponentListener(new ComponentAdapter() {
            public void componentResized (ComponentEvent e)
            {
                SwingUtil.refresh(EditorScrollBox.this);
            }
        });

        createMiniMap(1, 1);
    }

    /**
     * Update the view of the editor map.
     */
    public void updateView ()
    {
        _updatingView = true;
        _horz.setValue(_horz.getMinimum());
        _vert.setValue(_vert.getMinimum());
    }

    /**
     * Get the graphics to draw into when rendering onto the minimap.
     */
    public Graphics2D getMiniGraphics ()
    {
        Graphics2D gfx = (Graphics2D) _miniMap.getGraphics();
        gfx.transform(AffineTransform.getTranslateInstance(_box.x, _box.y));
        gfx.transform(AffineTransform.getScaleInstance(
            _hFactor, _vFactor));

        if (_updatingView) {
            EventQueue.invokeLater(new Runnable() {
                public void run ()
                {
                    doNextPieceOfUpdate();
                }
            });
        }

        return gfx;
    }

    /**
     * Do the next piece of the update of the view.
     */
    protected void doNextPieceOfUpdate ()
    {
        int x = _horz.getValue();
        int y = _vert.getValue();
        int lastX = _horz.getMaximum() - _horz.getExtent();
        if (x != lastX) {
            x = Math.min(x + _horz.getExtent(), lastX);
        } else {
            x = _horz.getMinimum();

            int lastY = _vert.getMaximum() - _vert.getExtent();
            if (y != lastY) {
                y = Math.min(y + _vert.getExtent(), lastY);
            } else {
                // we're done
                _updatingView = false;
                return;
            }
        }

        _horz.setValue(x);
        _vert.setValue(y);
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        int horz = _horz.getMaximum() - _horz.getMinimum();
        int vert = _vert.getMaximum() - _vert.getMinimum();
        int height = MAX_HEIGHT;
        int width = (int) Math.round(horz * (((float) height) / vert));

        int maxwidth = getParent().getWidth();
        if (maxwidth > 0 && width > maxwidth) {
            height = (int) Math.round(height * (((float) maxwidth) / width));
            width = maxwidth;
        }

        return new Dimension(width, height);
    }

    // documentation inherited
    public void setBounds (int x, int y, int w, int h)
    {
        super.setBounds(x, y, w, h);

        if ((w != _miniMap.getWidth()) || (h != _miniMap.getHeight())) {
            createMiniMap(w, h);
        }
    }

    // documentation inherited
    protected void paintBackground (Graphics g)
    {
        g.drawImage(_miniMap, 0, 0, null);
    }

    /**
     * Create a mini map image of the correct dimensions.
     */
    protected void createMiniMap (int w, int h)
    {
        BufferedImage newMini = new BufferedImage(
            w, h, BufferedImage.TYPE_INT_ARGB);

        // TODO: copy stuff over?
        _miniMap = newMini;
        Graphics g = _miniMap.getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, w, h);
        g.dispose();
    }

    // documentation inherited
    protected boolean isActiveButton (MouseEvent e)
    {
        // all buttons are ok.
        return true;
    }

    /** The minimap image. */
    protected BufferedImage _miniMap;

    /** The panel we box for. */
    protected EditorScenePanel _panel;

    /** Whether or not we're updating our little view. */
    protected boolean _updatingView;

    /** Our maximum height. */
    protected static final int MAX_HEIGHT = 200;
}
