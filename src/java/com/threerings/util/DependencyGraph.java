package com.threerings.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Maintains a bidirectional graph to manage the order that the items are removed.  Children
 *  must wait until their parents are accessed - thus removing an available element means that
 *  a node without parents (an orphan) is removed and returned and the rest of the graph is
 *  updated to reflect that removal.
 * @param <T>
 */
public class DependencyGraph<T>
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
