//
// $Id: MessageManager.java,v 1.7 2004/03/31 02:09:37 mdb Exp $

package com.threerings.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.samskivert.util.StringUtil;

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
    public static final String GLOBAL_BUNDLE = "global";

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
        Log.info("Using locale: " + _locale + ".");

        // make sure the prefix ends with a dot
        if (!_prefix.endsWith(".")) {
            _prefix += ".";
        }

        // load up the global bundle
        _global = getBundle(GLOBAL_BUNDLE);
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
        ResourceBundle rbundle = null;
        try {
            rbundle = ResourceBundle.getBundle(fqpath, _locale);
        } catch (MissingResourceException mre) {
            Log.warning("Unable to resolve resource bundle " +
                        "[path=" + fqpath + ", locale=" + _locale + "].");
        }

        // if the resource bundle contains a special resource, we'll
        // interpret that as a derivation of MessageBundle to instantiate
        // for handling that class
        if (rbundle != null) {
            String mbclass = null;
            try {
                mbclass = rbundle.getString(MBUNDLE_CLASS_KEY);
                if (!StringUtil.blank(mbclass)) {
                    bundle = (MessageBundle)
                        Class.forName(mbclass).newInstance();
                }

            } catch (MissingResourceException mre) {
                // nothing to worry about

            } catch (Throwable t) {
                Log.warning("Failure instantiating custom message bundle " +
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

    /** The prefix we prepend to resource paths prior to loading. */
    protected String _prefix;

    /** The locale for which we're obtaining message bundles. */
    protected Locale _locale;

    /** A cache of instantiated message bundles. */
    protected HashMap _cache = new HashMap();

    /** Our top-level message bundle, from which others obtain messages if
     * they can't find them within themselves. */
    protected MessageBundle _global;

    /** A key that can contain the classname of a custom message bundle
     * class to be used to handle messages for a particular bundle. */
    protected static final String MBUNDLE_CLASS_KEY = "msgbundle_class";
}
