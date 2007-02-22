//
// $Id$

package com.threerings.presents.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.samskivert.util.StringUtil;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;

/**
 * Generates our own ResourceBundle classes.
 */
public class GenActionScriptBundlesTask extends Task
{
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    public void setAsroot (File asroot)
    {
        _asroot = asroot;
    }

    public void execute ()
        throws BuildException
    {
        // boilerplate
        for (FileSet fs : _filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (int f = 0; f < srcFiles.length; f++) {
                try {
                    processBundle(new File(fromDir, srcFiles[f]));
                } catch (IOException ioe) {
                    throw new BuildException(ioe);
                }
            }
        }
    }

    protected void processBundle (File source)
        throws IOException
    {
        Properties props = new Properties();
        props.load(new FileInputStream(source));

        String name = source.getName();
        name = name.replace('.', '_');
        File outfile = new File(_asroot, name + ".as");
        PrintWriter out = new PrintWriter(outfile);

        out.println("package {");
        out.println();
        out.println("import com.threerings.util.ResourceBundle;");
        out.println();
        out.println("// Generated at " + new Date());
        out.println("public class " + name + " extends ResourceBundle");
        out.println("{");
        out.println("    override protected function getContent () :Object");
        out.println("    {");
        if (true) {
            // create an array with all the values, then populate in a loop
            out.println("        var data :Array = [");
            for (Map.Entry entry : props.entrySet()) {
                String key = saveConvert((String) entry.getKey());
                String val = saveConvert((String) entry.getValue());
                out.println("            \"" + key + "\", \"" + val + "\",");
            }
            out.println("            null];");
            out.println("        var o :Object = new Object();");
            out.println("        for (var ii :int = 0; ii < data.length; ii += 2) {");
            out.println("            o[data[ii]] = data[ii + 1];");
            out.println("        }");

        } else {
            // alternate impl: just set each value directly. For non-trivial
            // resource bundles, this generates a larger class after compilation
            out.println("        var o :Object = new Object();");
            for (Map.Entry entry : props.entrySet()) {
                String key = saveConvert((String) entry.getKey());
                String val = saveConvert((String) entry.getValue());
                out.println("        o[\"" + key + "\"] = \"" + val + "\";");
            }
        }
        out.println("        return o;");
        out.println("   }");
        out.println("}}");
        out.close();
    }

    /**
     * Convert a string to be safe to output inside a string constant.
     */
    protected String saveConvert (String str)
    {
        int len = str.length();
        StringBuilder buf = new StringBuilder(len * 2);

        for (int ii = 0; ii < len; ii++) {
            char ch = str.charAt(ii);
            switch (ch) {
            case '\\':
            case '"':
                buf.append('\\').append(ch);
                break;

            case '\t':
                buf.append('\\').append('t');
                break;

            case '\n':
                buf.append('\\').append('n');
                break;

            case '\r':
                buf.append('\\').append('r');
                break;

            case '\f':
                buf.append('\\').append('f');
                break;

            default:
                buf.append(ch);
                break;
            }
        }
        return buf.toString();
    }

    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();

    protected File _asroot;
}
