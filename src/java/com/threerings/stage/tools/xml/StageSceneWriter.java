//
// $Id: StageSceneWriter.java 20143 2005-03-30 01:12:48Z mdb $

package com.threerings.stage.tools.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;

import com.threerings.miso.tools.xml.SparseMisoSceneWriter;

import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.spot.tools.xml.SpotSceneWriter;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.tools.xml.SceneWriter;

import com.threerings.stage.data.StageMisoSceneModel;

import com.threerings.stage.data.StageSceneModel;

/**
 * Generates an XML representation of a {@link StageSceneModel}.
 */
public class StageSceneWriter extends SceneWriter
{
    public StageSceneWriter ()
    {
        // register our auxiliary model writers
        registerAuxWriter(SpotSceneModel.class, new SpotSceneWriter());
        registerAuxWriter(StageMisoSceneModel.class,
                          new SparseMisoSceneWriter());
    }

    // documentation inherited
    protected void addSceneAttributes (SceneModel scene, AttributesImpl attrs)
    {
        super.addSceneAttributes(scene, attrs);
        StageSceneModel sscene = (StageSceneModel)scene;
        attrs.addAttribute("", "type", "", "", sscene.type);
    }

    // documentation inherited
    protected void writeSceneData (SceneModel scene, DataWriter writer)
        throws SAXException
    {
        // write out any default colorizations
        StageSceneModel sscene = (StageSceneModel)scene;
        if (sscene.defaultColors != null) {
            writer.startElement("zations");
            int[] keys = sscene.defaultColors.getKeys();
            for (int ii=0, nn=keys.length; ii < nn; ii++) {
                int value = sscene.defaultColors.get(keys[ii]);
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "classId", "", "",
                                   String.valueOf(keys[ii]));
                attrs.addAttribute("", "colorId", "", "",
                                   String.valueOf(value));
                writer.emptyElement("", "zation", "", attrs);
            }
            writer.endElement("zations");
        }

        super.writeSceneData(scene, writer);
    }
}
