//
// $Id$

package com.threerings.crowd.chat.client {

import com.threerings.util.Name;
import com.threerings.util.StringUtil;

/**
 * A chat filter that can filter out curse words from user chat.
 */
public /*abstract*/ class CurseFilter implements ChatFilter
{
    /** Filtration constants. */
    public static const DROP :int = 0;
    public static const COMIC :int = 1;
    public static const VERNACULAR :int = 2;
    public static const UNFILTERED :int = 3;

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
    public function CurseFilter (curseWords :String, stopWords :String)
    {
        configureCurseWords(curseWords);
        configureStopWords(stopWords);
    }

    /**
     * The client will need to provide a way to look up our current chat filter
     * mode.
     */
    public function getFilterMode () :int
    {
        throw new Error("abstract");
    }

    // from interface ChatFilter
    public function filter (
        msg :String, otherUser :Name, outgoing :Boolean) :String
    {
        // first, check against the drop-always list
        if (_stopExp.test(msg)) {
            return null;
        }

        // then see what kind of curse filtering the user has configured
        var level :int = getFilterMode();
        if (level == UNFILTERED) {
            return msg;
        }

        var numStops :int = _curseExps.length;
        for (var ii :int = 0; ii < numStops; ii++) {
            var regexp :RegExp = (_curseExps[ii] as RegExp);
            regexp.lastIndex = 0; // reset the matcher

            var result :Object;
            var replacement :String;
            while (null != (result = regexp.exec(msg))) {
                switch (level) {
                case DROP:
                    return null;

                case COMIC: default:
                    replacement = comicChars(int(_comicLength[ii]));
                    break;

                case VERNACULAR:
                    replacement = String(_vernacular[ii]);
                    // preserve the case of the first letter of the text
                    // we're replacing
                    if (StringUtil.isUpperCase(String(result[2]))) {
                        replacement = replacement.charAt(0).toUpperCase() +
                            replacement.substring(1);
                    }
                    break;
                }

                // sub in the replacement text, carrying over the pre/post
                // matching groups
                replacement = StringUtil.substitute(String(_replacements[ii]),
                    String(result[1]), replacement, String(result[3]));

                // do the replacement. Note the jimmying of lastIndex:
                // abjectscript does not make this smooth!
                var matchLength :int = String(result[0]).length;
                var matchIndex :int = int(result.index);
                msg = msg.substring(0, matchIndex) + replacement +
                    msg.substring(matchIndex + matchLength);
                regexp.lastIndex += (replacement.length - matchLength);
            }
        }

        return msg;
    }

    /**
     * Configure the curse word portion of our filtering.
     */
    protected function configureCurseWords (curseWords :String) :void
    {
        var tokens :Array = curseWords.split(" ");
        for each (var token :String in tokens) {
            token = StringUtil.trim(token);
            if (token == "") {
                continue;
            }
            var bits :Array = token.split("=");
            if (bits.length != 2) {
                Log.getLog(this).warning("Something looks wrong with " +
                    "your cursewords (" + token + "), skipping.");
                continue;
            }

            var curse :String = String(bits[0]);
            var sub :String = "";
            var patPre :String = "()";
            var patPost :String = "";
            if (curse.charAt(0) == "*") {
                curse = curse.substring(1);
                patPre = "([a-zA-Z]*)";
                sub += "{0}";
            }
            sub += "{1}";
            if (curse.charAt(curse.length - 1) == "*") {
                curse = curse.substring(0, curse.length - 1);
                patPost = "([a-zA-Z]*)";
                sub += "{2}";
            }

            var regexp :RegExp = new RegExp(
                "\\b" + patPre + "(" + curse + ")" + patPost + "\\b", "gi");
            _curseExps.push(regexp);
            _replacements.push(sub);

            var slang :String = StringUtil.trim(String(bits[1]));
            slang = slang.replace(/_/g, " ");
            _vernacular.push(slang);
            _comicLength.push(curse.length);
        }
    }

    /**
     * Configure the words that will stop.
     */
    protected function configureStopWords (stopWords :String) :void
    {
        var tokens :Array = stopWords.split(" ");
        var pattern :String = "";
        for each (var token :String in tokens) {
            token = StringUtil.trim(token);
            if (token == "") {
                continue;
            }
            if ("" == pattern) {
                pattern += ".*(";
            } else {
                pattern += "|";
            }
            pattern += "\\b" +
                token.replace(/\*/g, "[A-Za-z]*") + "\\b";
        }
        pattern += ").*";

        _stopExp = new RegExp(pattern, "i");
    }

    /**
     * Return a comicy replacement of the specified length;
     */
    protected function comicChars (length :int) :String
    {
        var str :String = "";
        while (length-- > 0) {
            str += COMIC_CHARS.charAt(int(Math.random() * COMIC_CHARS.length));
        }
        return str;
    }

    /** A regexp that will always cause a message to be dropped if it
     * matches. */
    protected var _stopExp :RegExp;

    /** Regexps for each curseword. */
    protected var _curseExps :Array = [];

    /** Length of comic-y replacements for each curseword. */
    protected var _comicLength :Array = [];

    /** Replacements. */
    protected var _replacements :Array = [];

    /** Replacements for each curseword "in the vernacular". */
    protected var _vernacular :Array = [];

    /** Comic replacement characters. */
    protected const COMIC_CHARS :String = "!@#%&*";
}
}
