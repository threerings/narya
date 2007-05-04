//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

import com.samskivert.util.StringUtil;

/**
 * Reads in a source file, allows the replacement of a "generated fields" and "generated methods"
 * section and writing of the file back out to the disk.
 */
public class SourceFile
{
    /**
     * Reads the code from the supplied source file.
     */
    public void readFrom (File source)
        throws IOException
    {
        // slurp our source file into newline separated strings
        BufferedReader bin = new BufferedReader(new FileReader(source));
        ArrayList<String> llist = new ArrayList<String>();
        String line = null;
        while ((line = bin.readLine()) != null) {
            llist.add(line);
        }
        _lines = llist.toArray(new String[llist.size()]);
        bin.close();

        // now determine where to insert our static field declarations and our generated methods
        int bstart = -1, bend = -1;
        for (int ii = 0; ii < _lines.length; ii++) {
            line = _lines[ii].trim();

            // look for the start of the class body
            if (GenUtil.NAME_PATTERN.matcher(line).find()) {
                if (line.endsWith("{")) {
                    bstart = ii+1;
                } else {
                    // search down a few lines for the open brace
                    for (int oo = 1; oo < 10; oo++) {
                        if (get(_lines, ii+oo).trim().endsWith("{")) {
                            bstart = ii+oo+1;
                            break;
                        }
                    }
                }

            // track the last } on a line by itself and we'll call that the end of the class body
            } else if (line.equals("}")) {
                bend = ii;

            // look for our field and method markers
            } else if (line.equals(FIELDS_START)) {
                _nstart = ii;
            } else if (line.equals(FIELDS_END)) {
                _nend = ii+1;
            } else if (line.equals(METHODS_START)) {
                _mstart = ii;
            } else if (line.equals(METHODS_END)) {
                _mend = ii+1;
            }
        }

        // sanity check the markers
        check(source, "fields start", _nstart, "fields end", _nend);
        check(source, "fields end", _nend, "fields start", _nstart);
        check(source, "methods start", _mstart, "methods end", _mend);
        check(source, "methods end", _mend, "methods start", _mstart);

        // we have no previous markers then stuff the fields at the top of the class body and the
        // methods at the bottom
        if (_nstart == -1) {
            _nstart = bstart;
            _nend = bstart;
        }
        if (_mstart == -1) {
            _mstart = bend;
            _mend = bend;
        }
    }

    /**
     * Returns true if the supplied text appears in the non-auto-generated section.
     */
    public boolean containsString (String text)
    {
        for (int ii = 0; ii < _nstart; ii++) {
            if (_lines[ii].indexOf(text) != -1) {
                return true;
            }
        }
        for (int ii = _nend; ii < _mstart; ii++) {
            if (_lines[ii].indexOf(text) != -1) {
                return true;
            }
        }
        for (int ii = _mend; ii < _lines.length; ii++) {
            if (_lines[ii].indexOf(text) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes the code out to the specified file.
     *
     * @param fsection the new "generated fields" to write to the file, or null.
     * @param msection the new "generated methods" to write to the file, or null.
     */
    public void writeTo (File dest, String fsection, String msection)
        throws IOException
    {
        BufferedWriter bout = new BufferedWriter(new FileWriter(dest));

        // write the preamble
        for (int ii = 0; ii < _nstart; ii++) {
            writeln(bout, _lines[ii]);
        }

        // write the field section
        if (!StringUtil.isBlank(fsection)) {
            String prev = get(_lines, _nstart-1);
            if (!StringUtil.isBlank(prev) && !prev.equals("{")) {
                bout.newLine();
            }
            writeln(bout, "    " + FIELDS_START);
            bout.write(fsection);
            writeln(bout, "    " + FIELDS_END);
            if (!StringUtil.isBlank(get(_lines, _nend))) {
                bout.newLine();
            }
        }

        // write the mid-amble
        for (int ii = _nend; ii < _mstart; ii++) {
            writeln(bout, _lines[ii]);
        }

        // write the method section
        if (!StringUtil.isBlank(msection)) {
            if (!StringUtil.isBlank(get(_lines, _mstart-1))) {
                bout.newLine();
            }
            writeln(bout, "    " + METHODS_START);
            bout.write(msection);
            writeln(bout, "    " + METHODS_END);
            String next = get(_lines, _mend);
            if (!StringUtil.isBlank(next) && !next.equals("}")) {
                bout.newLine();
            }
        }

        // write the postamble
        for (int ii = _mend; ii < _lines.length; ii++) {
            writeln(bout, _lines[ii]);
        }
        bout.close();
    }

    /** Helper function for sanity checking marker existence. */
    protected void check (File source, String mname, int mline, String fname, int fline)
        throws IOException
    {
        if (mline == -1 && fline != -1) {
            throw new IOException(
                "Found " + fname + " marker (at line " + (fline+1) + ") but no " + mname +
                " marker in '" + source + "'.");
        }
    }

    /** Safely gets the <code>index</code>th line, returning the empty string if we exceed the
     * length of the array. */
    protected String get (String[] lines, int index)
    {
        return (index < lines.length) ? lines[index] : "";
    }

    /** Helper function for writing a string and a newline to a writer. */
    protected void writeln (BufferedWriter bout, String line)
        throws IOException
    {
        bout.write(line);
        bout.newLine();
    }

    protected String[] _lines;

    protected int _nstart = -1, _nend = -1;
    protected int _mstart = -1, _mend = -1;

    // markers
    protected static final String MARKER = "// AUTO-GENERATED: ";
    protected static final String FIELDS_START = MARKER + "FIELDS START";
    protected static final String FIELDS_END = MARKER + "FIELDS END";
    protected static final String METHODS_START = MARKER + "METHODS START";
    protected static final String METHODS_END = MARKER + "METHODS END";
}
