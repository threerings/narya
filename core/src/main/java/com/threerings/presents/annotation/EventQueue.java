//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import com.samskivert.util.RunQueue;

/**
 * An annotation that identifies the distributed object event dispatcher's {@link RunQueue}. Code
 * that requires the ability to post runnables for execution on the event dispatcher thread can
 * inject this queue like so:
 *
 * <code>@Inject @EventQueue RunQueue _dobjq;</code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@BindingAnnotation
public @interface EventQueue
{
}
