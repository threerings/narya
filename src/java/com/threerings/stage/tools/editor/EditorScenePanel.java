//
// $Id: EditorScenePanel.java 20143 2005-03-30 01:12:48Z mdb $

package com.threerings.stage.tools.editor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.samskivert.swing.Controller;
import com.samskivert.swing.TGraphics2D;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.StringUtil;
import com.threerings.util.DirectionCodes;
import com.threerings.util.RandomUtil;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileUtil;

import com.threerings.whirled.spot.tools.EditablePortal;
import com.threerings.whirled.spot.data.Portal;

import com.threerings.miso.client.ObjectActionHandler;
import com.threerings.miso.client.SceneBlock;
import com.threerings.miso.client.SceneObject;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.util.MisoUtil;

import com.threerings.stage.client.StageScenePanel;
import com.threerings.stage.data.StageMisoSceneModel;

import com.threerings.stage.data.StageScene;
import com.threerings.stage.tools.editor.util.EditorContext;
import com.threerings.stage.tools.editor.util.EditorDialogUtil;
import com.threerings.stage.tools.editor.util.ExtrasPainter;

/**
 * Displays the scene view and handles UI events on the scene. Various
 * actions may be performed on the scene depending on the selected action
 * mode, including placing and deleting tiles or locations and creating
 * portals.
 */
