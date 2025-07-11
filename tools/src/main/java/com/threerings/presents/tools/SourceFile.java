//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.tools;

import java.util.List;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import com.google.common.collect.Lists;

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
        _lines = Lists.newArrayList();
        BufferedReader bin = new BufferedReader(new FileReader(source));
        String line = null;
        while ((line = bin.readLine()) != null) {
            _lines.add(line);
        }
        bin.close();

        // now determine where to insert our static field declarations and our generated methods
        int bstart = -1, bend = -1;
        for (int ii = 0, nn = _lines.size(); ii < nn; ii++) {
            line = _lines.get(ii).trim();

            // look for the start of the class body
            if (GenUtil.NAME_PATTERN.matcher(line).find()) {
                if (line.endsWith("{")) {
                    bstart = ii+1;
                } else {
                    // search down a few lines for the open brace
                    for (int oo = 1; oo < 10; oo++) {
                        if (safeGetLine(ii+oo).trim().endsWith("{")) {
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
        for (int ii = 0, nn = _lines.size(); ii < nn; ii++) {
            // don't look inside the autogenerated areas
            if (!(ii >= _nstart && ii < _nend) && !(ii >= _mstart && ii < _mend) &&
                    _lines.get(ii).contains(text)) {
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
    public String generate (String fsection, String msection)
        throws IOException
    {
        StringWriter writer = new StringWriter();
        BufferedWriter bout = new BufferedWriter(writer);
        addOrRemoveGeneratedImport(StringUtil.deNull(fsection).contains("@Generated(") ||
            StringUtil.deNull(msection).contains("@Generated("));

        // write the preamble
        for (int ii = 0; ii < _nstart; ii++) {
            writeln(bout, _lines.get(ii));
        }

        // write the field section
        if (!StringUtil.isBlank(fsection)) {
            String prev = safeGetLine(_nstart-1);
            if (!StringUtil.isBlank(prev) && !prev.equals("{")) {
                bout.newLine();
            }
            writeln(bout, "    " + FIELDS_START);
            bout.write(fsection);
            writeln(bout, "    " + FIELDS_END);
            if (!StringUtil.isBlank(safeGetLine(_nend))) {
                bout.newLine();
            }
        }

        // write the mid-amble
        for (int ii = _nend; ii < _mstart; ii++) {
            writeln(bout, _lines.get(ii));
        }

        // write the method section
        if (!StringUtil.isBlank(msection)) {
            if (!StringUtil.isBlank(safeGetLine(_mstart-1))) {
                bout.newLine();
            }
            writeln(bout, "    " + METHODS_START);
            bout.write(msection);
            writeln(bout, "    " + METHODS_END);
            String next = safeGetLine(_mend);
            if (!StringUtil.isBlank(next) && !next.equals("}")) {
                bout.newLine();
            }
        }

        // write the postamble
        for (int ii = _mend, nn = _lines.size(); ii < nn; ii++) {
            writeln(bout, _lines.get(ii));
        }
        bout.close();
        return writer.toString();
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
    protected String safeGetLine (int index)
    {
        return (index < _lines.size()) ? _lines.get(index) : "";
    }

    /** Helper function for writing a string and a newline to a writer. */
    protected void writeln (BufferedWriter bout, String line)
        throws IOException
    {
        bout.write(line);
        bout.newLine();
    }

    /**
     * Add or remove an import for "@Generated", if needed.
     */
    protected void addOrRemoveGeneratedImport (boolean add)
    {
        final String IMPORT = "import javax.annotation.Generated;";

        int packageLine = -1;
        int importLine = -1;
        int lastJavaImport = -1;
        int firstNonJavaImport = -1;
        for (int ii = 0, nn = _lines.size(); ii < nn; ii++) {
            String line = _lines.get(ii);
            if (line.startsWith(IMPORT)) {
                if (add) {
                    return; // we already got one!
                }
                importLine = ii;
                break;

            } else if (line.startsWith("package ")) {
                packageLine = ii;

            } else if (line.startsWith("import java")) {
                lastJavaImport = ii;

            } else if (firstNonJavaImport == -1 && line.startsWith("import ")) {
                firstNonJavaImport = ii;
            }
        }

        if (importLine != -1) {
            // we must be removing, or we'd have already exited
            _lines.remove(importLine);

        } else if (!add) {
            return; // it's already not there!

        } else {
            importLine = (lastJavaImport != -1) ? lastJavaImport + 1
                : ((firstNonJavaImport != -1) ? firstNonJavaImport : packageLine + 1);
            _lines.add(importLine, IMPORT);
        }

        // the import line is always above these other lines, so they can be adjusted wholesale
        int adjustment = add ? 1 : -1;
        _nstart += adjustment;
        _nend += adjustment;
        _mstart += adjustment;
        _mend += adjustment;
    }

    protected List<String> _lines;

    protected int _nstart = -1, _nend = -1;
    protected int _mstart = -1, _mend = -1;

    // markers
    protected static final String MARKER = "// AUTO-GENERATED: ";
    protected static final String FIELDS_START = MARKER + "FIELDS START";
    protected static final String FIELDS_END = MARKER + "FIELDS END";
    protected static final String METHODS_START = MARKER + "METHODS START";
    protected static final String METHODS_END = MARKER + "METHODS END";
}
