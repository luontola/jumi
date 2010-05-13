// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
