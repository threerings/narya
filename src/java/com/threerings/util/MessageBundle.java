//
// $Id: MessageBundle.java,v 1.9 2002/05/01 02:45:00 mdb Exp $

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
     * Initializes the message bundle which will obtain localized messages
     * from the supplied resource bundle. The path is provided purely for
     * reporting purposes.
     */
    public void init (String path, ResourceBundle bundle)
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
        // if we were unable to resolve our resource bundle, we can't do
        // any translations
        if (_bundle == null) {
            return key;
        }

        // if this string is tainted, we don't translate it, instead we
        // simply remove the taint character and return it to the caller
        if (key.startsWith(TAINT_CHAR)) {
            return key.substring(1);
        }

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
        // if we were unable to resolve our resource bundle, we can't do
        // any translations
        if (_bundle == null) {
            return key + StringUtil.toString(args);
        }

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
        int tidx = compoundKey.indexOf('|');
        if (tidx == -1) {
            return get(compoundKey);

        } else {
            String key = compoundKey.substring(0, tidx);
            String argstr = compoundKey.substring(tidx+1);
            String[] args = StringUtil.split(argstr, "|");
            // unescape and translate the arguments
            for (int i = 0; i < args.length; i++) {
                args[i] = xlate(unescape(args[i]));
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
    public static String taint (String text)
    {
        return TAINT_CHAR + text;
    }

    /**
     * Composes a message key with an array of arguments. The message can
     * subsequently be translated in a single call using {@link #xlate}.
     */
    public static String compose (String key, String[] args)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(key);
        buf.append('|');
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                buf.append('|');
            }
            // escape the string while adding to the buffer
            String arg = args[i];
            int alength = arg.length();
            for (int p = 0; p < alength; p++) {
                char ch = arg.charAt(p);
                if (ch == '|') {
                    buf.append("\\!");
                } else if (ch == '\\') {
                    buf.append("\\\\");
                } else {
                    buf.append(ch);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Unescapes characters that are escaped in a call to compose.
     */
    protected static String unescape (String value)
    {
        int bsidx = value.indexOf('\\');
        if (bsidx == -1) {
            return value;
        }

        StringBuffer buf = new StringBuffer();
        int vlength = value.length();
        for (int i = 0; i < vlength; i++) {
            char ch = value.charAt(i);
            if (ch != '\\') {
                buf.append(ch);
            } else if (i < vlength-1) {
                // look at the next character
                ch = value.charAt(++i);
                buf.append((ch == '!') ? '|' : ch);
            } else {
                buf.append(ch);
            }
        }

        return buf.toString();
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with a single argument.
     */
    public static String compose (String key, String arg)
    {
        return compose(key, new String[] { arg });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with two arguments.
     */
    public static String compose (String key, String arg1, String arg2)
    {
        return compose(key, new String[] { arg1, arg2 });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with three arguments.
     */
    public static String compose (
        String key, String arg1, String arg2, String arg3)
    {
        return compose(key, new String[] { arg1, arg2, arg3 });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with a single argument that will be automatically (see {@link
     * #taint}).
     */
    public static String tcompose (String key, String arg)
    {
        return compose(key, new String[] { taint(arg) });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with two arguments that will be automatically tainted (see {@link
     * #taint}).
     */
    public static String tcompose (String key, String arg1, String arg2)
    {
        return compose(key, new String[] { taint(arg1), taint(arg2) });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with three arguments that will be automatically tainted (see {@link
     * #taint}).
     */
    public static String tcompose (
        String key, String arg1, String arg2, String arg3)
    {
        return compose(key, new String[] {
            taint(arg1), taint(arg2), taint(arg3) });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with an array of arguments that will be automatically tainted (see
     * {@link #taint}).
     */
    public static String tcompose (String key, String[] args)
    {
        int acount = args.length;
        String[] targs = new String[acount];
        for (int ii = 0; ii < acount; ii++) {
            targs[ii] = taint(args[ii]);
        }
        return compose(key, targs);
    }

    /** The path that identifies the resource bundle we are using to
     * obtain our messages. */
    protected String _path;

    /** The resource bundle from which we obtain our messages. */
    protected ResourceBundle _bundle;

    /** Text prefixed by this character will be considered tainted when
     * doing recursive translations and won't be translated. */
    protected static final String TAINT_CHAR = "~";
    // protected static final String TAINT_CHAR = '~';
}
