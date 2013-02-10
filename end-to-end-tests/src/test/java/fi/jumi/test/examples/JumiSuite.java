// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.examples;

import fi.jumi.launcher.JumiBootstrap;
import org.junit.Test;

public class JumiSuite {

    @Test
    public void all_tests() throws Exception {
        JumiBootstrap bootstrap = new JumiBootstrap();
        bootstrap.suite
                .addJvmOptions("-ea")
                .setIncludedTestsPattern("glob:com/example/**Test.class"); // uses Java 7 glob patterns
        bootstrap
                .enableDebugMode() // shows Jumi's internal messaging; can be useful for testing framework developers
                .setPassingTestsVisible(true) // shows full output from all tests, instead of just failing tests
                .runSuite();
    }
}
