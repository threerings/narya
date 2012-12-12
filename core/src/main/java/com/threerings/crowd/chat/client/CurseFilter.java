//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.crowd.chat.client;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import com.samskivert.util.RandomUtil;
import com.threerings.util.Name;

import static com.threerings.crowd.Log.log;

/**
 * A chat filter that can filter out curse words from user chat.
 */
public abstract class CurseFilter implements ChatFilter
{
    /** Indicates how messages should be handled. */
    public enum Mode { DROP, COMIC, VERNACULAR, UNFILTERED; }

    /**
     * Return a comicy replacement of the specified length.
     */
    public static String comicChars (int length)
    {
        char[] chars = new char[length];
        for (int ii=0; ii < length; ii++) {
            chars[ii] = RandomUtil.pickRandom(COMIC_CHARS);
        }
        return new String(chars);
    }


    /**
     * Creates a curse filter. The curse words should be a string in the following format:
     *
     * <pre>
     * *penis*=John_Thomas shit*=barnacle muff=britches
     * </pre>
     *
     * The key/value pairs are separated by spaces, * matches word characters and the value after
     * the = is the string into which to convert the text when converting to the vernacular.
     * Underscores in the target string will be turned into spaces.
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
     * The client will need to provide a way to look up our current chat filter mode.
     */
    public abstract Mode getFilterMode ();

    // from interface ChatFilter
    public String filter (String msg, Name otherUser, boolean outgoing)
    {
        // first, check against the drop-always list
        _stopMatcher.reset(msg);
        if (_stopMatcher.find()) {
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

                case COMIC:
                    m.appendReplacement(outbuf,
                        _replacements[ii].replace(" ", comicChars(_comicLength[ii])));
                    break;

                case VERNACULAR:
                    String vernacular = _vernacular[ii];
                    if (Character.isUpperCase(m.group(2).codePointAt(0))) {
                        int firstCharLen = Character.charCount(vernacular.codePointAt(0));
                        vernacular = vernacular.substring(0, firstCharLen).toUpperCase() +
                                     vernacular.substring(firstCharLen);
                    }
                    m.appendReplacement(outbuf, _replacements[ii].replace(" ", vernacular));
                    break;

                case UNFILTERED:
                    // We returned the msg unadulterated above in this case, so it should be
                    // impossible to wind up here, but let's enumerate it so we can let the compiler
                    // scream about missing enum values in a switch
                    log.warning("Omg? We're trying to filter chat even though we're unfiltered?");
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
                log.warning("Something looks wrong in the x.cursewords properties (" +
                    mapping + "), skipping.");
                continue;
            }
            String curse = st2.nextToken();

            String s = "";
            String p = "";
            if (curse.startsWith("*")) {
                curse = curse.substring(1);
                p += "([\\p{L}\\p{Digit}]*)";
                s += "$1";
            } else {
                p += "()";
            }
            s += " ";
            p += " ";
            if (curse.endsWith("*")) {
                curse = curse.substring(0, curse.length() - 1);
                p += "([\\p{L}\\p{Digit}]*)";
                s += "$3";
            }

            String pattern = "\\b" + p.replace(" ", "(" + curse + ")") + "\\b";
            Pattern pat = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            _matchers[ii] = pat.matcher("");
            _replacements[ii] = s;
            _vernacular[ii] = st2.nextToken().replace('_', ' ');
            _comicLength[ii] = curse.codePointCount(0, curse.length());
        }
    }

    /**
     * Configure the words that will stop.
     */
    protected void configureStopWords (String stopWords)
    {
        List<String> patterns = Lists.newArrayList();
        for (StringTokenizer st = new StringTokenizer(stopWords); st.hasMoreTokens(); ) {
            patterns.add(getStopWordRegexp(st.nextToken()));
        }
        String pattern = patterns.isEmpty()
            ? ".\\A" // matches nothing
            : "(" + Joiner.on('|').join(patterns) + ")";
        setStopPattern(pattern);
    }

    /**
     * Sets our stop word matcher to one for the given regular expression.
     */
    protected void setStopPattern (String pattern)
    {
        _stopMatcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher("");
    }

    /**
     * Turns a naughty word into a regular expression to catch it.
     */
    protected String getStopWordRegexp (String word)
    {
        return "\\b" + word.replace("*", "[A-Za-z]*") + "\\b";
    }

    /** A matcher that will always cause a message to be dropped if it matches. */
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
    protected static final Character[] COMIC_CHARS = { '!', '@', '#', '%', '&', '*' };
}
