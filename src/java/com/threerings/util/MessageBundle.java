//
// $Id: MessageBundle.java,v 1.1 2002/01/29 20:44:35 mdb Exp $

package com.threerings.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.samskivert.util.StringUtil;

/**
 * A message bundle provides an easy mechanism by which to obtain
 * translated message strings from a resource bundle. It uses the {@link
 * MessageFormat} class to substitute arguments into the translation
 * strings. Message bundles would generally be obtained via the {@link
 * MessageManager}, but could be constructed individually if so desired.
 */
public class MessageBundle
{
    /**
     * Constructs a message bundle which will obtain localized messages
     * from the supplied resource bundle. The path is provided purely for
     * reporting purposes.
     */
    public MessageBundle (String path, ResourceBundle bundle)
    {
        _path = path;
        _bundle = bundle;
    }

    /**
     * Obtains the translation for the specified message key. No arguments
     * are substituted into the translated string. If a translation
     * message does not exist for the specified key, an error is logged
     * and the key itself is returned so that the caller need not worry
     * about handling a null response.
     */
    public String get (String key)
    {
        try {
            return _bundle.getString(key);

        } catch (MissingResourceException mre) {
            Log.warning("Missing translation message " +
                        "[bundle=" + _path + ", key=" + key + "].");
            return key;
        }
    }

    /**
     * Obtains the translation for the specified message key. The
     * specified argument is substituted into the translated string. See
     * {@link MessageFormat} for more information on how the substitution
     * is performed. If a translation message does not exist for the
     * specified key, an error is logged and the key itself (plus the
     * argument) is returned so that the caller need not worry about
     * handling a null response.
     */
    public String get (String key, Object arg1)
    {
        return get(key, new Object[] { arg1 });
    }

    /**
     * Obtains the translation for the specified message key. The
     * specified arguments are substituted into the translated string. See
     * {@link MessageFormat} for more information on how the substitution
     * is performed. If a translation message does not exist for the
     * specified key, an error is logged and the key itself (plus the
     * arguments) is returned so that the caller need not worry about
     * handling a null response.
     */
    public String get (String key, Object arg1, Object arg2)
    {
        return get(key, new Object[] { arg1, arg2 });
    }

    /**
     * Obtains the translation for the specified message key. The
     * specified arguments are substituted into the translated string. See
     * {@link MessageFormat} for more information on how the substitution
     * is performed. If a translation message does not exist for the
     * specified key, an error is logged and the key itself (plus the
     * arguments) is returned so that the caller need not worry about
     * handling a null response.
     */
    public String get (String key, Object arg1, Object arg2, Object arg3)
    {
        return get(key, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Obtains the translation for the specified message key. The
     * specified arguments are substituted into the translated string. See
     * {@link MessageFormat} for more information on how the substitution
     * is performed. If a translation message does not exist for the
     * specified key, an error is logged and the key itself (plus the
     * arguments) is returned so that the caller need not worry about
     * handling a null response.
     */
    public String get (String key, Object[] args)
    {
        try {
            String message = _bundle.getString(key);
            return MessageFormat.format(message, args);

        } catch (MissingResourceException mre) {
            Log.warning("Missing translation message " +
                        "[bundle=" + _path + ", key=" + key + "].");
            return key + StringUtil.toString(args);
        }
    }

    /** The path that identifies the resource bundle we are using to
     * obtain our messages. */
    protected String _path;

    /** The resource bundle from which we obtain our messages. */
    protected ResourceBundle _bundle;
}
