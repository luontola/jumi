// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import net.orfjackal.dimdwarf.util.Clock;

public class ScheduledOneTimeRun extends AbstractSchedulingStrategy {
    private static final long serialVersionUID = 1L;

    public static SchedulingStrategy create(long initialDelay, Clock clock) {
        long scheduledTime = initialDelay + clock.currentTimeMillis();
        return new ScheduledOneTimeRun(scheduledTime, clock);
    }

    private ScheduledOneTimeRun(long scheduledTime, Clock clock) {
        super(scheduledTime, clock);
    }

    public SchedulingStrategy nextRepeatedRun() {
        return null;
    }
}
