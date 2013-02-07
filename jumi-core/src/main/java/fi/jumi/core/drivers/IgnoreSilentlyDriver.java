// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import fi.jumi.api.drivers.*;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.Executor;

@Immutable
public class IgnoreSilentlyDriver extends Driver {

    @Override
    public void findTests(Class<?> testClass, SuiteNotifier notifier, Executor executor) {
    }
}
