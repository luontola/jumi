// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.auth;

public final class PasswordCredentials implements Credentials {

    private final String username;
    private final String password;

    public PasswordCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String toString() {
        return String.format("%s(%s, %X)", getClass().getSimpleName(), username, password.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PasswordCredentials)) {
            return false;
        }
        PasswordCredentials that = (PasswordCredentials) obj;
        return this.username.equals(that.username) &&
                this.password.equals(that.password);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}
