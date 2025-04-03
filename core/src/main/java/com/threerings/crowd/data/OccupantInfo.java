//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.data;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

/**
 * The occupant info object contains all of the information about an occupant of a place that
 * should be shared with other occupants of the place. These objects are stored in the place object
 * itself and are updated when bodies enter and exit a place.
 *
 * <p> A system that builds upon the Crowd framework can extend this class to include extra
 * information about their occupants. They will need to provide a derived {@link BodyObject} that
 * creates and configures their occupant info in {@link BodyObject#createOccupantInfo}.
 *
 * <p> Note also that this class implements {@link Cloneable} which means that if derived classes
 * add non-primitive attributes, they are responsible for adding the code to clone those attributes
 * when a clone is requested.
 */
public class OccupantInfo extends SimpleStreamableObject
    implements DSet.Entry, Cloneable
{
    /** Constant value for {@link #status}. */
    public static final byte ACTIVE = 0;

    /** Constant value for {@link #status}. */
    public static final byte IDLE = 1;

    /** Constant value for {@link #status}. */
    public static final byte DISCONNECTED = 2;

    /** Maps status codes to human readable strings. */
    public static final String[] X_STATUS = { "active", "idle", "discon" };

    /** Used by PlaceManager.updateOccupantInfo. */
    public static interface Updater<T extends OccupantInfo>
    {
        /**
         * Make whatever changes are desired to your {@link OccupantInfo} here.
         *
         * @return true if the record was modified and should be published, false if no
         * modifications were made (it will not be published).
         */
        public boolean update (T info);
    }

    /** An update to dispatch when an occupant's name changes. */
    public static class NameUpdater implements Updater<OccupantInfo>
    {
        public NameUpdater (Name name) {
            _name = name;
        }
        public boolean update (OccupantInfo info) {
            // The behaviour here used to be to compare the names themselves against one another
            // using equals(), but was changed to accommodate the idea of display name changing
            // while fundamental identity stays the same -- case in point, Whirled's MemberName
            // bases equal()ity on an integer identifier. TODO: investigate whether this is a
            // reasonable assumption and whether the behaviour change might break something.
            if (info.username.getNormal().equals(_name.getNormal())) {
                return false;
            }
            info.username = _name;
            return true;
        }
        protected Name _name;
    }

    /** The body object id of this occupant (and our entry key). */
    public Integer bodyOid;

    /** The username of this occupant. */
    public Name username;

    /** The status of this occupant. */
    public byte status = ACTIVE;

    /**
     * Creates an occupant info with information from the specified occupant's body object.
     */
    public OccupantInfo (BodyObject body)
    {
        bodyOid = Integer.valueOf(body.getOid());
        username = body.getVisibleName();
        status = body.status;
    }

    /** Used for unserialization. */
    public OccupantInfo ()
    {
    }

    /** Access to the body object id as an int. */
    public int getBodyOid ()
    {
        return bodyOid.intValue();
    }

    // documentation inherited
    public Comparable<?> getKey ()
    {
        return bodyOid;
    }

    @Override
    public OccupantInfo clone ()
    {
        try {
            return (OccupantInfo) super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse);
        }
    }
}
