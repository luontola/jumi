// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.api;

import fi.jumi.actors.generator.GenerateEventizer;
import fi.jumi.core.config.SuiteConfiguration;

@GenerateEventizer(targetPackage = "fi.jumi.core.events")
public interface RequestHandler extends RequestListener {

    void start();

    void finish();

    // XXX: overrides needed due to jumi-actors-generator not yet supporting inheritance

    @Override
    void runTests(SuiteConfiguration suiteConfiguration);

    @Override
    void shutdown();
}
