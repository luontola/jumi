// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.drivers;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class DriverFinderFactory {

    public static CompositeDriverFinder createDriverFinder() {
        return new CompositeDriverFinder(
                new RunViaAnnotationDriverFinder()
        );
    }
}
