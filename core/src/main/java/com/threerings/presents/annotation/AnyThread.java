//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation indicating that a particular method in a class is safe to be called from any
 * thread. This is not allowed on a class because it serves purely as documentation to demonstrate
 * specific situations where methods are intended to be used by both the event dispatch and
 * blocking threads which are generally uncommon.
 */
@Target(value=ElementType.METHOD)
@Retention(value=RetentionPolicy.SOURCE)
public @interface AnyThread
{
}
