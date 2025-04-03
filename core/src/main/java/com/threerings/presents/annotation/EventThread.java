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
 * An annotation indicating that a particular method or all methods in a class (that are not
 * otherwise explicitly annotated) should only be called while on the distributed object event
 * dispatch thread.
 *
 * NOTE: These annotations are currently merely advisory, but someday we would like to use AspectJ
 * or something like that to inject code that enforces these requirements on dev server builds.
 */
@Target(value={ ElementType.METHOD, ElementType.TYPE })
@Retention(value=RetentionPolicy.SOURCE)
public @interface EventThread
{
}
