// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package end2endtests.apps.echo;

import com.google.inject.AbstractModule;
import end2endtests.apps.FakeCredentialsChecker;
import net.orfjackal.dimdwarf.auth.CredentialsChecker;

public class EchoApp extends AbstractModule {

    protected void configure() {
        bind(CredentialsChecker.class).to(FakeCredentialsChecker.class);
    }
}
