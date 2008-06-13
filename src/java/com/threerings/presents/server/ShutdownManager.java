//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.presents.server;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.ObserverList;
import com.samskivert.util.RunQueue;

import com.threerings.presents.annotation.EventQueue;

import static com.threerings.presents.Log.log;

/**
 * Handles the orderly shutdown of all server services.
 */
@Singleton
public class ShutdownManager
{
    /** Implementers of this interface will be notified when the server is shutting down. */
    public static interface Shutdowner
    {
        /**
         * Called when the server is shutting down.
         */
        public void shutdown ();
    }

    public static enum Constraint { RUNS_BEFORE, RUNS_AFTER };

    @Inject ShutdownManager (@EventQueue RunQueue dobjq)
    {
        _dobjq = dobjq;
    }

    /**
     * Registers an entity that will be notified when the server is shutting down.
     */
    public void registerShutdowner (Shutdowner downer)
    {
        _downers.add(downer);
    }

    /**
     * Unregisters the shutdowner from hearing when the server is shutdown.
     */
    public void unregisterShutdowner (Shutdowner downer)
    {
        _downers.remove(downer);
    }

    /**
     * Adds a constraint that a certain shutdowner must be run before another.
     */
    public void addConstraint (Shutdowner lhs, Constraint constraint, Shutdowner rhs)
        throws IllegalArgumentException
    {
        if (lhs == null || rhs == null) {
            throw new IllegalArgumentException("Cannot add constraint about null shutdowner.");
        }
        Shutdowner before = (constraint == Constraint.RUNS_BEFORE) ? lhs : rhs;
        Shutdowner after = (constraint == Constraint.RUNS_BEFORE) ? rhs : lhs;
        _downers.addDependency(after, before);
    }

    /**
     * Queues up a request to shutdown on the dobjmgr thread. This method may be safely called from
     * any thread.
     */
    public void queueShutdown ()
    {
        _dobjq.postRunnable(new Runnable() {
            public void run () {
                shutdown();
            }
        });
    }

    /**
     * Shuts down all shutdowners immediately on the caller's thread.
     */
    public void shutdown ()
    {
        if (_downers == null) {
            log.warning("Refusing repeat shutdown request.");
            return;
        }

        ObserverList<Shutdowner> downers = ObserverList.newSafeInOrder();

        while (!_downers.isEmpty()) {
            downers.add(_downers.removeAvailableElement());
        }

        _downers = null;

        // shut down all shutdown participants
        downers.apply(new ObserverList.ObserverOp<Shutdowner>() {
            public boolean apply (Shutdowner downer) {
                downer.shutdown();
                return true;
            }
        });
    }

    /**
     * We maintain a bidirectional graph to manage the order that the items are removed.  Children
     *  must wait until their parents are accessed - thus removing an available element means that
     *  a node without parents (an orphan) is removed and returned.
     * @param <T>
     */
    protected class DependencyGraph<T>
    {
        /**
         * Adds an element with no initial dependencies from the graph.
         */
        public void add (T element)
        {
            DependencyNode<T> node = new DependencyNode<T>(element);
            _nodes.put(element, node);
            _orphans.add(element);
        }

        /**
         * Removes an element and its dependencies from the graph.
         */
        public void remove (T element)
        {
            DependencyNode<T> node = _nodes.remove(element);
            _orphans.remove(element);

            // Remove ourselves as a child of our parents.
            for (DependencyNode<T> parent : node.parents) {
                parent.children.remove(node);
            }

            // Remove ourselves as a parent of our children, possibly orphaning them.
            for (DependencyNode<T> child : node.children) {
                child.parents.remove(node);
                if (child.parents.isEmpty()) {
                    _orphans.add(child.content);
                }
            }
        }

        /**
         * Removes and returns an element which is available, meaning not dependent upon any other
         *  still in the graph.
         */
        public T removeAvailableElement ()
        {
            T elem = _orphans.get(0);
            DependencyNode<T> node = _nodes.get(elem);
            remove(elem);

            return elem;
        }

        /**
         * Returns the number of elements in the graph.
         */
        public int size ()
        {
            return _nodes.size();
        }

        /**
         * Returns whether there are no more elements in the graph.
         */
        public boolean isEmpty ()
        {
            return size() == 0;
        }

        /**
         * Records a new dependency of the dependant upon the dependee.
         */
        public void addDependency (T dependant, T dependee)
            throws IllegalArgumentException
        {
            _orphans.remove(dependant);
            DependencyNode<T> dependantNode = _nodes.get(dependant);
            DependencyNode<T> dependeeNode = _nodes.get(dependee);

            if (dependsOn(dependee, dependant)) {
                throw new IllegalArgumentException("Refusing to create circular dependency.");
            }
            dependantNode.parents.add(dependeeNode);
            dependeeNode.children.add(dependantNode);

        }

        /**
         * Returns whether elem1 is designated to depend on elem2.
         */
        public boolean dependsOn (T elem1, T elem2)
        {
            DependencyNode<T> node1 = _nodes.get(elem1);
            DependencyNode<T> node2 = _nodes.get(elem2);

            ArrayList<DependencyNode<T>> nodesToCheck = new ArrayList<DependencyNode<T>>();
            ArrayList<DependencyNode<T>> nodesAlreadyChecked = new ArrayList<DependencyNode<T>>();
            nodesToCheck.addAll(node1.parents);

            // We prevent circular dependencies when we add dependencies.  Otherwise, this'd be
            //  potentially non-terminating.
            while (!nodesToCheck.isEmpty()) {
                // We take it off the end since we don't care about order and this is faster.
                DependencyNode<T> checkNode = nodesToCheck.remove(nodesToCheck.size() - 1);

                if (nodesAlreadyChecked.contains(checkNode)) {
                    // We've seen him before, no need to check again.
                    continue;

                } else if (checkNode == node2) {
                    // We've found our dependency
                    return true;

                } else  {
                    nodesAlreadyChecked.add(checkNode);
                    nodesToCheck.addAll(checkNode.parents);
                }
            }

            return false;
        }

        /** All the nodes included in the graph. */
        protected HashMap<T, DependencyNode<T>> _nodes = new HashMap<T, DependencyNode<T>>();

        /** Nodes in the graph with no parents/dependencies. */
        protected ArrayList<T> _orphans = new ArrayList<T>();

        protected class DependencyNode<T>
        {
            public T content;
            public ArrayList<DependencyNode<T>> parents;
            public ArrayList<DependencyNode<T>> children;

            public DependencyNode (T contents)
            {
                this.content = contents;
                this.parents = new ArrayList<DependencyNode<T>>();
                this.children = new ArrayList<DependencyNode<T>>();
            }
        }
    }

    /** All of the registered shutdowners along with related constraints. */
    protected DependencyGraph<Shutdowner> _downers = new DependencyGraph<Shutdowner>();

    /** The queue we'll use to get onto the dobjmgr thread before shutting down. */
    protected RunQueue _dobjq;

}
