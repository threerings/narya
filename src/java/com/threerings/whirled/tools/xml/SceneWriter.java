//
// $Id: SceneWriter.java,v 1.3 2003/01/30 19:16:50 mdb Exp $

package com.threerings.whirled.tools.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;
import com.samskivert.util.StringUtil;

import com.threerings.whirled.tools.EditableScene;

/**
 * Generates an XML representation of an {@link EditableScene}.
 */
public class SceneWriter
{
    /**
     * Writes the data for the supplied {@link EditableScene} to the XML
     * writer supplied. The writer will already be configured with the
     * appropriate indentation level so that this writer can simply output
     * its elements and allow the calling code to determine where in the
     * greater scene description file the scene data should live.
     */
    public void writeScene (EditableScene scene, DataWriter writer)
        throws SAXException
    {
        AttributesImpl attrs = new AttributesImpl();
        addSceneAttributes(scene, attrs);
        writer.startElement("", sceneElementName(), "", attrs);
        writeSceneData(scene, writer);
        writer.endElement(sceneElementName());
    }

    /**
     * Returns the name of the top-level element that we'll use when we
     * write out the scene (defaults to <code>scene</code>).
     */
    protected String sceneElementName ()
    {
        return "scene";
    }

    /**
     * Adds attributes to the top-level element before it gets written.
     */
    protected void addSceneAttributes (
        EditableScene scene, AttributesImpl attrs)
    {
        attrs.addAttribute("", "name", "", "", scene.getName());
        attrs.addAttribute("", "version", "", "",
                           Integer.toString(scene.getVersion()));
    }

    /**
     * Writes just the scene data which is handy for derived classes which
     * may wish to add their own scene data to the scene output.
     */
    protected void writeSceneData (EditableScene scene, DataWriter writer)
        throws SAXException
    {
        Iterator iter = scene.getNeighborNames().iterator();
        while (iter.hasNext()) {
            writer.dataElement("neighbor", (String)iter.next());
        }
    }

    /**
     * Writes the supplied scene out to the specified file using the
     * supplied scene writer.
     */
    public static void writeScene (File out, SceneWriter writer,
                                   EditableScene scene)
        throws IOException, SAXException
    {
        FileWriter fout = new FileWriter(out);
        DataWriter dout = new DataWriter(fout);
        dout.setIndentStep(2);
        dout.startDocument();
        writer.writeScene(scene, dout);
        dout.endDocument();
        fout.close();
    }
}
