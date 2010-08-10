package com.threerings.presents.tools;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.velocity.app.VelocityEngine;

import com.samskivert.velocity.VelocityUtil;

public abstract class GenTask extends Task
{
    public GenTask ()
    {
        try {
            _velocity = VelocityUtil.createEngine();
        } catch (Exception e) {
            throw new BuildException("Failure initializing Velocity", e);
        }
    }

    /**
     * Configures to output extra information when generating code.
     */
    public void setVerbose (boolean verbose)
    {
        _verbose = verbose;
    }

    /** Configures our classpath which we'll use to load service classes. */
    public void setClasspathref (Reference pathref)
    {
        _cloader = ClasspathUtils.getClassLoaderForPath(getProject(), pathref);

        // set the parent of the classloader to be the classloader used to load this task, rather
        // than the classloader used to load Ant, so that we have access to Narya classes like
        // TransportHint
        ((AntClassLoader)_cloader).setParent(getClass().getClassLoader());
    }

    protected Class<?> loadClass (String name)
    {
        if (_cloader == null) {
            throw new BuildException("This task requires a 'classpathref' attribute " +
                "to be set to the project's classpath.");
        }
        try {
            return _cloader.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            throw new BuildException(
                "Failed to load " + name + ".  Be sure to set the 'classpathref' attribute to a " +
                "classpath that contains your project's presents classes.", cnfe);
        }
    }

    /** Show extra output if set. */
    protected boolean _verbose;

    /** Used to do our own classpath business. */
    protected ClassLoader _cloader;

    /** Used to generate source files from templates. */
    protected VelocityEngine _velocity;

}
