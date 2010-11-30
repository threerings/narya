//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.DObject;

/**
 * Generates ActionScript versions of {@link Streamable} classes and provides routines used by the
 * {@link GenDObjectTask} to create ActionScript versions of distributed objects.
 */
public class GenActionScriptTask extends GenTask
{
    /**
     * Configures the path to our ActionScript source files.
     */
    public void setAsroot (File asroot)
    {
        _asroot = asroot;
    }

    /**
     * Processes a resolved Streamable class instance.
     */
    @Override
    public void processClass (File javaSource, Class<?> sclass)
        throws Exception
    {
        // make sure we implement Streamable but don't extend DObject or InvocationMarshaller and
        // that we're a class not an interface
        if (!Streamable.class.isAssignableFrom(sclass) ||
            DObject.class.isAssignableFrom(sclass) ||
            InvocationMarshaller.class.isAssignableFrom(sclass) ||
            ((sclass.getModifiers() & Modifier.INTERFACE) != 0) ||
            ActionScriptUtils.hasOmitAnnotation(sclass)) {
            // System.err.println("Skipping " + sclass.getName() + "...");
            return;
        }

        File output = ActionScriptUtils.createActionScriptPath(_asroot, sclass);

        System.err.println("Converting " + sclass.getName() + "...");
        // parse the existing ActionScript source and generate what we don't
        // have from the Java class
        ActionScriptSource assrc = new ActionScriptSource(sclass);
        assrc.absorbJava(javaSource);
        assrc.imports.add(ObjectInputStream.class.getName());
        assrc.imports.add(ObjectOutputStream.class.getName());

        // see if our parent also implements Streamable
        boolean needSuper = Streamable.class.isAssignableFrom(sclass.getSuperclass());

        // add readObject() and writeObject() definitions
        ActionScriptSource.Member member;
        member = new ActionScriptSource.Member(
            "readObject", (needSuper ? "override " : "") + READ_SIG);
        member.noreplace = true;
        member.comment = "    // from interface Streamable\n";
        StringBuilder body = new StringBuilder("    {\n");
        if (needSuper) {
            body.append("        super.readObject(ins);\n");
        }
        int added = 0;
        for (Field field : sclass.getDeclaredFields()) {
            if (!isStreamable(field)) {
                continue;
            }
            body.append("        ");
            body.append(field.getName()).append(" = ins.");
            body.append(ActionScriptUtils.toReadObject(field.getType()));
            body.append(";\n");
            added++;
        }
        member.body = body.append("    }\n").toString();
        if (added > 0) {
            assrc.publicMethods.add(member);
        }

        member = new ActionScriptSource.Member(
            "writeObject", (needSuper ? "override " : "") + WRITE_SIG);
        member.noreplace = true;
        member.comment = "    // from interface Streamable\n";
        body = new StringBuilder("    {\n");
        if (needSuper) {
            body.append("        super.writeObject(out);\n");
        }
        added = 0;
        for (Field field : sclass.getDeclaredFields()) {
            if (!isStreamable(field)) {
                continue;
            }
            body.append("        out.");
            body.append(ActionScriptUtils.toWriteObject(field.getType(), field.getName()));
            body.append(";\n");
            added++;
        }
        member.body = body.append("    }\n").toString();
        if (added > 0) {
            assrc.publicMethods.add(member);
        }

        // now we can parse existing definitions from any extant ActionScript source file
        assrc.absorbActionScript(output);

        // make sure our parent directory exists
        output.getParentFile().mkdirs();

        // now write all that out to the target source file
        BufferedWriter out = new BufferedWriter(new FileWriter(output));
        assrc.write(new PrintWriter(out));
    }

    protected boolean isStreamable (Field field)
    {
        int mods = field.getModifiers();
        return !Modifier.isStatic(mods) && !Modifier.isTransient(mods);
    }

    /** The path to our ActionScript source files. */
    protected File _asroot;

    protected static final String READ_SIG =
        "public function readObject (ins :ObjectInputStream) :void";
    protected static final String WRITE_SIG =
        "public function writeObject (out :ObjectOutputStream) :void";

}
