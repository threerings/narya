//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.admin.server.persist;

/**
 * Contains a single datum of configuration information.
 */
public class ConfigDatum
{
    public String node;
    public String object;
    public String field;
    public String value;

    @Override
    public String toString () {
        return node + "." + object + "." + field + "=" + value + "]";
    }
}
