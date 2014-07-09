// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.channel;

import javax.annotation.concurrent.Immutable;

@Immutable
public enum PollResult {
    HAD_SOME_MESSAGES, NO_NEW_MESSAGES, END_OF_STREAM
}