public class EditorScenePanel extends StageScenePanel
    implements EditorModelListener, KeyListener, ChangeListener
{
    /**
     * Constructs the editor scene view panel.
     */
    public EditorScenePanel (EditorContext ctx, JFrame frame, EditorModel model)
    {
        super(ctx, new Controller() {
        });

        // keep these around for later
        _ctx = ctx;
        _frame = frame;

        // save off references to our objects
        _emodel = model;
        _emodel.addListener(this);

        // listen for alt keys
        ctx.getKeyDispatcher().addGlobalKeyListener(this);

        // listen to our range models and scroll our badself
        _horizRange.addChangeListener(this);
        _vertRange.addChangeListener(this);
    }

    /**
     * Returns a range model that controls the scrollability of the scene
     * in the horizontal direction.
     */
    public BoundedRangeModel getHorizModel ()
    {
        return _horizRange;
    }

    /**
     * Returns a range model that controls the scrollability of the scene
     * in the vertical direction.
     */
    public BoundedRangeModel getVertModel ()
    {
        return _vertRange;
    }

    // documentation inherited from interface
    public void stateChanged (ChangeEvent e)
    {
        setViewLocation(_horizRange.getValue(), _vertRange.getValue());
        _refreshBox = true;
    }

    // documentation inherited
    public void setSceneModel (MisoSceneModel model)
    {
        super.setSceneModel(model);

        // compute the "area" in which we'll allow the view to scroll
        computeScrollArea();
    }

    /**
     * Updates the default tileset in the currently edited scene.
     */
    public void updateDefaultTileSet (int tileSetId)
    {
        _model.setDefaultBaseTileSet(tileSetId);
        _blocks.clear();
        rethink();
    }

    // documentation inherited
    public void setBounds (int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        updateScrollArea(_horizRange.getValue(), _vertRange.getValue());
    }

    /**
     * Computes the area in which the view is allowed to scroll.
     */
    protected void computeScrollArea ()
    {
        StageMisoSceneModel ysmodel = (StageMisoSceneModel)_model;
        _area = null;
        for (Iterator iter = ysmodel.getSections(); iter.hasNext(); ) {
            StageMisoSceneModel.Section sect =
                (StageMisoSceneModel.Section)iter.next();
            Rectangle sbounds = MisoUtil.getFootprintPolygon(
                _metrics, sect.x, sect.y,
                ysmodel.swidth, ysmodel.sheight).getBounds();
            if (_area == null) {
                _area = sbounds;
            } else {
                _area.add(sbounds);
            }
        }

        // if we have no blocks, fake something up to start
        if (_area == null) {
            _area = new Rectangle(-250, -250, 500, 500);
        }

        updateScrollArea(_horizRange.getValue(), _vertRange.getValue());
    }

    /**
     * Updates our bounded range models to reflect potential changes in
     * the viewable area and the scrollable area.
     */
    protected void updateScrollArea (int hval, int vval)
    {
        int hmax = _area.x+_area.width;
        hval = Math.min(hval, hmax-_vbounds.width);
        _horizRange.setRangeProperties(
            hval, _vbounds.width, _area.x, hmax, false);

        int vmax = _area.y+_area.height;
        vval = Math.min(vval, vmax-_vbounds.height);
        _vertRange.setRangeProperties(
            vval, _vbounds.height, _area.y, vmax, false);

//         Log.info("Updated extents area:" + StringUtil.toString(_area) +
//                  " vb:" + StringUtil.toString(_vbounds) + ".");
        // update the dimensions of the scrollbox

        // possibly refresh the dimensions of the box
        // and queue a repaint for it
        SwingUtil.refresh(_box);
        _refreshBox = true;
    }

    /**
     * Handle placing the currently selected tile at the given screen
     * coordinates in the scene.
     */
    protected void placeTile (int x, int y)
    {
        Rectangle drag = clearTileSelectRegion(x, y);

        // sanity check
        if (!_emodel.isTileValid()) {
            return;
        }

        switch (_emodel.getLayerIndex()) {
        case EditorModel.BASE_LAYER:
            updateBaseTiles(x, y, drag, _emodel.getFQTileId(),
                            _emodel.getTileSetId(),
                            _emodel.getTileSet().getTileCount());
            break;

        case EditorModel.OBJECT_LAYER:
            addObject((ObjectTile)_emodel.getTile(),
                      _emodel.getFQTileId(), x, y);
            break;
        }

        // potentially update our scrollable area
        computeScrollArea();
    }

    /**
     * Handle deleting the tile at the given screen coordinates from
     * the scene.
     */
    protected void deleteTile (int x, int y)
    {
        Rectangle drag = clearTileSelectRegion(x, y);
        Log.info("Deleting " + drag);

        switch (_emodel.getLayerIndex()) {
        case EditorModel.BASE_LAYER:
            updateBaseTiles(x, y, drag, 0, 0, 1);
            break;

        case EditorModel.OBJECT_LAYER:
            if (drag != null) {
                // locate any object that intersects this rectangle
                ArrayList hits = new ArrayList();
                for (Iterator iter = _vizobjs.iterator(); iter.hasNext(); ) {
                    SceneObject scobj = (SceneObject)iter.next();
                    if (scobj.objectFootprintOverlaps(drag)) {
                        hits.add(scobj);
                    }
                }
                // and delete 'em
                for (int ii = 0; ii < hits.size(); ii++) {
                    deleteObject((SceneObject)hits.get(ii));
                }

            } else {
                // delete the object tile over which the mouse is hovering
                if (_hobject instanceof SceneObject) {
                    deleteObject((SceneObject)_hobject);
                }
            }
            break;
        }
    }

    /**
     * Used to place or delete base tiles.
     */
    protected void updateBaseTiles (int x, int y, Rectangle drag,
                                    int fqTileId, int tileSetId, int tileCount)
    {
        if (drag == null) {
            setBaseTile(fqTileId, x, y);
        } else {
            setBaseTiles(drag, tileSetId, tileCount);
        }
    }

    /**
     * Handle editing the tile at the given screen coordinates from the
     * scene. If the tile is not an object tile, we don't do anything.
     */
    protected void editTile (int x, int y)
    {
        // bail if we're not hovering over a scene object
        if (_hobject == null || !(_hobject instanceof SceneObject)) {
            return;
        }

        // create our object editor dialog if we haven't yet
        if (_objEditor == null) {
            _objEditor = new ObjectEditorDialog(_ctx, this);
        }

        // prepare and display our object editor dialog
        _eobject = (SceneObject)_hobject;
        _objEditor.prepare(_eobject);
        EditorDialogUtil.display(_frame, _objEditor);
    }

    /**
     * Called by the {@link ObjectEditorDialog} when it is dismissed.
     */
    protected void objectEditorDismissed ()
    {
        recomputeVisible();
        _model.updateObject(_eobject.info);
        _eobject = null;
    }

    /**
     * Pop up the portal dialog for the specified location.
     */
    protected void editPortal (EditablePortal portal)
    {
        // create our portal dialog if we haven't yet
        if (_dialogPortal == null) {
            _dialogPortal = new PortalDialog();
        }

        // pass location information on to the dialog
        _dialogPortal.prepare((StageScene)_scene, portal);

        // allow the user to edit the info
        EditorDialogUtil.display(_frame, _dialogPortal);

        // this gets called when a portal is added, so go ahead and
        // recompute them little buggers
        recomputePortals();
        recomputeVisible();
    }

    // documentation inherited
    public void modelChanged (int event)
    {
        switch (event) {
        case ACTION_MODE_CHANGED:
            switch (_emodel.getActionMode()) {
            case EditorModel.ACTION_PLACE_TILE:
                enableCoordHighlighting(false);
                if (_emodel.isTileValid()) {
                    setPlacingTile(_emodel.getTile());
                }
                break;

            case EditorModel.ACTION_EDIT_TILE:
                setPlacingTile(null);
                enableCoordHighlighting(false);
                break;

            case EditorModel.ACTION_PLACE_PORTAL:
                setPlacingTile(null);
                enableCoordHighlighting(true);
                break;
            }
            break;

        case TILE_CHANGED:
            if (_emodel.isTileValid() &&
                _emodel.getActionMode() == EditorModel.ACTION_PLACE_TILE) {
                setPlacingTile(_emodel.getTile());
            }
            break;
        }

        repaint();
    }

    // documentation inherited from interface
    public void mousePressed (MouseEvent event)
    {
        int mx = event.getX(), my = event.getY();
        switch (_emodel.getActionMode()) {
        case EditorModel.ACTION_PLACE_TILE:
//             if (_emodel.getLayerIndex() == EditorModel.BASE_LAYER) {
                setTileSelectRegion(getTileCoords(mx, my));
//             }
            break;

        case EditorModel.ACTION_PLACE_PORTAL:
            Point fcoords = getFullCoords(mx, my);
            // mouse button three is delete
            if (event.getButton() == MouseEvent.BUTTON3) {
                deletePortal(fcoords.x, fcoords.y);
            } else {
                // if they clicked on an existing portal...
                EditablePortal portal = (EditablePortal)
                    getPortal(fcoords.x, fcoords.y);
                if (portal != null) {
                    // ...edit it...
                    editPortal(portal);
                } else {
                    // ...otherwise create a new one
                    new PortalTool().init(this, mx, my);
                }
            }
            break;

        default:
            super.mousePressed(event);
            break;
        }
    }

    // documentation inherited
    protected boolean handleMousePressed (Object hobject, MouseEvent event)
    {
        // don't do the standard cluster and location stuff here
        return false;
    }

    // documentation inherited
    public void mouseReleased (MouseEvent e)
    {
        super.mouseReleased(e);

        Point tc = getTileCoords(e.getX(), e.getY());
        switch (_emodel.getActionMode()) {
        case EditorModel.ACTION_PLACE_TILE:
            switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                placeTile(tc.x, tc.y);
                _refreshBox = true;
                break;

            case MouseEvent.BUTTON2:
                editTile(tc.x, tc.y);
                break;

            case MouseEvent.BUTTON3:
                deleteTile(tc.x, tc.y);
                _refreshBox = true;
                break;
            }
            break;

        case EditorModel.ACTION_EDIT_TILE:
            editTile(tc.x, tc.y);
            break;

        case EditorModel.ACTION_PLACE_PORTAL:
            // nothing to do here; the portal tool handles all
            break;
        }

        repaint();
    }

    // documentation inherited
    public void mouseMoved (MouseEvent e)
    {
        super.mouseMoved(e);
        int x = e.getX(), y = e.getY();
        boolean repaint = false;

        // update the potential tile placement
        if (_ptile != null) {
            boolean changed;

            if (_ptile instanceof ObjectTile) {
                if (changed = updateObjectTileCoords(
                        x, y, _ppos, (ObjectTile)_ptile)) {
                    _pscobj.relocateObject(_metrics, _ppos.x, _ppos.y);
                }
            } else {
                changed = updateTileCoords(x, y, _ppos);
            }

            if (changed) {
                _validPlacement =
                    isTilePlacementValid(_ppos.x, _ppos.y, _ptile);
                repaint = true;
            }
        }

        // update the highlighted portal's fine coordinates
        if (_coordHighlighting) {
            repaint = (updateCoordPos(x, y, _hfull) || repaint);
        }

        // TODO: dirty things with a finer grain
        if (repaint) {
            repaint();
        }
    }

    // documentation inherited
    public void mouseDragged (MouseEvent e)
    {
        super.mouseDragged(e);
        // do the same thing as when we move
        mouseMoved(e);
    }

    // documentation inherited
    public void mouseExited (MouseEvent e)
    {
        super.mouseExited(e);

        // remove any highlighted tiles and placing tile
        _ppos.setLocation(Integer.MIN_VALUE, 0);
        _hfull.setLocation(Integer.MIN_VALUE, 0);
    }

    // documentation inherited
    public void keyPressed (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            // enable scene view tooltips
            setShowFlags(SHOW_TIPS, true);
        }
    }
    
    // documentation inherited
    public void keyReleased (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            // disable scene view tooltips
            setShowFlags(SHOW_TIPS, false);
        }
    }

    // documentation inherited
    public void keyTyped (KeyEvent e)
    {
        // nothing
    }

    // documentation inherited
    protected void fireObjectAction (
        ObjectActionHandler handler, SceneObject scobj, ActionEvent event)
    {
        // do nothing in the editor thanksverymuch
    }

    /**
     * A place for subclasses to react to the hover object changing.
     * One of the supplied arguments may be null.
     */
    protected void hoverObjectChanged (Object oldHover, Object newHover)
    {
        super.hoverObjectChanged(oldHover, newHover);

        // we always repaint our objects when the hover changes
        if (oldHover instanceof SceneObject) {
            SceneObject oldhov = (SceneObject)oldHover;
            _remgr.invalidateRegion(oldhov.getObjectFootprint().getBounds());
        }
        if (newHover instanceof SceneObject) {
            SceneObject newhov = (SceneObject)newHover;
            _remgr.invalidateRegion(newhov.getObjectFootprint().getBounds());
        }
    }

    /**
     * Sets a base tile at the specified position in the scene (in tile
     * coordinates).
     */
    public void setBaseTile (int fqTileId, int x, int y)
    {
        if (!_model.setBaseTile(fqTileId, x, y)) {
            return;
        }
        getBlock(x, y).updateBaseTile(fqTileId, x, y);

        // and recompute any surrounding fringe
        for (int fx = x-1, xn = x+1; fx <= xn; fx++) {
            for (int fy = y-1, yn = y+1; fy <= yn; fy++) {
                getBlock(fx, fy).updateFringe(fx, fy);
            }
        }
    }

    /**
     * Set a region of tiles to a random selection from the supplied
     * tileset.
     */
    public void setBaseTiles (Rectangle r, int setId, int tileCount)
    {
        for (int x = r.x; x < r.x + r.width; x++) {
            for (int y = r.y; y < r.y + r.height; y++) {
                int index = RandomUtil.getInt(tileCount);
                int fqTileId = TileUtil.getFQTileId(setId, index);
                setBaseTile(fqTileId, x, y);
            }
        }
    }

    /**
     * Sets an object tile at the specified position in the scene (in tile
     * coordinates).
     */
    public void addObject (ObjectTile tile, int fqTileId, int x, int y)
    {
        Point p = new Point(x, y);
        adjustObjectCoordsAccordingToGrip(p, tile);

        ObjectInfo oinfo = new ObjectInfo(fqTileId, x, y);

        // first attempt to add it to the appropriate scene block; this
        // will fail if there's already a copy of the same object at this
        // coordinate
        if (getBlock(x, y).addObject(oinfo)) {
            // create an object info and add it to the scene model
            _model.addObject(oinfo);
            // recompute our visible object set
            recomputeVisible();
        }
    }

    /**
     * Deletes the object tile at the specified tile coordinates.
     */
    public void deleteObject (SceneObject scobj)
    {
        // remove it from the scene model
        if (_model.removeObject(scobj.info)) {
            // clear the object out of its block
            getBlock(scobj.info.x, scobj.info.y).deleteObject(scobj.info);
        } else {
            Log.warning("Requested to remove unknown object " + scobj + ".");
        }

        // recompute our visible object set
        recomputeVisible();

        // make sure we clear the hover if that's what we're deleting
        if (_hobject == scobj) {
            _hobject = null;
        }
    }

    /**
     * Sets the tile that is currently being placed. It will not be
     * rendered until after a call to {@link #updateTileCoords} on the
     * placing tile (which happens automatically when the mouse moves).
     */
    public void setPlacingTile (Tile tile)
    {
        _ptile = tile;

        // if this is an object tile, create a temporary scene object we
        // can use to perform calculations with the object while placing
        if (_ptile instanceof ObjectTile) {
            _pscobj = new SceneObject(this, new ObjectInfo(0, _ppos.x, _ppos.y),
                                      (ObjectTile)tile);
        } else {
            _pscobj = null;
        }
    }

    /**
     * Sets the start (in tile coords) of a mouse drag when placing
     * a rectangular area of base tiles.
     */
    public void setTileSelectRegion (Point drag)
    {
        _drag = drag;
    }

    /**
     * Clear and return the drag rectangle for selecting a rectangular
     * region.
     *
     * @return null if the drag is the same as the supplied tile
     * coordinates, a rectangle containing the selected region if it was
     * different.
     */
    public Rectangle clearTileSelectRegion (int x, int y)
    {
        Rectangle drect = null;
        if (_drag != null && (x != _drag.x || y != _drag.y)) {
            int w = 1 + ((x > _drag.x) ? (x - _drag.x) : (_drag.x - x));
            int h = 1 + ((y > _drag.y) ? (y - _drag.y) : (_drag.y - y));
            drect = new Rectangle(
                Math.min(x, _drag.x), Math.min(y, _drag.y), w, h);
        }
        _drag = null;
        return drect;
    }

    /**
     * Enables or disables highlighting of the tile over which the mouse
     * is currently positioned.
     */
    public void enableCoordHighlighting (boolean enabled)
    {
        _coordHighlighting = enabled;
    }

    /**
     * Deletes the portal at the specified full coordinates.
     */
    public void deletePortal (int x, int y)
    {
        Portal port = getPortal(x, y);
        if (port != null) {
            _scene.removePortal(port);
            recomputePortals();
            recomputeVisible();
        }
    }

    /**
     * Returns the portal that serves as the default entrance to this
     * scene or null if no default is set.
     */
    public Portal getEntrance ()
    {
        return _scene.getDefaultEntrance();
    }

    /**
     * Makes the specified portal the default entrance to this scene.
     */
    public void setEntrance (Portal port)
    {
        _scene.setDefaultEntrance(port);
    }

    // documentation inherited
    protected void recomputeVisible ()
    {
        super.recomputeVisible();

        // see if any of our visible objects overlap and mark them as bad
        // monkeys; we love N^2 algorithms
        for (Iterator iter = _vizobjs.iterator(); iter.hasNext(); ) {
            SceneObject scobj = (SceneObject)iter.next();
            scobj.setWarning(overlaps(scobj));
        }
    }

    // documentation inherited
    protected void warnVisible (SceneBlock block, Rectangle sbounds)
    {
        // nothing doing
    }

    /** Helper function for {@link #recomputeVisible}. */
    protected boolean overlaps (SceneObject tobj)
    {
        for (Iterator iter = _vizobjs.iterator(); iter.hasNext(); ) {
            SceneObject scobj = (SceneObject)iter.next();
            if (scobj != tobj && tobj.objectFootprintOverlaps(scobj) &&
                tobj.getPriority() == scobj.getPriority()) {
                return true;
            }
        }
        return false;
    }

    // documentation inherited
    protected void paintHighlights (Graphics2D gfx, Rectangle dirty)
    {
        Polygon hpoly = null;

        if (_hobject != null && _hobject instanceof SceneObject) {
            SceneObject scobj = (SceneObject)_hobject;
            hpoly = scobj.getObjectFootprint();

        }

        if (_emodel.getActionMode() == EditorModel.ACTION_PLACE_TILE &&
            (hpoly == null ||
             _emodel.getLayerIndex() == EditorModel.BASE_LAYER)) {
            hpoly = MisoUtil.getTilePolygon(_metrics, _hcoords.x, _hcoords.y);
        }

        if (hpoly != null) {
            gfx.setColor(Color.green);
            gfx.draw(hpoly);
        }

        // paint the highlighted full coordinate
        if (_coordHighlighting && _hfull.x != Integer.MIN_VALUE) {
            Point spos = new Point();
            MisoUtil.fullToScreen(_metrics, _hfull.x, _hfull.y, spos);

            // set the desired stroke and color
            Stroke ostroke = gfx.getStroke();
            gfx.setStroke(HIGHLIGHT_STROKE);

            // draw a red circle at the coordinate
            gfx.setColor(Color.red);
            gfx.draw(new Ellipse2D.Float(spos.x - 1, spos.y - 1, 3, 3));

            // restore the original stroke
            gfx.setStroke(ostroke);
        }
    }

    // documentation inherited
    protected void paintExtras (Graphics2D gfx, Rectangle dirty)
    {
        super.paintExtras(gfx, dirty);

        // we don't want to paint the 'extras' stuff to the copy..
        if (gfx instanceof TGraphics2D) {
            gfx = ((TGraphics2D) gfx).getPrimary();
        }

        paintPortals(gfx);
        paintHighlights(gfx, dirty);
        paintPlacingTile(gfx);

        // and call into any extras painters
        for (int ii = 0; ii < _extras.size(); ii++) {
            ((ExtrasPainter)_extras.get(ii)).paintExtras(gfx);
        }
    }

    /**
     * Add an extras painter.
     */
    protected void addExtrasPainter (ExtrasPainter painter)
    {
        _extras.add(painter);
    }

    /**
     * Remove the specified extras painter.
     */
    protected void removeExtrasPainter (ExtrasPainter painter)
    {
        _extras.remove(painter);
    }

    /**
     * Paints a transparent image of the tile being placed and draws a
     * highlight around the bounds of the tile's current prospective
     * position. The highlight is drawn in green if the tile placement is
     * valid, or red if not.
     *
     * @param gfx the graphics context.
     */
    protected void paintPlacingTile (Graphics2D gfx)
    {
        // bail if we've no placing tile
        if (_ptile == null || _ppos.x == Integer.MIN_VALUE) {
            return;
        }

        // draw a transparent rendition of the placing tile image
        Composite ocomp = gfx.getComposite();
        gfx.setComposite(ALPHA_PLACING);
        Shape bpoly;
        if (_pscobj != null) {
            _pscobj.paint(gfx);
            bpoly = _pscobj.getObjectFootprint();

        } else {
            bpoly = MisoUtil.getTilePolygon(_metrics, _ppos.x, _ppos.y);
            Rectangle bounds = bpoly.getBounds();
            _ptile.paint(gfx, bounds.x, bounds.y);
        }
        gfx.setComposite(ocomp);

        // if we're dragging, grab that footprint
        if (_drag != null) {
            bpoly = MisoUtil.getMultiTilePolygon(_metrics, _ppos, _drag);
        }

        // draw an outline around the tile footprint
        gfx.setColor(_validPlacement ? Color.blue : Color.red);
        gfx.draw(bpoly);
    }

    /**
     * Paint demarcations at all portals in the scene.
     *
     * @param gfx the graphics context.
     */
    protected void paintPortals (Graphics2D gfx)
    {
        Iterator iter = _scene.getPortals();
        while (iter.hasNext()) {
            paintPortal(gfx, (EditablePortal)iter.next());
        }
    }

    /**
     * Paint the specified portal.
     */
    protected void paintPortal (Graphics2D gfx, EditablePortal port)
    {
        // get the portal's center coordinate
        Point spos = new Point();
        MisoUtil.fullToScreen(_metrics, port.loc.x, port.loc.y, spos);
        int cx = spos.x, cy = spos.y;

        // translate the origin to center on the portal
        gfx.translate(cx, cy);

        // rotate to reflect the portal orientation
        double rot = (Math.PI / 4.0f) * port.loc.orient;
        gfx.rotate(rot);

        // draw the triangle
        gfx.setColor(Color.blue);
        gfx.fill(_locTri);

        // outline the triangle in black
        gfx.setColor(Color.black);
        gfx.draw(_locTri);

        // draw the rectangle
        gfx.setColor(Color.red);
        gfx.fillRect(-1, 2, 3, 3);

        // restore the original transform
        gfx.rotate(-rot);
        gfx.translate(-cx, -cy);

        // highlight the portal if it's the default entrance
        if (port.equals(_scene.getDefaultEntrance())) {
            gfx.setColor(Color.cyan);
            gfx.drawRect(spos.x - 5, spos.y - 5, 10, 10);
        }
    }

    /**
     * Updates the coordinate position and returns true if it has changed.
     */
    public boolean updateCoordPos (int x, int y, Point cpos)
    {
        Point npos = MisoUtil.screenToFull(_metrics, x, y, new Point());
        if (!cpos.equals(npos)) {
            cpos.setLocation(npos.x, npos.y);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether placing a tile at the given coordinates in the
     * scene is valid.  Makes sure placing an object fits within the scene
     * and doesn't overlap any other objects.
     */
    protected boolean isTilePlacementValid (int x, int y, Tile tile)
    {
        if (tile instanceof ObjectTile) {
            // create a temporary scene object for this tile
            SceneObject nobj = new SceneObject(this, new ObjectInfo(0, x, y),
                                               (ObjectTile)tile);
            // report invalidity if overlaps any existing objects
            int ocount = _vizobjs.size();
            for (int ii = 0; ii < ocount; ii++) {
                SceneObject scobj = (SceneObject)_vizobjs.get(ii);
                if (scobj.objectFootprintOverlaps(nobj)) {
                    return false;
                }
            }
        }

        return true;
    }

    // documentation inherited
    protected boolean skipHitObject (SceneObject scobj)
    {
        return false; // skip nothing
    }

    /**
     * Converts the supplied screen coordinates into tile coordinates for
     * an object tile. (See {@link #updateTileCoords}.)
     *
     * @return true if the tile coordinates have changed.
     */
    protected boolean updateObjectTileCoords (int sx, int sy, Point tpos,
                                              ObjectTile otile)
    {
        Point npos = new Point();
        MisoUtil.screenToTile(_metrics, sx, sy, npos);
        adjustObjectCoordsAccordingToGrip(npos, otile);
        if (!tpos.equals(npos)) {
            tpos.setLocation(npos.x, npos.y);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Alter the position of the object according to which corner
     * we are holding it by.
     */
    protected void adjustObjectCoordsAccordingToGrip (Point p, ObjectTile tile)
    {
        int dy = Math.max(3, (tile.getHeight() / _metrics.tilehei));
        int dx = Math.max(3, (tile.getWidth() / _metrics.tilewid));

        switch (_emodel.getObjectGripDirection()) {
        case DirectionCodes.NORTH:
            p.x += dy;
            p.y += dy;
            break;

        case DirectionCodes.WEST:
            p.x += dx;
            break;

        case DirectionCodes.EAST:
            p.y += dx;
            break;

        case DirectionCodes.NORTHWEST:
            p.x += dy + (dx / 2);
            p.y += dy - (dx / 2);
            break;

        case DirectionCodes.NORTHEAST:
            p.x += dy - (dx / 2);
            p.y += dy + (dx / 2);
            break;

        case DirectionCodes.SOUTHWEST:
            p.x += (dx / 2);
            p.y -= (dx / 2);
            break;

        case DirectionCodes.SOUTHEAST:
            p.x -= (dx / 2);
            p.y += (dx / 2);
            break;
        }
    }

    /**
     * Sets the editor model.
     */
    protected void setEditorModel (EditorModel model)
    {
        _emodel = model;
    }

    /**
     * Set the scroll box that tracks our view.
     */
    public void setEditorScrollBox (EditorScrollBox box)
    {
        _box = box;
    }

    // documentation inherited
    protected void paint (Graphics2D gfx, Rectangle[] dirty)
    {
        // if we need to refresh the box and we have all the scene data, do it
        if (_refreshBox && _visiBlocks.isEmpty()) {
            _refreshBox = false;
            Graphics2D mini = _box.getMiniGraphics();
            Graphics2D t = new TGraphics2D(gfx, mini);
            super.paint(t, dirty);
            mini.dispose();
            _box.repaint();

        } else {
            // otherwise, just do a normal fast paint
            super.paint(gfx, dirty);
        }
    }

    /** Provides access to stuff. */
    protected EditorContext _ctx;

    /** Our editor model. */
    protected EditorModel _emodel;

    /** The scrollbox that tracks our view. */
    protected EditorScrollBox _box;

    /** Do we need to refresh the image being displayed in our scrollbox? */
    protected boolean _refreshBox;

    /** We need this to create our dialogs when they are needed. */
    protected JFrame _frame;

    /** Allows scrolling horizontally. */
    protected BoundedRangeModel _horizRange = new DefaultBoundedRangeModel();

    /** Allows scrolling vertically. */
    protected BoundedRangeModel _vertRange = new DefaultBoundedRangeModel();

    /** The virtual screen rectangle around which we scroll. */
    protected Rectangle _area;

    /** Whether or not coordinate highlighting is enabled. */
    protected boolean _coordHighlighting;

    /** The currently highlighted full coordinate. */
    protected Point _hfull = new Point(Integer.MIN_VALUE, 0);

    /** The location of the start of a tile drag in tile coords. */
    protected Point _drag = null;

    /** The position of the tile currently being placed. */
    protected Point _ppos = new Point(Integer.MIN_VALUE, 0);

    /** Used to track whether or not the current "placing" tile is in a
     * valid position. */
    protected boolean _validPlacement = false;

    /** The tile currently being placed. */
    protected Tile _ptile;

    /** Metrics for the tile currently being placed if it is an object
     * tile. */
    protected SceneObject _pscobj;

    /** A list of things that will do some extra painting for us. */
    protected ArrayList _extras = new ArrayList();

    /** The dialog providing portal edit functionality. */
    protected PortalDialog _dialogPortal;

    /** The dialog providing object edit functionality. */
    protected ObjectEditorDialog _objEditor;

    /** The object currently being edited by the object editor dialog. */
    protected SceneObject _eobject;

    /** The triangle used to render a portal on-screen. */
    protected static Polygon _locTri;

    static {
        _locTri = new Polygon();
        _locTri.addPoint(-3, -3);
        _locTri.addPoint(3, -3);
        _locTri.addPoint(0, 3);
    };

    /** The stroke object used to draw highlighted tiles and coordinates. */
    protected static final Stroke HIGHLIGHT_STROKE = new BasicStroke(2);

    /** Alpha level used to render transparent placing tile image. */
    protected static final Composite ALPHA_PLACING =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
}
