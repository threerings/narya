//
// $Id: XMLUtil.java,v 1.1 2001/07/20 08:08:59 shaper Exp $

package com.threerings.miso.util;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import javax.xml.parsers.*;

/**
 * The XMLUtil class provides a simplified interface for XML parsing.
 *
 * <p> Classes wishing to parse XML data need only extend the
 * <code>org.xml.sax.helpers.DefaultHandler</code> class, override the
 * desired methods, and then call <code>XMLUtil.parse()</code>.
 */
public class XMLUtil
{
    /**
     * Parse the XML data in the given input stream, using the
     * specified handler object as both the content and error handler.
     */
    public static void parse (DefaultHandler handler, InputStream in)
	throws IOException, ParserConfigurationException, SAXException
    {
	XMLReader xr = _pfactory.newSAXParser().getXMLReader();

	xr.setContentHandler(handler);
	xr.setErrorHandler(handler);

	xr.parse(new InputSource(in));
    }

    // the slithy tove from whence came the XMLReader object
    protected static SAXParserFactory _pfactory;

    static {
	_pfactory = SAXParserFactory.newInstance();
	_pfactory.setValidating(false);
    }
}
