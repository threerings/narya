//
// $Id: ObjectStreamException.java,v 1.5 2001/10/16 16:44:20 mdb Exp $

package com.threerings.presents.io;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.util.exception.Nestable;
import org.apache.commons.util.exception.NestableDelegate;

/**
 * The object stream exception is used to communicate an error in
 * processing a typed object stream. As any error is fatal, we don't
 * differentiate exceptions with separate classes, but instead communicate
 * the nature of the error in the exception message with the assumption
 * that the caller will want to raise holy hell in every case.
 */
public class ObjectStreamException
    extends IOException implements Nestable
{
    /**
     * Constructs an object stream exception with the specified message.
     */
    public ObjectStreamException (String message)
    {
        super(message);
    }

    /**
     * Constructs an object stream exception with the specified message
     * and causing exception.
     */
    public ObjectStreamException (String message, Throwable cause)
    {
        super(message);
        _cause = cause;
    }

    // documentation inherited
    public Throwable getCause ()
    {
        return _cause;
    }

    // documentation inherited
    public String getMessage ()
    {
        StringBuffer msg = new StringBuffer();

        // include our message if we have one
        String ourMsg = super.getMessage();
        if (ourMsg != null) {
            msg.append(ourMsg);
        }

        // and append the message from our causing exception if we've got
        // one of those
        if (_cause != null) {
            String causeMsg = _cause.getMessage();
            if (causeMsg != null) {
                if (ourMsg != null) {
                    msg.append(": ");
                }
                msg.append(causeMsg);
            }
        }

        return (msg.length() > 0 ? msg.toString() : null);
    }

    /**
     * Prints the stack trace of this exception the the standar error
     * stream.
     */
    public void printStackTrace ()
    {
        _delegate.printStackTrace();
    }

    /**
     * Prints the stack trace of this exception to the specified print
     * stream.
     *
     * @param out {@link PrintStream} to use for output.
     */
    public void printStackTrace (PrintStream out)
    {
        _delegate.printStackTrace(out);
    }

    // documentation inherited
    public void printStackTrace (PrintWriter out)
    {
        _delegate.printStackTrace(out);
    }

    // documentation inherited
    public final void printPartialStackTrace (PrintWriter out)
    {
        super.printStackTrace(out);
    }

    /**
     * The helper instance which contains much of the code which we
     * delegate to.
     */
    protected NestableDelegate _delegate = new NestableDelegate(this);

    /**
     * Holds the reference to the exception or error that caused this
     * exception to be thrown.
     */
    protected Throwable _cause = null;
}
