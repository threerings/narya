//
// $Id: DSet.java,v 1.14 2002/02/08 04:44:32 mdb Exp $

package com.threerings.presents.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.io.Streamable;

/**
 * The distributed set class provides a means by which an unordered set of
 * objects can be maintained as a distributed object field. Elements can
 * be added to and removed from the set, requests for which will generate
 * events much like other distributed object fields.
 *
 * <p> A set can be either homogenous, whereby the type of object to be
 * contained in the set is configured before the set is used and does not
 * change; or heterogenous, whereby a set can contain any type of element
 * as long as it implements {@link Element}. Homogenous sets take
 * advantage of their homogeneity by not transfering the classname of each
 * element as it is sent over the wire.
 *
 * <p> Classes that wish to act as set elements must implement the {@link
 * com.threerings.presents.dobj.DSet.Element} interface which extends
 * {@link Streamable} and adds the requirement that the object provide a
 * key which will be used to identify element equality. Thus an element is
 * declared to be in a set of the object returned by that element's
 * <code>geyKey()</code> method is equal (using <code>equal()</code>) to
 * the element returned by the <code>getKey()</code> method of some other
 * element in the set.  Additionally, in the case of element removal, only
 * the key for the element to be removed will be transmitted with the
 * removal event to save network bandwidth. Lastly, the object returned by
 * <code>getKey()</code> must be a valid distributed object type.
 */
