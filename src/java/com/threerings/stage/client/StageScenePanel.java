//
// $Id: WorldScenePanel.java 18366 2004-12-15 22:56:58Z ray $

package com.threerings.stage.client;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.RuntimeAdjust;

import com.samskivert.util.Tuple;
import com.threerings.util.StreamableArrayList;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.UniformTileSet;

import com.threerings.miso.client.MisoScenePanel;
import com.threerings.miso.client.SceneObject;
import com.threerings.miso.client.SceneObjectTip;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.util.MisoUtil;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.data.Cluster;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;

import com.threerings.stage.Log;
import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageScene;
import com.threerings.stage.data.StageSceneModel;
import com.threerings.stage.util.StageContext;
import com.threerings.stage.util.StageSceneUtil;

/**
 * Extends the basic Miso scene panel with Stage fun stuff like portals,
 * clusters and locations.
 */
public class StageScenePanel extends MisoScenePanel
    implements ControllerProvider, KeyListener, PlaceView
{
    /** An action command generated when the user clicks on a location
     * within the scene. */
    public static final String LOCATION_CLICKED = "LocationClicked";

    /** An action command generated when something besides the user wants
     * us to move to a location.  */
    public static final String LOCATION_REQUESTED = "LocationRequested";

    /** An action command generated when a cluster is clicked. */
    public static final String CLUSTER_CLICKED = "ClusterClicked";

    /** Show flag that indicates we should show all clusters. */
    public static final int SHOW_CLUSTERS = (1 << 1);

    /** Show flag that indicates we should render known land plots
     * (expensive, don't turn this on willy nilly). */
    public static final int SHOW_PLOTS = (1 << 2);

    /**
     * Constructs a stage scene view panel.
     */
    public StageScenePanel (StageContext ctx, StageSceneController ctrl)
    {
        super(ctx, StageSceneUtil.getMetrics());

        // keep these around for later
        _ctx = ctx;
        _ctrl = ctrl;
        _ctrl.setControlledPanel(this);

        // no layout manager
        setLayout(null);
    }

    /**
     * Get the tileset colorizer in use in this scene.
     */
    public SceneColorizer getColorizer ()
    {
        return _rizer;
    }

    // documentation inherited
    protected TileSet.Colorizer getColorizer (ObjectInfo oinfo)
    {
        return _rizer.getColorizer(oinfo);
    }

    /**
     * Returns the scene being displayed by this panel. Do not modify it.
     */
    public StageScene getScene ()
    {
        return _scene;
    }

    /**
     * Sets the scene managed by the panel.
     */
    public void setScene (StageScene scene)
    {
        _scene = scene;
        if (_scene != null) {
            recomputePortals();
            setSceneModel(StageMisoSceneModel.getSceneModel(
                              scene.getSceneModel()));
            _rizer = new SceneColorizer(_ctx.getColorPository(), scene);
        } else {
            Log.warning("Zoiks! We can't display a null scene!");
            // TODO: display something to the user letting them know that
            // we're so hosed that we don't even know what time it is
        }
    }

    /**
     * Called when we have received a scene update from the server.
     */
    public void sceneUpdated (SceneUpdate update)
    {
        // recompute the portals as those may well have changed
        recomputePortals();

        // we go aheand and completely replace our scene model which will
        // reload the whole good goddamned business; it is a little
        // shocking to the user, but it's guaranteed to work
        refreshScene();
    }

    /**
     * Computes a set of display objects for the portals in this scene.
     */
    protected void recomputePortals ()
    {
        // create scene objects for our portals
        UniformTileSet ots = loadPortalTileSet();

        _portobjs.clear();
        for (Iterator iter = _scene.getPortals(); iter.hasNext(); ) {
            Portal portal = (Portal) iter.next();
            Point p = getScreenCoords(portal.x, portal.y);
            int tx = MisoUtil.fullToTile(portal.x);
            int ty = MisoUtil.fullToTile(portal.y);
            Point ts = MisoUtil.tileToScreen(_metrics, tx, ty, new Point());

//             Log.info("Added portal " + portal +
//                      " [screen=" + StringUtil.toString(p) +
//                      ", tile=" + StringUtil.coordsToString(tx, ty) +
//                      ", tscreen=" + StringUtil.toString(ts) + "].");

            ObjectInfo info = new ObjectInfo(0, tx, ty);
            info.action = "portal:" + portal.portalId;

            // TODO: cache me
            ObjectTile tile = new PortalObjectTile(
                ts.x + _metrics.tilehwid - p.x + (PORTAL_ICON_WIDTH / 2),
                ts.y + _metrics.tilehei - p.y + (PORTAL_ICON_HEIGHT / 2));
            tile.setImage(ots.getTileMirage(portal.orient));

            _portobjs.add(new SceneObject(this, info, tile) {
                public boolean setHovered (boolean hovered) {
                    ((PortalObjectTile)this.tile).hovered = hovered;
                    return isResponsive();
                }
            });
        }
    }

    // documentation inherited
    protected void recomputeVisible ()
    {
        super.recomputeVisible();

        // add our visible portal objects to the list of visible objects
        for (int ii = 0, ll = _portobjs.size(); ii < ll; ii++) {
            SceneObject pobj = (SceneObject)_portobjs.get(ii);
            if (pobj.bounds != null && _vbounds.intersects(pobj.bounds)) {
                _vizobjs.add(pobj);
            }
        }
    }

    // documentation inherited from interface ControllerProvider
    public Controller getController ()
    {
        return _ctrl;
    }

    // documentation inherited from interface KeyListener
    public void keyPressed (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            // display all tooltips
            setShowFlags(SHOW_TIPS, true);
        }
    }
    
    // documentation inherited from interface KeyListener
    public void keyReleased (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            // stop displaying all tooltips
            setShowFlags(SHOW_TIPS, defaultShowTips());
        }
    }

    // documentation inherited from interface PlaceView
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    // documentation inherited from interface PlaceView
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    /**
     * Returns true if we should always show the object tooltips by
     * default, false if they should only be shown while the 'Alt' key is
     * depressed.
     */
    protected boolean defaultShowTips ()
    {
        return false;
    }

    // documentation inherited
    public void keyTyped (KeyEvent e)
    {
        // nothing
    }

    // documentation inherited
    protected boolean handleMousePressed (Object hobject, MouseEvent event)
    {
        // let our parent have a crack at the old mouse press
        if (super.handleMousePressed(hobject, event)) {
            return true;
        }

        // if the hover object is a cluster, we clicked it!
        if (event.getButton() == MouseEvent.BUTTON1) {
            if (hobject instanceof Cluster) {
                int mx = event.getX(), my = event.getY();
                Object actarg = new Tuple(hobject, new Point(mx, my));
                Controller.postAction(this, CLUSTER_CLICKED, actarg);
            } else {
                // post an action indicating that we've clicked on a location
                Point lc = MisoUtil.screenToFull(
                    _metrics, event.getX(), event.getY(), new Point());
                Controller.postAction(this, LOCATION_CLICKED,
                                      new Location(lc.x, lc.y, (byte)0));
            }
            return true;
        }
        return false;
    }

    /**
     * Called when our show flags have changed.
     */
    protected void showFlagsDidChange (int oldflags)
    {
        super.showFlagsDidChange(oldflags);

        if ((oldflags & SHOW_CLUSTERS) != (_showFlags & SHOW_CLUSTERS)) {
            // dirty every cluster rectangle
            Iterator iter = _clusters.values().iterator();
            while (iter.hasNext()) {
                dirtyCluster((Shape)iter.next());
            }
        }
    }

    /**
     * Called when a real cluster is created or updated in the scene.
     */
    protected void clusterUpdated (Cluster cluster)
    {
        // compute a screen rectangle that contains all possible "spots"
        // in this cluster
        ArrayList spots = StageSceneUtil.getClusterLocs(cluster);
        Rectangle cbounds = null;
        for (int ii = 0, ll = spots.size(); ii < ll; ii++) {
            Location loc = (Location)spots.get(ii);
            Point sp = getScreenCoords(loc.x, loc.y);
            if (cbounds == null) {
                cbounds = new Rectangle(sp.x, sp.y, 0, 0);
            } else {
                cbounds.add(sp.x, sp.y);
            }
        }

        if (cbounds == null) {
            // if we found no one actually in this cluster, nix it
            removeCluster(cluster.clusterOid);
        } else {
            // otherwise have the view update the cluster
            updateCluster(cluster, cbounds);
        }
    }

    /**
     * Adds or updates the specified cluster in the view. Metrics will be
     * created that allow the cluster to be rendered and hovered over
     * (which would make it the active cluster as indicated by {@link
     * #getActiveCluster}).
     *
     * @param cluster the cluster record to be added.
     * @param bounds the screen coordinates that bound the occupants of
     * the cluster.
     */
    public void updateCluster (Cluster cluster, Rectangle bounds)
    {
        // dirty any old bounds
        dirtyCluster(cluster);

        // compute the screen coordinate bounds of this cluster
        Shape shape = new Ellipse2D.Float(
            bounds.x, bounds.y, bounds.width, bounds.height);
        _clusters.put(cluster, shape);

        // if the mouse is inside these bounds, we highlight this cluster
        Shape mshape = new Ellipse2D.Float(
            bounds.x-CLUSTER_SLOP, bounds.y-CLUSTER_SLOP,
            bounds.width+2*CLUSTER_SLOP, bounds.height+2*CLUSTER_SLOP);
        _clusterWells.put(cluster, mshape);

        // dirty our new bounds
        dirtyCluster(shape);
    }

    /**
     * Removes the specified cluster from the view.
     *
     * @return true if such a cluster existed and was removed.
     */
    public boolean removeCluster (int clusterOid)
    {
        Cluster key = new Cluster();
        key.clusterOid = clusterOid;
        _clusterWells.remove(key);
        Shape shape = (Shape)_clusters.remove(key);
        if (shape == null) {
            return false;
        }

        dirtyCluster(shape);
        // clear out the hover object if this cluster was it
        if (_hobject instanceof Cluster &&
            ((Cluster)_hobject).clusterOid == clusterOid) {
            _hobject = null;
        }
        return true;
    }

    /**
     * A place for subclasses to react to the hover object changing.
     * One of the supplied arguments may be null.
     */
    protected void hoverObjectChanged (Object oldHover, Object newHover)
    {
        super.hoverObjectChanged(oldHover, newHover);

        if (oldHover instanceof Cluster) {
            dirtyCluster((Cluster)oldHover);
        }
        if (newHover instanceof Cluster) {
            dirtyCluster((Cluster)newHover);
        }
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
        if (!isResponsive()) {
            return null;
        }

        // if the current hover object is a cluster, see if we're still in
        // that cluster
        if (_hobject instanceof Cluster) {
            Cluster cluster = (Cluster)_hobject;
            if (containsPoint(cluster, mx, my)) {
                return cluster;
            }
        }

        // otherwise, check to see if the mouse is in some new cluster
        Iterator iter = _clusters.keySet().iterator();
        while (iter.hasNext()) {
            Cluster cclust = (Cluster)iter.next();
            if (containsPoint(cclust, mx, my)) {
                return cclust;
            }
        }

        return null;
    }

    /**
     * Returns true if the specified cluster contains the supplied screen
     * coordinate.
     */
    protected boolean containsPoint (Cluster cluster, int mx, int my)
    {
        Shape shape = (Shape)_clusterWells.get(cluster);
        return (shape == null) ? false : shape.contains(mx, my);
    }

    /**
     * Dirties the supplied cluster.
     */
    protected void dirtyCluster (Cluster cluster)
    {
        if (cluster != null) {
            dirtyCluster((Shape)_clusters.get(cluster));
        }
    }

    /**
     * Dirties the supplied cluster rectangle.
     */
    protected void dirtyCluster (Shape shape)
    {
        if (shape != null) {
            Rectangle r = shape.getBounds();
            _remgr.invalidateRegion(
                r.x - (CLUSTER_PAD / 2),
                r.y - (CLUSTER_PAD / 2),
                r.width + (CLUSTER_PAD * 3 / 2),
                r.height + (CLUSTER_PAD * 3 / 2));
        }
    }

    /**
     * Returns the portal at the specified full coordinates or null if no
     * portal exists at said coordinates.
     */
    public Portal getPortal (int fullX, int fullY)
    {
        Iterator iter = _scene.getPortals();
        while (iter.hasNext()) {
            Portal portal = (Portal)iter.next();
            if (portal.x == fullX && portal.y == fullY) {
                return portal;
            }
        }
        return null;
    }

    // documentation inherited
    protected void paintBaseDecorations (Graphics2D gfx, Rectangle clip)
    {
        super.paintBaseDecorations(gfx, clip);

        paintClusters(gfx, clip);
    }

    /**
     * Paints any visible clusters.
     */
    protected void paintClusters (Graphics2D gfx, Rectangle clip)
    {
        // remember how daddy's things were arranged
        Object oalias = SwingUtil.activateAntiAliasing(gfx);
        Composite ocomp = gfx.getComposite();
        Stroke ostroke = gfx.getStroke();

        // get ready to draw clusters
        gfx.setStroke(CLUSTER_STROKE);
        gfx.setColor(CLUSTER_COLOR);

        if (checkShowFlag(SHOW_CLUSTERS)
            /* || // _alwaysShowClusters.getValue() */) {
            // draw all clusters
            Iterator iter = _clusters.keySet().iterator();
            while (iter.hasNext()) {
                drawCluster(gfx, clip, (Cluster)iter.next());
            }

        } else if (_hobject instanceof Cluster) {
            // or just draw the active cluster
            drawCluster(gfx, clip, (Cluster)_hobject);
        }

        // put back daddy's things
        gfx.setComposite(ocomp);
        gfx.setStroke(ostroke);
        SwingUtil.restoreAntiAliasing(gfx, oalias);
    }

    /**
     * Draw the cluster specified by the rectangle.
     */
    protected void drawCluster (Graphics2D gfx, Rectangle clip, Cluster cluster)
    {
        Shape shape = (Shape)_clusters.get(cluster);
        if ((shape != null) && shape.intersects(clip)) {
            if (_hobject == cluster) {
                gfx.setComposite(HIGHLIGHT_ALPHA);
            } else {
                gfx.setComposite(SHOWN_ALPHA);
            }
            gfx.draw(shape);
        }
    }

    /**
     * Returns true if the specified location is associated with a portal.
     */
    protected boolean isPortal (Location loc)
    {
        Iterator iter = _scene.getPortals();
        while (iter.hasNext()) {
            Portal p = (Portal)iter.next();
            if (p.x == loc.x && p.y == loc.y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads up the tileset used to render the portal arrows.
     */
    protected UniformTileSet loadPortalTileSet ()
    {
//         return YoUI.client.loadTileSet(
//             "media/yohoho/icons/portal_arrows.png",
//             PORTAL_ICON_WIDTH, PORTAL_ICON_HEIGHT);
        return null;
    }

    /** Used to render portals as objects in a scene. */
    protected class PortalObjectTile extends ObjectTile
    {
        public boolean hovered = false;

        public PortalObjectTile (int ox, int oy)
        {
            setOrigin(ox, oy);
        }

        public void paint (Graphics2D gfx, int x, int y)
        {
            Composite ocomp = gfx.getComposite();
            if (!isResponsive() || !hovered) {
                gfx.setComposite(INACTIVE_PORTAL_ALPHA);
            }
            super.paint(gfx, x, y);
            gfx.setComposite(ocomp);
        }
    }

    /** A reference to our client context. */
    protected StageContext _ctx;

    /** The controller with which we work in tandem. */
    protected StageSceneController _ctrl;

    /** Our currently displayed scene. */
    protected StageScene _scene;

    /** Contains scene objects for our portals. */
    protected ArrayList _portobjs = new ArrayList();

    /** Shapes describing the clusters, indexed by cluster. */
    protected HashMap _clusters = new HashMap();

    /** Shapes describing the clusters, indexed by cluster. */
    protected HashMap _clusterWells = new HashMap();

    /** Handles scene object colorization. */
    protected SceneColorizer _rizer;

//     /** A debug hook that toggles always-on rendering of clusters. */
//     protected static RuntimeAdjust.BooleanAdjust _alwaysShowClusters =
//         new RuntimeAdjust.BooleanAdjust(
//             "Causes all clusters to always be rendered.",
//             "yohoho.miso.always_show_clusters",
//             ClientPrefs.config, false);

    /** The width of the portal icons. */
    protected static final int PORTAL_ICON_WIDTH = 48;
    
    /** The height of the portal icons. */
    protected static final int PORTAL_ICON_HEIGHT = 48;

    /** The distance within which the mouse must be from a location
     * in order to highlight it. */
    protected static final int MAX_LOCATION_DIST = 25;

    /** The amount the stroke a cluster. */
    protected static final int CLUSTER_PAD = 4;

    /** The width with which to draw the cluster. */
    protected static final Stroke CLUSTER_STROKE = new BasicStroke(CLUSTER_PAD);

    /** The color used to render clusters. */
    protected static final Color CLUSTER_COLOR = Color.ORANGE;

    /** Alpha level used to hightlight locations or clusters. */
    protected static final Composite HIGHLIGHT_ALPHA =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);

    /** Alpha level used to render clusters when they're not selected. */
    protected static final Composite SHOWN_ALPHA =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f);

    /** The alpha with which to render inactive portals. */
    protected static final Composite INACTIVE_PORTAL_ALPHA = HIGHLIGHT_ALPHA;

    /** The number of pixels outside a cluster when we assume the mouse is
     * "over" that cluster. */
    protected static final int CLUSTER_SLOP = 25;
}
