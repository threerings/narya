//
// $Id: DSet.java,v 1.1 2001/08/16 03:31:09 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.io.Streamable;

/**
 * The distributed set class provides a means by which an unordered set of
 * objects can be maintained as a distributed object field. Elements can
 * be added to and removed from the set, requests for which will generate
 * events much like other distributed object fields.
 *
 * <p> The sets must contain a homogenous set of objects (all of exactly
 * the same type) and the master copy of the set (the one in the master
 * copy of the distributed object, ie. the server copy), must be
 * configured with the class object of the type of object that this set
 * will contain. This allows the set to distributed updates without
 * communicating the class of the object contained therein. That need only
 * be communicated once when the entire containing distributed object is
 * sent over the wire.
 *
 * <p> Classes that wish to act as set elements must implement the {@link
 * com.threerings.cocktail.cher.dobj.DSet.Element} interface which extends
 * {@link com.threerings.cocktail.cher.io.Streamable} and adds the
 * requirement that the object provide a key which will be used to
 * identify element equality. Thus an element is declared to be in a set
 * of the object returned by that element's <code>geyKey()</code> method
 * is equal (using <code>equal()</code>) to the element returned by the
 * <code>getKey()</code> method of some other element in the set.
 * Additionally, in the case of element removal, only the key for the
 * element to be removed will be transmitted with the removal event to
 * save network bandwidth. Lastly, the object returned by
 * <code>getKey()</code> must be a valid distributed object type.
 */
public class DSet
{
    /**
     * Elements of the set must implement this interface.
     */
    public static interface Element extends Streamable
    {
        /**
         * Each element provide an associated key which is used to
         * determine its uniqueness in the set. See the <code>DSet</code>
         * class documentation for further information.
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
     * Constructs a distributed set without specifying the element
     * type. This is valid if the distributed set will soon be
     * unserialized or if one plans to set the element type by hand before
     * using the set.
     */
    public DSet ()
    {
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
     * Instantiates a blank element of the type managed by this set. This
     * is used during the process of unserializing elements from the
     * network.
     */
    public DSet.Element newElement ()
    {
        try {
            return (Element)_elementType.newInstance();
        } catch (Exception e) {
            Log.warning("Unable to instantiate element! We're hosed! " +
                        "[eclass=" + _elementType + "].");
            return null;
        }
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
        if (index >= elength) {
            Element[] elems = new Element[elength*2];
            System.arraycopy(_elements, 0, elems, 0, elength);
            _elements = elems;
        }

        // insert the item
        _elements[index] = elem;
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
                return true;
            }
        }
        return false;
    }

    protected Class _elementType;
    protected Element[] _elements;
}
