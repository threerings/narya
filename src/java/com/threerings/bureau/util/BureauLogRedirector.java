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
            public void run () {
                copyLoop();
            }};
        thread.setDaemon(true);
        thread.start();
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
        }
    }

    protected String _bureauId;
    protected BufferedReader _reader;

    protected static Logger _target = Logger.getLogger(BureauLogRedirector.class);
}
