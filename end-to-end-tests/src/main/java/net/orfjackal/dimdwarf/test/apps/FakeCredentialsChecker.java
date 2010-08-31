// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.apps;

import net.orfjackal.dimdwarf.auth.*;

public class FakeCredentialsChecker implements CredentialsChecker<PasswordCredentials> {

    public static final String CORRECT_PASSWORD = "secret";

    public boolean isValid(PasswordCredentials credentials) {
        return credentials.getPassword().equals(CORRECT_PASSWORD);
    }
}
