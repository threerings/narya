//
// $Id$

package com.threerings.crowd.chat.client;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.samskivert.util.RandomUtil;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.crowd.Log;

/**
 * A chat filter that can filter out curse words from user chat.
 */
public abstract class CurseFilter implements ChatFilter
{
    /** Indicates how messages should be handled. */
    public enum Mode { DROP, COMIC, VERNACULAR, UNFILTERED; }

    /**
     * Creates a curse filter. The curse words should be a string in the
     * following format:
     *
     * <pre>
     * *penis*=John_Thomas shit*=barnacle muff=britches
     * </pre>
     *
     * The key/value pairs are separated by spaces, * matches word characters
     * and the value after the = is the string into which to convert the text
     * when converting to the vernacular. Underscores in the target string will
     * be turned into spaces.
     *
     * <p> And stopWords should be in the following format:
     *
     * <pre>
     * *faggot* rape rapes raped raping
     * </pre>
     *
     * Words are separated by spaces and * matches any other word characters.
     */
    public CurseFilter (String curseWords, String stopWords)
    {
        configureCurseWords(curseWords);
        configureStopWords(stopWords);
    }

    /**
     * The client will need to provide a way to look up our current chat filter
     * mode.
     */
    public abstract Mode getFilterMode ();

    // from interface ChatFilter
    public String filter (String msg, Name otherUser, boolean outgoing)
    {
        // first, check against the drop-always list
        _stopMatcher.reset(msg);
        if (_stopMatcher.matches()) {
            return null;
        }

        // then see what kind of curse filtering the user has configured
        Mode level = getFilterMode();
        if (level == Mode.UNFILTERED) {
            return msg;
        }

        StringBuffer inbuf = new StringBuffer(msg);
        StringBuffer outbuf = new StringBuffer(msg.length());
        for (int ii=0, nn=_matchers.length; ii < nn; ii++) {
            Matcher m = _matchers[ii];
            m.reset(inbuf);
            while (m.find()) { 
                switch (level) {
                case DROP:
                    return null;

                case COMIC: default:
                    m.appendReplacement(outbuf,
                        StringUtil.replace(_replacements[ii], " ", 
                            comicChars(_comicLength[ii])));
                    break;

                case VERNACULAR:
                    String vernacular = _vernacular[ii];
                    if (Character.isUpperCase(m.group(2).charAt(0))) {
                        vernacular = new String(
                            Character.toUpperCase(vernacular.charAt(0)) +
                            vernacular.substring(1));
                    }
                    m.appendReplacement(outbuf, 
                        StringUtil.replace(_replacements[ii], " ", vernacular));
                    break;
                }
            }
            if (outbuf.length() == 0) {
                // optimization: if we didn't find a match, jump to the next
                // pattern without doing any StringBuilder jimmying
                continue;
            }
            m.appendTail(outbuf);

            // swap the buffers around and clear the output
            StringBuffer temp = inbuf;
            inbuf = outbuf;
            outbuf = temp;
            outbuf.setLength(0);
        }

        return inbuf.toString();
    }

    /**
     * Configure the curse word portion of our filtering.
     */
    protected void configureCurseWords (String curseWords)
    {
        StringTokenizer st = new StringTokenizer(curseWords);
        int numWords = st.countTokens();

        _matchers = new Matcher[numWords];
        _replacements = new String[numWords];
        _vernacular = new String[numWords];
        _comicLength = new int[numWords];

        for (int ii=0; ii < numWords; ii++) {
            String mapping = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(mapping, "=");
            if (st2.countTokens() != 2) {
                Log.warning("Something looks wrong in the x.cursewords " +
                    "properties (" + mapping + "), skipping.");
                continue;
            }
            String curse = st2.nextToken();

            String s = "";
            String p = "";
            if (curse.startsWith("*")) {
                curse = curse.substring(1);
                p += "([a-zA-Z]*)";
                s += "$1";
            } else {
                p += "()";
            }
            s += " ";
            p += " ";
            if (curse.endsWith("*")) {
                curse = curse.substring(0, curse.length() - 1);
                p += "([a-zA-Z]*)";
                s += "$3";
            }

            String pattern = "\\b" +
                StringUtil.replace(p, " ", "(" + curse + ")") + "\\b";
            Pattern pat = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            _matchers[ii] = pat.matcher("");
            _replacements[ii] = s;
            _vernacular[ii] = st2.nextToken().replace('_', ' ');
            _comicLength[ii] = curse.length();
        }
    }

    /**
     * Configure the words that will stop.
     */
    protected void configureStopWords (String stopWords)
    {
        StringTokenizer st = new StringTokenizer(stopWords);

        String pattern = "";
        while (st.hasMoreTokens()) {
            if ("".equals(pattern)) {
                pattern += ".*(";
            } else {
                pattern += "|";
            }
            pattern += "\\b" +
                StringUtil.replace(st.nextToken(), "*", "[A-Za-z]*") + "\\b";
        }
        pattern += ").*";

        Pattern pat = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        _stopMatcher = pat.matcher("");
    }

    /**
     * Return a comicy replacement of the specified length;
     */
    protected String comicChars (int length)
    {
        StringBuilder buf = new StringBuilder();
        for (int ii=0; ii < length; ii++) {
            buf.append(RandomUtil.pickRandom(COMIC_CHARS));
        }

        return buf.toString();
    }

    /** A matcher that will always cause a message to be dropped if it
     * matches. */
    protected Matcher _stopMatcher;

    /** Matchers for each curseword. */
    protected Matcher[] _matchers;

    /** Length of comic-y replacements for each curseword. */
    protected int[] _comicLength;

    /** Replacements. */
    protected String[] _replacements;

    /** Replacements for each curseword "in the vernacular". */
    protected String[] _vernacular;

    /** Comic replacement characters. */
    protected String[] COMIC_CHARS = { "!", "@", "#", "%", "&", "*" };
}
