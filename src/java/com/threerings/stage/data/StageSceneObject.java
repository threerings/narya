//
// $Id: StageSceneObject.java 18473 2004-12-28 03:52:57Z mdb $

package com.threerings.stage.data;

import com.threerings.whirled.spot.data.SpotSceneObject;

/**
 * Extends the basic {@link SpotSceneObject} with data and services
 * specific to isometric stage scenes.
 */
public class StageSceneObject extends SpotSceneObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>stageSceneService</code> field. */
    public static final String STAGE_SCENE_SERVICE = "stageSceneService";

    /** The field name of the <code>lightLevel</code> field. */
    public static final String LIGHT_LEVEL = "lightLevel";

    /** The field name of the <code>lightShade</code> field. */
    public static final String LIGHT_SHADE = "lightShade";
    // AUTO-GENERATED: FIELDS END

    /** Provides stage scene services. */
    public StageSceneMarshaller stageSceneService;

    /** The light level in this scene. 0f being fully on, 1f fully shaded. */
    public float lightLevel = 0f;

    /** The color of the light. */
    public int lightShade = 0xFFFFFF;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>stageSceneService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setStageSceneService (StageSceneMarshaller value)
    {
        StageSceneMarshaller ovalue = this.stageSceneService;
        requestAttributeChange(
            STAGE_SCENE_SERVICE, value, ovalue);
        this.stageSceneService = value;
    }

    /**
     * Requests that the <code>lightLevel</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setLightLevel (float value)
    {
        float ovalue = this.lightLevel;
        requestAttributeChange(
            LIGHT_LEVEL, Float.valueOf(value), Float.valueOf(ovalue));
        this.lightLevel = value;
    }

    /**
     * Requests that the <code>lightShade</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setLightShade (int value)
    {
        int ovalue = this.lightShade;
        requestAttributeChange(
            LIGHT_SHADE, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.lightShade = value;
    }
    // AUTO-GENERATED: METHODS END
}
