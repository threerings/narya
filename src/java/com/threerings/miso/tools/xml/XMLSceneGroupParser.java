//
// $Id: XMLSceneGroupParser.java,v 1.1 2001/08/16 20:14:06 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.*;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.samskivert.util.*;
import com.samskivert.xml.XMLUtil;
import com.threerings.miso.Log;
import com.threerings.miso.scene.*;

/**
 * Parses an XML scene group description file, loads the referenced
 * scenes into the runtime scene repository, and creates bindings
 * between the portals in the scenes.
 *
 * <p> Does not currently perform validation on the input XML stream,
 * though the parsing code assumes the XML document is well-formed.
 */
public class XMLSceneGroupParser extends DefaultHandler
{
    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
	_tag = qName;
    }

    public void endElement (String uri, String localName, String qName)
    {
	// we know we've received the entirety of the character data
        // for the elements we're tracking at this point, so proceed
        // with saving off element data for later use.

	// note that we're not within a tag to avoid considering any
	// characters during this quiescent time
	_tag = null;

        // and clear out the character data we're gathering
        _chars = new StringBuffer();
    }

    public void characters (char ch[], int start, int length)
    {
	// bail if we're not within a meaningful tag
	if (_tag == null) return;

  	_chars.append(ch, start, length);
    }

    /**
     * Parse the specified XML file and return a list of miso scene
     * objects that have been fully bound and loaded into the scene
     * repository.
     *
     * @param fname the file name.
     *
     * @return the list of scene objects, or null if an error occurred.
     */
    public List loadSceneGroup (String fname) throws IOException
    {
        _fname = fname;

	try {
            // get the file input stream
	    FileInputStream fis = new FileInputStream(fname);
            BufferedInputStream bis = new BufferedInputStream(fis);

            // prepare temporary data storage for parsing
	    _chars = new StringBuffer();

            // read the XML input stream and construct all scene objects
	    XMLUtil.parse(this, bis);

            // return the final list of scene objects
            return _info.scenes;

        } catch (ParserConfigurationException pce) {
  	    throw new IOException(pce.toString());

	} catch (SAXException saxe) {
	    throw new IOException(saxe.toString());
	}
    }

    /** The file currently being processed. */
    protected String _fname;

    /** The XML element tag currently being processed. */
    protected String _tag;

    /** Temporary storage of scene group values and data. */
    protected StringBuffer _chars;

    /** The scene group info gathered while parsing. */
    protected SceneGroupInfo _info;

    /**
     * A class to hold information on the overall scene group while
     * parsing, as well as the resulting list of scene objects
     * produced when finished.
     */
    class SceneGroupInfo
    {
	/** The list of scenes constructed once parsing is complete. */
	public ArrayList scenes;

	/** The scene info for all scenes described in the group. */
	public ArrayList sceneinfo;

	/** The current scene info. */
	public SceneInfo curscene;
    }

    /**
     * A class to hold information on a single scene's bindings.
     */
    class SceneInfo
    {
	/** The logical scene name. */
	public String name;

	/** The scene description file path. */
	public String src;

	/** The default scene entrance. */
	public String entrance;

	/** The list of portal info objects describing portal bindings. */
	public ArrayList portals;

	public SceneInfo (String src)
	{
	    this.src = src;
	}
    }

    /**
     * A class to hold information on a single portal's bindings.
     */
    class PortalInfo
    {
	/** The source portal name. */
	public String src;

	/** The destination portal name within the destination scene. */
	public String dest;

	/** The logical destination scene name. */
	public String destScene;

	public PortalInfo (String src, String destScene, String dest)
	{
	    this.src = src;
	    this.destScene = destScene;
	    this.dest = dest;
	}
    }
}
