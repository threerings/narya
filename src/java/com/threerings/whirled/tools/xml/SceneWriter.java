//
// $Id: SceneWriter.java,v 1.4 2003/02/12 07:23:32 mdb Exp $

package com.threerings.whirled.tools.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;
import com.samskivert.util.StringUtil;
import com.threerings.tools.xml.NestableWriter;

import com.threerings.whirled.Log;
import com.threerings.whirled.data.AuxModel;
import com.threerings.whirled.data.SceneModel;

/**
 * Generates an XML representation of an {@link SceneModel}.
 */
public class SceneWriter
{
    /** The outer element used to enclose our scene definition. */
    public static final String OUTER_ELEMENT = "scene";

    /**
     * Registers a writer for writing auxiliary scene models of the
     * supplied class.
     */
    public void registerAuxWriter (Class aclass, NestableWriter writer)
    {
        _auxers.put(aclass, writer);
    }

    /**
     * Writes the supplied scene out to the specified file.
     */
    public void writeScene (File out, SceneModel model)
        throws IOException, SAXException
    {
        FileWriter fout = new FileWriter(out);
        DataWriter dout = new DataWriter(fout);
        dout.setIndentStep(2);
        dout.startDocument();
        writeSceneModel(model, dout);
        dout.endDocument();
        fout.close();
    }

    /**
     * Writes the data for the supplied {@link SceneModel} to the XML data
     * writer supplied. The writer should already be configured with the
     * appropriate indentation level so that this writer can simply output
     * its elements and allow the calling code to determine where in the
     * greater scene description file the scene data should live.
     */
    public void writeSceneModel (SceneModel model, DataWriter writer)
        throws SAXException
    {
        AttributesImpl attrs = new AttributesImpl();
        addSceneAttributes(model, attrs);
        writer.startElement("", OUTER_ELEMENT, "", attrs);
        writeSceneData(model, writer);
        writer.endElement(OUTER_ELEMENT);
    }

    /**
     * Adds attributes to the top-level element before it gets written.
     */
    protected void addSceneAttributes (
        SceneModel model, AttributesImpl attrs)
    {
        attrs.addAttribute("", "name", "", "", model.name);
        attrs.addAttribute("", "version", "", "",
                           Integer.toString(model.version));
    }

    /**
     * Writes just the scene data which is handy for derived classes which
     * may wish to add their own scene data to the scene output.
     */
    protected void writeSceneData (SceneModel model, DataWriter writer)
        throws SAXException
    {
        // write out our auxiliary scene models
        for (int ii = 0; ii < model.auxModels.length; ii++) {
            AuxModel amodel = model.auxModels[ii];
            NestableWriter awriter = (NestableWriter)
                _auxers.get(amodel.getClass());
            if (awriter != null) {
                awriter.write(amodel, writer);
            } else {
                Log.warning("No writer registered for auxiliary scene model " +
                            "[mclass=" + amodel.getClass() + "].");
            }
        }
    }

    protected HashMap _auxers = new HashMap();
}
