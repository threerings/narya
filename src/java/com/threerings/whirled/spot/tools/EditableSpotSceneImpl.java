//
// $Id: EditableSpotSceneImpl.java,v 1.7 2001/12/05 08:45:06 mdb Exp $

package com.threerings.whirled.tools.spot;

import java.util.ArrayList;
import java.util.List;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.tools.EditableSceneModel;
import com.threerings.whirled.tools.EditableSceneImpl;

import com.threerings.whirled.spot.client.DisplaySpotSceneImpl;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotSceneModel;

import com.threerings.whirled.spot.Log;

/**
 * The editable spot scene interface is used in the offline scene building
 * tools as well as by the tools that load those prototype scenes into the
 * runtime database. Accordingly, it provides a means for modifying scene
 * values and for obtaining access to the underlying scene models that
 * represent the underlying scene information.
 *
 * <p> Scrutinizers of the code might cringe at the somewhat inefficient
 * array manipulation used to preserve the integrity of the arrays
 * returned by {@link #getLocations} and {@link #getPortals}. Since we
 * expect reading to happen more often than modifying, this seemed a
 * reasonable tradeoff, and one should also note that this class will only
 * ever be used in the offline editor and not in any performance
 * constrained runtime system.
 *
 * @see com.threerings.whirled.tools.EditableScene
 */
public class EditableSpotSceneImpl extends EditableSceneImpl
    implements EditableSpotScene
{
    /**
     * Creates an instance that will create and use a blank scene model.
     */
    public EditableSpotSceneImpl ()
    {
        this(EditableSpotSceneModel.blankSpotSceneModel());
    }

    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and update it when changes are made.
     */
    public EditableSpotSceneImpl (EditableSpotSceneModel model)
    {
        this(model, new DisplaySpotSceneImpl(model.spotSceneModel, null));
    }

    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and update it when changes are made. It will delegate to the
     * supplied display spot scene instead of creating its own delegate.
     */
    public EditableSpotSceneImpl (
        EditableSpotSceneModel model, DisplaySpotSceneImpl delegate)
    {
        super(model, delegate);

        // keep track of these
        _model = model.spotSceneModel;
        _emodel = model;
        _delegate = delegate;

        // go through and replace the plain portals with editable portals
        // (oh the machinations we have to go through for the lack of
        // multiple inheritance)
        List portals = _delegate.getPortals();
        for (int i = 0; i < portals.size(); i++) {
            EditablePortal port = dupePortal((Portal)portals.get(i));;
            port.name = _emodel.portalNames[i];
            port.targetSceneName = (String)_emodel.neighborNames.get(i);
            port.targetPortalName = _emodel.targetPortalNames[i];
            portals.set(i, port);
        }
    }

    /**
     * Constructs an editable portal with the same configuration as the
     * supplied regular portal.
     */
    protected EditablePortal dupePortal (Portal source)
    {
        EditablePortal port = new EditablePortal();
        port.locationId = source.locationId;
        port.x = source.x;
        port.y = source.y;
        port.orientation = source.orientation;
        port.clusterIndex = source.clusterIndex;
        port.targetSceneId = source.targetSceneId;
        port.targetLocId = source.targetLocId;
        return port;
    }

    // documentation inherited
    public int getDefaultEntranceId ()
    {
        return _delegate.getDefaultEntranceId();
    }

    // documentation inherited
    public List getLocations ()
    {
        return _delegate.getLocations();
    }

    // documentation inherited
    public List getPortals ()
    {
        return _delegate.getPortals();
    }

    // documentation inherited
    public void setDefaultEntranceId (int defaultEntranceId)
    {
        _model.defaultEntranceId = defaultEntranceId;
    }

    // documentation inherited
    public void addLocation (Location loc)
    {
        // add the location to the end of the location list
        _delegate.getLocations().add(loc);
    }

    // documentation inherited
    public void removeLocation (Location loc)
    {
        // remove the location from the list
        _delegate.getLocations().remove(loc);
    }

    // documentation inherited
    public void addPortal (EditablePortal eport)
    {
        // add it to the locations lists as well
        addLocation(eport);

        // now add it to the end of the _portals list
        _delegate.getPortals().add(eport);
    }

    // documentation inherited
    public void removePortal (EditablePortal eport)
    {
        // remove it from the locations list
        removeLocation(eport);

        // and remove it from the portals list
        _delegate.getPortals().remove(eport);
    }

    // documentation inherited
    public EditableSpotSceneModel getSpotSceneModel ()
    {
        flushToModel();
        return _emodel;
    }

    /**
     * This ensures that the modifications we've made to the location and
     * portal arrays are flushed to the model. It is called before
     * returning our model back to the caller.
     */
    protected void flushToModel ()
    {
        List locations = _delegate.getLocations();

        // flush the locations
        int lcount = locations.size();
        _model.locationIds = new int[lcount];
        _model.locationX = new int[lcount];
        _model.locationY = new int[lcount];
        _model.locationOrients = new int[lcount];
        _model.locationClusters = new int[lcount];

        for (int i = 0; i < lcount; i++) {
            Location loc = (Location)locations.get(i);
            _model.locationIds[i] = loc.locationId;
            _model.locationX[i] = loc.x;
            _model.locationY[i] = loc.y;
            _model.locationOrients[i] = loc.orientation;
            _model.locationClusters[i] = loc.clusterIndex;
        }

        // flush the portals
        List portals = _delegate.getPortals();
        int pcount = portals.size();
        _model.portalIds = new int[pcount];
        _model.neighborIds = new int[pcount];
        _model.targetLocIds = new int[pcount];
        _emodel.neighborNames = new ArrayList();
        _emodel.portalNames = new String[pcount];
        _emodel.targetPortalNames = new String[pcount];

        for (int i = 0; i < pcount; i++) {
            EditablePortal port = (EditablePortal)portals.get(i);
            _model.portalIds[i] = port.locationId;
            _model.neighborIds[i] = port.targetSceneId;
            _model.targetLocIds[i] = port.targetLocId;
            _emodel.portalNames[i] = port.name;
            _emodel.neighborNames.add(port.targetSceneName);
            _emodel.targetPortalNames[i] = port.targetPortalName;
        }
    }

    /** Our spot scene model. */
    protected SpotSceneModel _model;

    /** Our editable spot scene model. */
    protected EditableSpotSceneModel _emodel;

    /** Our display spot scene delegate. */
    protected DisplaySpotSceneImpl _delegate;
}
