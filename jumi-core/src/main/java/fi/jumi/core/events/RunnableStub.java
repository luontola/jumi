// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.events;

import fi.jumi.actors.generator.GenerateEventizer;

@SuppressWarnings("unused")
@GenerateEventizer(targetPackage = "fi.jumi.core.events", useParentInterface = true)
interface RunnableStub extends Runnable {
}
