//
// $Id: SceneUpdate.java,v 1.1 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.io.Streamable;

import com.threerings.whirled.Log;

/**
 * Used to encapsulate updates to scenes in such a manner that updates can
 * be stored persistently and sent to clients to update their own local
 * copies of scenes.
 */
public abstract class SceneUpdate
    implements Streamable, Cloneable
{
    /**
     * Creates a scene update that will operate on a scene with the
     * specified target scene and version number.
     *
     * @param targetId the id of the scene on which we are to operate.
     * @param targetVersion the version of the scene on which we are to
     * operate.
     */
    public SceneUpdate (int targetId, int targetVersion)
    {
        _targetId = targetId;
        _targetVersion = targetVersion;
    }

    /**
     * Returns the scene id for which this update is appropriate.
     */
    public int getSceneId ()
    {
        return _targetId;
    }

    /**
     * Returns the scene version for which this update is appropriate.
     */
    public int getSceneVersion ()
    {
        return _targetVersion;
    }

    /**
     * Called to ensure that the scene is in the appropriate state prior
     * to applying the update.
     *
     * @exception IllegalStateException thrown if the update cannot be
     * applied to the scene because it is not in a valid state
     * (appropriate previous updates were not applied, it's the wrong kind
     * of scene, etc.).
     */
    public void validate (SceneModel model)
        throws IllegalStateException
    {
        if (model.sceneId != _targetId) {
            String errmsg = "Wrong target scene, expected id " +
                _targetId + " got id " + model.sceneId;
            throw new IllegalStateException(errmsg);
        }
        if (model.version != _targetVersion) {
            String errmsg = "Target scene not proper version, expected " +
                _targetVersion + " got " + model.version;
            throw new IllegalStateException(errmsg);
        }
    }

    /**
     * Applies this update to the specified scene model. Derived classes
     * will want to override this method and apply updates of their own,
     * being sure to call <code>super.applyToScene</code>.
     */
    public void apply (SceneModel model)
    {
        // increment the version; disallowing integer overflow
        model.version = Math.max(_targetVersion + 1, model.version);

        // sanity check for the amazing two billion updates
        if (model.version == _targetVersion) {
            Log.warning("Egads! This scene has been updated two billion " +
                        "times [model=" + model + ", update=" + this + "].");
        }
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * An extensible mechanism for generating a string representation of
     * this instance.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("sceneId=").append(_targetId);
        buf.append(", version=").append(_targetVersion);
    }

    /** The version number of the scene on which we operate. */
    protected int _targetId;

    /** The version number of the scene on which we operate. */
    protected int _targetVersion;
}
