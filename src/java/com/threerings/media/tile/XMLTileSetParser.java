//
// $Id: XMLTileSetParser.java,v 1.1 2001/07/17 17:21:33 shaper Exp $

package com.threerings.cocktail.miso.tile;

import com.threerings.cocktail.miso.Log;

import com.samskivert.util.StringUtil;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import javax.xml.parsers.*;
import java.util.ArrayList;

/**
 * Parse an XML tileset description file and construct tileset objects
 * for each valid description.  Does not currently perform validation
 * on the input XML stream, though the parsing code assumes the XML
 * document is well-formed.
 */
public class XMLTileSetParser extends DefaultHandler
    implements TileSetParser
{
    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
	_tag = qName;
    }

    public void endElement (String uri, String localName, String qName)
    {
	// construct the tileset object on tag close
	if (qName.equals("tileset")) {
	    TileSet tset = new TileSet(
		_tsName, _tsTsid, _tsImgFile, _tsRowHeight, _tsTileCount);
	    _tilesets.add(tset);
	}

	// note that we're not within a tag to avoid considering any
	// characters during this quiescent time
	_tag = null;
    }

    public void characters (char ch[], int start, int length)
    {
	// bail if we're not within a meaningful tag
	if (_tag == null) return;

  	String str = String.copyValueOf(ch, start, length);

	// store the value associated with the current tag for use
	// when we construct the tileset object.
	if (_tag.equals("name")) {
	    _tsName = str;

	} else if (_tag.equals("tsid")) {
	    try {
		_tsTsid = Integer.parseInt(str);
	    } catch (NumberFormatException nfe) {
		Log.warning("Malformed integer tilesetid [str=" + str + "].");
		_tsTsid = -1;
	    }

	} else if (_tag.equals("imagefile")) {
	    _tsImgFile = str;

	} else if (_tag.equals("rowheight")) {
	    _tsRowHeight = StringUtil.parseIntArray(str);

	} else if (_tag.equals("tilecount")) {
	    _tsTileCount = StringUtil.parseIntArray(str);
	}
    }

    public void loadTileSets (InputStream tis) throws IOException
    {
    	try {
	    XMLReader xr = _pfactory.newSAXParser().getXMLReader();

	    xr.setContentHandler(this);
	    xr.setErrorHandler(this);

	    xr.parse(new InputSource(tis));

	} catch (ParserConfigurationException pce) {
  	    throw new IOException(pce.toString());

	} catch (SAXException saxe) {
	    throw new IOException(saxe.toString());
	}
    }

    public ArrayList getTileSets ()
    {
	if (_tilesets.size() == 0) return null;
	return _tilesets;
    }

    protected String _tag;
    protected ArrayList _tilesets = new ArrayList();

    // temporary storage of tileset object values
    protected String _tsName;
    protected int    _tsTsid;
    protected String _tsImgFile;
    protected int[]  _tsRowHeight, _tsTileCount;

    // the slithy tove from whence came the XMLReader object
    protected static SAXParserFactory _pfactory;

    static {
	_pfactory = SAXParserFactory.newInstance();
	_pfactory.setValidating(false);
    }
}
