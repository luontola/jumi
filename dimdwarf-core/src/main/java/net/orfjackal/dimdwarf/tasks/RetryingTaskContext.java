// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface RetryingTaskContext {
}