//
// $Id: ObjectStreamException.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.io;

/**
 * The object stream exception is used to communicate an error in
 * processing a typed object stream. As any error is fatal, we don't
 * differentiate exceptions with separate classes, but instead communicate
 * the nature of the error in the exception message with the assumption
 * that the caller will want to raise holy hell in every case.
 */
public class ObjectStreamException extends Exception
{
    public ObjectStreamException (String message)
    {
        super(message);
    }
}
