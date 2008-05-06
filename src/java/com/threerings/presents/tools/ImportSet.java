package com.threerings.presents.tools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.StringUtil;

/**
 * Manages a set of strings to be used as a set of imports. Provides useful functions for 
 * manipulating the set and sorts results.
 * 
 * <p>Some methods in this class use a variable length String parameter 'replace'. This is a 
 * convenience for easily specifying multiple find/replace pairs. For example, to replace "Foo" 
 * with "Bar" and "123" with "ABC", a function can be called with the 4 arguments "Foo", "Bar", 
 * "123", "ABC" for the String... replace argument.
 */
public class ImportSet
{
    /**
     * Adds the given class' name to the set of imports.
     * @param clazz the class to add
     */
    public void add (Class clazz)
    {
        _imports.add(clazz.getName());
    }

    /**
     * Adds the given name to the set of imports.
     * @param name the name to add
     */
    public void add (String name)
    {
        _imports.add(name);
    }

    /**
     * Adds all the imports from another import set into this one.
     * @param other the import set whose imports should be added
     */
    public void addAll (ImportSet other)
    {
        _imports.addAll(other._imports);
    }
    
    /**
     * Adds a class' name to the imports but first performs the given list of search/replaces as 
     * described above.
     * @param clazz the class whose name is munged and added
     * @param replace array of pairs to search/replace on the name before adding
     */
    public void addMunged (Class<?> clazz, String... replace)
    {
        String name = clazz.getName();
        for (int i = 0 ; i < replace.length; i+=2) {
            name = name.replace(replace[i], replace[i + 1]);
        }
        _imports.add(name);
    }

    /**
     * Makes a copy of this import set. 
     */ 
    public ImportSet clone ()
    {
        ImportSet newset = new ImportSet();
        newset.addAll(this);
        return newset;
    }
    
    /**
     * Gets rid of primitive and java.lang imports.
     */
    public void removeGlobals ()
    {
        Iterator<String> i = _imports.iterator();
        while (i.hasNext()) {
            String name = i.next();
            if (name.indexOf('.') == -1) {
                i.remove();
            }
            else if (name.startsWith("java.lang")) {
                i.remove();
            }
        }
    }
    
    /**
     * Replaces inner class imports (those with a '$') with an import of the parent class.
     */
    public void swapInnerClassesForParents ()
    {
        ImportSet declarers = new ImportSet();
        Iterator<String> i = _imports.iterator();
        while (i.hasNext()) {
            String name = i.next();
            int dollar = name.indexOf('$');
            if (dollar >= 0) {
                i.remove();
                declarers.add(name.substring(0, dollar));
            }
        }
        
        addAll(declarers);
    }

    /**
     * Replace all inner classes' separator characters ('$') with an underscore ('_') for use 
     * when generating ActionScript.
     */
    public void translateInnerClasses ()
    {
        ImportSet inner = new ImportSet();
        Iterator<String> i = _imports.iterator();
        while (i.hasNext()) {
            String name = i.next();
            int dollar = name.indexOf('$');
            if (dollar >= 0) {
                i.remove();
                inner.add(name.replace("$", "_"));
            }
        }
        
        addAll(inner);
    }

    /**
     * Removes the name of a class from the imports.
     * @param clazz the class whose name should be removed
     */
    public void remove (Class<?> clazz)
    {
        _imports.remove(clazz.getName());
    }
    
    /**
     * Replaces any import exactly matching the 0th argument with the 1st argument and so on. 
     * See the description above.
     * @param replace array of pairs for search/replace
     */
    public void replace (String... replace)
    {
        HashSet<String> toAdd = new HashSet<String>();
        Iterator<String> i = _imports.iterator();
        while (i.hasNext()) {
            String name = i.next();
            for (int j = 0; j < replace.length; j += 2) {
                if (name.equals(replace[j])) {
                    toAdd.add(replace[j + 1]);
                    i.remove();
                    break;
                }
            }
        }
        _imports.addAll(toAdd);
    }
    
    /**
     * Adds a new munged import for each existing import that matches a suffix. The new entry is 
     * a copy of the old entry but modified according to the "replace" pattern described above.
     * @param suffix to filter the search for imports to duplicate 
     * @param replace array of string pairs to search/replace on the duplicated import
     */
    public void duplicateAndMunge (String suffix, String... replace)
    {
        HashSet<String> toMunge = new HashSet<String>();
        for (String name : _imports) {
            if (name.endsWith(suffix)) {
                toMunge.add(name);
            }
        }
        for (String name : toMunge) {
            String newname = name;
            for (int i = 0; i < replace.length; i+=2) {
                newname = newname.replace(replace[i], replace[i + 1]);
            }
            _imports.add(newname);
        }
    }

    /**
     * Convert the set of imports to a sorted list, ready to be output to a generated file.
     * @return the sorted list of imports
     */
    public List<String> toList ()
    {
        ComparableArrayList<String> list = new ComparableArrayList<String>();
        list.addAll(_imports);
        list.sort();
        return list;
    }

    @Override // from Object
    public String toString ()
    {
        return StringUtil.toString(_imports);
    }

    protected HashSet<String> _imports = new HashSet<String>();
}
