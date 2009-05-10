// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

/**
 * @author Esko Luontola
 * @since 13.12.2008
 */
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
