//
// $Id: MessageBundle.java,v 1.2 2002/02/09 01:35:26 mdb Exp $

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
     * specified argument is substituted into the translated string.
     *
     * <p> See {@link MessageFormat} for more information on how the
     * substitution is performed. If a translation message does not exist
     * for the specified key, an error is logged and the key itself (plus
     * the argument) is returned so that the caller need not worry about
     * handling a null response.
     */
    public String get (String key, Object arg1)
    {
        return get(key, new Object[] { arg1 });
    }

    /**
     * Obtains the translation for the specified message key. The
     * specified arguments are substituted into the translated string.
     *
     * <p> See {@link MessageFormat} for more information on how the
     * substitution is performed. If a translation message does not exist
     * for the specified key, an error is logged and the key itself (plus
     * the arguments) is returned so that the caller need not worry about
     * handling a null response.
     */
    public String get (String key, Object arg1, Object arg2)
    {
        return get(key, new Object[] { arg1, arg2 });
    }

    /**
     * Obtains the translation for the specified message key. The
     * specified arguments are substituted into the translated string.
     *
     * <p> See {@link MessageFormat} for more information on how the
     * substitution is performed. If a translation message does not exist
     * for the specified key, an error is logged and the key itself (plus
     * the arguments) is returned so that the caller need not worry about
     * handling a null response.
     */
    public String get (String key, Object arg1, Object arg2, Object arg3)
    {
        return get(key, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Obtains the translation for the specified message key. The
     * specified arguments are substituted into the translated string.
     *
     * <p> See {@link MessageFormat} for more information on how the
     * substitution is performed. If a translation message does not exist
     * for the specified key, an error is logged and the key itself (plus
     * the arguments) is returned so that the caller need not worry about
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

    /**
     * Obtains the translation for the specified compound message key. A
     * compound key contains the message key followed by a tab separated
     * list of message arguments which will be subsituted into the
     * translation string.
     *
     * <p> See {@link MessageFormat} for more information on how the
     * substitution is performed. If a translation message does not exist
     * for the specified key, an error is logged and the key itself (plus
     * the arguments) is returned so that the caller need not worry about
     * handling a null response.
     */
    public String xlate (String compoundKey)
    {
        // to be more efficient about creating unnecessary objects, we
        // do some checking before splitting
        int tidx = compoundKey.indexOf("\t");
        if (tidx == -1) {
            return get(compoundKey);

        } else {
            String key = compoundKey.substring(0, tidx);
            String argstr = compoundKey.substring(tidx+1);
            String[] args = StringUtil.split(argstr, "\t");
            return get(key, args);
        }
    }

    /** The path that identifies the resource bundle we are using to
     * obtain our messages. */
    protected String _path;

    /** The resource bundle from which we obtain our messages. */
    protected ResourceBundle _bundle;
}
