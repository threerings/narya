//
// $Id: MetadataBundlerTask.java,v 1.4 2003/06/17 23:29:33 ray Exp $

package com.threerings.cast.bundle.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.zip.Deflater;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.samskivert.util.Tuple;

import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.tools.xml.SwissArmyTileSetRuleSet;

import com.threerings.cast.ActionSequence;
import com.threerings.cast.ComponentClass;
import com.threerings.cast.bundle.BundleUtil;

import com.threerings.cast.tools.xml.ActionRuleSet;
import com.threerings.cast.tools.xml.ClassRuleSet;

/**
 * Ant task for creating metadata bundles, which contain action sequence
 * and component class definition information. This task must be
 * configured with a number of parameters:
 *
 * <pre>
 * actiondef=[path to actions.xml]
 * classdef=[path to classes.xml]
 * file=[path to metadata bundle, which will be created]
 * </pre>
 */
public class MetadataBundlerTask extends Task
{
    public void setActiondef (String actiondef)
    {
        _actionDef = actiondef;
    }

    public void setClassdef (String classdef)
    {
        _classDef = classdef;
    }

    public void setTarget (File target)
    {
        _target = target;
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute ()
        throws BuildException
    {
        // make sure everythign was set up properly
        ensureSet(_actionDef, "Must specify the action sequence " +
                  "definitions via the 'actiondef' attribute.");
        ensureSet(_classDef, "Must specify the component class definitions " +
                  "via the 'classdef' attribute.");
        ensureSet(_target, "Must specify the path to the target bundle " +
                  "file via the 'target' attribute.");

        // make sure we can write to the target bundle file
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(_target);

            // parse our metadata
            Tuple tuple = parseActions();
            HashMap actions = (HashMap)tuple.left;
            HashMap actionSets = (HashMap)tuple.right;
            HashMap classes = parseClasses();

            // and create the bundle file
            JarOutputStream jout = new JarOutputStream(fout);
            jout.setLevel(Deflater.BEST_COMPRESSION);

            // throw the serialized actions table in there
            JarEntry aentry = new JarEntry(BundleUtil.ACTIONS_PATH);
            jout.putNextEntry(aentry);
            ObjectOutputStream oout = new ObjectOutputStream(jout);
            oout.writeObject(actions);
            oout.flush();

            // throw the serialized action tilesets table in there
            JarEntry sentry = new JarEntry(BundleUtil.ACTION_SETS_PATH);
            jout.putNextEntry(sentry);
            oout = new ObjectOutputStream(jout);
            oout.writeObject(actionSets);
            oout.flush();

            // throw the serialized classes table in there
            JarEntry centry = new JarEntry(BundleUtil.CLASSES_PATH);
            jout.putNextEntry(centry);
            oout = new ObjectOutputStream(jout);
            oout.writeObject(classes);
            oout.flush();

            // close it up and we're done
            jout.close();

        } catch (IOException ioe) {
            String errmsg = "Unable to output to target bundle " +
                "[path=" + _target.getPath() + "].";
            throw new BuildException(errmsg, ioe);

        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ioe) {
                    // nothing to complain about here
                }
            }
        }
    }

    protected Tuple parseActions ()
        throws BuildException
    {
        // scan through the XML once to read the actions
        Digester digester = new Digester();
        ActionRuleSet arules = new ActionRuleSet();
        arules.setPrefix("actions");
        digester.addRuleSet(arules);
        digester.addSetNext("actions" + ActionRuleSet.ACTION_PATH,
                            "add", Object.class.getName());
        ArrayList actlist = parseList(digester, _actionDef);

        // now go through a second time reading the tileset info
        digester = new Digester();
        SwissArmyTileSetRuleSet srules = new SwissArmyTileSetRuleSet();
        srules.setPrefix("actions" + ActionRuleSet.ACTION_PATH);
        digester.addRuleSet(srules);
        digester.addSetNext("actions" + ActionRuleSet.ACTION_PATH +
                            SwissArmyTileSetRuleSet.TILESET_PATH, "add",
                            Object.class.getName());
        ArrayList setlist = parseList(digester, _actionDef);

        // sanity check
        if (actlist.size() != setlist.size()) {
            String errmsg = "An action is missing its tileset " +
                "definition, or something even wackier is going on.";
            throw new BuildException(errmsg);
        }

        // now create our mappings
        HashMap actmap = new HashMap();
        HashMap setmap = new HashMap();

        // create the action map
        for (int i = 0; i < setlist.size(); i++) {
            TileSet set = (TileSet)setlist.get(i);
            ActionSequence act = (ActionSequence)actlist.get(i);
            // make sure nothing was missing in the action sequence
            // definition parsed from XML
            String errmsg = ActionRuleSet.validate(act);
            if (errmsg != null) {
                errmsg = "Action sequence invalid [seq=" + act +
                    ", error=" + errmsg + "].";
                throw new BuildException(errmsg);
            }
            actmap.put(act.name, act);
            setmap.put(act.name, set);
        }

        return new Tuple(actmap, setmap);
    }

    protected HashMap parseClasses ()
        throws BuildException
    {
        // load up our action and class info
        Digester digester = new Digester();

        // add our action rule set and a a rule to grab parsed actions
        ClassRuleSet crules = new ClassRuleSet();
        crules.setPrefix("classes");
        digester.addRuleSet(crules);
        digester.addSetNext("classes" + ClassRuleSet.CLASS_PATH,
                            "add", Object.class.getName());

        ArrayList setlist = parseList(digester, _classDef);
        HashMap clmap = new HashMap();

        // create the action map
        for (int i = 0; i < setlist.size(); i++) {
            ComponentClass cl = (ComponentClass)setlist.get(i);
            clmap.put(cl.name, cl);
        }

        return clmap;
    }

    protected ArrayList parseList (Digester digester, String path)
        throws BuildException
    {
        try {
            FileInputStream fin = new FileInputStream(path);
            BufferedInputStream bin = new BufferedInputStream(fin);

            ArrayList setlist = new ArrayList();
            digester.push(setlist);

            // now fire up the digester to parse the stream
            try {
                digester.parse(bin);
            } catch (Exception e) {
                throw new BuildException("Parsing error.", e);
            }

            return setlist;

        } catch (FileNotFoundException fnfe) {
            String errmsg = "Unable to load metadata definition file " +
                "[path=" + path + "].";
            throw new BuildException(errmsg, fnfe);
        }
    }

    protected void ensureSet (Object value, String errmsg)
        throws BuildException
    {
        if (value == null) {
            throw new BuildException(errmsg);
        }
    }

    protected String _actionDef;
    protected String _classDef;
    protected File _target;
}
