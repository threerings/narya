//
// $Id: MisoScenePanel.java,v 1.22 2003/04/28 21:46:33 mdb Exp $

package com.threerings.miso.client;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.samskivert.swing.Controller;
import com.samskivert.swing.RadialMenu;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.RuntimeAdjust;
import com.samskivert.util.StringUtil;

import com.threerings.media.VirtualMediaPanel;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;
import com.threerings.media.util.MathUtil;
import com.threerings.media.util.Path;

import com.threerings.miso.Log;
import com.threerings.miso.MisoPrefs;
import com.threerings.miso.client.DirtyItemList.DirtyItem;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.tile.BaseTile;
import com.threerings.miso.util.AStarPathUtil;
import com.threerings.miso.util.MisoContext;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;
import com.threerings.miso.util.ObjectSet;

/**
 * Renders a Miso scene for all to see.
 */
public class MisoScenePanel extends VirtualMediaPanel
    implements MouseListener, MouseMotionListener, AStarPathUtil.TraversalPred,
               RadialMenu.Host
{
    /** Show flag that indicates we should show all tips. */
    public static final int SHOW_TIPS = (1 << 0);

    /** Show flag that indicates we should show everything. */
    public static final int SHOW_ALL = 0xFFFFFF;

    /**
     * Creates a blank miso scene display. Configure it with a scene model
     * via {@link #setSceneModel} to cause it to display something.
     */
    public MisoScenePanel (MisoContext ctx, MisoSceneMetrics metrics)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
        _metrics = metrics;

        // set ourselves up
        setOpaque(true);
        addMouseListener(this);
        addMouseMotionListener(this);

        // handy rectangle
        _tbounds = new Rectangle(0, 0, _metrics.tilewid, _metrics.tilehei);

        // create the resolver if it's not already around
        if (_resolver == null) {
            _resolver = new SceneBlockResolver();
            _resolver.setDaemon(true);
            _resolver.setPriority(Thread.currentThread().getPriority()-2);
            _resolver.start();
        }
    }

    /**
     * Configures this display with a scene model which will immediately
     * be resolved and displayed.
     */
    public void setSceneModel (MisoSceneModel model)
    {
        _model = model;

        // clear out old blocks and objects
        _blocks.clear();
        _vizobjs.clear();
        _masks.clear();

        centerOnTile(0, 0);
        if (isShowing()) {
            rethink();
            _remgr.invalidateRegion(_vbounds);
        }
    }

    /**
     * Moves the scene such that the specified tile is in the center.
     */
    public void centerOnTile (int tx, int ty)
    {
        Rectangle trect = MisoUtil.getTilePolygon(_metrics, tx, ty).getBounds();
        int nx = trect.x + trect.width/2 - _vbounds.width/2;
        int ny = trect.y + trect.height/2 - _vbounds.height/2;
//         Log.info("Centering on t:" + StringUtil.coordsToString(tx, ty) +
//                  " b:" + StringUtil.toString(trect) +
//                  " vb: " + StringUtil.toString(_vbounds) +
//                  ", n:" + StringUtil.coordsToString(nx, ny) + ".");
        setViewLocation(nx, ny);
    }

    /**
     * Returns the scene model being displayed by this panel. <em>Do
     * not</em> modify!
     */
    public MisoSceneModel getSceneModel ()
    {
        return _model;
    }

    /**
     * Returns the scene metrics in use by this panel. <em>Do not</em>
     * modify!
     */
    public MisoSceneMetrics getSceneMetrics ()
    {
        return _metrics;
    }

    /**
     * Set whether or not to highlight object tooltips (and potentially
     * other scene entities).
     */
    public void setShowFlags (int flags, boolean on)
    {
        int oldshow = _showFlags;

        if (on) {
            _showFlags |= flags;
        } else {
            _showFlags &= ~flags;
        }

        if (oldshow != _showFlags) {
            showFlagsDidChange(oldshow);
        }
    }

    /**
     * Check to see if the specified show flag is on.
     */
    public boolean checkShowFlag (int flag)
    {
        return (0 != (flag & _showFlags));
    }

    /**
     * Called when our show flags have changed.
     */
    protected void showFlagsDidChange (int oldflags)
    {
        if ((oldflags & SHOW_TIPS) != (_showFlags & SHOW_TIPS)) {
            for (Iterator iter = _tips.values().iterator(); iter.hasNext(); ) {
                dirtyTip((SceneObjectTip)iter.next());
            }
        }
    }

    /**
     * Returns the top-most object over which the mouse is hovering; this
     * may be a sprite or a {@link SceneObject}.
     */
    public Object getHoverObject ()
    {
        return _hobject;
    }

    /**
     * Returns the tile coordinates of the tile over which the mouse is
     * hovering.
     */
    public Point getHoverCoords ()
    {
        return _hcoords;
    }

    /**
     * Computes a path for the specified sprite to the specified tile
     * coordinates.
     */
    public Path getPath (Sprite sprite, int x, int y)
    {
        // get the destination tile coordinates
        Point src = MisoUtil.screenToTile(
            _metrics, sprite.getX(), sprite.getY(), new Point());
        Point dest = MisoUtil.screenToTile(_metrics, x, y, new Point());

        // TODO: compute this value from the screen size or something
        int longestPath = 50;

        // get a reasonable tile path through the scene
        List points = AStarPathUtil.getPath(
            this, sprite, longestPath, src.x, src.y, dest.x, dest.y);

        // construct a path object to guide the sprite on its merry way
        return (points == null) ? null :
            new TilePath(_metrics, sprite, points, x, y);
    }

    /**
     * Converts the supplied full coordinates to screen coordinates.
     */
    public Point getScreenCoords (int x, int y)
    {
        return MisoUtil.fullToScreen(_metrics, x, y, new Point());
    }

    /**
     * Coverts the supplied screen coordinates to full coordinates.
     */
    public Point getFullCoords (int x, int y)
    {
        return MisoUtil.screenToFull(_metrics, x, y, new Point());
    }

    /**
     * Coverts the supplied screen coordinates to tile coordinates.
     */
    public Point getTileCoords (int x, int y)
    {
        return MisoUtil.screenToTile(_metrics, x, y, new Point());
    }

    /**
     * Clears any radial menu being displayed.
     */
    public void clearRadialMenu ()
    {
        if (_activeMenu != null) {
            _activeMenu.deactivate();
        }
    }

    // documentation inherited from interface
    public void mouseClicked (MouseEvent e)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void mousePressed (MouseEvent e)
    {
        // ignore mouse presses if we're not responsive
        if (!isResponsive()) {
            return;
        }

        if (_hobject instanceof Sprite) {
            handleSpritePressed((Sprite)_hobject, e.getX(), e.getY());
        } else if (_hobject instanceof SceneObject) {
            handleObjectPressed((SceneObject)_hobject, e.getX(), e.getY());
        } else {
            handleMousePressed(_hobject, e);
        }
    }

    /**
     * Called when the user presses the mouse button over a sprite.
     */
    protected void handleSpritePressed (Sprite sprite, int mx, int my)
    {
    }

    /**
     * Called when the user presses the mouse button over an object.
     */
    protected void handleObjectPressed (final SceneObject scobj, int mx, int my)
    {
        String action = scobj.info.action;
        final ObjectActionHandler handler = ObjectActionHandler.lookup(action);

        // if there's no handler, just fire the action immediately
        if (handler == null) {
            fireObjectAction(null, scobj, new ActionEvent(this, 0, action, 0));
            return;
        }

        // if the action's not allowed, pretend like we handled it
        if (!handler.actionAllowed(action)) {
            return;
        }

        // if there's no menu for this object, fire the action immediately
        RadialMenu menu = handler.handlePressed(scobj);
        if (menu == null) {
            fireObjectAction(handler, scobj,
                             new ActionEvent(this, 0, action, 0));
            return;
        }

        // make the menu surround the clicked object, but don't go crazy
        // if the object is huge
        Rectangle mbounds = new Rectangle(scobj.bounds);
        if (mbounds.width > 100) {
            mbounds.x += (mbounds.width-100)/2;
            mbounds.width = 100;
        }
        if (mbounds.height > 100) {
            mbounds.y += (mbounds.height-100)/2;
            mbounds.height = 100;
        }

        _activeMenu = menu;
        _activeMenu.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                fireObjectAction(handler, scobj, e);
            }
        });
        _activeMenu.activate(this, mbounds, scobj);
    }

    /**
     * Called when an object or object menu item has been clicked.
     */
    protected void fireObjectAction (
        ObjectActionHandler handler, SceneObject scobj, ActionEvent event)
    {
        if (handler == null) {
            Controller.postAction(event);
        } else {
            handler.handleAction(scobj, event);
        }
    }

    /**
     * Called when the mouse is pressed over an unknown or non-existent
     * hover object.
     *
     * @param hobject the hover object at the time of the mouse press or
     * null if no hover object is active.
     *
     * @return true if the mouse press was handled, false if not.
     */
    protected boolean handleMousePressed (Object hobject, MouseEvent event)
    {
        return false;
    }

    // documentation inherited from interface
    public void mouseReleased (MouseEvent e)
    {
        // nothing doing; everything is handled on pressed
    }

    // documentation inherited from interface
    public void mouseEntered (MouseEvent e)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void mouseExited (MouseEvent e)
    {
        // clear the highlight tracking data
        _hcoords.setLocation(-1, -1);
        changeHoverObject(null);
        _remgr.invalidateRegion(_vbounds);
    }

    // documentation inherited from interface
    public void mouseDragged (MouseEvent e)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void mouseMoved (MouseEvent e)
    {
        int x = e.getX(), y = e.getY();

        // update the mouse's tile coordinates
        updateTileCoords(x, y, _hcoords);

        // stop now if we're not responsive
        if (!isResponsive()) {
            return;
        }

        // give derived classes a chance to start with a hover object
        Object hobject = computeOverHover(x, y);

        // if they came up with nothing, compute the list of objects over
        // which the mouse is hovering
        if (hobject == null) {
            // start with the sprites that contain the point
            _spritemgr.getHitSprites(_hitSprites, x, y);
            int hslen = _hitSprites.size();
            for (int i = 0; i < hslen; i++) {
                Sprite sprite = (Sprite)_hitSprites.get(i);
                appendDirtySprite(_hitList, sprite);
            }

            // add the object tiles that contain the point
            getHitObjects(_hitList, x, y);

            // sort the list of hit items by rendering order
            _hitList.sort();

            // the last element in the array is what we want (assuming
            // there are any items in the array)
            int icount = _hitList.size();
            if (icount > 0) {
                DirtyItem item = (DirtyItem)_hitList.get(icount-1);
                hobject = item.obj;
            }

            // clear out the hitlists
            _hitList.clear();
            _hitSprites.clear();
        }

        // if the user isn't hovering over a sprite or object with an
        // action, allow derived classes to provide some other hover
        if (hobject == null) {
            hobject = computeUnderHover(x, y);
        }

        changeHoverObject(hobject);
    }

    /**
     * Gives derived classes a chance to compute a hover object that takes
     * precedence over sprites and actionable objects. If this method
     * returns non-null, no sprite or object hover calculations will be
     * performed and the object returned will become the new hover object.
     */
    protected Object computeOverHover (int mx, int my)
    {
        return null;
    }

    /**
     * Gives derived classes a chance to compute a hover object that is
     * used if the mouse is not hovering over a sprite or actionable
     * object. If this method is called, it means that there are no
     * sprites or objects under the mouse. Thus if it returns non-null,
     * the object returned will become the new hover object.
     */
    protected Object computeUnderHover (int mx, int my)
    {
        return null;
    }

    // documentation inherited
    public boolean canTraverse (Object traverser, int tx, int ty)
    {
        SceneBlock block = getBlock(tx, ty);
        return (block == null) ? false : block.canTraverse(traverser, tx, ty);
    }

    // documentation inherited from interface
    public Rectangle getViewBounds ()
    {
        return _vbounds;
    }

    // documentation inherited from interface
    public Component getComponent ()
    {
        return this;
    }

    // documentation inherited from interface
    public void repaintRect (int x, int y, int width, int height)
    {
        // translate back into view coordinates
        x -= _vbounds.x;
        y -= _vbounds.y;
        repaint(x, y, width, height);
    }

    // documentation inherited from interface
    public void menuDeactivated (RadialMenu menu)
    {
        _activeMenu = null;
    }

    // documentation inherited
    public void setBounds (int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        // if we change size...
        if (width != _rsize.width || height != _rsize.height) {
            // ...adjust our view location to preserve the center of the
            // screen...
            int dx = (_rsize.width-width)/2, dy = (_rsize.height-height)/2;
//             Log.info("Adjusting offset " +
//                      "rsize:" + StringUtil.toString(_rsize) +
//                      " nsize:" + width + "x" + height +
//                      " vb:" + StringUtil.toString(_vbounds) +
//                      " d:" + StringUtil.coordsToString(dx, dy) + ".");
            setViewLocation(_nx+dx, _ny+dy);
            _rsize.setSize(width, height);

            // ...and force a rethink on the next tick
            _ulpos = null;
        }
    }

    // documentation inherited
    protected void viewLocationDidChange (int dx, int dy)
    {
        super.viewLocationDidChange(dx, dy);

        // compute the tile coordinates of our upper left screen
        // coordinate and request a rethink if they've changed
        MisoUtil.screenToTile(_metrics, _vbounds.x, _vbounds.y, _tcoords);
        if (_ulpos == null || !_tcoords.equals(_ulpos)) {
            // if this is a forced rethink (_ulpos is null), we might
            // delay paint as a result of it, but only if we queue up
            // blocks for resolution in our rethink
            boolean mightDelayPaint = false;
            if (_ulpos == null) {
                _ulpos = new Point();
                mightDelayPaint = true;
            }
            _ulpos.setLocation(_tcoords);
            if (rethink() > 0) {
                _delayRepaint = mightDelayPaint;
            }
        }
    }

    /**
     * Derived classes can override this method and provide a colorizer
     * that will be used to colorize the supplied scene object when
     * rendering.
     */
    protected TileSet.Colorizer getColorizer (ObjectInfo oinfo)
    {
        return null;
    }

    /**
     * Computes the tile coordinates of the supplied sprite and appends it
     * to the supplied dirty item list.
     */
    protected void appendDirtySprite (DirtyItemList list, Sprite sprite)
    {
        MisoUtil.screenToTile(_metrics, sprite.getX(), sprite.getY(), _tcoords);
        list.appendDirtySprite(sprite, _tcoords.x, _tcoords.y);
    }

    /**
     * Returns the tile manager from which we load our tiles.
     */
    protected TileManager getTileManager ()
    {
        return _ctx.getTileManager();
    }

    /**
     * This is called when our view position has changed by more than one
     * tile in any direction. Herein we do a whole crapload of stuff:
     *
     * <ul><li> Queue up loads for any new influential blocks.
     * <li> Flush any blocks that are no longer influential.
     * <li> Recompute the list of potentially visible scene objects.
     * </ul>
     *
     * @return the count of blocks pending after this rethink.
     */
    protected int rethink ()
    {
        // recompute our "area of influence"
        computeInfluentialBounds();

//         Log.info("Rethinking vb:" + StringUtil.toString(_vbounds) +
//                  " ul:" + StringUtil.toString(_ulpos) +
//                  " ibounds: " + StringUtil.toString(_ibounds));

        // not to worry if we presently have no scene model
        if (_model == null) {
            return 0;
        }

        // compute the intersecting set of blocks
        applyToTiles(_ibounds, _rethinkOp);
//         Log.info("Influential blocks " +
//                  StringUtil.toString(_rethinkOp.blocks) + ".");

        // prune any blocks that are no longer influential
        Point key = new Point();
        for (Iterator iter = _blocks.values().iterator(); iter.hasNext(); ) {
            SceneBlock block = (SceneBlock)iter.next();
            key.x = block.getBounds().x;
            key.y = block.getBounds().y;
            if (!_rethinkOp.blocks.contains(key)) {
//                 Log.info("Flushing block " + block + ".");
                iter.remove();
            }
        }

        for (Iterator iter = _rethinkOp.blocks.iterator(); iter.hasNext(); ) {
            Point origin = (Point)iter.next();
            int bx = MathUtil.floorDiv(origin.x, _metrics.blockwid);
            int by = MathUtil.floorDiv(origin.y, _metrics.blockhei);
            int bkey = compose(bx, by);
            if (!_blocks.containsKey(bkey)) {
                SceneBlock block = new SceneBlock(
                    this, origin.x, origin.y,
                    _metrics.blockwid, _metrics.blockhei);
                _blocks.put(bkey, block);
//                 Log.info("Created new block " + block + ".");

                // queue the block up to be resolved
                _pendingBlocks++;
                _resolver.resolveBlock(block);
            }
        }
        _rethinkOp.blocks.clear();

        // recompute our visible object set
        recomputeVisible();

        return _pendingBlocks;
    }

    /**
     * Called during the {@link #rethink} process, configures {@link
     * #_ibounds} to contain the bounds of the potentially "influential"
     * world. Everything that intersects the influential area will be
     * resolved on the expectation that it could be scrolled into view at
     * any time. The influential bounds should be large enough that the
     * time between a block becoming influential and the time at which it
     * is resolved is longer than the expected time by which it will be
     * scrolled into view, otherwise the users will see the man behind the
     * curtain.
     */
    protected void computeInfluentialBounds ()
    {
        int infborx = _vbounds.width/3;
        int infbory = _vbounds.height/3;
        _ibounds.setBounds(_vbounds.x-infborx, _vbounds.y-infbory,
                           _vbounds.width+2*infborx,
                           // we go extra on the height because objects
                           // below can influence fairly high up
                           _vbounds.height+3*infbory);
    }

    /**
     * Called by a scene block when it has completed its resolution
     * process.
     */
    protected void blockResolved (SceneBlock block)
    {
        Rectangle sbounds = block.getScreenBounds();
        if (!_delayRepaint && sbounds != null && sbounds.intersects(_vbounds)) {
            Log.warning("Block visible during resolution " + block + ".");
        }

        // once all the pending blocks have completed their resolution,
        // recompute our visible object set
        if (--_pendingBlocks == 0) {
            recomputeVisible();
            _delayRepaint = false;
            _remgr.invalidateRegion(_vbounds);
        }
    }

    /**
     * Recomputes our set of visible objects and their tips.
     */
    protected void recomputeVisible ()
    {
        // flush our visible object set which we'll recreate later
        _vizobjs.clear();

        Rectangle vbounds = new Rectangle(
            _vbounds.x-_metrics.tilewid, _vbounds.y-_metrics.tilehei,
            _vbounds.width+2*_metrics.tilewid,
            _vbounds.height+2*_metrics.tilehei);

        for (Iterator iter = _blocks.values().iterator(); iter.hasNext(); ) {
            SceneBlock block = (SceneBlock)iter.next();
            if (!block.isResolved()) {
                continue;
            }

            // links this block to its neighbors; computes coverage
            block.update(_blocks);

            // see which of this block's objects are visible
            SceneObject[] objs = block.getObjects();
            for (int ii = 0; ii < objs.length; ii++) {
                if (objs[ii].bounds != null &&
                    vbounds.intersects(objs[ii].bounds)) {
                    _vizobjs.add(objs[ii]);
                }
            }
        }

        // recompute our object tips
        computeTips();

//         Log.info("Computed " + _vizobjs.size() + " visible objects from " +
//                  _blocks.size() + " blocks.");

//         Log.info(StringUtil.listToString(_vizobjs, new StringUtil.Formatter() {
//             public String toString (Object object) {
//                 SceneObject scobj = (SceneObject)object;
//                 return (TileUtil.getTileSetId(scobj.info.tileId) + ":" +
//                         TileUtil.getTileIndex(scobj.info.tileId));
//             }
//         }));
    }

    /**
     * Returns the resolved block that contains the specified tile
     * coordinate or null if no block is resolved for that coordinate.
     */
    protected SceneBlock getBlock (int tx, int ty)
    {
        int bx = MathUtil.floorDiv(tx, _metrics.blockwid);
        int by = MathUtil.floorDiv(ty, _metrics.blockhei);
        return (SceneBlock)_blocks.get(compose(bx, by));
    }

    /**
     * Masks off the lower 16 bits of the supplied integers and composes
     * them into a single int.
     */
    protected static int compose (int x, int y)
    {
        return (x << 16) | (y & 0xFFFF);
    }

    /**
     * Compute the tips for any objects in the scene.
     */
    protected void computeTips ()
    {
        // clear any old tips
        _tips.clear();

        for (int ii = 0, nn = _vizobjs.size(); ii < nn; ii++) {
            SceneObject scobj = (SceneObject)_vizobjs.get(ii);
            String action = scobj.info.action;

            // if the object has no action, skip it
            if (StringUtil.blank(action)) {
                continue;
            }

            // if we have an object action handler, possibly let them veto
            // the display of this tooltip and action
            ObjectActionHandler oah = ObjectActionHandler.lookup(action);
            if (oah != null && !oah.actionAllowed(action)) {
                continue;
            }

            String tiptext = getTipText(scobj, action);
            if (tiptext != null) {
                Icon icon = (oah == null) ? null : oah.getTipIcon(action);
                SceneObjectTip tip = new SceneObjectTip(tiptext, icon);
                _tips.put(scobj, tip);
            }
        }

        _tipsLaidOut = false;
    }

    /**
     * Derived classes can provide human readable object tips via this
     * method.
     */
    protected String getTipText (SceneObject scobj, String action)
    {
        return action;
    }

    /**
     * Dirties the specified tip.
     */
    protected void dirtyTip (SceneObjectTip tip)
    {
        if (tip != null) {
            Rectangle r = tip.bounds;
            if (r != null) {
                _remgr.invalidateRegion(r);
            }
        }
    }

    /**
     * Change the hover object to the new object.
     *
     * @return true if we need to repaint the entire scene. Bah!
     */
    protected void changeHoverObject (Object newHover)
    {
        if (newHover == _hobject) {
            return;
        }
        Object oldHover = _hobject;
        _hobject = newHover;
        hoverObjectChanged(oldHover, newHover);
    }

    /**
     * A place for subclasses to react to the hover object changing.
     * One of the supplied arguments may be null.
     */
    protected void hoverObjectChanged (Object oldHover, Object newHover)
    {
        // deal with objects that care about being hovered over
        if (oldHover instanceof SceneObject) {
            SceneObject oldhov = (SceneObject)oldHover;
            if (oldhov.setHovered(false)) {
                _remgr.invalidateRegion(oldhov.bounds);
            }
        }
        if (newHover instanceof SceneObject) {
            SceneObject newhov = (SceneObject)newHover;
            if (newhov.setHovered(true)) {
                _remgr.invalidateRegion(newhov.bounds);
            }
        }

        // dirty the tips associated with the hover objects
        dirtyTip((SceneObjectTip)_tips.get(oldHover));
        dirtyTip((SceneObjectTip)_tips.get(newHover));
    }

    /**
     * Adds to the supplied dirty item list, all of the object tiles that
     * are hit by the specified point (meaning the point is contained
     * within their bounds and intersects a non-transparent pixel in the
     * actual object image.
     */
    protected void getHitObjects (DirtyItemList list, int x, int y)
    {
        for (int ii = 0, ll = _vizobjs.size(); ii < ll; ii++) {
            SceneObject scobj = (SceneObject)_vizobjs.get(ii);
            Rectangle pbounds = scobj.bounds;
            if (!pbounds.contains(x, y)) {
                continue;
            }

            // skip actionless objects if we're configured to do so
            if (skipActionlessObjects() &&
                StringUtil.blank(scobj.info.action)) {
                continue;
            }

            // now check that the pixel in the tile image is
            // non-transparent at that point
            if (!scobj.tile.hitTest(x - pbounds.x, y - pbounds.y)) {
                continue;
            }

            // we've passed the test, add the object to the list
            list.appendDirtyObject(scobj);
        }
    }

    /**
     * Indicates whether or not actionless objects are allowed to be the
     * hover object. By default, they are not.
     */
    protected boolean skipActionlessObjects ()
    {
        return true;
    }

    /**
     * Converts the supplied screen coordinates into tile coordinates,
     * writing the values into the supplied {@link Point} instance and
     * returning true if the screen coordinates translated into a
     * different set of tile coordinates than were already contained in
     * the point (so that the caller can know to update a highlight, for
     * example).
     *
     * @return true if the tile coordinates have changed.
     */
    protected boolean updateTileCoords (int sx, int sy, Point tpos)
    {
        Point npos = MisoUtil.screenToTile(_metrics, sx, sy, new Point());
        if (!tpos.equals(npos)) {
            tpos.setLocation(npos.x, npos.y);
            return true;
        } else {
            return false;
        }
    }

    // documentation inherited
    public void paint (Graphics g)
    {
        if (_delayRepaint) {
            return;
        }
        super.paint(g);
    }

    // documentation inherited
    protected void paintInFront (Graphics2D gfx, Rectangle dirty)
    {
        super.paintInFront(gfx, dirty);

        // paint any active menu (this should in theory check to see if
        // the active menu intersects one or more of the dirty rects)
        if (_activeMenu != null) {
            _activeMenu.render(gfx);
        }
    }

    // documentation inherited
    protected void paintBetween (Graphics2D gfx, Rectangle dirty)
    {
        // render any intersecting tiles
        paintTiles(gfx, dirty);

        // render anything that goes on top of the tiles
        paintBaseDecorations(gfx, dirty);

        // render our dirty sprites and objects
        paintDirtyItems(gfx, dirty);

        // draw sprite paths
        if (_pathsDebug.getValue()) {
            _spritemgr.renderSpritePaths(gfx);
        }

        // paint any extra goodies
        paintExtras(gfx, dirty);
    }

    /**
     * We don't want sprites rendered using the standard mechanism because
     * we intersperse them with objects in our scene and need to manage
     * their z-order.
     */
    protected void paintBits (Graphics2D gfx, int layer, Rectangle dirty)
    {
        _animmgr.renderMedia(gfx, layer, dirty);
    }

    /**
     * A function where derived classes can paint things after the base
     * tiles have been rendered but before anything else has been rendered
     * (so that whatever is painted appears to be on the ground).
     */
    protected void paintBaseDecorations (Graphics2D gfx, Rectangle clip)
    {
        // nothing for now
    }

    /**
     * Renders the dirty sprites and objects in the scene to the given
     * graphics context.
     */
    protected void paintDirtyItems (Graphics2D gfx, Rectangle clip)
    {
        // add any sprites impacted by the dirty rectangle
        _dirtySprites.clear();
        _spritemgr.getIntersectingSprites(_dirtySprites, clip);
        int size = _dirtySprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_dirtySprites.get(ii);
            Rectangle bounds = sprite.getBounds();
            if (!bounds.intersects(clip)) {
                continue;
            }
            appendDirtySprite(_dirtyItems, sprite);
//             Log.info("Dirtied item: " + sprite);
        }

        // add any objects impacted by the dirty rectangle
        for (int ii = 0, ll = _vizobjs.size(); ii < ll; ii++) {
            SceneObject scobj = (SceneObject)_vizobjs.get(ii);
            if (!scobj.bounds.intersects(clip)) {
                continue;
            }
            _dirtyItems.appendDirtyObject(scobj);
//             Log.info("Dirtied item: " + scobj);
        }

