// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import fi.jumi.api.RunVia;
import fi.jumi.simpleunit.SimpleUnit;

import java.io.IOException;
import java.nio.file.Paths;

@RunVia(SimpleUnit.class)
@SuppressWarnings({"UnusedDeclaration"})
public class WorkingDirectoryTest {

    public void testWorkingDirectory() throws IOException {
        System.out.print("working directory: " + Paths.get(".").toRealPath());
    }
}
