// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

public class GivenUpOnTaskException extends RuntimeException {

    public GivenUpOnTaskException() {
    }

    public GivenUpOnTaskException(String message) {
        super(message);
    }

    public GivenUpOnTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public GivenUpOnTaskException(Throwable cause) {
        super(cause);
    }
}
