//
// $Id: OccupantList.java,v 1.7 2002/10/27 01:25:18 mdb Exp $

package com.threerings.micasa.client;

import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JList;

import com.threerings.crowd.client.OccupantObserver;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.micasa.util.MiCasaContext;

/**
 * The occupant list displays the list of users that are in a particular
 * place.
 */
public class OccupantList
    extends JList implements PlaceView, OccupantObserver
{
    /**
     * Constructs an occupant list with the supplied context which it will
     * use to register itself with the necessary managers.
     */
    public OccupantList (MiCasaContext ctx)
    {
        // set up our list model
        _model = new DefaultListModel();
        setModel(_model);

        // keep our context around for later
        _ctx = ctx;

        // register ourselves as an occupant observer
        _ctx.getOccupantDirector().addOccupantObserver(this);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        // add all of the occupants of the place to our list
        Iterator users = plobj.occupantInfo.entries();
        while (users.hasNext()) {
            OccupantInfo info = (OccupantInfo)users.next();
            _model.addElement(info.username);
        }
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        // clear out our occupant entries
        _model.clear();
    }

    // documentation inherited
    public void occupantEntered (OccupantInfo info)
    {
        // simply add this user to our list
        _model.addElement(info.username);
    }

    // documentation inherited
    public void occupantLeft (OccupantInfo info)
    {
        // remove this occupant from our list
        _model.removeElement(info.username);
    }

    // documentation inherited
    public void occupantUpdated (OccupantInfo oinfo, OccupantInfo info)
    {
        // nothing doing
    }

    /** Our client context. */
    protected MiCasaContext _ctx;

    /** A list model that provides a vector interface. */
    protected DefaultListModel _model;
}
