// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scopes;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.*;

/**
 * Indicates that an object needs to be task scoped. Each task will run in a transaction
 * which is automatically committed when the task ends.
 *
 * @author Esko Luontola
 * @see net.orfjackal.dimdwarf.tasks.TaskExecutor
 * @since 13.9.2008
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface TaskScoped {
}
