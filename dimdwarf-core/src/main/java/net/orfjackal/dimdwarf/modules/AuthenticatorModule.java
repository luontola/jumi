// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.Provides;
import net.orfjackal.dimdwarf.actors.ActorModule;
import net.orfjackal.dimdwarf.auth.*;

public class AuthenticatorModule extends ActorModule<AuthenticatorMessage> {

    public AuthenticatorModule() {
        super("Authenticator");
    }

    protected void configure() {
        bindControllerTo(AuthenticatorController.class);
        bindActorTo(AuthenticatorActor.class);

        bind(Authenticator.class).to(AuthenticatorController.class);
        expose(Authenticator.class);

        requireBinding(CredentialsChecker.class);
    }

    @Provides
    @SuppressWarnings({"unchecked"})
    CredentialsChecker<Credentials> credentialsChecker(CredentialsChecker checker) {
        // To make it easier to write bindings in the application code (no TypeLiterals),
        // we create a binding from the raw type to the generic type.
        return checker;
    }
}
