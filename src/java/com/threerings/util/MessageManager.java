//
// $Id: MessageManager.java,v 1.1 2002/01/29 20:44:35 mdb Exp $

package com.threerings.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

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
    public MessageManager (String resourcePrefix)
    {
        // keep the prefix
        _prefix = resourcePrefix;

        // use the default locale
        _locale = Locale.getDefault();

        // make sure the prefix ends with a dot
        if (!_prefix.endsWith(".")) {
            _prefix += ".";
        }
    }

    /**
     * Sets the locale to the specified locale. Subsequent message bundles
     * fetched via the message manager will use the new locale. The
     * message bundle cache will also be cleared.
     */
    public void setLocale (Locale locale)
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
    public MessageBundle getBundle (String path)
    {
        // first look in the cache
        MessageBundle bundle = (MessageBundle)_cache.get(path);
        if (bundle != null) {
            return bundle;
        }

        // if it's not cached, we'll need to resolve it
        String fqpath = _prefix + path;
        ResourceBundle rbundle = ResourceBundle.getBundle(fqpath, _locale);
        if (rbundle == null) {
            Log.warning("Unable to resolve resource bundle " +
                        "[path=" + fqpath + "].");
        }

        // create our message bundle, cache it and return it
        bundle = new MessageBundle(path, rbundle);
        _cache.put(path, bundle);
        return bundle;
    }

    /** The prefix we prepend to resource paths prior to loading. */
    protected String _prefix;

    /** The locale for which we're obtaining message bundles. */
    protected Locale _locale;

    /** A cache of instantiated message bundles. */
    protected HashMap _cache = new HashMap();
}
