//
// $Id: SceneObjectTip.java,v 1.3 2003/06/19 22:12:51 mdb Exp $

package com.threerings.miso.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.UIManager;

import com.samskivert.swing.Label;
import com.samskivert.swing.LabelSausage;
import com.samskivert.swing.LabelStyleConstants;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.StringUtil;

/**
 * A lightweight tooltip used by the {@link MisoScenePanel}. The tip
 * foreground and background are controlled by the following {@link
 * UIManager} properties:
 *
 * <pre>
 * SceneObjectTip.background
 * SceneObjectTip.foreground
 * SceneObjectTip.font (falls back to Label.font)
 * </pre>
 */
public class SceneObjectTip extends LabelSausage
{
    /** The bounding box of this tip, or null prior to layout(). */
    public Rectangle bounds;

    /**
     * Construct a SceneObjectTip.
     */
    public SceneObjectTip (String text, Icon icon)
    {
        super(new Label(text, _foreground, _font), icon);
    }

    /**
     * Called to initialize the tip so that it can be painted.
     *
     * @param tipFor the bounding rectangle for the object we tip for.
     * @param boundary the boundary of all displayable space.
     * @param othertips other tip boundaries that we should avoid.
     */
    public void layout (Graphics2D gfx, Rectangle tipFor, Rectangle boundary,
                        Collection othertips)
    {
        layout(gfx, ICON_PAD, EXTRA_PAD);
        bounds = new Rectangle(_size);
        boundary = MAX_RECT;

        // center in the on-screen portion of the bounding box of the
        // object we're tipping for, but don't go above MAX_HEIGHT from
        // the bottom...
        Rectangle anchor = boundary.intersection(tipFor);
        bounds.setLocation(
            anchor.x + (anchor.width - bounds.width) / 2,
            anchor.y + Math.max(
                (anchor.height - bounds.height) / 2,
                anchor.height - MAX_HEIGHT));

        // and jiggle it to not overlap any other tips
        SwingUtil.positionRect(bounds, boundary, othertips);
    }

    /**
     * Paint this tip at it's location.
     */
    public void paint (Graphics2D gfx)
    {
        paint(gfx, bounds.x, bounds.y, _background, null);
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return _label.getText() + "[" + StringUtil.toString(bounds) + "]";
    }

    // documentation inherited
    protected void drawBase (Graphics2D gfx, int x, int y)
    {
        Composite ocomp = gfx.getComposite();
        gfx.setComposite(ALPHA);
        super.drawBase(gfx, x, y);
        gfx.setComposite(ocomp);
    }

    /** The alpha we use for our base. */
    protected static final Composite ALPHA = AlphaComposite.getInstance(
        AlphaComposite.SRC_OVER, .75f);

    /** Colors to use when rendering the tip. */
    protected static Color _background, _foreground;

    /** The font to use when rendering the tip. */
    protected static Font _font;

    // initialize resources shared by all tips
    static {
        _background = UIManager.getColor("SceneObjectTip.background");
        _foreground = UIManager.getColor("SceneObjectTip.foreground");
        _font = UIManager.getFont("SceneObjectTip.font");
        if (_font == null) {
            _font = UIManager.getFont("Label.font");
        }
    }

    /** The number of pixels to pad around the icon. */
    protected static final int ICON_PAD = 4;

    /** The number of pixels to pad between the icon and text. */
    protected static final int EXTRA_PAD = 2;

    /** The maximum height above the bottom of the object bounds that we are
     * to center ourselves. */
    protected static final int MAX_HEIGHT = 80;

    /** Since we don't bound our tooltips into the view, we provide this
     * generous region in which they may lay themselves out. */
    protected static final Rectangle MAX_RECT = new Rectangle(
        Integer.MIN_VALUE/2, Integer.MIN_VALUE/2,
        Integer.MAX_VALUE, Integer.MAX_VALUE);
}