//         Log.info("paintDirtyItems [items=" + _dirtyItems.size() + "].");

        // sort the dirty items so that we can paint them back-to-front
        _dirtyItems.sort();
        _dirtyItems.paintAndClear(gfx);
    }

    /**
     * A function where derived classes can paint extra stuff while we've
     * got the clipping region set up.
     */
    protected void paintExtras (Graphics2D gfx, Rectangle clip)
    {
        if (isResponsive()) {
            paintTips(gfx, clip);
        }
    }

    /**
     * Paint all the appropriate tips for our scene objects.
     */
    protected void paintTips (Graphics2D gfx, Rectangle clip)
    {
        // make sure the tips are ready
        if (!_tipsLaidOut) {
            ArrayList boxlist = new ArrayList();
            for (Iterator iter = _tips.keySet().iterator(); iter.hasNext(); ) {
                SceneObject scobj = (SceneObject)iter.next();
                SceneObjectTip tip = (SceneObjectTip)_tips.get(scobj);
                tip.layout(gfx, scobj.bounds, _vbounds, boxlist);
                boxlist.add(tip.bounds);
            }
            _tipsLaidOut = true;
        }

        if (checkShowFlag(SHOW_TIPS)) {
            // show all the tips
            for (Iterator iter = _tips.values().iterator(); iter.hasNext(); ) {
                paintTip(gfx, clip, (SceneObjectTip)iter.next());
            }

        } else {
            // show maybe one tip
            SceneObjectTip tip = (SceneObjectTip)_tips.get(_hobject);
            if (tip != null) {
                paintTip(gfx, clip, tip);
            }
        }
    }

    /**
     * Paint the specified tip if it intersects the clipping rectangle.
     */
    protected void paintTip (Graphics2D gfx, Rectangle clip, SceneObjectTip tip)
    {
        if (clip.intersects(tip.bounds)) {
            tip.paint(gfx);
        }
    }

    /**
     * Applies the supplied tile operation to all tiles that intersect the
     * supplied screen rectangle.
     */
    protected void applyToTiles (Rectangle bounds, TileOp op)
    {
        // determine which tiles intersect this region: this is going to
        // be nearly incomprehensible without some sort of diagram; i'll
        // do what i can to comment it, but you'll want to print out a
        // scene diagram (docs/miso/scene.ps) and start making notes if
        // you want to follow along

        // obtain our upper left tile
        Point tpos = MisoUtil.screenToTile(
            _metrics, bounds.x, bounds.y, new Point());

        // determine which quadrant of the upper left tile we occupy
        Point spos = MisoUtil.tileToScreen(
            _metrics, tpos.x, tpos.y, new Point());
        boolean left = (bounds.x - spos.x < _metrics.tilehwid);
        boolean top = (bounds.y - spos.y < _metrics.tilehhei);

        // set up our tile position counters
        int dx, dy;
        if (left) {
            dx = 0; dy = 1;
        } else {
            dx = 1; dy = 0;
        }

        // if we're in the top-half of the tile we need to move up a row,
        // either forward or back depending on whether we're in the left
        // or right half of the tile
        if (top) {
            if (left) {
                tpos.x -= 1;
            } else {
                tpos.y -= 1;
            }
            // we'll need to start zig-zagging the other way as well
            dx = 1 - dx;
            dy = 1 - dy;
        }

        // these will bound our loops
        int rightx = bounds.x + bounds.width,
            bottomy = bounds.y + bounds.height;

//         Log.info("Preparing to apply [tpos=" + StringUtil.toString(tpos) +
//                  ", left=" + left + ", top=" + top +
//                  ", bounds=" + StringUtil.toString(bounds) +
//                  ", spos=" + StringUtil.toString(spos) +
//                  "].");

        // obtain the coordinates of the tile that starts the first row
        // and loop through, applying to the intersecting tiles
        MisoUtil.tileToScreen(_metrics, tpos.x, tpos.y, spos);
        while (spos.y < bottomy) {
            // set up our row counters
            int tx = tpos.x, ty = tpos.y;
            _tbounds.x = spos.x;
            _tbounds.y = spos.y;

//             Log.info("Applying to row [tx=" + tx + ", ty=" + ty + "].");

            // apply to the tiles in this row
            while (_tbounds.x < rightx) {
                op.apply(tx, ty, _tbounds);

                // move one tile to the right
                tx += 1; ty -= 1;
                _tbounds.x += _metrics.tilewid;
            }

            // update our tile coordinates
            tpos.x += dx; dx = 1-dx;
            tpos.y += dy; dy = 1-dy;

            // obtain the screen coordinates of the next starting tile
            MisoUtil.tileToScreen(_metrics, tpos.x, tpos.y, spos);
        }
    }

    /**
     * Renders the base and fringe layer tiles that intersect the
     * specified clipping rectangle.
     */
    protected void paintTiles (Graphics2D gfx, Rectangle clip)
    {
        // go through rendering our tiles
        _paintOp.setGraphics(gfx);
        applyToTiles(clip, _paintOp);
        _paintOp.setGraphics(null);
    }

    /**
     * Fills the specified tile with the given color at 50% alpha.
     * Intended for debug-only tile highlighting purposes.
     */
    protected void fillTile (
        Graphics2D gfx, int tx, int ty, Color color)
    {
        Composite ocomp = gfx.getComposite();
        gfx.setComposite(ALPHA_FILL_TILE);
        Polygon poly = MisoUtil.getTilePolygon(_metrics, tx, ty);
        gfx.setColor(color);
        gfx.fill(poly);
        gfx.setComposite(ocomp);
    }

    /** Returns the base tile for the specified tile coordinate. */
    protected BaseTile getBaseTile (int tx, int ty)
    {
        SceneBlock block = getBlock(tx, ty);
        return (block == null) ? null : block.getBaseTile(tx, ty);
    }

    /** Returns the fringe tile for the specified tile coordinate. */
    protected Tile getFringeTile (int tx, int ty)
    {
        SceneBlock block = getBlock(tx, ty);
        return (block == null) ? null : block.getFringeTile(tx, ty);
    }

    /** Computes the fringe tile for the specified coordinate. */
    protected Tile computeFringeTile (int tx, int ty)
    {
        return _ctx.getTileManager().getAutoFringer().getFringeTile(
            _model, tx, ty, _masks, _rando);
    }

    /**
     * Returns true if we're responding to user input. This is used to
     * control the display of tooltips and other potential user
     * interactions. By default we are always responsive.
     */
    protected boolean isResponsive ()
    {
        return true;
    }

    /** Used with {@link #applyToTiles}. */
    protected static interface TileOp
    {
        public void apply (int tx, int ty, Rectangle tbounds);
    }

    /** Used by {@link #paintTiles}. */
    protected class PaintTileOp implements TileOp
    {
        public void setGraphics (Graphics2D gfx) {
            _gfx = gfx;
            _thw = 0;
            _thh = 0;
            _fhei = 0;
            _fm = null;

            // if we're showing coordinates, we need to do some setting up
            if (gfx != null && _coordsDebug.getValue()) {
                _fm = gfx.getFontMetrics(_font);
                _fhei = _fm.getAscent();
                _thw = _metrics.tilehwid;
                _thh = _metrics.tilehhei;
                gfx.setFont(_font);
            }
        }

        public void apply (int tx, int ty, Rectangle tbounds) {
            // draw the base and fringe tile images
            try {
                Tile tile;
                boolean passable = true;

                if ((tile = getBaseTile(tx, ty)) != null) {
                    tile.paint(_gfx, tbounds.x, tbounds.y);
                    passable = ((BaseTile)tile).isPassable();

                    // highlight impassable tiles
                    if (_traverseDebug.getValue() && !passable) {
                        fillTile(_gfx, tx, ty, Color.yellow);
                    }

                } else {
                    // draw black where there are no tiles
                    Polygon poly = MisoUtil.getTilePolygon(_metrics, tx, ty);
                    _gfx.setColor(Color.black);
                    _gfx.fill(poly);
                }

                if ((tile = getFringeTile(tx, ty)) != null) {
                    tile.paint(_gfx, tbounds.x, tbounds.y);
                }

                // highlight passable non-traversable tiles
                if (_traverseDebug.getValue() && passable && 
                    !canTraverse(null, tx, ty)) {
                    fillTile(_gfx, tx, ty, Color.green);
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                Log.warning("Whoops, booched it [tx=" + tx +
                            ", ty=" + ty + ", tb.x=" + tbounds.x + "].");
                e.printStackTrace(System.err);
            }

            // if we're showing coordinates, do that
            if (_coordsDebug.getValue()) {
                // set the color according to the scene block
                int bx = MathUtil.floorDiv(tx, _metrics.blockwid);
                int by = MathUtil.floorDiv(ty, _metrics.blockhei);
                if (((bx % 2) ^ (by % 2)) == 0) {
                    _gfx.setColor(Color.white);
                } else {
                    _gfx.setColor(Color.yellow);
                }

                // get the top-left screen coordinates of the tile
                int sx = tbounds.x, sy = tbounds.y;

                // draw x-coordinate
                String str = String.valueOf(tx);
                int xpos = sx + _thw - (_fm.stringWidth(str) / 2);
                _gfx.drawString(str, xpos, sy + _thh);

                // draw y-coordinate
                str = String.valueOf(ty);
                xpos = sx + _thw - (_fm.stringWidth(str) / 2);
                _gfx.drawString(str, xpos, sy + _thh + _fhei);

                // draw the tile polygon as well
                _gfx.draw(MisoUtil.getTilePolygon(_metrics, tx, ty));
            }
        }

        protected Graphics2D _gfx;
        protected FontMetrics _fm;
        protected int _thw, _thh, _fhei;
        protected Font _font = new Font("Arial", Font.PLAIN, 7);
    }

    /** Used by {@link #rethink}. */
    protected class RethinkOp implements TileOp
    {
        public HashSet blocks = new HashSet();

        public HashSet vizblocks = new HashSet();

        public void apply (int tx, int ty, Rectangle tbounds) {
            _key.x = MathUtil.floorDiv(tx, _metrics.blockwid) *
                _metrics.blockwid;
            _key.y = MathUtil.floorDiv(ty, _metrics.blockhei) *
                _metrics.blockhei;
            if (!blocks.contains(_key)) {
                blocks.add(new Point(_key.x, _key.y));
            }
        }

        protected Point _key = new Point();
    }

    /** Provides access to a few things. */
    protected MisoContext _ctx;

    /** Contains basic scene metrics like tile width and height. */
    protected MisoSceneMetrics _metrics;

    /** The scene model to be displayed. */
    protected MisoSceneModel _model;

    /** Used to colorize object tiles in this scene. */
    protected TileSet.Colorizer _rizer;

    /** Tracks the size at which we were last "rethunk". */
    protected Dimension _rsize = new Dimension();

    /** Contains the tile coords of our upper-left view coord. */
    protected Point _ulpos;

    /** Contains the bounds of our "area of influence" in screen coords. */
    protected Rectangle _ibounds = new Rectangle();

    /** Used by {@link #rethink}. */
    protected RethinkOp _rethinkOp = new RethinkOp();

    /** Contains our scene blocks. See {@link #getBlock} for details. */
    protected HashIntMap _blocks = new HashIntMap();

    /** A count of blocks in the process of being resolved. */
    protected int _pendingBlocks;

    /** Used to avoid repaints while we don't yet have resolved all the
     * blocks needed to render the visible view. */
    protected boolean _delayRepaint = false;

    /** A list of the potentially visible objects in the scene. */
    protected ArrayList _vizobjs = new ArrayList();

    /** For computing fringe tiles. */
    protected HashMap _masks = new HashMap();

    /** For computing fringe tiles. */
    protected Random _rando = new Random();

    /** The dirty sprites and objects that need to be re-painted. */
    protected DirtyItemList _dirtyItems = new DirtyItemList();

    /** The working sprites list used when calculating dirty regions. */
    protected ArrayList _dirtySprites = new ArrayList();

    /** Used when rendering tiles. */
    protected Rectangle _tbounds;

    /** Used to paint tiles. */
    protected PaintTileOp _paintOp = new PaintTileOp();

    /** Used when dirtying sprites. */
    protected Point _tcoords = new Point();

    /** Used to collect the list of sprites "hit" by a particular mouse
     * location. */
    protected List _hitSprites = new ArrayList();

    /** The list that we use to track and sort the items over which the
     * mouse is hovering. */
    protected DirtyItemList _hitList = new DirtyItemList();

    /** Info on the object that the mouse is currently hovering over. */
    protected Object _hobject;

    /** The item that the user has clicked on with the mouse. */
    protected Object _armedItem = null;

    /** The active radial menu (or null). */
    protected RadialMenu _activeMenu;

    /** Used to track the tile coordinates over which the mouse is hovering. */
    protected Point _hcoords = new Point();

    /** Our object tips, indexed by the object that they tip for. */
    protected HashMap _tips = new HashMap();

    /** Have the tips been laid out? */
    protected boolean _tipsLaidOut = false;

    /** Flags indicating which features we should show in the scene. */
    protected int _showFlags = 0;

    /** A scene block resolver shared by all scene panels. */
    protected static SceneBlockResolver _resolver;

    /** A debug hook that toggles debug rendering of traversable tiles. */
    protected static RuntimeAdjust.BooleanAdjust _traverseDebug =
        new RuntimeAdjust.BooleanAdjust(
            "Toggles debug rendering of traversable and impassable tiles in " +
            "the iso scene view.", "narya.miso.iso_traverse_debug_render",
            MisoPrefs.config, false);

    /** A debug hook that toggles debug rendering of tile coordinates. */
    protected static RuntimeAdjust.BooleanAdjust _coordsDebug =
        new RuntimeAdjust.BooleanAdjust(
            "Toggles debug rendering of tile coordinates in the iso scene " +
            "view.", "narya.miso.iso_coords_debug_render",
            MisoPrefs.config, false);

    /** A debug hook that toggles debug rendering of sprite paths. */
    protected static RuntimeAdjust.BooleanAdjust _pathsDebug =
        new RuntimeAdjust.BooleanAdjust(
            "Toggles debug rendering of sprite paths in the iso scene view.",
            "narya.miso.iso_paths_debug_render", MisoPrefs.config, false);

    /** The stroke used to draw dirty rectangles. */
    protected static final Stroke DIRTY_RECT_STROKE = new BasicStroke(2);

    /** The alpha used to fill tiles for debugging purposes. */
    protected static final Composite ALPHA_FILL_TILE =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
}
