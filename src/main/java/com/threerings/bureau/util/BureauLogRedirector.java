//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.bureau.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;

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
     * Creates a new redirector with no size limit.
     * @param bureauId the id of the bureau being redirected - this will become the thread name
     * @param input the stream that is the output of the bureau process
     */
    public BureauLogRedirector (String bureauId, InputStream input)
    {
        this(bureauId, input, 0);
    }

    /**
     * Creates a new redirector.
     * @param bureauId the id of the bureau being redirected - this will become the thread name
     * @param input the stream that is the output of the bureau process
     * @param limit approximate limit for the total characters written to the logger
     */
    public BureauLogRedirector (String bureauId, InputStream input, int limit)
    {
        _bureauId = bureauId;
        _reader = new BufferedReader(new InputStreamReader(input));
        _limit = limit;
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
     * Gets the total number of characters written to the log.
     */
    public int getWritten ()
    {
        return _written;
    }

    /**
     * Gets the character limit associated with the log.
     */
    public int getLimit ()
    {
        return _limit;
    }

    /**
     * Resets the redirector's truncation status and allows additional output up to the given
     * character limit.
     */
    public synchronized void reset (int limit)
    {
        _written = 0;
        _truncated = false;
        _limit = limit;
    }

    /**
     * Tests if this redirector has stopped copying lines due to the size limit being exceeded.
     */
    public boolean isTruncated ()
    {
        return _truncated;
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
                int length = line.length();
                boolean showTrunc = false;

                synchronized (this) {
                    if (_truncated) {
                        line = null;
                    } else if (_limit > 0 && _written + length > _limit) {
                        _truncated = true;
                        showTrunc = true;
                        line = null;
                    }
                }

                if (line != null) {
                    _target.info(line); // this should get prefixed by the thread name
                    _written += length;

                } else if (showTrunc) {
                    DateFormat format =
                        DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
                    _target.info(
                        format.format(new Date()) +
                            ": Size limit reached, suppressing further output");
                }
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
    protected int _limit;
    protected int _written;
    protected boolean _truncated;

    protected static Logger _target = Logger.getLogger(BureauLogRedirector.class);
}
