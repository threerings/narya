//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.bureau.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.samskivert.io.StreamUtil;
import com.samskivert.util.Logger;

import static com.threerings.bureau.Log.log;

/**
 * Captures the output of a bureau and redirects it into a single logger instance using a thread
 * name equal to the bureau id. The {@link Logger} instance is the one for this class. The intent
 * is that log4j will be configured to use %t (thread name) to embed the bureau id.
 */
public class BureauLogRedirector
{
    /**
     * Creates a new redirector.
     * @param bureauId the id of the bureau being redirected - this will become the thread name
     * @param input the stream that is the output of the bureau process
     */
    public BureauLogRedirector (String bureauId, InputStream input)
    {
        _bureauId = bureauId;
        _reader = new BufferedReader(new InputStreamReader(input));
        Thread thread = new Thread(bureauId) {
            @Override public void run () {
                copyLoop();
            }};
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Gets the bureau id this was created with.
     */
    public String getBureauId ()
    {
        return _bureauId;
    }

    /**
     * Returns true if the redirector is still active. Normally this indicates that the launched
     * process is still running.
     */
    public boolean isRunning ()
    {
        return _reader != null;
    }

    protected void copyLoop ()
    {
        String line;
        try {
            while ((line = _reader.readLine()) != null) {
                // this should get prefixed by the thread name
                _target.info(line);
            }
        } catch (Exception e) {
            log.warning("Failed to read bureau output", "bureauId", _bureauId, e);
        } finally {
            StreamUtil.close(_reader);
            _reader = null;
        }
    }

    protected String _bureauId;
    protected BufferedReader _reader;

    protected static Logger _target = Logger.getLogger(BureauLogRedirector.class);
}
