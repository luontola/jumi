// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import org.junit.rules.Timeout;

public class Timeouts {

    public static final int TIMEOUT = 1000;

    public static Timeout forUnitTest() {
        return new Timeout(TIMEOUT);
    }
}
