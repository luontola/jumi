// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