public class DSet
    implements Streamable, Cloneable
{
    /**
     * Elements of the set must implement this interface.
     */
    public static interface Element extends Streamable
    {
        /**
         * Each element provide an associated key which is used to
         * determine its uniqueness in the set. See the {@link DSet} class
         * documentation for further information.
         */
        public Object getKey ();
    }

    /**
     * Constructs a distributed set that will contain the specified
     * element type.
     */
    public DSet (Class elementType)
    {
        setElementType(elementType);
    }

    /**
     * Creates a distributed set and populates it with values from the
     * supplied iterator. This should be done before the set is unleashed
     * into the wild distributed object world because no associated
     * element added events will be generated. Additionally, this
     * operation does not check for duplicates when adding elements, so
     * one should be sure that the iterator contains only unique elements.
     *
     * @param elementType the type of elements that will be stored in this
     * set. <em>Only</em> elements of this <em>exact</em> type may be
     * stored in the set.
     * @param source an iterator from which we will initially populate the
     * set.
     */
    public DSet (Class elementType, Iterator source)
    {
        this(source);
        setElementType(elementType);
    }

    /**
     * Creates a distributed set and populates it with values from the
     * supplied iterator. This should be done before the set is unleashed
     * into the wild distributed object world because no associated
     * element added events will be generated. Additionally, this
     * operation does not check for duplicates when adding elements, so
     * one should be sure that the iterator contains only unique elements.
     *
     * @param source an iterator from which we will initially populate the
     * set.
     */
    public DSet (Iterator source)
    {
        for (int index = 0; source.hasNext(); index++) {
            Element elem = (Element)source.next();

            // expand the array if necessary
            if (index >= _elements.length) {
                expand(index);
            }

            // insert the item
            _elements[index] = elem;
            _size++;
        }
    }

    /**
     * Constructs a distributed set without specifying the element
     * type. The set will assume that it is heterogenous, unless a
     * homogenous class type is otherwise specified via {@link
     * #setElementType}.
     */
    public DSet ()
    {
    }

    /**
     * Returns true if this set contains only elements of exactly the same
     * type, false if not.
     */
    public boolean homogenous ()
    {
        return _elementType != null;
    }

    /**
     * Indicates what type of elements will be stored in this set. This
     * can be called multiple times before the set is used (in the event
     * that one wishes to further specialize the contents of a set that
     * has already been configured to use a particular element type), but
     * once the set goes into use, it must not be changed. Also bear in
     * mind that the class of elements added to the set are not checked at
     * runtime, and adding elements of invalid class will simply result in
     * the serialization mechanism failing when an event is dispatched to
     * broadcast the addition of an element.
     */
    public void setElementType (Class elementType)
    {
        _elementType = elementType;
    }

    /**
     * Returns the number of elements in this set.
     */
    public int size ()
    {
        return _size;
    }

    /**
     * Returns true if the set contains an element whose
     * <code>getKey()</code> method returns a key that
     * <code>equals()</code> the key returned by <code>getKey()</code> of
     * the supplied element. Returns false otherwise.
     */
    public boolean contains (Element elem)
    {
        return containsKey(elem.getKey());
    }

    /**
     * Returns true if an element in the set has a key that
     * <code>equals()</code> the supplied key. Returns false otherwise.
     */
    public boolean containsKey (Object key)
    {
        return get(key) != null;
    }

    /**
     * Returns the element that matches
     * (<code>getKey().equals(key)</code>) the specified key or null if no
     * element could be found that matches the key.
     */
    public Element get (Object key)
    {
        // scan the array looking for a matching element
        int elength = _elements.length;
        for (int i = 0; i < elength; i++) {
            // the array may be sparse
            if (_elements[i] != null) {
                Element elem = _elements[i];
                if (elem.getKey().equals(key)) {
                    return elem;
                }
            }
        }
        return null;
    }

    /**
     * Returns an iterator over the elements of this set. It does not
     * support modification (nor iteration while modifications are being
     * made to the set). It should not be kept around as it can quickly
     * become out of date.
     */
    public Iterator elements ()
    {
        return new Iterator() {
            public boolean hasNext ()
            {
                // we need to scan to the next element the first time
                if (_index < 0) {
                    scanToNext();
                }
                return (_index < _elements.length);
            }

            public Object next ()
            {
                Object val = _elements[_index];
                scanToNext();
                return val;
            }

            public void remove ()
            {
                throw new UnsupportedOperationException();
            }

            protected void scanToNext ()
            {
                for (_index++; _index < _elements.length; _index++) {
                    if (_elements[_index] != null) {
                        return;
                    }
                }
            }

            int _index = -1;
        };
    }

    /**
     * Adds the specified element to the set. This should not be called
     * directly, instead the associated <code>addTo{Set}()</code> method
     * should be called on the distributed object that contains the set in
     * question.
     *
     * @return true if the element was added, false if it was already in
     * the set.
     */
    protected boolean add (Element elem)
    {
        Object key = elem.getKey();
        int elength = _elements.length;
        int index = elength;

        // scan the array looking for a slot and/or the element already in
        // the set
        for (int i = 0; i < elength; i++) {
            Element el = _elements[i];
            // the array may be sparse
            if (el == null) {
                if (index == elength) {
                    index = i;
                }
            } else if (el.getKey().equals(key)) {
                return false;
            }
        }

        // expand the array if necessary
        if (index >= _elements.length) {
            expand(index);
        }

        // insert the item
        _elements[index] = elem;
        _size++;
        return true;
    }

    /**
     * Removes the specified element from the set. This should not be
     * called directly, instead the associated
     * <code>removeFrom{Set}()</code> method should be called on the
     * distributed object that contains the set in question.
     *
     * @return true if the element was removed, false if it was not in the
     * set.
     */
    protected boolean remove (Element elem)
    {
        return removeKey(elem.getKey());
    }

    /**
     * Removes from the set the element whose key matches the supplied
     * key. This should not be called directly, instead the associated
     * <code>removeFrom{Set}()</code> method should be called on the
     * distributed object that contains the set in question.
     *
     * @return true if a matching element was removed, false if no element
     * in the set matched the key.
     */
    protected boolean removeKey (Object key)
    {
        // scan the array looking for a matching element
        int elength = _elements.length;
        for (int i = 0; i < elength; i++) {
            Element el = _elements[i];
            if (el != null && el.getKey().equals(key)) {
                _elements[i] = null;
                _size--;
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the specified element by locating an element whose key
     * matches the key of the supplied element and overwriting it. This
     * should not be called directly, instead the associated
     * <code>update{Set}()</code> method should be called on the
     * distributed object that contains the set in question.
     *
     * @return true if the element was updated, false if it was not
     * already in the set (in which case nothing is updated).
     */
    protected boolean update (Element elem)
    {
        Object key = elem.getKey();

        // scan the array looking for a matching element
        int elength = _elements.length;
        for (int i = 0; i < elength; i++) {
            Element el = _elements[i];
            if (el != null && el.getKey().equals(key)) {
                _elements[i] = elem;
                return true;
            }
        }

        return false;
    }

    /**
     * Serializes this instance to the supplied output stream.
     */
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        if (_elementType == null) {
            out.writeUTF("");
        } else {
            out.writeUTF(_elementType.getName());
        }
        out.writeInt(_size);
        int elength = _elements.length;
        for (int i = 0; i < elength; i++) {
            Element elem = _elements[i];
            if (elem != null) {
                elem.writeTo(out);
            }
        }
    }

    /**
     * Unserializes this instance from the supplied input stream.
     */
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // read our element class and forName() it (if we read an element
        // class, we're a homogenous set; otherwise we're heterogenous)
        String eclass = in.readUTF();
        try {
            if (!StringUtil.blank(eclass)) {
                _elementType = Class.forName(eclass);
            }

        } catch (Exception e) {
            String err = "Unable to instantiate element class [err=" + e + "]";
            throw new IOException(err);
        }

        // find out how many elements we'll be reading
        _size = in.readInt();

        // make sure we can fit _size elements
        expand(_size);

        for (int i = 0; i < _size; i++) {
            _elements[i] = readElement(in);
        }
    }

    /**
     * Reads an element from the wire and unserializes it. Takes into
     * account whether or not we're a homogenous set.
     */
    public Element readElement (DataInputStream in)
        throws IOException
    {
        try {
            Element elem = null;

            // instantiate the appropriate element instance
            if (_elementType != null) {
                elem = (Element)_elementType.newInstance();
            } else {
                elem = (Element)Class.forName(in.readUTF()).newInstance();
            }

            // unserialize it and return it
            elem.readFrom(in);
            return elem;

        } catch (Exception e) {
            Log.warning("Unable to unserialize set element " +
                        "[set=" + this + "].");
            Log.logStackTrace(e);
            return null;
        }
   }

    /**
     * Generates a shallow copy of this object.
     */
    public Object clone ()
    {
        DSet nset = new DSet(_elementType);
        nset._elements = new Element[_elements.length];
        System.arraycopy(_elements, 0, nset._elements, 0, _elements.length);
        nset._size = _size;
        return nset;
    }

    /**
     * Generates a string representation of this set instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        if (_elementType == null) {
            buf.append("etype=NONE");
        } else {
            buf.append("etype=").append(_elementType.getName());
        }
        buf.append(", elems=(");
        String prefix = "";
        for (int i = 0; i < _elements.length; i++) {
            Element elem = _elements[i];
            if (elem != null) {
                buf.append(prefix);
                prefix = ", ";
                buf.append(elem);
            }
        }
        buf.append(")]");
        return buf.toString();
    }

    protected void expand (int index)
    {
        // sanity check
        if (index < 0) {
            Log.warning("Requested to expand to accomodate bogus index! " +
                        "[index=" + index + "].");
            Thread.dumpStack();
            index = 0;
        }

        // increase our length in powers of two until we're big enough
        int tlength = _elements.length;
        while (index >= tlength) {
            tlength *= 2;
        }

        // further sanity checks
        if (tlength > 4096) {
            Log.warning("Requested to expand to questionably large size " +
                        "[index=" + index + ", tlength=" + tlength + "].");
            Thread.dumpStack();
        }

        // create a new array and copy our data into it
        Element[] elems = new Element[tlength];
        System.arraycopy(_elements, 0, elems, 0, _elements.length);
        _elements = elems;
    }

    /** The type of element this set holds. */
    protected Class _elementType;

    /** The elements of the set (in a sparse array). */
    protected Element[] _elements = new Element[INITIAL_CAPACITY];

    /** The number of elements in this set. */
    protected int _size;

    /** The default capacity of a set instance. */
    protected static final int INITIAL_CAPACITY = 2;
}
