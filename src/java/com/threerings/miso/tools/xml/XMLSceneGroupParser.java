//
// $Id: XMLSceneGroupParser.java,v 1.7 2001/10/17 22:21:22 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.*;
import java.util.*;

import org.xml.sax.*;

import com.samskivert.util.*;
import com.samskivert.xml.SimpleParser;

import com.threerings.miso.Log;
import com.threerings.miso.scene.*;
import com.threerings.miso.scene.util.MisoSceneUtil;

import com.threerings.whirled.data.Scene;

/**
 * Parses an XML scene group description file, loads the referenced
 * scenes into the runtime scene repository, and creates bindings
 * between the portals in the scenes.  Does not currently perform
 * validation on the input XML stream, though the parsing code assumes
 * the XML document is well-formed.
 */
public class XMLSceneGroupParser extends SimpleParser
{
    // documentation inherited
    public void startElement (
        String uri, String localName, String qName, Attributes attributes)
    {
	if (qName.equals("scene")) {
	    // construct a new scene info object
	    _info.curscene = new SceneInfo(attributes.getValue("src"));

	    // add it to the list of scene info objects
	    _info.sceneinfo.add(_info.curscene);

	} else if (qName.equals("portal")) {
	    // pull out the portal data
	    String src = attributes.getValue("src");
	    String destScene = attributes.getValue("destscene");
	    String dest = attributes.getValue("dest");

	    // construct a new portal info object
	    PortalInfo pinfo = new PortalInfo(src, destScene, dest);

	    // add it to the current scene's list of portal info objects
	    _info.curscene.portals.add(pinfo);
	}
    }

    // documentation inherited
    public void finishElement (
        String uri, String localName, String qName, String data)
    {
	if (qName.equals("name")) {
	    _info.curscene.name = data;

	} else if (qName.equals("entrance")) {
	    _info.curscene.entrance = data;

	} else if (qName.equals("scene")) {
	    // TODO: load scene from scene repository and set
	    // _info.curscene.scene to the resulting scene object

	} else if (qName.equals("scenegroup")) {
	    // now that we've obtained all scene info and fully loaded
	    // the associated scene objects, resolve bindings between
	    // all scenes in the group.
	    _info.resolveBindings();

	    // warn about any remaining un-bound portals
	    _info.checkUnboundPortals();
	}
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
        _info = new SceneGroupInfo();
        parseFile(fname);
        return _info.getScenes();
    }

    /** The scene group info gathered while parsing. */
    protected SceneGroupInfo _info;

    /**
     * A class to hold information on the overall scene group while
     * parsing, as well as the resulting list of scene objects
     * produced when finished.
     */
    class SceneGroupInfo
    {
	/** The scene info for all scenes described in the group. */
	public ArrayList sceneinfo;

	/** The current scene info. */
	public SceneInfo curscene;

	public SceneGroupInfo ()
	{
	    sceneinfo = new ArrayList();
	}

	/**
	 * Return a list of the scene objects contained within all
	 * scene info objects.  Intended to be called once all parsing
	 * and resolving of portal bindings has been completed.
	 */
	public List getScenes ()
	{
	    ArrayList scenes = new ArrayList();
	    int size = sceneinfo.size();
	    for (int ii = 0; ii < size; ii++) {
		scenes.add(((SceneInfo)sceneinfo.get(ii)).scene);
	    }
	    return scenes;
	}

	/**
	 * Return the scene object associated with the named scene
	 * info object.
	 *
	 * @param name the scene info name.
	 *
	 * @return the scene object or null if the scene info object
	 *         isn't found or has no scene object.
	 */
	public MisoScene getScene (String name)
	{
	    int size = sceneinfo.size();
	    for (int ii = 0; ii < size; ii++) {
		SceneInfo sinfo = (SceneInfo)sceneinfo.get(ii);
		if (sinfo.name.equals(name)) return sinfo.scene;
	    }
	    return null;
	}

