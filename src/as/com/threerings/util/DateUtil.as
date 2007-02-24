//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

/**
 * Utility for dates.
 */
public class DateUtil
{
    /**
     * Calculates a brief, conversational representation of the given date relative to 'now':
     *
     * Date occured in a past year:
     *    10/10/1969
     * Date occured over 6 days ago:
     *    Oct 10
     * Date occured over 23 hours ago:
     *    Wed 15:10
     * Date occured in the past 23 hours:
     *   15:10
     **/
    public static function getConversationalDateString (date :Date, now :Date = null) :String
    {
        if (now == null) {
            now = new Date();
        }
        if (date.fullYear != now.fullYear) {
            // e.g. 25/10/06
            return date.day + "/" + date.month + "/" + date.fullYear;
        }
        var hourDiff :uint = (now.time - date.time) / (3600 * 1000);
        if (hourDiff > 6*24) {
            // e.g. Oct 25
            return getMonthName(date.month) + " " + date.day;
        }
        if (hourDiff > 23) {
            // e.g. Wed 15:10
            return getDayName(date.day) + " " + date.hours + ":" +
                   (date.minutes < 10 ? "0" : "") + date.minutes;
           }
        // e.g. 15:10
        return date.hours + ":" + (date.minutes < 10 ? "0" : "") + date.minutes;
    }

    /**
     * Return the name of the given (integer) month; 0 is January, and so on.
     */
    public static function getMonthName (month :uint, full :Boolean = false) :String
    {
        return full ? _months[month] : _months[month].substr(0, 3);
    }

    /**
     * Return the name of the given (integer) day; 0 is Sunday, and so on.
     */
    public static function getDayName (day :uint, full :Boolean = false) :String
    {
        return full ? _days[day] : _days[day].substr(0, 3);
    }

    protected static var _days :Array =
        [ "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" ];
    protected static var _months :Object =
        [ "January", "February", "March", "April", "May", "June",
          "July", "August", "September", "October", "November", "December" ];
}
}
