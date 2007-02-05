//
// $Id$

package com.threerings.presents.peer.data;

import com.samskivert.util.ObjectUtil;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.presents.dobj.DSet;

/**
 * Describes a lock held by one of the peers.
 */
public class Lock extends SimpleStreamableObject
    implements DSet.Entry
{
    /**
     * Identifies a locked resource by its type and type-specific identifier.
     */
    public static class Name extends SimpleStreamableObject
        implements Comparable
    {
        /**
         * Creates a new lock name.
         *
         * @param type the type of resource.  Only names with the same type will have their ids
         * compared
         * @param id the resource instance identifier.  Can be <code>null</code> for resources with
         * only one instance
         */
        public Name (String type, Comparable id)
        {
            _type = type;
            _id = id;
        }
        
        public Name ()
        {
        }
        
        /**
         * Returns the resource type.
         */
        public String getType ()
        {
            return _type;
        }
        
        /**
         * Returns the resource instance identifier.
         */
        public Comparable getId ()
        {
            return _id;
        }
        
        // documentation inherited from interface Comparable
        public int compareTo (Object other)
        {
            Name oname = (Name)other;
            int v1 = _type.compareTo(oname._type);
            if (v1 != 0 || _id == null) {
                return v1;
            }
            @SuppressWarnings("unchecked") int v2 = _id.compareTo(oname._id);
            return v2;
        }
        
        @Override // documentation inherited
        public int hashCode ()
        {
            return _type.hashCode() + (_id == null ? 0 : _id.hashCode());
        }
        
        @Override // documentation inherited
        public boolean equals (Object other)
        {
            Name oname = (Name)other;
            return _type.equals(oname._type) &&
                ObjectUtil.equals(_id, oname._id);
        }
        
        protected String _type;
        protected Comparable _id;
    }
    
    /**
     * Creates a newly acquired lock.
     */
    public Lock (Name name, String owner)
    {
        _name = name;
        _owner = owner;
    }
    
    /**
     * No-arg constructor for deserialization.
     */
    public Lock ()
    {
    }
    
    /**
     * Returns the name of the locked resource.
     */
    public Name getName ()
    {
        return _name;
    }
    
    /**
     * Returns the owner of the lock.
     */
    public String getOwner ()
    {
        return _owner;
    }
    
    // documentation inherited from interface DSet.Entry
    public Comparable getKey ()
    {
        return _name;
    }
    
    @Override // documentation inherited
    public boolean equals (Object other)
    {
        Lock olock = (Lock)other;
        return olock._name.equals(_name) &&
            olock._owner.equals(_owner);
    }
    
    /** Identifies what is locked. */
    protected Name _name;
    
    /** The name of the node that owns the lock. */
    protected String _owner;
}