	/**
	 * Resolve the portal bindings and entrance portal for all scenes.
	 */
	public void resolveBindings ()
	{
	    int size = sceneinfo.size();
	    for (int ii = 0; ii < size; ii++) {
		// retrieve the next scene info object
		SceneInfo sinfo = (SceneInfo)sceneinfo.get(ii);

		int psize = sinfo.portals.size();
		for (int jj = 0; jj < psize; jj++) {
		    // retrieve the next portal info object
		    PortalInfo pinfo = (PortalInfo)sinfo.portals.get(jj);

		    // resolve the portal binding
		    resolvePortal(sinfo, pinfo);

		    // resolve the entrance portal
		    resolveEntrance(sinfo);
		}
	    }
	}

	/**
	 * Resolve the binding for the portal in the given scene.
	 *
	 * @param sinfo the scene info.
	 * @param pinfo the portal info.
	 */
	protected void resolvePortal (SceneInfo sinfo, PortalInfo pinfo)
	{
	    // get the source portal object
	    Portal src = MisoSceneUtil.getPortal(sinfo.scene, pinfo.src);
	    if (src == null) {
		Log.warning("Missing source portal [scene=" + sinfo.name +
			    ", pinfo=" + pinfo + "].");
		return;
	    }

	    // get the destination scene
	    MisoScene destScene = getScene(pinfo.destScene);
	    if (destScene == null) {
		Log.warning("Missing destination scene [scene=" + sinfo.name +
			    ", pinfo=" + pinfo + "].");
		return;
	    }

	    // get the destination portal object
	    Portal dest = MisoSceneUtil.getPortal(destScene, pinfo.dest);
	    if (dest == null) {
		Log.warning("Missing destination portal [scene=" + sinfo.name +
			    ", pinfo=" + pinfo + "].");
		return;
	    }

	    // update source portal with full destination information
	    src.setDestination(destScene.getId(), dest);
	}

	/**
	 * Resolve the binding for the entrance portal in the given scene.
	 *
	 * @param sinfo the scene info.
	 */
	protected void resolveEntrance (SceneInfo sinfo)
	{
	    Portal entrance = MisoSceneUtil.getPortal(
                sinfo.scene, sinfo.entrance);
	    if (entrance == null) {
		Log.warning( "Missing entrance portal [scene=" + sinfo.name +
			     ", entrance=" + sinfo.entrance + "].");
		return;
	    }

	    sinfo.scene.setEntrance(entrance);
	}

	/**
	 * Scan through all scenes and output a warning message if any
	 * portals are found that aren't bound to a destination
	 * portal.  Intended for calling after {@link
	 * #resolveBindings} is called to make sure the scene group
	 * description wasn't lacking a binding for some portal; or,
	 * that the scenes don't contain any unintended or unnecessary
	 * portals.
	 */
	public void checkUnboundPortals ()
	{
	    int size = sceneinfo.size();
	    for (int ii = 0; ii < size; ii++) {
		// retrieve the next scene info object
		SceneInfo sinfo = (SceneInfo)sceneinfo.get(ii);

		// get the portals in the scene
		List portals = sinfo.scene.getPortals();

		// check each portal to make sure it has a valid destination
		int psize = portals.size();
		for (int jj = 0; jj < size; jj++) {
		    Portal portal = (Portal)portals.get(jj);
		    if (!portal.hasDestination()) {
			Log.warning("Unbound portal [scene=" + sinfo.name +
				    ", portal=" + portal + "].");
		    }
		}
	    }
	}
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

	/** The scene object obtained from the scene repository. */
	public MisoSceneImpl scene;

	public SceneInfo (String src)
	{
	    this.src = src;
	    portals = new ArrayList();
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

	public String toString ()
	{
	    StringBuffer buf = new StringBuffer();
	    buf.append("[src=").append(src);
	    buf.append(", dest=").append(dest);
	    buf.append(", destScene=").append(destScene);
	    return buf.append("]").toString();
	}
    }
}
