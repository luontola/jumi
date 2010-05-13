// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.*;

/**
 * Indicates that an object needs to be task scoped. Each task will run in a transaction
 * which is automatically committed when the task ends.
 *
 * @see net.orfjackal.dimdwarf.tasks.TaskExecutor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface TaskScoped {
}
