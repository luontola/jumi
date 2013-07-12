// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.stdout;

import javax.annotation.concurrent.Immutable;

@Immutable
public class NullOutputListener implements OutputListener {

    @Override
    public void out(String text) {
    }

    @Override
    public void err(String text) {
    }
}
