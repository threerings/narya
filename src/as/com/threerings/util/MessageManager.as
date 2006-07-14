//
// $Id: MessageManager.java 3749 2005-11-09 04:00:16Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.util {

import mx.managers.ISystemManager;

import mx.resources.Locale;
import mx.resources.ResourceBundle;

/**
 * The message manager provides a thin wrapper around Java's built-in
 * localization support, supporting a policy of dividing up localization
 * resources into logical units, all of the translations for which are
 * contained in a single messages file.
 *
 * <p> The message manager assumes that the locale remains constant for
 * the duration of its operation. If the locale were to change during the
 * operation of the client, a call to {@link #setLocale} should be made to
 * inform the message manager of the new locale (which will clear the
 * message bundle cache).
 */
public class MessageManager
{
    /** The name of the global resource bundle (which other bundles revert
     * to if they can't locate a message within themselves). It must be
     * named <code>global.properties</code> and live at the top of the
     * bundle hierarchy. */
    public static const GLOBAL_BUNDLE :String = "global";

    /**
     * Constructs a message manager with the supplied resource prefix and
     * the default locale. The prefix will be prepended to the path of all
     * resource bundles prior to their resolution. For example, if a
     * prefix of <code>rsrc.messages</code> was provided and a message
     * bundle with the name <code>game.chess</code> was later requested,
     * the message manager would attempt to load a resource bundle with
     * the path <code>rsrc.messages.game.chess</code> and would eventually
     * search for a file in the classpath with the path
     * <code>rsrc/messages/game/chess.properties</code>.
     *
     * <p> See the documentation for {@link
     * ResourceBundle#getBundle(String,Locale,ClassLoader)} for a more
     * detailed explanation of how resource bundle paths are resolved.
     */
    public function MessageManager (sysMgr :ISystemManager)
    {
        // use the default locale
        _locale = Locale.getCurrent(sysMgr);

        // load up the global bundle
        _global = getBundle(GLOBAL_BUNDLE);
    }

    /**
     * Get the locale that is being used to translate messages.
     * This may be useful if using standard translations, for example
     * new SimpleDateFormat("EEEE", getLocale()) to get the name of a weekday
     * that matches the language being used for all other client translations.
     */
    public function getLocale () :Locale
    {
        return _locale;
    }

    /**
     * Sets the locale to the specified locale. Subsequent message bundles
     * fetched via the message manager will use the new locale. The
     * message bundle cache will also be cleared.
     */
    public function setLocale (locale :Locale) :void
    {
        _locale = locale;
        _cache.clear();
    }

    /**
     * Fetches the message bundle for the specified path. If no bundle can
     * be located with the specified path, a special bundle is returned
     * that returns the untranslated message identifiers instead of an
     * associated translation. This is done so that error code to handle a
     * failed bundle load need not be replicated wherever bundles are
     * used. Instead an error will be logged and the requesting service
     * can continue to function in an impaired state.
     */
    public function getBundle (path :String) :MessageBundle
    {
        // first look in the cache
        var bundle :MessageBundle = (_cache.get(path) as MessageBundle);
        if (bundle != null) {
            return bundle;
        }

        // if it's not cached, we'll need to resolve it
        var rbundle :ResourceBundle = null;
        try {
            rbundle = ResourceBundle.getResourceBundle(path);
        } catch (mre :Error) {
            Log.getLog(this).warning("Unable to resolve resource bundle " +
                "[path=" + path + "].");
        }

        // if the resource bundle contains a special resource, we'll
        // interpret that as a derivation of MessageBundle to instantiate
        // for handling that class
        if (rbundle != null) {
            var mbclass :String = null;
            try {
                mbclass = rbundle.getString(MBUNDLE_CLASS_KEY);
                if (!StringUtil.isBlank(mbclass)) {
                    var clazz :Class = ClassUtil.getClassByName(mbclass);
                    bundle = new clazz();
                }

            } catch (t :Error) {
                Log.getLog(this).warning(
                    "Failure instantiating custom message bundle " +
                    "[mbclass=" + mbclass + ", error=" + t + "].");
            }
        }

        // if there was no custom class, or we failed to instantiate the
        // custom class, use a standard message bundle
        if (bundle == null) {
            bundle = new MessageBundle();
        }

        // initialize our message bundle, cache it and return it (if we
        // couldn't resolve the bundle, the message bundle will cope with
        // it's null resource bundle)
        bundle.init(this, path, rbundle, _global);
        _cache.put(path, bundle);
        return bundle;
    }

    /** The locale for which we're obtaining message bundles. */
    protected var _locale :Locale;

    /** A cache of instantiated message bundles. */
    protected var _cache :HashMap = new HashMap();

    /** Our top-level message bundle, from which others obtain messages if
     * they can't find them within themselves. */
    protected var _global :MessageBundle;

    /** A key that can contain the classname of a custom message bundle
     * class to be used to handle messages for a particular bundle. */
    protected static const MBUNDLE_CLASS_KEY :String = "msgbundle_class";
}
}
