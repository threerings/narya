//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that controls ActionScript code generation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD,
         ElementType.METHOD, ElementType.TYPE})
public @interface ActionScript
{
    /**
     * Indicates whether this field, method or class should be omitted from the ActionScript
     * translation.
     */
    boolean omit () default false;

    /**
     * Indicates a custom name to be used for the ActionScript version of this field, method or
     * class.
     */
    String name () default "";

    /**
     * Indicates a custom type to be used for the ActionScript version of this field. Ignored if
     * used on a method or class.
     */
    String type () default "";
}
