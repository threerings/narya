//
// $Id: MessageBundle.java,v 1.25 2004/03/06 11:29:19 mdb Exp $

package com.threerings.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.samskivert.text.MessageUtil;
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
     * Initializes the message bundle which will obtain localized messages
     * from the supplied resource bundle. The path is provided purely for
     * reporting purposes.
     */
    public void init (MessageManager msgmgr, String path,
                      ResourceBundle bundle, MessageBundle parent)
    {
        _msgmgr = msgmgr;
        _path = path;
        _bundle = bundle;
        _parent = parent;
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
        // if this string is tainted, we don't translate it, instead we
        // simply remove the taint character and return it to the caller
        if (key.startsWith(MessageUtil.TAINT_CHAR)) {
            return key.substring(1);
        }

        String msg = getResourceString(key);
        return (msg != null) ? msg : key;
    }

    /**
     * Returns true if we have a translation mapping for the supplied key,
     * false if not.
     */
    public boolean exists (String key)
    {
        return getResourceString(key, false) != null;
    }

    /**
     * Get a String from the resource bundle, or null if there was an error.
     */
    protected String getResourceString (String key)
    {
        return getResourceString(key, true);
    }

    /**
     * Get a String from the resource bundle, or null if there was an
     * error.
     *
     * @param key the resource key.
     * @param reportMissing whether or not the method should log an error
     * if the resource didn't exist.
     */
    protected String getResourceString (String key, boolean reportMissing)
    {
        try {
            if (_bundle != null) {
                return _bundle.getString(key);
            }
        } catch (MissingResourceException mre) {
            // fall through and try the parent
        }

        // if we have a parent, try getting the string from them
        if (_parent != null) {
            String value = _parent.getResourceString(key, false);
            if (value != null) {
                return value;
            }
            // if we didn't find it in our parent, we want to fall
            // through and report missing appropriately
        }

        if (reportMissing) {
            Log.warning("Missing translation message " +
                        "[bundle=" + _path + ", key=" + key + "].");
            Thread.dumpStack();
        }

        return null;
    }

    /**
     * Obtains the translation for the specified message key accounting
     * for plurality in the following manner. Assuming a message key of
     * <code>m.widgets</code>, the following translations should be
     * defined:
     * <pre>
     * m.widgets.0 = no widgets.
     * m.widgets.1 = {0} widget.
     * m.widgets.n = {0} widgets.
     * </pre>
     *
     * The specified argument is substituted into the translated string as
     * appropriate. Consider using:
     *
     * <pre>
     * m.widgets.n = {0,number,integer} widgets.
     * </pre>
     *
     * to obtain proper insertion of commas and dots as appropriate for
     * the locale.
     */
    public String get (String key, int value)
    {
        // we could use ChoiceFormat for this, but there's no indication
        // that it does anything more useful than what we're doing here
        // when it comes to handling plurals
        String format;
        switch (value) {
        case 0: format = getResourceString(key + ".0"); break;
        case 1: format = getResourceString(key + ".1"); break;
        default: format = getResourceString(key + ".n"); break;
        }
        Object[] args = new Object[] { new Integer(value) };
        return (format == null) ? (key + StringUtil.toString(args)) :
            MessageFormat.format(MessageUtil.escape(format), args);
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
        // if this is a qualified key, we need to pass the buck to the
        // appropriate message bundle
        if (key.startsWith(MessageUtil.QUAL_PREFIX)) {
            MessageBundle qbundle = _msgmgr.getBundle(getBundle(key));
            return qbundle.get(getUnqualifiedKey(key), args);
        }

        String msg = getResourceString(key);
        return (msg != null) ?
            MessageFormat.format(MessageUtil.escape(msg), args)
            : (key + StringUtil.toString(args));
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
        // if this is a qualified key, we need to pass the buck to the
        // appropriate message bundle; we have to do it here because we
        // want the compound arguments of this key to be translated in the
        // context of the containing message bundle qualification
        if (compoundKey.startsWith(MessageUtil.QUAL_PREFIX)) {
            MessageBundle qbundle = _msgmgr.getBundle(getBundle(compoundKey));
            return qbundle.xlate(getUnqualifiedKey(compoundKey));
        }

        // to be more efficient about creating unnecessary objects, we
        // do some checking before splitting
        int tidx = compoundKey.indexOf('|');
        if (tidx == -1) {
            return get(compoundKey);

        } else {
            String key = compoundKey.substring(0, tidx);
            String argstr = compoundKey.substring(tidx+1);
            String[] args = StringUtil.split(argstr, "|");
            // unescape and translate the arguments
            for (int i = 0; i < args.length; i++) {
                // if the argument is tainted, do no further translation
                // (it might contain |s or other fun stuff)
                if (args[i].startsWith(MessageUtil.TAINT_CHAR)) {
                    args[i] = MessageUtil.unescape(args[i].substring(1));
                } else {
                    args[i] = xlate(MessageUtil.unescape(args[i]));
                }
            }
            return get(key, args);
        }
    }

    /**
     * Call this to "taint" any string that has been entered by an entity
     * outside the application so that the translation code knows not to
     * attempt to translate this string when doing recursive translations
     * (see {@link #xlate}).
     */
    public static String taint (Object text)
    {
        return MessageUtil.taint(text);
    }

    /**
     * Composes a message key with an array of arguments. The message can
     * subsequently be translated in a single call using {@link #xlate}.
     */
    public static String compose (String key, Object[] args)
    {
        return MessageUtil.compose(key, args);
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with a single argument.
     */
    public static String compose (String key, Object arg)
    {
        return compose(key, new Object[] { arg });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with two arguments.
     */
    public static String compose (String key, Object arg1, Object arg2)
    {
        return compose(key, new Object[] { arg1, arg2 });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with three arguments.
     */
    public static String compose (
        String key, Object arg1, Object arg2, Object arg3)
    {
        return compose(key, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with a single argument that will be automatically tainted (see
     * {@link #taint}).
     */
    public static String tcompose (String key, Object arg)
    {
        return compose(key, new Object[] { taint(arg) });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with two arguments that will be automatically tainted (see {@link
     * #taint}).
     */
    public static String tcompose (String key, Object arg1, Object arg2)
    {
        return compose(key, new Object[] { taint(arg1), taint(arg2) });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with three arguments that will be automatically tainted (see {@link
     * #taint}).
     */
    public static String tcompose (
        String key, Object arg1, Object arg2, Object arg3)
    {
        return compose(key, new Object[] {
            taint(arg1), taint(arg2), taint(arg3) });
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with an array of arguments that will be automatically tainted (see
     * {@link #taint}).
     */
    public static String tcompose (String key, Object[] args)
    {
        return MessageUtil.tcompose(key, args);
    }

    /**
     * Returns a fully qualified message key which, when translated by
     * some other bundle, will know to resolve and utilize the supplied
     * bundle to translate this particular key.
     */
    public static String qualify (String bundle, String key)
    {
        return MessageUtil.qualify(bundle, key);
    }

    /**
     * Returns the bundle name from a fully qualified message key.
     *
     * @see #qualify
     */
    public static String getBundle (String qualifiedKey)
    {
        return MessageUtil.getBundle(qualifiedKey);
    }

    /**
     * Returns the unqualified portion of the key from a fully qualified
     * message key.
     *
     * @see #qualify
     */
    public static String getUnqualifiedKey (String qualifiedKey)
    {
        return MessageUtil.getUnqualifiedKey(qualifiedKey);
    }

    /** The message manager via whom we'll resolve fully qualified
     * translation strings. */
    protected MessageManager _msgmgr;

    /** The path that identifies the resource bundle we are using to
     * obtain our messages. */
    protected String _path;

    /** The resource bundle from which we obtain our messages. */
    protected ResourceBundle _bundle;

    /** Our parent bundle if we're not the global bundle. */
    protected MessageBundle _parent;
}
