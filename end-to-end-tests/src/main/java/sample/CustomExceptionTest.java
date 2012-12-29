// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import fi.jumi.api.RunVia;
import fi.jumi.simpleunit.SimpleUnit;

@RunVia(SimpleUnit.class)
@SuppressWarnings({"UnusedDeclaration"})
public class CustomExceptionTest {

    public void testThrowCustomException() throws Exception {
        throw (Exception) Class.forName("sample.extra.CustomException")
                .getConstructor(String.class).newInstance("dummy failure");
    }
}
