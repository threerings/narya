//
// $Id: NestableWriter.java,v 1.1 2003/02/12 05:35:21 mdb Exp $

package com.threerings.tools.xml;

import org.xml.sax.SAXException;
import com.megginson.sax.DataWriter;

/**
 * Provides the writing component of the nestable parsing system described
 * by {@link NestableRuleSet}.
 */
public interface NestableWriter
{
    /**
     * Called to generate XML for the supplied object to the supplied data
     * writer.
     */
    public void write (Object object, DataWriter writer)
        throws SAXException;
}
